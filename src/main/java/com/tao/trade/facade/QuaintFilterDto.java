package com.tao.trade.facade;

import lombok.Data;

@Data
public class QuaintFilterDto {
    private String stock;
    private String tsCode;
    private String industry;
    private String strategyList;
}
