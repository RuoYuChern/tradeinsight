package com.tao.trade.infra;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.infra.db.model.CnStockInfo;
import com.tao.trade.infra.db.model.CnStockIpo;
import com.tao.trade.infra.vo.StockBasicVo;
import com.tao.trade.infra.vo.StockDailyVo;
import com.tao.trade.infra.vo.StockIpoVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DtoConvert {
    DtoConvert INSTANCE = Mappers.getMapper( DtoConvert.class );

    @Mapping(source = "tsCode", target = "symbol")
    @Mapping(source = "listStatus", target = "status")
    @Mapping(source = "listDate", target = "listDate", dateFormat="yyyyMMdd")
    CnStockInfo fromBasic(StockBasicVo basicVo);

    @Mapping(source = "symbol", target = "tsCode")
    @Mapping(source = "status", target = "listStatus")
    @Mapping(source = "listDate", target = "listDate", dateFormat="yyyyMMdd")
    StockBasicVo toBasic(CnStockInfo info);

    @Mapping(source = "open", target = "openPrice")
    @Mapping(source = "close", target = "closePrice")
    @Mapping(source = "high", target = "highPrice")
    @Mapping(source = "low", target = "lowPrice")
    @Mapping(source = "preClose", target = "preClose")
    @Mapping(source = "day", target = "tradeDate", dateFormat="yyyyMMdd")
    CnStockDaily fromDaily(StockDailyVo dailyVo);


    @Mapping(source = "tsCode", target = "symbol")
    @Mapping(source = "ipoDate", target = "ipoDate", dateFormat="yyyyMMdd")
    @Mapping(source = "issueDate", target = "issueDate", dateFormat="yyyyMMdd")
    CnStockIpo fromIpo(StockIpoVo ipoVo);
}
