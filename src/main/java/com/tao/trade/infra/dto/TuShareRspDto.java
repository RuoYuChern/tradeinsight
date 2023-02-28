package com.tao.trade.infra.dto;

import lombok.Data;

@Data
public class TuShareRspDto {
    private String request_id;
    private Integer code;
    private String msg;
    private TuShareDataDto data;
}
