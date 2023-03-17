package com.tao.trade.ml;

import com.tao.trade.facade.MarketSignal;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.ml.impl.ADX_TAT;
import com.tao.trade.ml.impl.SMA653CC;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MarketFilterChain {
    private static final int WINDOW_SIZE = 250;
    private List<QFilter> filterList;
    public static int getWindowSize(){
        return WINDOW_SIZE;
    }
    public String getName(int code){
        for(QFilter qf:filterList){
            if(code == qf.code()){
                return qf.name();
            }
        }
        return "NA#";
    }
    public MarketFilterChain(){
        filterList = new LinkedList<>();
        AtomicInteger num = new AtomicInteger(0);
        filterList.add(new SMA653CC(num.incrementAndGet()));
        filterList.add(new ADX_TAT(num.incrementAndGet()));
    }
    public List<FilterResult> filter(List<CnStockDaily> stockList){
        List<FilterResult> resultList = new ArrayList<>(filterList.size());
        for(QFilter qf:filterList){
            FilterResult result = qf.filter(stockList);
            if(result.getSignal().equals(MarketSignal.NO)){
                continue;
            }
            result.setName(qf.name());
            result.setCode(qf.code());
            resultList.add(result);
        }
        return resultList;
    }
}
