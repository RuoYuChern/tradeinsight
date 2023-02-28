package com.tao.trade.api.facade;

import lombok.Data;

@Data
public class TaoResponse<T> {
    private int status;
    private String msg;
    private T data;
}
