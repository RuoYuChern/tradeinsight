package com.tao.trade.proccess;

import com.tao.trade.domain.TaoData;
import com.tao.trade.facade.CnDownTopDto;
import com.tao.trade.facade.StockBaseDto;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.infra.db.model.CnStockDailyStat;
import com.tao.trade.infra.vo.StockBasicVo;
import com.tao.trade.ml.TimeSeries;
import com.tao.trade.utils.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class IndicatorCalc{
    private static String TU_DATE_FMT = "yyyyMMdd";
    private final static int WINDOW_SIZE = 10;
    private final static int MV_SIZE = 200;
    private final static int MV_DATE_SIZE = 360;
    private final CnStockDao stockDao;
    private final TuShareClient tuShareClient;
    private final TaoData stockBaseData;

    public IndicatorCalc(CnStockDao stockDao, TuShareClient tuShareClient, TaoData stockBaseData) {
        this.stockDao = stockDao;
        this.tuShareClient = tuShareClient;
        this.stockBaseData = stockBaseData;
    }

    public CnDownTopDto getDayDownUpTop(Date date){
        List<StockBaseDto> upTopList = new LinkedList<>();
        List<StockBaseDto> downTopList = new LinkedList<>();
        CnDownTopDto cnDownTopDto = new CnDownTopDto();
        cnDownTopDto.setDay(DateHelper.dateToStr("yyyyMMdd", date));
        cnDownTopDto.setUpTopList(upTopList);
        cnDownTopDto.setDownTopList(downTopList);

        /**查询股票数据**/
        log.info("Get top:{}", cnDownTopDto.getDay());
        List<CnStockDaily> dailyList = stockDao.getDailyBetween(date, DateHelper.afterNDays(date, 1));
        if(CollectionUtils.isEmpty(dailyList)){
            log.info("Date:{} is empty", date);
            return cnDownTopDto;
        }

        BigDecimal rateLevel = new BigDecimal("0.096");
        for(CnStockDaily daily:dailyList){
            BigDecimal diff = daily.getClosePrice().subtract(daily.getOpenPrice());
            BigDecimal rate = diff.divide(daily.getOpenPrice(), 5, RoundingMode.HALF_DOWN).abs();
            if(rate.compareTo(rateLevel) < 0){
                continue;
            }
            StockBaseDto baseDto = new StockBaseDto();
            baseDto.setName(daily.getName());
            StockBasicVo basicVo = stockBaseData.getStockBase(daily.getSymbol());
            baseDto.setOpen(daily.getOpenPrice());
            baseDto.setPrice(daily.getClosePrice());
            baseDto.setTsCode(daily.getSymbol());
            baseDto.setRate(rate.multiply(BigDecimal.valueOf(100)).setScale(3, RoundingMode.HALF_DOWN));
            if(basicVo != null) {
                baseDto.setIndustry(basicVo.getIndustry());
            }
            if(diff.compareTo(BigDecimal.ZERO) > 0){
                baseDto.setFlag("upTop");
                upTopList.add(baseDto);
            }else{
                baseDto.setFlag("downTop");
                downTopList.add(baseDto);
            }
        }
        log.info("Date={}, up={}, down={}", DateHelper.dateToStr("yyyyMMdd", date), upTopList.size(),
                downTopList.size());
        return cnDownTopDto;
    }

    public void calHistory(String indStart, String indEnd){
        List<StockBasicVo> basicVoList = stockBaseData.getBasic();
        if(CollectionUtils.isEmpty(basicVoList)){
            log.info("basicVoList is empty");
            return;
        }
        log.info("calHistory:{} to {}", indStart, indEnd);
        Date first = DateHelper.strToDate(TU_DATE_FMT, indStart);
        Date second = DateHelper.strToDate(TU_DATE_FMT, indEnd);
        List<CnStockDailyStat> statList = new LinkedList<>();
        for(StockBasicVo vo: basicVoList){
            calcDaily(null, vo.getTsCode(), first, second, statList);
            /**save data**/
            if(statList.size() >= 50) {
                stockDao.batchInsertDailyStat(statList);
                statList.clear();
            }
        }
        if(statList.size() > 0) {
            stockDao.batchInsertDailyStat(statList);
            statList.clear();
        }
    }

    public void calDelta(String indStart, String indEnd){
        List<StockBasicVo> basicVoList = stockBaseData.getBasic();
        if(CollectionUtils.isEmpty(basicVoList)){
            log.info("basicVoList is empty");
            return;
        }
        Date startDate = DateHelper.strToDate(TU_DATE_FMT, indStart);
        Date endDate = DateHelper.strToDate(TU_DATE_FMT, indEnd);
        Date lowDate = DateHelper.getLessDay(tuShareClient, DateHelper.beforeNDays(startDate, MV_DATE_SIZE), startDate);
        String strLowDate = DateHelper.dateToStr(TU_DATE_FMT, lowDate);
        log.info("calDelta:{} to {}, lowDate:{}", indStart, indEnd, strLowDate);
        int size = 0;
        lowDate = DateHelper.strToDate(TU_DATE_FMT, strLowDate);
        List<CnStockDailyStat> statList = new LinkedList<>();
        for (StockBasicVo vo: basicVoList){
            calcDaily(startDate, vo.getTsCode(), lowDate, endDate, statList);
            /**save data**/
            if(statList.size() >= 50) {
                stockDao.batchInsertDailyStat(statList);
                size += statList.size();
                statList.clear();
            }
        }
        if(statList.size() > 0) {
            stockDao.batchInsertDailyStat(statList);
            size += statList.size();
            statList.clear();
        }
        log.info("calDelta:{} to {}, lowDate:{} finish: size is {}", indStart, indEnd, strLowDate, size);
    }

    private void calcDaily(Date startDate, String tsCode, Date lowDate, Date endDate, List<CnStockDailyStat> statList){
        List<CnStockDaily> dailyList = stockDao.getSymbolDailyBetween(tsCode, lowDate, endDate);
        if(CollectionUtils.isEmpty(dailyList) || (dailyList.size() < WINDOW_SIZE)){
            log.info("Get symbol={},first={},second={},size={}", tsCode, lowDate, endDate, dailyList.size());
            return;
        }
        for(int pos = dailyList.size() - 1; pos >= (WINDOW_SIZE - 1); pos--){
            CnStockDaily stockDaily = dailyList.get(pos);
            if((startDate != null) && (stockDaily.getTradeDate().before(startDate))){
                break;
            }
            CnStockDailyStat dailyStat = new CnStockDailyStat();
            dailyStat.setSymbol(stockDaily.getSymbol());
            dailyStat.setPrice(stockDaily.getClosePrice());
            dailyStat.setTradeDate(stockDaily.getTradeDate());

            if(pos >= (MV_SIZE - 1)) {
                dailyStat.setMaPrice(TimeSeries.MA(MV_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
            }else{
                dailyStat.setMaPrice(BigDecimal.ZERO);
            }
            dailyStat.setEmaPrice(TimeSeries.EMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
            dailyStat.setSmaPrice(TimeSeries.SMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
            dailyStat.setWmaPrice(TimeSeries.WMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
            statList.add(dailyStat);
        }
    }

    public static List<CnStockDailyStat> calc(List<CnStockDaily> dailyList){
        List<CnStockDailyStat> dailyStatList = new LinkedList<>();
        if(CollectionUtils.isEmpty(dailyList) || (dailyList.size() < WINDOW_SIZE)){
            return dailyStatList;
        }

        for(int pos = dailyList.size() - 1; pos >= (WINDOW_SIZE - 1); pos--){
            CnStockDaily stockDaily = dailyList.get(pos);
            CnStockDailyStat dailyStat = new CnStockDailyStat();
            dailyStat.setSymbol(stockDaily.getSymbol());
            dailyStat.setPrice(stockDaily.getClosePrice());
            dailyStat.setTradeDate(stockDaily.getTradeDate());
            if(pos >= (MV_SIZE - 1)) {
                dailyStat.setMaPrice(TimeSeries.MA(MV_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
            }else{
                dailyStat.setMaPrice(BigDecimal.ZERO);
            }

            dailyStat.setEmaPrice(TimeSeries.EMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
            dailyStat.setSmaPrice(TimeSeries.SMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
            dailyStat.setWmaPrice(TimeSeries.WMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
            dailyStatList.add(0, dailyStat);
        }
        return dailyStatList;
    }
}
