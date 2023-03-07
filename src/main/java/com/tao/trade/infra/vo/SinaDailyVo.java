package com.tao.trade.infra.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SinaDailyVo {
    private String symbol;
    private String name;
    private String day;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal volume;
    private BigDecimal amount;
}
