package com.tao.trade.api.facade;

import lombok.Data;

import java.util.List;

@Data
public class StockRangDailyDto {
    private String tsCode;
    private List<DailyHqDto> dtoList;
    private List<String> holidays;
}
