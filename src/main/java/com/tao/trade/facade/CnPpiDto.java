package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CnPpiDto {
    private String month;
    private BigDecimal ppiYoy;
    private BigDecimal ppiMpYoy;
    private BigDecimal ppiCgYoy;
    private BigDecimal ppiMom;
    private BigDecimal ppiMpMom;
    private BigDecimal ppiCgMom;
    private BigDecimal ppiAccu;
    private BigDecimal ppiMpAccu;
    private BigDecimal ppiCgAccu;
}
