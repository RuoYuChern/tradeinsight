package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CnStockDailyDto {
    private String symbol;
    private BigDecimal price;
    private BigDecimal smaPrice;
    private BigDecimal maPrice;
    private BigDecimal emaPrice;
    private BigDecimal wmaPrice;
    private String tradeDate;
}
