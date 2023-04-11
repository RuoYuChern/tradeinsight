package com.tao.trade.facade;

import java.math.BigDecimal;

public interface TaoConstants {
    String LOCK_HISTORY = "history";
    String DATA_DATE = "cn.stock";
    String FIND_DATE = "cn.find";
    String TU_DATE_FMT = "yyyyMMdd";
    String TU_DATE_TIME_FMT = "yyyyMMddHH:mm:ss";

    int MAX_QUAINT_SELL_DAY = 30;
    int MAX_QUAINT_BUY_DAY = 5;
    BigDecimal QUAINT_TRADE_NUMBER = new BigDecimal("1000");
}
