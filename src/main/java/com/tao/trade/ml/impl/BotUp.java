package com.tao.trade.ml.impl;

import com.tao.trade.facade.MarketSignal;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.ml.FilterResult;
import com.tao.trade.ml.QFilter;
import com.tao.trade.utils.DateHelper;
import com.tao.trade.utils.Help;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

public class BotUp implements QFilter {
    private final static int WINDOW_SIZE = 100;
    private final static BigDecimal LEVEL = new BigDecimal("0.85");
    private final static int UP_SIZE = 7;
    private final int seq;
    public BotUp(int code){
        this.seq = code;
    }
    @Override
    public FilterResult filter(List<CnStockDaily> stockList) {
        if(stockList.size() < WINDOW_SIZE){
            return QFilter.NO;
        }
        /**最近7个交易日都是上涨**/
        if(!last7DaysUp(stockList)){
            return QFilter.NO;
        }

        CnStockDaily max = stockList.get(0);
        int down = 0;
        for(int i = 1; i < (stockList.size() - UP_SIZE); i++){
            CnStockDaily pre = stockList.get(i - 1);
            CnStockDaily cur = stockList.get(i);
            if(max.getClosePrice().compareTo(cur.getClosePrice()) < 0){
                max = cur;
            }
            if(pre.getClosePrice().compareTo(cur.getClosePrice()) > 0){
                down ++;
            }
        }
        BigDecimal rate = new BigDecimal(down).divide(new BigDecimal(stockList.size() - UP_SIZE), 3, RoundingMode.HALF_DOWN);
        if(rate.compareTo(LEVEL) <= 0){
            return QFilter.NO;
        }
        if(DateHelper.daysDiff(new Date(), max.getTradeDate()) < 60){
            return QFilter.NO;
        }
        FilterResult result = new FilterResult();
        result.setName(name());
        result.setCode(code());
        result.setSignal(MarketSignal.BUY);
        result.setScore(rate);
        return result;
    }

    private boolean last7DaysUp(List<CnStockDaily> stockList){
        int bott  = stockList.size() - (UP_SIZE + 1);
        int start = stockList.size() - UP_SIZE;
        int days = 0;
        CnStockDaily botDaily = stockList.get(bott);
        for(; start < stockList.size(); start++){
            CnStockDaily daily = stockList.get(start);
            if(daily.getClosePrice().compareTo(botDaily.getClosePrice()) < 0){
                continue;
            }
            botDaily = daily;
            days ++;
        }
        return (days >= 5);
    }

    @Override
    public String name() {
        return "bot-up";
    }

    @Override
    public int code() {
        return seq;
    }
}
