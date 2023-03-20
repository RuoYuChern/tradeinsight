package com.tao.trade.facade;

import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Data
public class TradingDto {
    private Integer totalNumber;
    private BigDecimal totalMoney;
    private Integer lossNumber;
    private BigDecimal pnl;
    private List<QuaintDto> quaintList;
    public TradingDto(){
        totalNumber = Integer.valueOf(0);
        totalMoney  = BigDecimal.ZERO;
        lossNumber  = Integer.valueOf(0);
        pnl = BigDecimal.ZERO;
        quaintList = new LinkedList<>();
    }
}
