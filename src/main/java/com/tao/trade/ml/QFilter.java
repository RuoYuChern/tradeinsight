package com.tao.trade.ml;

import com.tao.trade.infra.db.model.CnStockDaily;

import java.util.List;

public interface QFilter {
    FilterResult NO = new FilterResult();
    FilterResult filter(List<CnStockDaily> stockList);
    String name();
    int code();
}
