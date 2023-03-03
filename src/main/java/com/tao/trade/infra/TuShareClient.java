package com.tao.trade.infra;

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
            StockBasicVo vo = tuShareData.toBasicVo();
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
            return new ArrayList<>();
        }
        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }
        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<StockIpoVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            StockIpoVo vo = tuShareData.toIpo();
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
            return new ArrayList<>();
        }
        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }
        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<StockDailyVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            StockDailyVo vo = tuShareData.toDailyVo();
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
            return new ArrayList<>();
        }

        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }

        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<BorVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            BorVo vo = tuShareData.toBorVo(rate);
            list.add(vo);
        }
        return list;
    }

    public List<MoneySupplyVo> getMoneySupply(String startMonth, String endMonth){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("cn_m");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_m", startMonth);
        params.put("end_m", endMonth);
        reqDto.setParams(params);

        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return new ArrayList<>();
        }

        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }

        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<MoneySupplyVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            MoneySupplyVo vo = tuShareData.toMoneySupply();
            list.add(vo);
        }
        return list;
    }

    public List<GdpVo> getGdp(String startQ, String endQ){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("cn_gdp");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_q", startQ);
        params.put("end_q", endQ);
        reqDto.setParams(params);
        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return new ArrayList<>();
        }

        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }

        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<GdpVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            GdpVo vo = tuShareData.toGdpVo();
            list.add(vo);
        }
        return list;
    }

    public List<CnCpiVo> getCnCpi(String startMonth, String endMonth){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("cn_cpi");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_m", startMonth);
        params.put("end_m", endMonth);
        reqDto.setParams(params);
        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return new ArrayList<>();
        }

        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }

        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<CnCpiVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            CnCpiVo vo = tuShareData.toCnCpiVo();
            list.add(vo);
        }
        return list;
    }

    public List<CnPpiVo> getCnPpi(String startMonth, String endMonth){
        TuShareReqDto reqDto = new TuShareReqDto();
        reqDto.setApi_name("cn_ppi");
        reqDto.setToken(apiKey);
        Map<String, Object> params = new HashMap<>();
        params.put("start_m", startMonth);
        params.put("end_m", endMonth);
        reqDto.setParams(params);
        limiter.acquire(1);
        TuShareRspDto rsp = api.get(reqDto);
        if(rsp.getCode() != 0) {
            log.info("reqId:{}, code:{}", rsp.getRequest_id(), rsp.getCode());
            return new ArrayList<>();
        }

        TuShareDataDto dto = rsp.getData();
        if(dto.isHas_more()) {
            log.info("reqId:{}, code:{}, has_more:{}", rsp.getRequest_id(), rsp.getCode(), dto.isHas_more());
        }

        TuShareData tuShareData = new TuShareData(dto.getFields());
        List<CnPpiVo> list = new ArrayList<>(dto.getItems().size());
        for(List<Object> item: dto.getItems()){
            tuShareData.setValues(item);
            CnPpiVo vo = tuShareData.toCnPpiVo();
            list.add(vo);
        }
        return list;
    }
}
