package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndicatorDto {
    private BigDecimal price;
    private BigDecimal value;
    private String day;
}
