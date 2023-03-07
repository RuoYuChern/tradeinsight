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

    /**上证指数**/
    private List<DailyDto> shIndex;
    /**深圳指数**/
    private List<DailyDto> szIndex;
    /**沪深300**/
    private List<DailyDto> hs300;
    /**深圳300**/
    private List<DailyDto> sz300;

    /**深圳市场**/
    private List<DailyInfoDto> szMarket;
    /**深圳主板**/
    private List<DailyInfoDto> szMain;
    /**创业板**/
    private List<DailyInfoDto> szGEM;
    /**中小企业板**/
    private List<DailyInfoDto> szSME;

    /**上海市场**/
    private List<DailyInfoDto> shMARKET;
    /**科创板**/
    private List<DailyInfoDto> shSTAR;
    /**股票回购**/
    private List<DailyInfoDto> shREP;
}
