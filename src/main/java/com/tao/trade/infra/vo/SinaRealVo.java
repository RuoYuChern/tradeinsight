package com.tao.trade.infra.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SinaRealVo {
    private String tsCode;
    private String name;
    private BigDecimal open;
    private BigDecimal preClose;
    private BigDecimal curePrice;
    private BigDecimal high;
    private BigDecimal low;
    private String date;
    private String time;
}
