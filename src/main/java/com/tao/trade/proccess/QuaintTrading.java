package com.tao.trade.proccess;

import com.tao.trade.domain.TaoData;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.SinaClient;

public class QuaintTrading {
    private final SinaClient sinaClient;
    private final TaoData taoData;
    private final CnStockDao stockDao;

    public QuaintTrading(SinaClient sinaClient, TaoData taoData, CnStockDao stockDao) {
        this.sinaClient = sinaClient;
        this.taoData = taoData;
        this.stockDao = stockDao;
    }

    public void handle(){

    }
}
