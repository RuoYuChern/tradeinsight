package com.tao.trade.proccess;

import com.tao.trade.domain.TaoData;
import com.tao.trade.facade.CnDownTopDto;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.vo.StockBasicVo;
import com.tao.trade.utils.DateHelper;
import com.tao.trade.utils.Help;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class JobActors {
    private static String LOCK_HISTORY = "history";
    private static String DATA_DATE = "cn.stock";
    private static String TU_DATE_FMT = "yyyyMMdd";
    private Lock lock;
    private volatile boolean isDeltaDone;
    private final CnStockDao stockDao;
    private final TuShareClient tuShareClient;
    private final TaoData stockBaseData;
    private ScheduledExecutorService scheduledActor;

    @Autowired
    public JobActors(CnStockDao stockDao, TuShareClient tuShareClient, TaoData stockBaseData){
        lock = new ReentrantLock();
        this.isDeltaDone = false;
        this.stockDao = stockDao;
        this.tuShareClient = tuShareClient;
        this.stockBaseData = stockBaseData;

        ThreadFactory threadFactory = r -> {Thread thread = new Thread(r, "batch-sch");return thread;};
        scheduledActor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduledActor.scheduleWithFixedDelay(()->schedule(), 60,300, TimeUnit.SECONDS);
    }

    public void loadData(){
        try{
            lock.lock();
            Date lastDate = stockDao.getDeltaDate(DATA_DATE);
            if((lastDate == null)){
                log.info("{} is not exist", DATA_DATE);
                return;
            }
            List<StockBasicVo> basicVoList = Help.tryCall(()->tuShareClient.stock_basic("L"));
            stockBaseData.updateBasic(basicVoList);
            IndicatorCalc indicatorCalc = new IndicatorCalc(stockDao, tuShareClient, stockBaseData);
            CnDownTopDto cnDownTopDto = indicatorCalc.getDayDownUpTop(lastDate);
            stockBaseData.updateUpDownTop(cnDownTopDto);
            log.info("loadData finish");
        }catch (Throwable t){
            log.info("loadData:{}", t.getMessage());
        }finally {
            log.info("loadData unlock");
            lock.unlock();
        }
    }

    private void schedule(){
        try{
            log.info("schedule start");
            lock.lock();
            if(DateHelper.isHourBefore(17)){
                isDeltaDone = false;
                return;
            }
            if(!stockDao.hasLocked(LOCK_HISTORY)){
                log.info("Does not have initialized");
                return;
            }
            Date now = new Date();
            Date lastDeltaDate = stockDao.getDeltaDate(DATA_DATE);
            if((lastDeltaDate == null) || DateHelper.dateEqual(lastDeltaDate, now)){
                String str = "";
                if(lastDeltaDate != null){
                    str = DateHelper.dateToStr("yyyyMMdd", lastDeltaDate);
                    isDeltaDone = true;
                    log.info("set : isDeltaDone = {}", isDeltaDone);
                }
                log.info("lastDeltaDate is null or eq today:{}",str);
                return;
            }
            Date cureStartDate = DateHelper.afterNDays(lastDeltaDate, 1);
            Date cureEndDate = DateHelper.getBiggerDate(tuShareClient, cureStartDate, now);
            if(cureEndDate == null){
                log.info("new is out of:{}", cureStartDate);
                return;
            }
            /**判断是否节假日**/
            List<StockBasicVo> basicVoList = tuShareClient.stock_basic("L");
            stockBaseData.updateBasic(basicVoList);
            DataFetcher dataFetcher = new DataFetcher(stockDao, tuShareClient, stockBaseData);
            String strStart = DateHelper.dateToStr(TU_DATE_FMT, cureStartDate);
            String strEnd  = DateHelper.dateToStr(TU_DATE_FMT, cureEndDate);
            log.info("Start to handle:{} to {}", strStart, strEnd);
            dataFetcher.fetchDate(50, strStart, strEnd);
            /**计算指标**/
            IndicatorCalc indicatorCalc = new IndicatorCalc(stockDao, tuShareClient, stockBaseData);
            indicatorCalc.calDelta(strStart, strEnd);
            stockDao.updateDeltaDate(DATA_DATE, cureEndDate);
            CnDownTopDto cnDownTopDto = indicatorCalc.getDayDownUpTop(cureEndDate);
            stockBaseData.updateUpDownTop(cnDownTopDto);
            log.info("schedule finish");
            isDeltaDone = true;
        }catch (Throwable t){
            log.info("schedule:", t);
        }finally {
            log.info("schedule unlock");
            lock.unlock();
        }
    }

    public void addHistory(){
        if(stockDao.hasLocked(LOCK_HISTORY)){
            /**有数据**/
            log.info("Has initialized");
            return;
        }
        Thread thread = new Thread(()->handleHistory(), "Load.History");
        thread.setDaemon(true);
        thread.start();
    }

    private void handleHistory(){
        log.info("handleHistory start ......");
        try{
            lock.lock();
            if(stockDao.hasLocked(LOCK_HISTORY)){
                /**有数据**/
                log.info("Has initialized");
                return;
            }
            log.info("start");
            Callable<List<StockBasicVo>> callable = ()->tuShareClient.stock_basic("L");
            List<StockBasicVo> basicVoList = Help.tryCall(callable);
            stockBaseData.updateBasic(basicVoList);
            /**插入数据**/
            stockDao.batchInsertStockBasic(basicVoList);
            Date today = new Date();
            Date endDate = DateHelper.getBiggerDate(tuShareClient, DateHelper.beforeNDays(today, 30), today);
            String strEnd  = DateHelper.dateToStr(TU_DATE_FMT, endDate);
            /**取数据 2020，2021，2022**/
            List<Pair<String,String>> years = new ArrayList<>(3);
            years.add(Pair.of("20200101","20201231"));
            years.add(Pair.of("20210101","20211231"));
            years.add(Pair.of("20220101","20221231"));
            years.add(Pair.of("20230101", strEnd));
            log.info("Start to handle:{} to {}", "20200101", strEnd);
            DataFetcher dataFetcher = new DataFetcher(stockDao, tuShareClient, stockBaseData);
            for(Pair<String,String> year: years){
                log.info("Fetch data of year:{},{}", year.getLeft(), year.getRight());
                dataFetcher.fetchDate(10, year.getLeft(), year.getRight());
                /**sleep**/
                Help.sleep(3);
            }
            /***计算指标***/
            IndicatorCalc indicatorCalc = new IndicatorCalc(stockDao, tuShareClient, stockBaseData);
            indicatorCalc.calHistory("20200101", strEnd);
            stockDao.lockSys(LOCK_HISTORY);

            stockDao.updateDeltaDate(DATA_DATE, endDate);
            stockBaseData.updateUpDownTop(indicatorCalc.getDayDownUpTop(endDate));
            log.info("handleHistory end ......");
        }catch (Throwable t){
            log.info("handleHistory exceptions:", t);
        }finally {
            log.info("handleHistory unlock");
            lock.unlock();
        }
    }
}
