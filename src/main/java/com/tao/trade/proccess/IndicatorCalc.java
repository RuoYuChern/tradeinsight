package com.tao.trade.proccess;

import com.tao.trade.domain.CnStockBaseData;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.infra.db.model.CnStockDailyStat;
import com.tao.trade.infra.vo.StockBasicVo;
import com.tao.trade.infra.vo.TradeDateVo;
import com.tao.trade.ml.TimeSeries;
import com.tao.trade.utils.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class IndicatorCalc{
    private static String TU_DATE_FMT = "yyyyMMdd";
    private final static int WINDOW_SIZE = 10;
    private final static int MV_SIZE = 200;
    private final CnStockDao stockDao;
    private final TuShareClient tuShareClient;
    private final CnStockBaseData stockBaseData;

    public IndicatorCalc(CnStockDao stockDao, TuShareClient tuShareClient, CnStockBaseData stockBaseData) {
        this.stockDao = stockDao;
        this.tuShareClient = tuShareClient;
        this.stockBaseData = stockBaseData;
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
        for(StockBasicVo vo: basicVoList){
            List<CnStockDaily> dailyList = stockDao.getDailyBetween(vo.getSymbol(), first, second);
            if(CollectionUtils.isEmpty(dailyList) || (dailyList.size() < WINDOW_SIZE)){
                continue;
            }
            List<CnStockDailyStat> statList = new LinkedList<>();
            for(int pos = dailyList.size() - 1; pos >= WINDOW_SIZE; pos--){
                CnStockDailyStat dailyStat = new CnStockDailyStat();
                CnStockDaily stockDaily = dailyList.get(pos);

                dailyStat.setSymbol(stockDaily.getSymbol());
                dailyStat.setPrice(stockDaily.getClosePrice());
                dailyStat.setTradeDate(stockDaily.getTradeDate());

                if(pos >= MV_SIZE) {
                    dailyStat.setMaPrice(TimeSeries.MA(MV_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
                }else{
                    dailyStat.setMaPrice(BigDecimal.ZERO);
                }
                dailyStat.setEmaPrice(TimeSeries.EMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
                dailyStat.setSmaPrice(TimeSeries.EMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
                dailyStat.setWmaPrice(TimeSeries.EMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
                statList.add(dailyStat);
            }
            /**save data**/
            if(statList.size() > 0) {
                stockDao.batchInsertDailyStat(statList);
            }
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
        Date lowDate = DateHelper.afterNDays(startDate, 365);
        String strLowDate = DateHelper.dateToStr(TU_DATE_FMT, lowDate);
        List<TradeDateVo> dateVoList = tuShareClient.trade_cal(strLowDate, indStart);
        int num = 0;
        for(int i = dateVoList.size(); i > 0; i--){
            TradeDateVo dateVo = dateVoList.get(i);
            if(dateVo.getIsOpen() == 1){
                num ++;
            }
            if(num >= MV_SIZE){
                strLowDate = dateVo.getDate();
                break;
            }
        }
        log.info("calDelta:{} to {}, lowDate:{}", indStart, indEnd, strLowDate);
        lowDate = DateHelper.strToDate(TU_DATE_FMT, strLowDate);
        for (StockBasicVo vo: basicVoList){
            List<CnStockDaily> dailyList = stockDao.getDailyBetween(vo.getSymbol(), lowDate, endDate);
            if(CollectionUtils.isEmpty(dailyList) || (dailyList.size() < WINDOW_SIZE)){
                continue;
            }
            List<CnStockDailyStat> statList = new LinkedList<>();
            for(int pos = dailyList.size() - 1; pos >= WINDOW_SIZE; pos--){
                /**比较时间**/
                CnStockDaily stockDaily = dailyList.get(pos);
                if(stockDaily.getTradeDate().before(startDate)){
                    break;
                }
                CnStockDailyStat dailyStat = new CnStockDailyStat();
                dailyStat.setSymbol(stockDaily.getSymbol());
                dailyStat.setPrice(stockDaily.getClosePrice());
                dailyStat.setTradeDate(stockDaily.getTradeDate());
                if(pos >= MV_SIZE) {
                    dailyStat.setMaPrice(TimeSeries.MA(MV_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
                }else{
                    dailyStat.setMaPrice(BigDecimal.ZERO);
                }
                dailyStat.setEmaPrice(TimeSeries.EMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
                dailyStat.setSmaPrice(TimeSeries.EMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
                dailyStat.setWmaPrice(TimeSeries.EMA(WINDOW_SIZE, pos, dailyList, CnStockDaily::getClosePrice));
                statList.add(dailyStat);
            }
            /**save data**/
            if(statList.size() > 0) {
                stockDao.batchInsertDailyStat(statList);
            }
        }
    }
}
