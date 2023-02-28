package com.tao.trade.infra.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockDailyVo {
    private String symbol;
    private String name;
    private String industry;
    private String day;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal vol;
    private BigDecimal preClose;
    private BigDecimal pctChg;
    private BigDecimal change;
    private BigDecimal amount;
}
