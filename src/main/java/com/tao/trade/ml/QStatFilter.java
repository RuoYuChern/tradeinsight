package com.tao.trade.ml;

import com.tao.trade.infra.db.model.CnStockDailyStat;

import java.util.List;

public interface QStatFilter {
    FilterResult filter(List<CnStockDailyStat> stockList);
    String name();
}
