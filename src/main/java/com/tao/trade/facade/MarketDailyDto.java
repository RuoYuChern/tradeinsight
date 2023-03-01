package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarketDailyDto {
    private BigDecimal amount;
    private BigDecimal vol;
    private BigDecimal profit;
    private BigDecimal up;
    private BigDecimal down;
    private BigDecimal shibor;
    private BigDecimal listing;
    private BigDecimal mood;
    private String date;
}
