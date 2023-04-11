package com.tao.trade.proccess;

import com.tao.trade.domain.TaoData;
import com.tao.trade.facade.*;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.infra.db.model.CnStockDailyStat;
import com.tao.trade.infra.vo.StockBasicVo;
import com.tao.trade.ml.FilterResult;
import com.tao.trade.ml.MarketFilterChain;
import com.tao.trade.ml.QStatFilter;
import com.tao.trade.ml.TimeSeries;
import com.tao.trade.ml.impl.WMA_TAT;
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
    private final static int WINDOW_SIZE = 10;
    private final static int MV_SIZE = 200;
    private final static int MV_DATE_SIZE = 360;
    private final CnStockDao stockDao;
    private final TuShareClient tuShareClient;
    private final TaoData taoData;

    public IndicatorCalc(CnStockDao stockDao, TuShareClient tuShareClient, TaoData stockBaseData) {
        this.stockDao = stockDao;
        this.tuShareClient = tuShareClient;
        this.taoData = stockBaseData;
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
            StockBasicVo basicVo = taoData.getStockBase(daily.getSymbol());
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
        List<StockBasicVo> basicVoList = taoData.getBasic();
        if(CollectionUtils.isEmpty(basicVoList)){
            log.info("basicVoList is empty");
            return;
        }
        log.info("calHistory:{} to {}", indStart, indEnd);
        Date first = DateHelper.strToDate(TaoConstants.TU_DATE_FMT, indStart);
        Date second = DateHelper.strToDate(TaoConstants.TU_DATE_FMT, indEnd);
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

    public List<QuaintFilterDto> quaintDailyFilter(Date dataDate){
        List<StockBasicVo> list = taoData.getBasic();
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        Date lowDate = DateHelper.beforeNDays(dataDate, MarketFilterChain.getWindowSize());
        log.info("Find day={}, lowDay={}",DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, dataDate),
                DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, lowDate));
        MarketFilterChain filterChain = new MarketFilterChain();
        List<QuaintFilterDto> findList = new LinkedList<>();
        for(StockBasicVo vo:list){
            if(vo.getName().contains("ST")){
                continue;
            }
            List<CnStockDaily> dailyList = stockDao.getSymbolDailyBetween(vo.getTsCode(), lowDate, dataDate);
            List<FilterResult> results = filterChain.filter(dailyList);
            if(CollectionUtils.isEmpty(results)){
                continue;
            }
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;
            sb.append("[");
            for(FilterResult result:results){
                if(isFirst == false){
                    sb.append(",");
                }
                sb.append("{").append("\"name\":\"");
                sb.append(result.getName()).append("\",");
                sb.append("\"signal\":\"").append(result.getSignal()).append("\"}");
                isFirst = false;
            }
            sb.append("]");
            QuaintFilterDto quaintFind = new QuaintFilterDto();
            CnStockDaily daily = dailyList.get(dailyList.size() - 1);
            quaintFind.setTsCode(daily.getSymbol());
            quaintFind.setStock(vo.getName());
            quaintFind.setIndustry(vo.getIndustry());
            quaintFind.setStrategyList(sb.toString());
            findList.add(quaintFind);
        }
        return findList;
    }

    public List<QuaintFilterDto> quaintStatFilter(Date dataDate){
        List<StockBasicVo> list = taoData.getBasic();
        if(CollectionUtils.isEmpty(list)){
            return null;
        }
        Date lowDate = DateHelper.beforeNDays(dataDate, MarketFilterChain.getStatSize()*4);
        log.info("Find day={}, lowDay={}",DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, dataDate),
                DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, lowDate));
        List<QuaintFilterDto> findList = new LinkedList<>();
        QStatFilter qStatFilter = new WMA_TAT(MarketFilterChain.getStatSize());
        for(StockBasicVo vo:list){
            if(vo.getName().contains("ST")){
                continue;
            }
            List<CnStockDailyStat> dailyList = stockDao.getSymbolStatBetween(vo.getTsCode(), lowDate, dataDate);
            if(dailyList.size() > MarketFilterChain.getStatSize()){
                int start = dailyList.size() - MarketFilterChain.getStatSize();
                dailyList = dailyList.subList(start, dailyList.size());
            }
            FilterResult result = qStatFilter.filter(dailyList);
            if(result.getSignal().equals(MarketSignal.NO)){
                continue;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append("{").append("\"name\":\"");
            sb.append(result.getName()).append("\",");
            sb.append("\"signal\":\"").append(result.getSignal()).append("\"}");
            sb.append("]");
            QuaintFilterDto quaintFind = new QuaintFilterDto();
            quaintFind.setTsCode(vo.getTsCode());
            quaintFind.setStock(vo.getName());
            quaintFind.setIndustry(vo.getIndustry());
            quaintFind.setStrategyList(sb.toString());
            findList.add(quaintFind);
        }
        return findList;
    }

    public void calDelta(String indStart, String indEnd){
        List<StockBasicVo> basicVoList = taoData.getBasic();
        if(CollectionUtils.isEmpty(basicVoList)){
            log.info("basicVoList is empty");
            return;
        }
        Date startDate = DateHelper.strToDate(TaoConstants.TU_DATE_FMT, indStart);
        Date endDate = DateHelper.strToDate(TaoConstants.TU_DATE_FMT, indEnd);
        Date lowDate = DateHelper.beforeNDays(startDate, MV_DATE_SIZE);
        String strLowDate = DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, lowDate);
        log.info("calDelta:{} to {}, lowDate:{}", indStart, indEnd, strLowDate);
        int size = 0;
        lowDate = DateHelper.strToDate(TaoConstants.TU_DATE_FMT, strLowDate);
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
                dailyStat.setMaPrice(TimeSeries.MA((pos + 1), pos, dailyList, CnStockDaily::getClosePrice));
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
