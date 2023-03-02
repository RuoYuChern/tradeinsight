package com.tao.trade.domain;

import com.tao.trade.facade.*;
import com.tao.trade.infra.db.model.CnMarketDaily;
import com.tao.trade.infra.vo.CnCpiVo;
import com.tao.trade.infra.vo.CnPpiVo;
import com.tao.trade.infra.vo.GdpVo;
import com.tao.trade.infra.vo.MoneySupplyVo;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

public interface TaoConvert {
    TaoConvert CONVERT = Mappers.getMapper(TaoConvert.class);
    CnPpiDto fromPpi(CnPpiVo cnPpiVo);
    CnCpiDto fromCpi(CnCpiVo cnCpiVo);
    GDPDto   fromGdp(GdpVo gdpVo);
    MoneyQuantityDto fromMoney(MoneySupplyVo vo);

    @Mapping(source = "tradeDate", target = "date", dateFormat="yyyyMMdd")
    MarketDailyDto fromMarket(CnMarketDaily daily);
}
