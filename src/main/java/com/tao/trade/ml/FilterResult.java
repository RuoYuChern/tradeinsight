package com.tao.trade.ml;

import com.tao.trade.facade.MarketSignal;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FilterResult {
    private String name;
    private BigDecimal score;
    private MarketSignal signal;
    private int code;
    public FilterResult(){
        signal = MarketSignal.NO;
    }
}
