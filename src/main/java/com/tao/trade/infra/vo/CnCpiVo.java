package com.tao.trade.infra.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CnCpiVo {
    private String month;
    private BigDecimal ntVal;
    private BigDecimal ntYoy;
    private BigDecimal ntMom;
    private BigDecimal ntAccu;
    private BigDecimal townVal;
    private BigDecimal townYoy;
    private BigDecimal townMom;
    private BigDecimal townAccu;
    private BigDecimal cntVal;
    private BigDecimal cntYoy;
    private BigDecimal cntMom;
    private BigDecimal cntAccu;
}
