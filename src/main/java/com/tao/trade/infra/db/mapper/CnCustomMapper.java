package com.tao.trade.infra.db.mapper;

import com.tao.trade.infra.db.model.*;

import java.util.List;

public interface CnCustomMapper {
    CnStockDaily findNewBy(CnStockDailyExample example);
    int batchInsertUpdateBasic(List<CnStockInfo> stockInfoList);
    int batchInsertUpdateDaily(List<CnStockDaily> batchList);
    int batchInsertIpo(List<CnStockIpo> batchList);
    int batchInsertMarket(List<CnMarketDaily> batchList);
    int insertOrUpdate(SysStatus sysStatus);
    int batchInsertDailyStat(List<CnStockDailyStat> batchList);
    int insertOrUpdateDelta(DataDeltaDate row);
}
