package com.tao.trade.proccess;

import com.tao.trade.domain.TaoData;
import com.tao.trade.facade.*;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.SinaClient;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.vo.QuaintTradingStatus;
import com.tao.trade.infra.vo.StockBasicVo;
import com.tao.trade.utils.DateHelper;
import com.tao.trade.utils.Help;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class JobActors {
    private Lock lock;
    private volatile boolean isDeltaDone;
    private final CnStockDao stockDao;
    private final TuShareClient tuShareClient;
    private final SinaClient sinaClient;
    private final TaoData taoData;
    private ScheduledExecutorService scheduledActor;
    private ScheduledExecutorService tradingActor;

    @Autowired
    public JobActors(CnStockDao stockDao, TuShareClient tuShareClient, SinaClient sinaClient, TaoData stockBaseData){
        lock = new ReentrantLock();
        this.isDeltaDone = false;
        this.stockDao = stockDao;
        this.tuShareClient = tuShareClient;
        this.sinaClient = sinaClient;
        this.taoData = stockBaseData;

        ThreadFactory threadFactory = r -> {Thread thread = new Thread(r, "batch-sch");return thread;};
        scheduledActor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduledActor.scheduleWithFixedDelay(()->schedule(), 60,300, TimeUnit.SECONDS);

        threadFactory = r -> {Thread thread = new Thread(r, "trading-sch");return thread;};
        tradingActor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        tradingActor.scheduleWithFixedDelay(()->trading(), 60,60, TimeUnit.SECONDS);
    }

    public void loadData(){
        loadTopDown();
        handleFind();
        loadQuaintTrading();
    }

    public void addHistory(){
        if(stockDao.hasLocked(TaoConstants.LOCK_HISTORY)){
            /**有数据**/
            log.info("Has initialized");
            return;
        }
        Thread thread = new Thread(()->handleHistory(), "Load.History");
        thread.setDaemon(true);
        thread.start();
    }

    private void handleFind(){
        try {
            Date dataDate = stockDao.getDeltaDate(TaoConstants.DATA_DATE);
            if(dataDate == null){
                return;
            }
            IndicatorCalc indicatorCalc = new IndicatorCalc(stockDao, tuShareClient, taoData);
            List<QuaintFilterDto> findList = indicatorCalc.quaintDailyFilter(dataDate);
            QuaintDailyFilterDto quaintDailyFilterDto = new QuaintDailyFilterDto();
            quaintDailyFilterDto.setQuaintList(findList);
            findList = indicatorCalc.quaintStatFilter(dataDate);
            if(findList != null){
                quaintDailyFilterDto.getQuaintList().addAll(findList);
            }
            int size = 0;
            if(quaintDailyFilterDto.getQuaintList() != null) {
                size = quaintDailyFilterDto.getQuaintList().size();
            }
            quaintDailyFilterDto.setTradeDate(DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, dataDate));
            taoData.updateQuaintFilter(quaintDailyFilterDto);
            stockDao.updateDeltaDate(TaoConstants.FIND_DATE, dataDate);
            log.info("Find day={}, finish:{}",DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, dataDate), size);
        }catch (Throwable t){
            log.warn("Exceptions:", t);
        }
    }

    private void schedule(){
        boolean r = handleDelta();
        if(r) {
            handleFind();
        }
    }

    private void trading(){
        try {
            Date now = new Date();
            if (!DateHelper.isTradingTime(now)) {
                return;
            }
            Date cureEndDate = DateHelper.getBiggerDate(tuShareClient, now, now);
            if (cureEndDate == null) {
                log.info("new is out of:{}", now);
                return;
            }
            QuaintTrading trading = new QuaintTrading(sinaClient, taoData, stockDao);
            trading.handle();
        }catch (Throwable t){
            log.warn("trading:{}", t.getMessage());
        }
    }

    private void loadQuaintTrading(){
        Date lastDate = stockDao.getDeltaDate(TaoConstants.DATA_DATE);
        if((lastDate == null)){
            log.info("{} is not exist", TaoConstants.DATA_DATE);
            return;
        }
        Date lowDate = DateHelper.beforeNDays(lastDate, TaoConstants.MAX_QUAINT_SELL_DAY);
        List<QuaintTradingDto> quaintList = stockDao.getQuaintTradingList(lowDate, QuaintTradingStatus.BUY.getStatus());
        lowDate = DateHelper.beforeNDays(lastDate, TaoConstants.MAX_QUAINT_BUY_DAY);
        List<QuaintTradingDto> findList = stockDao.getQuaintFindList(lowDate, QuaintTradingStatus.TRADING.getStatus());

        if(!CollectionUtils.isEmpty(quaintList)){
            log.info("quaintList:{}", quaintList.size());
            for(QuaintTradingDto dto:quaintList){
                dto.setName(taoData.getStockName(dto.getTsCode()));
            }
            taoData.updateQuaintTradingList(quaintList);
        }

        if(!CollectionUtils.isEmpty(findList)){
            log.info("findList:{}", findList.size());
            for(QuaintTradingDto dto:findList){
                dto.setName(taoData.getStockName(dto.getTsCode()));
                quaintList.add(dto);
            }
            taoData.updateQuaintTradingList(findList);
        }
    }

    private void loadTopDown(){
        try{
            Date lastDate = stockDao.getDeltaDate(TaoConstants.DATA_DATE);
            if((lastDate == null)){
                log.info("{} is not exist", TaoConstants.DATA_DATE);
                return;
            }
            List<StockBasicVo> basicVoList = Help.tryCall(()->tuShareClient.stock_basic("L"));
            taoData.updateBasic(basicVoList);
            IndicatorCalc indicatorCalc = new IndicatorCalc(stockDao, tuShareClient, taoData);
            CnDownTopDto cnDownTopDto = indicatorCalc.getDayDownUpTop(lastDate);
            taoData.updateUpDownTop(cnDownTopDto);
            log.info("loadData finish");
        }catch (Throwable t){
            log.info("loadData:{}", t.getMessage());
        }finally {
            log.info("loadData unlock");
        }
    }

    private boolean handleDelta(){
        try{
            log.info("schedule start");
            lock.lock();
            if(DateHelper.isHourBefore(17)){
                isDeltaDone = false;
                return false;
            }
            if(!stockDao.hasLocked(TaoConstants.LOCK_HISTORY)){
                log.info("Does not have initialized");
                return false;
            }
            Date now = new Date();
            Date dataDate = stockDao.getDeltaDate(TaoConstants.DATA_DATE);
            if((dataDate == null) || DateHelper.dateEqual(dataDate, now)){
                String str = "";
                if(dataDate != null){
                    str = DateHelper.dateToStr("yyyyMMdd", dataDate);
                    isDeltaDone = true;
                    log.info("set : isDeltaDone = {}", isDeltaDone);
                }
                log.info("lastDeltaDate is null or eq today:{}",str);
                return false;
            }
            Date cureStartDate = DateHelper.afterNDays(dataDate, 1);
            Date cureEndDate = DateHelper.getBiggerDate(tuShareClient, cureStartDate, now);
            if(cureEndDate == null){
                log.info("new is out of:{}", cureStartDate);
                return false;
            }
            /**判断是否节假日**/
            List<StockBasicVo> basicVoList = tuShareClient.stock_basic("L");
            taoData.updateBasic(basicVoList);
            DataFetcher dataFetcher = new DataFetcher(stockDao, tuShareClient, taoData);
            String strStart = DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, cureStartDate);
            String strEnd  = DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, cureEndDate);
            log.info("Start to handle:{} to {}", strStart, strEnd);
            dataFetcher.fetchDate(50, strStart, strEnd);
            /**计算指标**/
            IndicatorCalc indicatorCalc = new IndicatorCalc(stockDao, tuShareClient, taoData);
            indicatorCalc.calDelta(strStart, strEnd);
            stockDao.updateDeltaDate(TaoConstants.DATA_DATE, cureEndDate);
            CnDownTopDto cnDownTopDto = indicatorCalc.getDayDownUpTop(cureEndDate);
            taoData.updateUpDownTop(cnDownTopDto);
            log.info("schedule finish");
            isDeltaDone = true;
            return true;
        }catch (Throwable t){
            log.info("schedule:", t);
            return false;
        }finally {
            log.info("schedule unlock");
            lock.unlock();
        }
    }

    private void handleHistory(){
        log.info("handleHistory start ......");
        try{
            lock.lock();
            if(stockDao.hasLocked(TaoConstants.LOCK_HISTORY)){
                /**有数据**/
                log.info("Has initialized");
                return;
            }
            log.info("start");
            Callable<List<StockBasicVo>> callable = ()->tuShareClient.stock_basic("L");
            List<StockBasicVo> basicVoList = Help.tryCall(callable);
            taoData.updateBasic(basicVoList);
            /**插入数据**/
            stockDao.batchInsertStockBasic(basicVoList);
            Date today = DateHelper.getCureDataDate();
            Date endDate = DateHelper.getBiggerDate(tuShareClient, DateHelper.beforeNDays(today, 30), today);
            String strEnd  = DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, endDate);
            /**取数据 2020，2021，2022**/
            List<Pair<String,String>> years = new ArrayList<>(3);
            years.add(Pair.of("20200101","20201231"));
            years.add(Pair.of("20210101","20211231"));
            years.add(Pair.of("20220101","20221231"));
            years.add(Pair.of("20230101", strEnd));
            log.info("Start to handle:{} to {}", "20200101", strEnd);
            DataFetcher dataFetcher = new DataFetcher(stockDao, tuShareClient, taoData);
            for(Pair<String,String> year: years){
                log.info("Fetch data of year:{},{}", year.getLeft(), year.getRight());
                dataFetcher.fetchDate(10, year.getLeft(), year.getRight());
                /**sleep**/
                Help.sleep(3);
            }
            /***计算指标***/
            IndicatorCalc indicatorCalc = new IndicatorCalc(stockDao, tuShareClient, taoData);
            indicatorCalc.calHistory("20200101", strEnd);
            stockDao.lockSys(TaoConstants.LOCK_HISTORY);

            stockDao.updateDeltaDate(TaoConstants.DATA_DATE, endDate);
            taoData.updateUpDownTop(indicatorCalc.getDayDownUpTop(endDate));
            log.info("handleHistory end ......");
        }catch (Throwable t){
            log.info("handleHistory exceptions:", t);
        }finally {
            log.info("handleHistory unlock");
            lock.unlock();
        }
    }
}
