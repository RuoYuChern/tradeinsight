package com.tao.trade.facade;

import lombok.Data;

import java.util.List;

@Data
public class DashBoardDto {
    /**每天大盘**/
    private List<MarketDailyDto> marketDailyList;
    /**GDP**/
    private List<GDPDto> gdpList;
    /**货币供应量***/
    private List<MoneyQuantityDto> moneyList;
    /**CPI***/
    private List<CnCpiDto> cpiDtoList;
    /**PPI**/
    private List<CnPpiDto> ppiDtoList;
}
