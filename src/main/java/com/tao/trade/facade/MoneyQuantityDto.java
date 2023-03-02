package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MoneyQuantityDto {
    private String month;
    private BigDecimal m0;
    private BigDecimal m0Yoy;
    private BigDecimal m0Mom;
    private BigDecimal m1;
    private BigDecimal m1Yoy;
    private BigDecimal m1Mom;
    private BigDecimal m2;
    private BigDecimal m2Yoy;
    private BigDecimal m2Mom;
}
