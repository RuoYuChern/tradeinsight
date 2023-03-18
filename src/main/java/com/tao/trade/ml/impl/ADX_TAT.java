package com.tao.trade.ml.impl;

import com.tao.trade.ml.FilterResult;
import com.tao.trade.facade.IndicatorDto;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.facade.MarketSignal;
import com.tao.trade.ml.QFilter;
import com.tao.trade.ml.TimeSeries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ADX_TAT implements QFilter {
    private static final int PERIOD = 18;
    private static final int TOP_DAY = 25;
    private final int num;

    public ADX_TAT(int num) {
        this.num = num;
    }

    @Override
    public int code() {
        return num;
    }

    @Override
    public FilterResult filter(List<CnStockDaily> stockList) {
        int delta = (2*PERIOD);
        if(stockList.size() <= (3*PERIOD)){
            return QFilter.NO;
        }
        List<IndicatorDto> indList = TimeSeries.ADX_CALC(PERIOD, delta, stockList);
        if(indList.size() < PERIOD){
            return QFilter.NO;
        }
        /**SUAN sma**/
        int cure = indList.size() -1;
        BigDecimal sma = TimeSeries.SMA(PERIOD, cure, indList, IndicatorDto::getValue);
        IndicatorDto last = indList.get(cure);
        boolean isNewHigh = isLastHigh(TOP_DAY, stockList);
        boolean isNewLow  = isLastLow(TOP_DAY, stockList);
        FilterResult result = new FilterResult();
        result.setScore(last.getValue().subtract(sma));
        IndicatorDto ind = indList.get(cure);

        /**ADX(18) < ADX(18,18) & h > 25h</>**/
        if(isNewHigh && (ind.getValue().compareTo(sma) < 0)){
            result.setSignal(MarketSignal.SELL);
            return result;
        }
        if(isNewLow && (ind.getValue().compareTo(sma) > 0)){
            result.setSignal(MarketSignal.BUY);
            return result;
        }
        return QFilter.NO;
    }

    private static boolean isLastHigh(int len, List<CnStockDaily> stockList){
        int end = stockList.size() - 1;
        int start = end - len;
        BigDecimal price = stockList.get(end).getClosePrice();
        for(int i = start; i < end; i++){
            BigDecimal v = stockList.get(i).getClosePrice();
            if(v.compareTo(price) >= 0){
                return false;
            }
        }
        BigDecimal rate = new BigDecimal("1.45");
        BigDecimal d = TimeSeries.SMA(end, end - 1, stockList, CnStockDaily::getClosePrice);
        return (price.divide(d, 2, RoundingMode.HALF_DOWN).compareTo(rate) >= 0);
    }

    private static boolean isLastLow(int len, List<CnStockDaily> stockList){
        int end = stockList.size() - 1;
        int start = end - len;
        BigDecimal price = stockList.get(end).getClosePrice();
        for(int i = start; i < end; i++){
            BigDecimal v = stockList.get(i).getClosePrice();
            if(v.compareTo(price) <= 0){
                return false;
            }
        }
        BigDecimal d = TimeSeries.SMA(end, end - 1, stockList, CnStockDaily::getClosePrice);
        BigDecimal rate = new BigDecimal("0.65");
        return (price.divide(d, 2, RoundingMode.HALF_DOWN).compareTo(rate) < 0);
    }

    @Override
    public String name() {
        return "ADX-T-AT";
    }
}
