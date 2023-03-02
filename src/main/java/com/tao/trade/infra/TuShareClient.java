package com.tao.trade.infra;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.tao.trade.infra.dto.TuShareDataDto;
import com.tao.trade.infra.dto.TuShareReqDto;
import com.tao.trade.infra.dto.TuShareRspDto;
import com.tao.trade.infra.http.TuShareApi;
import com.tao.trade.infra.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TuShareClient {
    private List<String> stockBasicFields;
    private List<String> ipoFields;
    private List<String> dailyFields;
    private List<String> moneyFields;
    private List<String> gdpFields;
    @Autowired
    private TuShareApi api;
    @Value("${tao.tu-share.token}")
    private String apiKey;
    private RateLimiter limiter;

    public TuShareClient(){
        stockBasicFields = new ArrayList<>();
        stockBasicFields.add("ts_code");
        stockBasicFields.add("symbol");
        stockBasicFields.add("name");
        stockBasicFields.add("area");
        stockBasicFields.add("industry");
        stockBasicFields.add("market");
        stockBasicFields.add("exchange");
        stockBasicFields.add("list_date");
        stockBasicFields.add("list_status");
        stockBasicFields.add("is_hs");

        ipoFields = new ArrayList<>();
        ipoFields.add("ts_code");
        ipoFields.add("name");
        ipoFields.add("ipo_date");
        ipoFields.add("issue_date");
        ipoFields.add("amount");
        ipoFields.add("market_amount");
        ipoFields.add("price");
        ipoFields.add("pe");
        ipoFields.add("limit_amount");
        ipoFields.add("funds");
        ipoFields.add("ballot");

        dailyFields = new ArrayList<>();
        dailyFields.add("ts_code");
        dailyFields.add("trade_date");
        dailyFields.add("open");
        dailyFields.add("high");
        dailyFields.add("low");
        dailyFields.add("close");
        dailyFields.add("pre_close");
        dailyFields.add("change");
        dailyFields.add("pct_chg");
        dailyFields.add("vol");
        dailyFields.add("amount");

        moneyFields = new ArrayList<>();
        for(int m = 0; m <= 2; m++) {
            moneyFields.add(String.format("m%d", m));
            moneyFields.add(String.format("m%d_yoy", m));
            moneyFields.add(String.format("m%d_mom", m));
        }

        gdpFields = new ArrayList<>();
        gdpFields.add("quarter");
        gdpFields.add("gdp");
        gdpFields.add("gdp_yoy");
        gdpFields.add("pi");
        gdpFields.add("pi_yoy");
        gdpFields.add("si");
        gdpFields.add("si_yoy");
        gdpFields.add("ti");
        gdpFields.add("ti_yoy");

        limiter = RateLimiter.create(100);
    }

    public List<TradeDateVo> trade_cal(String startDay, String endDay){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("trade_cal");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_date", startDay);
        params.put("end_date", endDay);
        params.put("exchange", "SSE");
        reqDto.setParams(params);

        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return null;
        }
        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }
        List<TradeDateVo> list = new ArrayList<>(dto.getItems().size());
        TuShareData tuShareData = new TuShareData(dto.getFields());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            TradeDateVo vo = new TradeDateVo();
            vo.setDate(tuShareData.getStr("cal_date"));
            vo.setExchange(tuShareData.getStr("exchange"));
            vo.setIsOpen(tuShareData.getI("is_open"));
            list.add(vo);
        }
        return list;
    }

    public List<StockBasicVo> stock_basic(String status){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("stock_basic");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("list_status", status);
        params.put("exchange", "");
        reqDto.setParams(params);
        reqDto.setFields(stockBasicFields);

        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return null;
        }
        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }
        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<StockBasicVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            StockBasicVo vo = new StockBasicVo();
            vo.setTsCode(tuShareData.getStr("ts_code"));
            vo.setSymbol(tuShareData.getStr("symbol"));
            vo.setName(tuShareData.getStr("name"));
            vo.setArea(tuShareData.getStr("area"));
            vo.setIndustry(tuShareData.getStr("industry"));
            if(!StringUtils.hasLength(vo.getIndustry())){
                vo.setIndustry("None");
            }
            vo.setMarket(tuShareData.getStr("market"));
            if(!StringUtils.hasLength(vo.getMarket())){
                vo.setMarket("None");
            }
            vo.setExchange(tuShareData.getStr("exchange"));
            vo.setListDate(tuShareData.getStr("list_date"));
            vo.setListStatus(tuShareData.getStr("list_status"));
            vo.setIsHs(tuShareData.getStr("is_hs"));
            list.add(vo);
        }
        return list;
    }

    public List<StockIpoVo> new_share(String startDate, String endDate){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("new_share");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_date", startDate);
        params.put("end_date", endDate);
        reqDto.setParams(params);
        reqDto.setFields(ipoFields);

        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return null;
        }
        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }
        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<StockIpoVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            StockIpoVo vo = new StockIpoVo();
            vo.setTsCode(tuShareData.getStr("ts_code"));
            vo.setName(tuShareData.getStr("name"));
            vo.setIpoDate(tuShareData.getStr("ipo_date"));
            vo.setIssueDate(tuShareData.getStr("issue_date"));
            vo.setAmount(tuShareData.getBD("amount"));
            vo.setMarketAmount(tuShareData.getBD("market_amount"));
            vo.setPrice(tuShareData.getBD("price"));
            vo.setPe(tuShareData.getBD("pe"));
            vo.setLimitAmount(tuShareData.getBD("limit_amount"));
            vo.setFunds(tuShareData.getBD("funds"));
            vo.setBallot(tuShareData.getBD("ballot"));

            list.add(vo);
        }
        return list;
    }

    public List<StockDailyVo> daily(String tsCode, String startDate, String endDate){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("daily");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("ts_code", tsCode);
        params.put("start_date", startDate);
        params.put("end_date", endDate);
        reqDto.setParams(params);
        reqDto.setFields(dailyFields);

        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return null;
        }
        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }
        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<StockDailyVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            StockDailyVo vo = new StockDailyVo();
            vo.setSymbol(tuShareData.getStr("ts_code"));
            vo.setDay(tuShareData.getStr("trade_date"));
            vo.setOpen(tuShareData.getBD("open"));
            vo.setHigh(tuShareData.getBD("high"));
            vo.setLow(tuShareData.getBD("low"));
            vo.setClose(tuShareData.getBD("close"));
            vo.setPreClose(tuShareData.getBD("pre_close"));
            vo.setChange(tuShareData.getBD("change"));
            vo.setPctChg(tuShareData.getBD("pct_chg"));
            vo.setVol(tuShareData.getBD("vol"));
            vo.setAmount(tuShareData.getBD("amount"));

            list.add(vo);
        }
        return list;
    }

    public List<BorVo> bankOfferedRate(BankOfferedRate rate, String startDate, String endDate){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name(rate.getName());
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_date", startDate);
        params.put("end_date", endDate);
        reqDto.setParams(params);

        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return null;
        }

        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }

        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<BorVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            BorVo vo = new BorVo();
            vo.setDate(tuShareData.getStr("date"));
            vo.setName(rate.getName());
            vo.setOn(tuShareData.getBD("on",6, RoundingMode.HALF_DOWN));
            vo.setOneWeek(tuShareData.getBD("1w", 6, RoundingMode.HALF_DOWN));
            vo.setTwoWeek(tuShareData.getBD("2w",6, RoundingMode.HALF_DOWN));
            vo.setOneMonth(tuShareData.getBD("1m",6, RoundingMode.HALF_DOWN));
            vo.setTwoMonth(tuShareData.getBD("2m",6, RoundingMode.HALF_DOWN));
            vo.setThreeMonth(tuShareData.getBD("3m",6, RoundingMode.HALF_DOWN));
            vo.setSixMonth(tuShareData.getBD("6m",6, RoundingMode.HALF_DOWN));
            vo.setNineMonth(tuShareData.getBD("9m",6, RoundingMode.HALF_DOWN));
            if(rate.equals(BankOfferedRate.SHIBOR)){
                vo.setTwelveMonth(tuShareData.getBD("1y", 6, RoundingMode.HALF_DOWN));
            }else {
                vo.setTwelveMonth(tuShareData.getBD("12m", 6, RoundingMode.HALF_DOWN));
            }
            list.add(vo);
        }
        return list;
    }

    private List<MoneySupplyVo> getMoneySupply(String startMonth, String endMonth){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("cn_m");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_m", startMonth);
        params.put("end_m", endMonth);
        reqDto.setParams(params);
        reqDto.setFields(moneyFields);

        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return null;
        }

        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }

        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<MoneySupplyVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            MoneySupplyVo vo = new MoneySupplyVo();
            vo.setMonth(tuShareData.getStr("month"));
            vo.setM0(tuShareData.getBD("m0",3, RoundingMode.HALF_DOWN));
            vo.setM1(tuShareData.getBD("m1", 3, RoundingMode.HALF_DOWN));
            vo.setM2(tuShareData.getBD("m2",3, RoundingMode.HALF_DOWN));
            vo.setM0Yoy(tuShareData.getBD("m0_yoy",3, RoundingMode.HALF_DOWN));
            vo.setM1Yoy(tuShareData.getBD("m1_yoy",3, RoundingMode.HALF_DOWN));
            vo.setM2Yoy(tuShareData.getBD("m2_yoy",3, RoundingMode.HALF_DOWN));
            vo.setM0Mom(tuShareData.getBD("m0_mom",3, RoundingMode.HALF_DOWN));
            vo.setM1Mom(tuShareData.getBD("m1_mom",3, RoundingMode.HALF_DOWN));
            vo.setM2Mom(tuShareData.getBD("m2_mom",3, RoundingMode.HALF_DOWN));
            list.add(vo);
        }
        return list;
    }

    private List<GdpVo> getGdp(String startQ, String endQ){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("cn_gdp");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_q", startQ);
        params.put("end_q", endQ);
        reqDto.setParams(params);
        reqDto.setFields(moneyFields);

        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return null;
        }

        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }

        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<GdpVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            GdpVo vo = new GdpVo();
            vo.setQuarter(tuShareData.getStr("month"));
            vo.setGdp(tuShareData.getBD("gdp",3, RoundingMode.HALF_DOWN));
            vo.setGdpYoy(tuShareData.getBD("gdp_yoy",3, RoundingMode.HALF_DOWN));

            vo.setPi(tuShareData.getBD("pi",3, RoundingMode.HALF_DOWN));
            vo.setPiYoy(tuShareData.getBD("pi_yoy",3, RoundingMode.HALF_DOWN));

            vo.setSi(tuShareData.getBD("si", 3, RoundingMode.HALF_DOWN));
            vo.setSiYoy(tuShareData.getBD("si_yoy",3, RoundingMode.HALF_DOWN));

            vo.setTi(tuShareData.getBD("ti",3, RoundingMode.HALF_DOWN));
            vo.setTiYoy(tuShareData.getBD("ti_yoy",3, RoundingMode.HALF_DOWN));

            list.add(vo);
        }
        return list;
    }
}
