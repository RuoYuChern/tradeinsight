package com.tao.trade.infra;

import com.tao.trade.infra.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TuShareData {
    private List<Object> values;
    private Map<String, Integer> cols;
    public TuShareData(List<String> colList){
        cols = new HashMap<>();
        for(int i = 0; i < colList.size(); i++){
            cols.put(colList.get(i), i);
        }
    }

    public void setValues(List<Object> values){
        this.values = values;
    }

    public String getStr(String col){
        Integer i = cols.get(col);
        if(i == null){
            return null;
        }
        Object v = values.get(i);
        if(v != null){
            return v.toString();
        }
        return null;
    }

    public Integer getI(String col){
        Integer i = cols.get(col);
        if(i == null){
            return null;
        }
        Object v = values.get(i);
        if(v != null){
            return Integer.valueOf(v.toString());
        }
        return null;
    }

    public BigDecimal getBD(String col){
        Integer i = cols.get(col);
        if(i == null){

            return null;
        }
        Object v = values.get(i);
        if(v != null){
            return new BigDecimal(v.toString());
        }
        return null;
    }

    public BigDecimal getBD(String col, int newScale, RoundingMode roundingMode){
        Integer i = cols.get(col);
        if(i == null){
            return null;
        }
        Object v = values.get(i);
        if(v != null){
            return new BigDecimal(v.toString()).setScale(newScale, roundingMode);
        }
        return null;
    }

    public StockIpoVo toIpo(){
        StockIpoVo vo = new StockIpoVo();
        vo.setTsCode(this.getStr("ts_code"));
        vo.setName(this.getStr("name"));
        vo.setIpoDate(this.getStr("ipo_date"));
        vo.setIssueDate(this.getStr("issue_date"));
        vo.setAmount(this.getBD("amount"));
        vo.setMarketAmount(this.getBD("market_amount"));
        vo.setPrice(this.getBD("price"));
        vo.setPe(this.getBD("pe"));
        vo.setLimitAmount(this.getBD("limit_amount"));
        vo.setFunds(this.getBD("funds"));
        vo.setBallot(this.getBD("ballot"));
        return  vo;
    }

    public StockDailyVo toDailyVo(){
        StockDailyVo vo = new StockDailyVo();
        vo.setSymbol(this.getStr("ts_code"));
        vo.setDay(this.getStr("trade_date"));
        vo.setOpen(this.getBD("open"));
        vo.setHigh(this.getBD("high"));
        vo.setLow(this.getBD("low"));
        vo.setClose(this.getBD("close"));
        vo.setPreClose(this.getBD("pre_close"));
        vo.setChange(this.getBD("change"));
        vo.setPctChg(this.getBD("pct_chg"));
        vo.setVol(this.getBD("vol"));
        vo.setAmount(this.getBD("amount"));
        return vo;
    }

    public StockBasicVo toBasicVo(){
        StockBasicVo vo = new StockBasicVo();
        vo.setTsCode(this.getStr("ts_code"));
        vo.setSymbol(this.getStr("symbol"));
        vo.setName(this.getStr("name"));
        vo.setArea(this.getStr("area"));
        vo.setIndustry(this.getStr("industry"));
        if(!StringUtils.hasLength(vo.getIndustry())){
            vo.setIndustry("None");
        }
        vo.setMarket(this.getStr("market"));
        if(!StringUtils.hasLength(vo.getMarket())){
            vo.setMarket("None");
        }
        vo.setExchange(this.getStr("exchange"));
        vo.setListDate(this.getStr("list_date"));
        vo.setListStatus(this.getStr("list_status"));
        vo.setIsHs(this.getStr("is_hs"));
        return vo;
    }

    public BorVo toBorVo(BankOfferedRate rate){
        BorVo vo = new BorVo();
        vo.setDate(this.getStr("date"));
        vo.setName(rate.getName());
        vo.setOn(this.getBD("on",6, RoundingMode.HALF_DOWN));
        vo.setOneWeek(this.getBD("1w", 6, RoundingMode.HALF_DOWN));
        vo.setTwoWeek(this.getBD("2w",6, RoundingMode.HALF_DOWN));
        vo.setOneMonth(this.getBD("1m",6, RoundingMode.HALF_DOWN));
        vo.setTwoMonth(this.getBD("2m",6, RoundingMode.HALF_DOWN));
        vo.setThreeMonth(this.getBD("3m",6, RoundingMode.HALF_DOWN));
        vo.setSixMonth(this.getBD("6m",6, RoundingMode.HALF_DOWN));
        vo.setNineMonth(this.getBD("9m",6, RoundingMode.HALF_DOWN));
        if(rate.equals(BankOfferedRate.SHIBOR)){
            vo.setTwelveMonth(this.getBD("1y", 6, RoundingMode.HALF_DOWN));
        }else {
            vo.setTwelveMonth(this.getBD("12m", 6, RoundingMode.HALF_DOWN));
        }
        return vo;
    }

    public MoneySupplyVo toMoneySupply(){
        MoneySupplyVo vo = new MoneySupplyVo();
        vo.setMonth(this.getStr("month"));
        vo.setM0(this.getBD("m0",3, RoundingMode.HALF_DOWN));
        vo.setM1(this.getBD("m1", 3, RoundingMode.HALF_DOWN));
        vo.setM2(this.getBD("m2",3, RoundingMode.HALF_DOWN));
        vo.setM0Yoy(this.getBD("m0_yoy",3, RoundingMode.HALF_DOWN));
        vo.setM1Yoy(this.getBD("m1_yoy",3, RoundingMode.HALF_DOWN));
        vo.setM2Yoy(this.getBD("m2_yoy",3, RoundingMode.HALF_DOWN));
        vo.setM0Mom(this.getBD("m0_mom",3, RoundingMode.HALF_DOWN));
        vo.setM1Mom(this.getBD("m1_mom",3, RoundingMode.HALF_DOWN));
        vo.setM2Mom(this.getBD("m2_mom",3, RoundingMode.HALF_DOWN));
        return vo;
    }

    public GdpVo toGdpVo(){
        GdpVo vo = new GdpVo();
        vo.setQuarter(this.getStr("quarter"));
        vo.setGdp(this.getBD("gdp",3, RoundingMode.HALF_DOWN));
        vo.setGdpYoy(this.getBD("gdp_yoy",3, RoundingMode.HALF_DOWN));
        vo.setPi(this.getBD("pi",3, RoundingMode.HALF_DOWN));
        vo.setPiYoy(this.getBD("pi_yoy",3, RoundingMode.HALF_DOWN));
        vo.setSi(this.getBD("si", 3, RoundingMode.HALF_DOWN));
        vo.setSiYoy(this.getBD("si_yoy",3, RoundingMode.HALF_DOWN));
        vo.setTi(this.getBD("ti",3, RoundingMode.HALF_DOWN));
        vo.setTiYoy(this.getBD("ti_yoy",3, RoundingMode.HALF_DOWN));
        return vo;
    }

    public CnCpiVo toCnCpiVo(){
        CnCpiVo vo = new CnCpiVo();
        vo.setMonth(this.getStr("month"));
        vo.setNtVal(this.getBD("nt_val",3, RoundingMode.HALF_DOWN));
        vo.setNtYoy(this.getBD("nt_yoy",3, RoundingMode.HALF_DOWN));
        vo.setNtMom(this.getBD("nt_mom",3, RoundingMode.HALF_DOWN));
        vo.setNtAccu(this.getBD("nt_accu", 3, RoundingMode.HALF_DOWN));
        vo.setTownVal(this.getBD("town_val",3, RoundingMode.HALF_DOWN));
        vo.setTownYoy(this.getBD("town_yoy",3, RoundingMode.HALF_DOWN));
        vo.setTownMom(this.getBD("town_mom",3, RoundingMode.HALF_DOWN));
        vo.setTownAccu(this.getBD("town_accu", 3, RoundingMode.HALF_DOWN));
        vo.setCntVal(this.getBD("cnt_val",3, RoundingMode.HALF_DOWN));
        vo.setCntYoy(this.getBD("cnt_yoy",3, RoundingMode.HALF_DOWN));
        vo.setCntMom(this.getBD("cnt_mom",3, RoundingMode.HALF_DOWN));
        vo.setCntAccu(this.getBD("cnt_accu", 3, RoundingMode.HALF_DOWN));
        return vo;
    }

    public CnPpiVo toCnPpiVo(){
        CnPpiVo vo = new CnPpiVo();
        vo.setMonth(this.getStr("month"));

        vo.setPpiYoy(this.getBD("ppi_yoy",3, RoundingMode.HALF_DOWN));
        vo.setPpiMpYoy(this.getBD("ppi_mp_yoy",3, RoundingMode.HALF_DOWN));
        vo.setPpiCgYoy(this.getBD("ppi_cg_yoy",3, RoundingMode.HALF_DOWN));

        vo.setPpiMom(this.getBD("ppi_mom",3, RoundingMode.HALF_DOWN));
        vo.setPpiMpMom(this.getBD("ppi_mp_mom",3, RoundingMode.HALF_DOWN));
        vo.setPpiCgMom(this.getBD("ppi_cg_mom",3, RoundingMode.HALF_DOWN));

        vo.setPpiAccu(this.getBD("ppi_accu",3, RoundingMode.HALF_DOWN));
        vo.setPpiMpAccu(this.getBD("ppi_mp_accu",3, RoundingMode.HALF_DOWN));
        vo.setPpiCgAccu(this.getBD("ppi_cg_accu",3, RoundingMode.HALF_DOWN));

        return vo;
    }
}
