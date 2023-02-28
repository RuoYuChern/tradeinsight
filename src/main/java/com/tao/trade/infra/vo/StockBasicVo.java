package com.tao.trade.infra.vo;

import lombok.Data;

@Data
public class StockBasicVo {
    private String tsCode;
    private String symbol;
    private String name;
    private String area;
    private String industry;
    private String market;
    private String exchange;
    private String listStatus;
    private String listDate;
    private String isHs;
}
