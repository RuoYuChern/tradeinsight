package com.tao.trade.infra.vo;

public enum BankOfferedRate {

    SHIBOR("shibor"),
    LIBOR("libor"),
    HIBOR("hibor");
    private final String name;

    BankOfferedRate(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
