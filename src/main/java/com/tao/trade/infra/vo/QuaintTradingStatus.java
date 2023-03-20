package com.tao.trade.infra.vo;

public enum QuaintTradingStatus {
    IDLE(0),
    TRADING(1),
    BUY(2),
    SELL(3),
    TRADED(4),
    CANCEL(-1);
    private final int status;

    QuaintTradingStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
