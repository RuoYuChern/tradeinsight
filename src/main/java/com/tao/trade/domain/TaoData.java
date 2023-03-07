package com.tao.trade.domain;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tao.trade.facade.*;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.SinaClient;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.db.model.CnMarketDaily;
import com.tao.trade.infra.db.model.CnStockDailyStat;
import com.tao.trade.infra.vo.*;
import com.tao.trade.ml.TimeSeries;
import com.tao.trade.utils.DateHelper;
import com.tao.trade.utils.Help;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class TaoData {
    private final static int MAX_DATE = 60;
    private final static int WINDOW_SIZE = 10;

    @Autowired
    private CnStockDao dao;
    @Autowired
    private TuShareClient tuShareClient;
    @Autowired
    private SinaClient sinaClient;
    private AtomicReference<List<StockBasicVo>> basicVoList;
    private volatile Map<String, StockBasicVo> basicVoMap;
    private volatile Map<String, String> nameTsCode;
    private AtomicReference<CnDownTopDto> cnDownTopDto;
    private Cache<String, Object> marketDaily;
    public TaoData(){
        basicVoList = new AtomicReference<>();
        marketDaily = CacheBuilder.newBuilder()
                .initialCapacity(5)
                .maximumSize(15)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
        cnDownTopDto = new AtomicReference<>();
    }

    public CnDownTopDto getupDownTop(){
        CnDownTopDto dto = cnDownTopDto.get();
        if(dto == null){
            dto = new CnDownTopDto();
            dto.setDownTopList(new ArrayList<>());
            dto.setUpTopList(new ArrayList<>());
            dto.setDay(DateHelper.dateToStr("yyyyMMdd", new Date()));
        }
        return dto;
    }

    public List<CnStockDailyDto> getSymbol(String tsCode){
        int limit = 100;
        List<CnStockDailyStat> dailyStats = dao.getSymbolStat(tsCode, limit);
        List<CnStockDailyDto> dailyDtoList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(dailyStats)) {
            int startOffset = ((dailyStats.size() > limit) ? (dailyStats.size() - limit) : 0);
            for(; startOffset < dailyStats.size(); startOffset++){
                CnStockDailyDto dto = TaoConvert.CONVERT.fromDailyStat(dailyStats.get(startOffset));
                dailyDtoList.add(dto);
            }
        }
        return dailyDtoList;
    }

    public String getStockName(String tsCode){
        if(basicVoMap != null){
            StockBasicVo vo = basicVoMap.get(tsCode);
            if(vo != null){
                return vo.getName();
            }
        }
        return null;
    }

    public List<CnStockDailyDto> getByName(String name){
        if(nameTsCode == null){
            log.info("nameTsCode is null");
            return null;
        }
        String tsCode = nameTsCode.get(name);
        if(!StringUtils.hasLength(tsCode)){
            log.info("Can not find ts code for:{}", Base64.getEncoder().encodeToString(name.getBytes()));
            return null;
        }
        log.info("tsCode:{}", tsCode);
        return getSymbol(tsCode);
    }

    public void updateUpDownTop(CnDownTopDto newList){
        cnDownTopDto.set(newList);
    }

    public void updateBasic(List<StockBasicVo> voList){
        basicVoList.set(voList);
        Map<String, StockBasicVo> tmp = new HashMap<>();
        Map<String, String> nameMap = new HashMap<>();
        for(StockBasicVo vo: voList){
            tmp.put(vo.getTsCode(), vo);
            nameMap.put(vo.getName(), vo.getTsCode());
        }
        basicVoMap = tmp;
        nameTsCode = nameMap;
    }

    public StockBasicVo getStockBase(String tsCode){
        if(basicVoMap == null){
            log.info("basicVoMap is null");
            return null;
        }
        return basicVoMap.get(tsCode);
    }

    public DashBoardDto getDashBoard(){
        DashBoardDto boardDto = new DashBoardDto();

        Date today = DateHelper.getDataDate();
        Date last30 = DateHelper.beforeNDays(today, MAX_DATE);
        Pair<String, String> yearMonth = DateHelper.getOneYearM();
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

        callable = ()->marketDaily.get(String.format("DISH-%s", tradeDate),()->loadDailyIndex(StockIndexName.SH));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setShIndex((List<DailyDto>) obj);
        }else{
            boardDto.setShIndex(new ArrayList<>());
        }

        callable = ()->marketDaily.get(String.format("DISZ-%s", tradeDate),()->loadDailyIndex(StockIndexName.SZ));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setSzIndex((List<DailyDto>) obj);
        }else{
            boardDto.setSzIndex(new ArrayList<>());
        }

        /**沪深300指数**/
        callable = ()->marketDaily.get(String.format("HS300-%s", tradeDate),()->loadDailyIndex(StockIndexName.HS_300));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setHs300((List<DailyDto>) obj);
        }else{
            boardDto.setHs300(new ArrayList<>());
        }

        /**沪深300指数**/
        callable = ()->marketDaily.get(String.format("SZ300-%s", tradeDate),()->loadDailyIndex(StockIndexName.SZ_300));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setSz300((List<DailyDto>) obj);
        }else{
            boardDto.setSz300(new ArrayList<>());
        }

        /**市场交易统计**/
        callable = ()->marketDaily.get(String.format("MDS-%s", tradeDate),()->loadDailyInfo(tradeDate));
        obj = Help.call(callable);
        handleDailyInfo(obj, boardDto);

        /**货币供应量**/
        callable = () -> marketDaily.get(String.format("MS-%s",yearMonth.getLeft()), ()->loadMoneySupply(yearMonth));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setMoneyList((List<MoneyQuantityDto>) obj);
        }else{
            boardDto.setMoneyList(new ArrayList<>());
        }

        Pair<String, String> quarter = DateHelper.getOneYearQ();
        /***GDP***/
        callable = () -> marketDaily.get(String.format("GDP-%s", quarter.getLeft()), ()->loadGdp(quarter));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setGdpList((List<GDPDto>) obj);
        }else {
            boardDto.setGdpList(new ArrayList<>());
        }

        /**CPI**/
        callable = () -> marketDaily.get(String.format("CPI-%s", yearMonth.getLeft()), ()->loadCpi(yearMonth));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setCpiDtoList((List<CnCpiDto>) obj);
        }else{
            boardDto.setCpiDtoList(new ArrayList<>());
        }

        /**PPI**/
        callable = () -> marketDaily.get(String.format("PPI-%s", yearMonth.getLeft()), ()->loadPpi(yearMonth));
        obj = Help.call(callable);
        if(obj != null){
            boardDto.setPpiDtoList((List<CnPpiDto>) obj);
        }else {
            boardDto.setPpiDtoList(new ArrayList<>());
        }

        return boardDto;
    }

    private List<DailyDto> loadDailyIndex(String symbol){
        log.info("loadDailyIndex symbol:{}", symbol);
        try {
            List<SinaDailyVo> list = sinaClient.getSymbolDaily(symbol, 240, "no", 40);
            List<DailyDto> indexList = new ArrayList<>(list.size());
            for (SinaDailyVo vo: list) {
                if(StringUtils.hasLength(vo.getDay())){
                    vo.setDay(vo.getDay().replace("-",""));
                }
                indexList.add(TaoConvert.CONVERT.fromSinaDaily(vo));
            }
            /**算EMA和WMA**/
            for(int pos = indexList.size() - 1; pos >= (WINDOW_SIZE - 1); pos--){
                DailyDto vo = indexList.get(pos);
                vo.setSma(TimeSeries.SMA(WINDOW_SIZE, pos, indexList, DailyDto::getClose));
                vo.setEma(TimeSeries.EMA(WINDOW_SIZE, pos, indexList, DailyDto::getClose));
                vo.setWma(TimeSeries.WMA(WINDOW_SIZE, pos, indexList, DailyDto::getClose));
            }
            log.info("loadDailyIndex symbol:{},size:{}", symbol, indexList.size());
            Iterator<DailyDto> it = indexList.iterator();
            while (it.hasNext()){
                DailyDto dto = it.next();
                if(dto.getSma() == null){
                    it.remove();
                }
            }
            return indexList;
        }catch (Throwable t){
            return null;
        }
    }

    private List<DailyInfoDto> loadDailyInfo(String tradeDate){
        String endDate = DateHelper.dateToStr("yyyyMMdd", new Date());
        log.info("loadDailyInfo between:{},{}", tradeDate, endDate);
        try {
            List<DailyInfoVo> list = tuShareClient.getDailyInfo(tradeDate, endDate);
            List<DailyInfoDto> indexList = new ArrayList<>();
            for (DailyInfoVo vo:list) {
                indexList.add(TaoConvert.CONVERT.fromDailyInfo(vo));
            }
            log.info("Load dailyInfo between:{},{},size:{}", tradeDate, endDate, indexList.size());
            return indexList;
        }catch (Throwable t){
            return null;
        }
    }

    private List<CnPpiDto> loadPpi(Pair<String,String> month){
        log.info("Load ppi between:{},{}", month.getLeft(), month.getRight());
        try{
            List<CnPpiVo> list = tuShareClient.getCnPpi(month.getLeft(), month.getRight());
            List<CnPpiDto> ppiList = new ArrayList<>(list.size());
            for(int i = list.size(); i > 0; i--){
                ppiList.add(TaoConvert.CONVERT.fromPpi(list.get(i - 1)));
            }
            log.info("Load ppi between:{},{},size:{}", month.getLeft(), month.getRight(), ppiList.size());
            return ppiList;
        }catch (Throwable t){
            log.warn("Load ppi between:{},{}, exceptions:{}", month.getLeft(), month.getRight(),
                    t.getMessage());
        }
        return null;
    }

    private List<CnCpiDto> loadCpi(Pair<String,String> month){
        log.info("Load cpi between:{},{}", month.getLeft(), month.getRight());
        try{
            List<CnCpiVo> list = tuShareClient.getCnCpi(month.getLeft(), month.getRight());
            List<CnCpiDto> cpiList = new ArrayList<>(list.size());
            for(int i = list.size(); i > 0; i--){
                cpiList.add(TaoConvert.CONVERT.fromCpi(list.get(i - 1)));
            }
            log.info("Load cpi between:{},{},size:{}", month.getLeft(), month.getRight(), cpiList.size());
            return cpiList;
        }catch (Throwable t){
            log.warn("Load cpi between:{},{}, exceptions:{}", month.getLeft(), month.getRight(),
                    t.getMessage());
        }
        return null;
    }

    private List<GDPDto> loadGdp(Pair<String, String> quarter){
        log.info("Load cpi between:{},{}", quarter.getLeft(), quarter.getRight());
        try{
            List<GdpVo> list = tuShareClient.getGdp(quarter.getLeft(), quarter.getRight());
            List<GDPDto> gdpList = new ArrayList<>(list.size());
            for(int i = list.size(); i > 0; i--){
                gdpList.add(TaoConvert.CONVERT.fromGdp(list.get(i - 1)));
            }
            log.info("Load cpi between:{},{},size:{}", quarter.getLeft(), quarter.getRight(), gdpList.size());
            return gdpList;
        }catch (Throwable t){
            log.warn("Load gdp between:{},{}, exceptions:{}", quarter.getLeft(), quarter.getRight(),
                    t.getMessage());
            t.printStackTrace();
        }
        return null;
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
            dto.setAmount(Help.baseWan(dto.getAmount()));
            dto.setVol(Help.baseWan(dto.getVol()));
            dto.setProfit(Help.baseWan(dto.getProfit()));
            list.add(dto);
        }
        log.info("loadMarketDaily:{},size:{}", key, list.size());
        return list;
    }

    private List<MoneyQuantityDto> loadMoneySupply(Pair<String, String> yearMonth){
        log.info("loadMoneySupply between:{}~{}", yearMonth.getLeft(), yearMonth.getRight());
        try {
            List<MoneySupplyVo> list = tuShareClient.getMoneySupply(yearMonth.getLeft(), yearMonth.getRight());
            List<MoneyQuantityDto> quantityList = new ArrayList<>();
            for(int i = list.size(); i > 0; i--){
                quantityList.add(TaoConvert.CONVERT.fromMoney(list.get(i - 1)));
            }
            log.info("loadMoneySupply between:{}~{},size:{}", yearMonth.getLeft(), yearMonth.getRight(), quantityList.size());
            return quantityList;
        }catch (Throwable t){
            log.warn("Load money supply between:{},{}, exceptions:{}", yearMonth.getLeft(), yearMonth.getRight(),
                    t.getMessage());
        }
        return null;
    }

    private static void handleDailyInfo(Object obj, DashBoardDto boardDto){
        boardDto.setSzMarket(new LinkedList<>());
        boardDto.setSzMain(new LinkedList<>());
        boardDto.setSzGEM(new LinkedList<>());
        boardDto.setSzSME(new LinkedList<>());

        boardDto.setShMARKET(new LinkedList<>());
        boardDto.setShSTAR(new LinkedList<>());
        boardDto.setShREP(new LinkedList<>());
        if(obj == null){
            return;
        }

        fillValue("SZ_MARKET", (List<DailyInfoDto>)obj, boardDto.getSzMarket());
        fillValue("SZ_MAIN", (List<DailyInfoDto>)obj, boardDto.getSzMain());
        fillValue("SZ_GEM", (List<DailyInfoDto>)obj, boardDto.getSzGEM());
        fillValue("SZ_SME", (List<DailyInfoDto>)obj, boardDto.getSzSME());

        fillValue("SH_MARKET", (List<DailyInfoDto>)obj, boardDto.getShMARKET());
        fillValue("SH_STAR", (List<DailyInfoDto>)obj, boardDto.getShSTAR());
        fillValue("SH_REP", (List<DailyInfoDto>)obj, boardDto.getShREP());

    }

    private static void fillValue(String tsCode, List<DailyInfoDto> list, List<DailyInfoDto> tgt){
        List<DailyInfoDto> tmp = new ArrayList<>();
        for(DailyInfoDto dto:list){
            if(dto.getTsCode().equals(tsCode)){
                tmp.add(dto);
            }
        }
        if(tmp.size() > 30){
            tmp = tmp.subList(0, 30);
        }
        for(int i = tmp.size(); i > 0; i--){
            tgt.add(tmp.get(i - 1));
        }
    }

    public List<StockBasicVo> getBasic(){
        return basicVoList.get();
    }

}
