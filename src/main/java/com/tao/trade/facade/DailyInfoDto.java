package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyInfoDto {
    private String tradeDate;
    private String tsCode;
    private String tsName;
    private Integer comCount;
    private BigDecimal totalShare;
    private BigDecimal floatShare;
    private BigDecimal totalMv;
    private BigDecimal floatMv;
    private BigDecimal amount;
    private BigDecimal vol;
    private Integer transCount;
    private BigDecimal pe;
    private String exchange;
}
