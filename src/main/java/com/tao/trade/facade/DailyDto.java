package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyDto {
    private String symbol;
    private String name;
    private String day;
    private BigDecimal close;
    private BigDecimal sma;
    private BigDecimal ema;
    private BigDecimal wma;
    private BigDecimal macd;
    private BigDecimal volume;
    private BigDecimal amount;
}
