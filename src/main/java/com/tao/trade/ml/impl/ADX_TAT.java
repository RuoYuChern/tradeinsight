package com.tao.trade.ml.impl;

import com.tao.trade.facade.IndicatorDto;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.facade.MarketSignal;
import com.tao.trade.ml.QFilter;
import com.tao.trade.ml.TimeSeries;

import java.math.BigDecimal;
import java.util.List;

public class ADX_TAT implements QFilter {
    private static final int PERIOD = 18;
    private static final int TOP_DAY = 25;
    @Override
    public MarketSignal filter(List<CnStockDaily> stockList) {
        int delta = (TOP_DAY - PERIOD);
        if(stockList.size() <= (3*PERIOD)){
            return MarketSignal.NO;
        }
        List<IndicatorDto> indList = TimeSeries.ADX_CALC(PERIOD, (TOP_DAY - PERIOD), stockList);
        if(indList.size() < PERIOD){
            return MarketSignal.NO;
        }
        /**SUAN sma**/
        int cure = indList.size() -1;
        BigDecimal sma = TimeSeries.SMA(PERIOD, cure, indList, IndicatorDto::getValue);
        IndicatorDto last = indList.get(cure);
        boolean isNewHigh = isLastHigh(TOP_DAY, stockList);
        boolean isNewLow  = isLastLow(TOP_DAY, stockList);

        if(last.getValue().compareTo(sma) > 0){
            if(isNewHigh){
                return MarketSignal.BUY;
            }
            if(isNewLow){
                return MarketSignal.SELL;
            }
        }
        if(last.getValue().compareTo(sma) < 0){
            if(isNewHigh){
                return MarketSignal.SELL;
            }
            if(isNewLow){
                return MarketSignal.BUY;
            }
        }
        return MarketSignal.NO;
    }

    private static boolean isLastHigh(int len, List<CnStockDaily> stockList){
        int end = stockList.size() - 1;
        int start = end - len + 1;
        BigDecimal price = stockList.get(end).getClosePrice();
        for(int i = start; i < end; i++){
            BigDecimal v = stockList.get(i).getClosePrice();

            if(price.compareTo(v) <= 0){
                return false;
            }
        }
        return true;
    }

    private static boolean isLastLow(int len, List<CnStockDaily> stockList){
        int end = stockList.size() - 1;
        int start = end - len + 1;
        BigDecimal price = stockList.get(end).getClosePrice();
        for(int i = start; i < end; i++){
            BigDecimal v = stockList.get(i).getClosePrice();
            if(v.compareTo(price) <= 0){
                return false;
            }
        }
        return true;
    }

    @Override
    public String name() {
        return "ADX-T-AT";
    }
}
