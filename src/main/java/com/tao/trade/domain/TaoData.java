package com.tao.trade.domain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tao.trade.facade.*;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.db.model.CnMarketDaily;
import com.tao.trade.infra.vo.*;
import com.tao.trade.utils.DateHelper;
import com.tao.trade.utils.Help;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class TaoData {

    @Autowired
    private CnStockDao dao;
    @Autowired
    private TuShareClient tuShareClient;
    private AtomicReference<List<StockBasicVo>> basicVoList;
    private Cache<String, Object> marketDaily;
    public TaoData(){
        basicVoList = new AtomicReference<>();
        marketDaily = CacheBuilder.newBuilder()
                .initialCapacity(5)
                .maximumSize(15)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    public void updateBasic(List<StockBasicVo> voList){
        basicVoList.set(voList);
    }

    public DashBoardDto getDashBoard(){
        DashBoardDto boardDto = new DashBoardDto();

        Date today = new Date();
        Date last30 = DateHelper.afterNDays(today, 60);
        String tradeDate = DateHelper.dateToStr("yyyyMMdd", last30);

        /**获取每日大盘**/
        String mdKey = String.format("MD-%s", tradeDate);
        Callable<Object> callable = ()-> marketDaily.get(mdKey, () -> loadMarketDaily(tradeDate, last30));
        Object obj = Help.call(callable);
        if(obj != null){
            boardDto.setMarketDailyList((List<MarketDailyDto>) obj);
        }else{
            boardDto.setMarketDailyList(new ArrayList<>());
        }

        /**货币供应量**/
        String month = DateHelper.dateToStr("yyyyMM", today);
        callable = () -> marketDaily.get(String.format("MS-%s",month), ()->loadMoneySupply(today));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setMoneyList((List<MoneyQuantityDto>) obj);
        }

        Pair<String, String> quarter = DateHelper.getOneYearQ();
        /***GDP***/
        callable = () -> marketDaily.get(String.format("GDP-%s", quarter.getLeft()), ()->loadGdp(quarter));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setGdpList((List<GDPDto>) obj);
        }

        /**CPI**/
        Pair<String, String> yearMonth = DateHelper.getOneYearM();
        callable = () -> marketDaily.get(String.format("CPI-%s", yearMonth.getLeft()), ()->loadCpi(yearMonth));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setCpiDtoList((List<CnCpiDto>) obj);
        }

        /**PPI**/
        callable = () -> marketDaily.get(String.format("PPI-%s", yearMonth.getLeft()), ()->loadPpi(yearMonth));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setPpiDtoList((List<CnPpiDto>) obj);
        }

        return boardDto;
    }

    private List<CnPpiDto> loadPpi(Pair<String,String> month){
        log.info("Load ppi between:{},{}", month.getLeft(), month.getRight());
        try{
            List<CnPpiVo> list = tuShareClient.getCnPpi(month.getLeft(), month.getRight());
            List<CnPpiDto> ppiList = new ArrayList<>(list.size());
            for(CnPpiVo vo: list){
                ppiList.add(TaoConvert.CONVERT.fromPpi(vo));
            }
            return ppiList;
        }catch (Throwable t){
            log.warn("Load ppi between:{},{}, exceptions:{}", month.getLeft(), month.getRight(),
                    t.getMessage());
        }
        return new ArrayList<>();
    }

    private List<CnCpiDto> loadCpi(Pair<String,String> month){
        log.info("Load cpi between:{},{}", month.getLeft(), month.getRight());
        try{
            List<CnCpiVo> list = tuShareClient.getCnCpi(month.getLeft(), month.getRight());
            List<CnCpiDto> cpiList = new ArrayList<>(list.size());
            for(CnCpiVo vo: list){
                cpiList.add(TaoConvert.CONVERT.fromCpi(vo));
            }
            return cpiList;
        }catch (Throwable t){
            log.warn("Load cpi between:{},{}, exceptions:{}", month.getLeft(), month.getRight(),
                    t.getMessage());
        }
        return new ArrayList<>();
    }

    private List<GDPDto> loadGdp(Pair<String, String> quarter){
        log.info("Load cpi between:{},{}", quarter.getLeft(), quarter.getRight());
        try{
            List<GdpVo> list = tuShareClient.getGdp(quarter.getLeft(), quarter.getRight());
            List<GDPDto> gdpList = new ArrayList<>(list.size());
            for(GdpVo vo: list){
                gdpList.add(TaoConvert.CONVERT.fromGdp(vo));
            }
            return gdpList;
        }catch (Throwable t){
            log.warn("Load gdp between:{},{}, exceptions:{}", quarter.getLeft(), quarter.getRight(),
                    t.getMessage());
        }
        return new ArrayList<>();
    }

    private List<MarketDailyDto> loadMarketDaily(String key,Date last30){
        log.info("loadMarketDaily:{}", key);
        List<CnMarketDaily> cnMarketDailyList = dao.loadDailyMarket(last30);
        if(CollectionUtils.isEmpty(cnMarketDailyList)){
            return new ArrayList<>();
        }
        int offset = ((cnMarketDailyList.size() <= 30) ? 0: (cnMarketDailyList.size() - 30));
        List<MarketDailyDto> list = new ArrayList<>(30);
        for (; offset < cnMarketDailyList.size(); offset++){
            MarketDailyDto dto = TaoConvert.CONVERT.fromMarket(cnMarketDailyList.get(offset));
            dto.setMood(BigDecimal.ZERO);
            list.add(dto);
        }
        return list;
    }

    private List<MoneyQuantityDto> loadMoneySupply(Date today){
        Pair<String,String> pair = DateHelper.getPreMonth(today);
        log.info("loadMoneySupply between:{}~{}", pair.getLeft(), pair.getRight());
        try {
            List<MoneySupplyVo> list = tuShareClient.getMoneySupply(pair.getLeft(), pair.getRight());
            List<MoneyQuantityDto> quantityList = new ArrayList<>();
            for(MoneySupplyVo vo:list){
                quantityList.add(TaoConvert.CONVERT.fromMoney(vo));
            }
            return quantityList;
        }catch (Throwable t){
            log.warn("Load money supply between:{},{}, exceptions:{}", pair.getLeft(), pair.getRight(),
                    t.getMessage());
        }
        return new ArrayList<>();
    }

    public List<StockBasicVo> getBasic(){
        return basicVoList.get();
    }

}
