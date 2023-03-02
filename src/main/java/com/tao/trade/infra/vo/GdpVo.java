package com.tao.trade.infra.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GdpVo {
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
