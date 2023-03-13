package com.tao.trade.api.facade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyHqDto {
    private String date;
    private BigDecimal value;
}
