package com.tao.trade.ml.impl;

import com.tao.trade.facade.MarketSignal;
import com.tao.trade.infra.db.model.CnStockDailyStat;
import com.tao.trade.ml.FilterResult;
import com.tao.trade.ml.QFilter;
import com.tao.trade.ml.QStatFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class WMA_TAT implements QStatFilter {
    private final static BigDecimal BUY_RATE = new BigDecimal("1.08");
    private final static BigDecimal SELL_RATE = new BigDecimal("0.90");
    private final int windowSize;

    public WMA_TAT(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public FilterResult filter(List<CnStockDailyStat> stockList) {
        if(stockList.size() < windowSize){
            return QFilter.NO;
        }
        CnStockDailyStat first = stockList.get(0);
        CnStockDailyStat end = stockList.get(stockList.size() - 1);
        BigDecimal rate = end.getWmaPrice().divide(first.getWmaPrice(), 2, RoundingMode.HALF_DOWN);
        if(rate.compareTo(BUY_RATE) >= 0){
            if(end.getPrice().compareTo(end.getWmaPrice()) < 0){
                return QFilter.NO;
            }
            /**上升***/
            CnStockDailyStat pre = null;
            for(CnStockDailyStat cure: stockList){
                if(pre == null){
                    pre = cure;
                    continue;
                }
                if(cure.getWmaPrice().compareTo(pre.getWmaPrice()) <= 0){
                    /**一直上升**/
                    return QFilter.NO;
                }
            }
            FilterResult result = new FilterResult();
            result.setName(name());
            result.setCode(1000);
            result.setSignal(MarketSignal.BUY);
            result.setScore(end.getWmaPrice());
            return result;
        }
        if(rate.compareTo(SELL_RATE) <= 0){
            if(end.getPrice().compareTo(end.getWmaPrice()) > 0){
                return QFilter.NO;
            }
            /**下降**/
            CnStockDailyStat pre = null;
            for(CnStockDailyStat cure: stockList){
                if(pre == null){
                    pre = cure;
                    continue;
                }
                /**下降**/
                if(cure.getWmaPrice().compareTo(pre.getWmaPrice()) >= 0){
                    return QFilter.NO;
                }
            }
            FilterResult result = new FilterResult();
            result.setName(name());
            result.setCode(1000);
            result.setSignal(MarketSignal.SELL);
            result.setScore(end.getWmaPrice());
            return result;
        }
        return QFilter.NO;
    }

    @Override
    public String name() {
        return "WMA_TAT";
    }
}
