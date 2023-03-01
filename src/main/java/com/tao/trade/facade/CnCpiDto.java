package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CnCpiDto {
    private String month;
    private BigDecimal ntVal;
    private BigDecimal ntYoy;
    private BigDecimal ntMon;
    private BigDecimal ntAccu;
    private BigDecimal townVal;
    private BigDecimal townYoy;
    private BigDecimal townMon;
    private BigDecimal townAccu;
    private BigDecimal cntVal;
    private BigDecimal cntYoy;
    private BigDecimal cntMon;
    private BigDecimal cntAccu;
}
