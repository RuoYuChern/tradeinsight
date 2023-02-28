package com.tao.trade.infra.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BorVo {
    private String name;
    private String date;
    private String currType;
    private BigDecimal on;
    private BigDecimal oneWeek;
    private BigDecimal twoWeek;
    private BigDecimal oneMonth;
    private BigDecimal twoMonth;
    private BigDecimal threeMonth;
    private BigDecimal sixMonth;
    private BigDecimal nineMonth;
    private BigDecimal twelveMonth;
}
