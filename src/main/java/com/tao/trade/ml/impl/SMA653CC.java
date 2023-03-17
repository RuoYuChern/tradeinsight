package com.tao.trade.ml.impl;

import com.tao.trade.ml.FilterResult;
import com.tao.trade.facade.IndicatorDto;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.facade.MarketSignal;
import com.tao.trade.ml.QFilter;
import com.tao.trade.ml.TimeSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
public class SMA653CC implements QFilter {
    private final int num;

    public SMA653CC(int num) {
        this.num = num;
    }

    @Override
    public int code() {
        return num;
    }

    @Override
    public FilterResult filter(List<CnStockDaily> stockList) {
        List<IndicatorDto> dtoList = TimeSeries.SMA(65, stockList, CnStockDaily::getClosePrice, CnStockDaily::getTradeDate);
        if(CollectionUtils.isEmpty(dtoList) || (dtoList.size() < 3)){
            return QFilter.NO;
        }

        /***up***/
        FilterResult result = new FilterResult();
        int last = dtoList.size() - 1;
        IndicatorDto indLast = dtoList.get(last);
        result.setScore(indLast.getValue().subtract(indLast.getPrice()));
        boolean isContinue = false;
        for(int i = 0; i < 3; i++){
            IndicatorDto ind = dtoList.get(last - i);
            if(ind.getValue().compareTo(ind.getPrice()) > 0){
                isContinue = true;
                continue;
            }
            isContinue = false;
            break;
        }
        if(isContinue){
            result.setSignal(MarketSignal.BUY);
            return result;
        }

        /***down***/
        for(int i = 0; i < 3; i++){
            IndicatorDto ind = dtoList.get(last - i);
            if(ind.getValue().compareTo(ind.getPrice()) < 0){
                isContinue = true;
                continue;
            }
            isContinue = false;
            break;
        }
        if(isContinue){
            result.setSignal(MarketSignal.SELL);
            return result;
        }
        return QFilter.NO;
    }

    @Override
    public String name() {
        return "65SMA-3CC";
    }
}
