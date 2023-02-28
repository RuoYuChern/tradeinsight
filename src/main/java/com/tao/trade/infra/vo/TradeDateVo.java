package com.tao.trade.infra.vo;

import lombok.Data;

@Data
public class TradeDateVo {
    private String exchange;
    private String date;
    private Integer isOpen;
}
