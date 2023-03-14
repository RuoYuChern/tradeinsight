package com.tao.trade.ml;

import com.tao.trade.facade.MarketSignal;
import com.tao.trade.infra.db.model.CnStockDaily;

import java.util.List;

public interface QFilter {
    MarketSignal filter(List<CnStockDaily> stockList);
    String name();
}
