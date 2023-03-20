package com.tao.trade.api.facade;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class QuaintTradeDto extends TaoRequest{
    private String stock;
    private BigDecimal price;
    private String strategy;
}
