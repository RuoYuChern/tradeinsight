package com.tao.trade.domain;

import com.tao.trade.facade.*;
import com.tao.trade.infra.db.model.CnMarketDaily;
import com.tao.trade.infra.db.model.CnStockDailyStat;
import com.tao.trade.infra.vo.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TaoConvert {
    TaoConvert CONVERT = Mappers.getMapper(TaoConvert.class);
    CnPpiDto fromPpi(CnPpiVo cnPpiVo);
    CnCpiDto fromCpi(CnCpiVo cnCpiVo);
    GDPDto   fromGdp(GdpVo gdpVo);
    MoneyQuantityDto fromMoney(MoneySupplyVo vo);

    @Mapping(source = "tradeDate", target = "date", dateFormat="MMdd")
    MarketDailyDto fromMarket(CnMarketDaily daily);

    IndexDailyDto fromDailyIndex(IndexDailyVo vo);
    DailyInfoDto fromDailyInfo(DailyInfoVo vo);
    DailyDto fromSinaDaily(SinaDailyVo vo);

    @Mapping(source = "tradeDate", target = "tradeDate", dateFormat="MMdd")
    CnStockDailyDto fromDailyStat(CnStockDailyStat dailyStat);
}
