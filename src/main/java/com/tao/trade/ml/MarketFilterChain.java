package com.tao.trade.ml;

import com.tao.trade.facade.FilterResult;
import com.tao.trade.facade.MarketSignal;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.ml.impl.ADX_TAT;
import com.tao.trade.ml.impl.SMA653CC;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MarketFilterChain {
    private List<QFilter> filterList;
    public MarketFilterChain(){
        filterList = new LinkedList<>();
        filterList.add(new SMA653CC());
        filterList.add(new ADX_TAT());
    }
    public List<FilterResult> filter(List<CnStockDaily> stockList){
        List<FilterResult> resultList = new ArrayList<>(filterList.size());
        for(QFilter qf:filterList){
            MarketSignal signal = qf.filter(stockList);
            if(signal.equals(MarketSignal.NO)){
                continue;
            }
            FilterResult result = new FilterResult();
            result.setName(qf.name());
            result.setSignal(signal);
            resultList.add(result);
        }
        return resultList;
    }
}
