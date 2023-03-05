package com.tao.trade.infra.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndexDailyVo {
    private String tsCode;
    private String tradeDate;
    private BigDecimal totalMv;
    private BigDecimal floatMv;
    private BigDecimal totalShare;
    private BigDecimal floatShare;
    private BigDecimal freeShare;
    private BigDecimal turnoverRate;
    private BigDecimal turnoverRateFree;
    private BigDecimal pe;
    private BigDecimal peTtm;
    private BigDecimal pb;
}
