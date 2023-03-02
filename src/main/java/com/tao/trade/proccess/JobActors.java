package com.tao.trade.proccess;

import com.tao.trade.domain.TaoData;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.vo.StockBasicVo;
import com.tao.trade.infra.vo.TradeDateVo;
import com.tao.trade.utils.DateHelper;
import com.tao.trade.utils.Help;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class JobActors {
    private static String LOCK_HISTORY = "history";
    private static String DATA_DATE = "cn.stock";
    private static String TU_DATE_FMT = "yyyyMMdd";
    private Lock lock;
    @Autowired
    private CnStockDao stockDao;
    @Autowired
    private TuShareClient tuShareClient;
    @Autowired
    private TaoData stockBaseData;
    private ScheduledExecutorService scheduledActor;
    public JobActors(){
        lock = new ReentrantLock();
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "batch-sch");
            return thread;
        };
        scheduledActor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduledActor.scheduleWithFixedDelay(()->schedule(), 60,60, TimeUnit.SECONDS);
    }


    private void schedule(){
        try{
            if(!Help.isHourAfter(16)){
                return;
            }
            Date now = new Date();
            lock.lock();
            if(!stockDao.hasLocked(LOCK_HISTORY)){
                log.info("Does not have initialized");
                return;
            }
            Date lastDate = stockDao.getDeltaDate(DATA_DATE);
            if((lastDate == null) || DateHelper.dateEqual(lastDate, now)){
                return;
            }
            Date startDate = DateHelper.afterNDays(lastDate, 1);
            if(isOutOf(startDate, now)){
                return;
            }
            /**判断是否节假日**/
            List<StockBasicVo> basicVoList = tuShareClient.stock_basic("L");
            stockBaseData.updateBasic(basicVoList);
            DataFetcher dataFetcher = new DataFetcher(stockDao, tuShareClient, stockBaseData);
            String strStart = DateHelper.dateToStr(TU_DATE_FMT, startDate);
            String strEnd  = DateHelper.dateToStr(TU_DATE_FMT, now);
            log.info("Start to handle:{} to {}", strStart, strEnd);
            dataFetcher.fetchDate(50, strStart, strEnd);
            /**计算指标**/
            IndicatorCalc indicatorCalc = new IndicatorCalc(stockDao, tuShareClient, stockBaseData);
            indicatorCalc.calDelta(strStart, strEnd);
            stockDao.updateDeltaDate(DATA_DATE, now);
        }finally {
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

    private boolean isOutOf(Date start, Date now){
        List<TradeDateVo> list = tuShareClient.trade_cal(DateHelper.dateToStr(TU_DATE_FMT, start),
                    DateHelper.dateToStr(TU_DATE_FMT, now));
        int t = 0;
        for(TradeDateVo vo:list){
            if(vo.getIsOpen() == 1){
                t++;
            }
        }
        return (t == 0);
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
            List<StockBasicVo> basicVoList = tuShareClient.stock_basic("L");
            stockBaseData.updateBasic(basicVoList);
            /**插入数据**/
            stockDao.batchInsertStockBasic(basicVoList);
            Date endDate = new Date();
            if(!Help.isHourAfter(16)){
                /**还没收盘，今天的数据放到增量同步**/
                endDate = DateHelper.afterNDays(endDate, -1);
            }
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
            log.info("handleHistory end ......");
        }catch (Throwable t){
            log.info("handleHistory exceptions:", t);
        }finally{
            lock.unlock();
        }
    }
}
