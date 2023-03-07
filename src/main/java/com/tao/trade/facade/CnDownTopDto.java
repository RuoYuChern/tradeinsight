package com.tao.trade.facade;

import lombok.Data;

import java.util.List;

@Data
public class CnDownTopDto {
    private String day;
    private List<StockBaseDto> upTopList;
    private List<StockBaseDto> downTopList;
}
