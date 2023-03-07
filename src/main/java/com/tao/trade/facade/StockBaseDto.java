package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockBaseDto {
    private String name;
    private String tsCode;
    private String industry;
    private String flag;
    private BigDecimal price;
    private BigDecimal open;
    private BigDecimal rate;
}
