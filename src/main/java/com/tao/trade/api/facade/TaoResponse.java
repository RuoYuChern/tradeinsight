package com.tao.trade.api.facade;

import lombok.Data;

@Data
public class TaoResponse<T> {
    private int status;
    private String msg;
    private T data;
    public TaoResponse(){

    }
    public TaoResponse(int status, String msg, T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
}
