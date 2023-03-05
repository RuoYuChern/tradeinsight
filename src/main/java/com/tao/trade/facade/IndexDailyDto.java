package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndexDailyDto {
    private String tsCode;
    private String tradeDate;
    /**当日总市值**/
    private BigDecimal totalMv;
    /**当日流通市值**/
    private BigDecimal floatMv;
    /**当日总股本**/
    private BigDecimal totalShare;
    /**当日流通股本**/
    private BigDecimal floatShare;
    /**当日自由流通股本**/
    private BigDecimal freeShare;
    /**换手率**/
    private BigDecimal turnoverRate;
    /**换手率(基于自由流通股本)**/
    private BigDecimal turnoverRateFree;
    /**市盈率**/
    private BigDecimal pe;
    /**市盈率TTM**/
    private BigDecimal peTtm;
    private BigDecimal pb;
}
