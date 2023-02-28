package com.tao.trade.infra.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockIpoVo {
    private String tsCode;
    private String name;
    private String ipoDate;
    private String issueDate;
    private BigDecimal amount;
    private BigDecimal marketAmount;
    private BigDecimal price;
    private BigDecimal pe;
    private BigDecimal limitAmount;
    private BigDecimal funds;
    private BigDecimal ballot;
}
