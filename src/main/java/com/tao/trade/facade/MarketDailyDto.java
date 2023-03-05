package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarketDailyDto {
    private BigDecimal amount;
    private BigDecimal vol;
    private BigDecimal profit;
    private Integer up;
    private Integer uplimit;
    private Integer down;
    private Integer downlimit;
    private BigDecimal shibor;
    private Integer listing;
    private BigDecimal mood;
    private String date;
}
