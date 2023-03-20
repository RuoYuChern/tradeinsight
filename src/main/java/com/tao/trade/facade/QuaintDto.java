package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuaintDto {
    private String stock;
    private BigDecimal tradePrice;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private BigDecimal pnl;
    private String buyDate;
    private String sellDate;
    private Integer holdDays;
    private String status;
    private String strategy;
}
