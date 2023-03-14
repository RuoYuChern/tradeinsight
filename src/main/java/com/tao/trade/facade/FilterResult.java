package com.tao.trade.facade;

import lombok.Data;

@Data
public class FilterResult {
    private String name;
    private MarketSignal signal;
}
