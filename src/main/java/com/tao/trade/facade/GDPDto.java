package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GDPDto {
    private String quarter;
    private BigDecimal gdp;
    private BigDecimal gdpYoy;
    private BigDecimal pi;
    private BigDecimal piYoy;
    private BigDecimal si;
    private BigDecimal siYoy;
    private BigDecimal ti;
    private BigDecimal tiYoy;
}
