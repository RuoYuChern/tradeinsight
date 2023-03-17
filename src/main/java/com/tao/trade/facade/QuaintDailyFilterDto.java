package com.tao.trade.facade;

import lombok.Data;

import java.util.List;

@Data
public class QuaintDailyFilterDto {
    private String tradeDate;
    private List<QuaintFilterDto> quaintList;
}
