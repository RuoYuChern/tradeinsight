package com.tao.trade.facade;

import lombok.Data;

import java.util.List;

@Data
public class DashBoardDto {
    private List<MarketDailyDto> marketDailyList;
    private List<GDPDto> gdpList;
    private MoneyQuantityDto moneyQuantity;
}
