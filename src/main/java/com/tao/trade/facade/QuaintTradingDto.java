package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class QuaintTradingDto {
    private BigDecimal price;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private String name;
    private String tsCode;
    private Date alterDate;
    private Date buyDate;
    private Date sellDate;
    private int status;
    private String strategy;
}
