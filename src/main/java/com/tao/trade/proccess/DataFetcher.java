package com.tao.trade.proccess;

import com.tao.trade.domain.TaoData;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.db.model.CnMarketDaily;
import com.tao.trade.infra.vo.*;
import com.tao.trade.ml.TimeSeries;
import com.tao.trade.utils.DateHelper;
import com.tao.trade.utils.Help;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Slf4j
public class DataFetcher {
    private static String TU_DATE_FMT = "yyyyMMdd";
    private final CnStockDao stockDao;
    private final TuShareClient tuShareClient;
    private final TaoData stockBaseData;

    public DataFetcher(CnStockDao stockDao, TuShareClient tuShareClient, TaoData stockBaseData) {
        this.stockDao = stockDao;
        this.tuShareClient = tuShareClient;
        this.stockBaseData = stockBaseData;
    }

    public void fetchDate(int batch, String startDate, String endDate){
        Map<String, CnMarketDaily> dailyMap = new HashMap<>();
        handleDaily(batch, startDate, endDate, dailyMap);
        /**获取上市列表**/
        List<StockIpoVo> ipoVos = tuShareClient.new_share(startDate, endDate);
        for(StockIpoVo ipoVo:ipoVos){
            CnMarketDaily marketDaily = dailyMap.get(ipoVo.getIpoDate());
            if(marketDaily != null){
                marketDaily.setListing(marketDaily.getListing() + 1);
            }
        }
        /**获取利率数据**/
        handleBor(startDate, endDate, dailyMap);
        /**保存数据:ipoVos**/
        stockDao.batchInsertIpo(ipoVos);
        /**保存market数据**/
        List<CnMarketDaily> marketDailyList = new ArrayList<>(dailyMap.size());
        marketDailyList.addAll(dailyMap.values());
        stockDao.batchInsertMarket(marketDailyList);
    }

    private void handleDaily(int batch, String startDate, String endDate, Map<String, CnMarketDaily> dailyMap){
        List<StockBasicVo> basicVoList = stockBaseData.getBasic();
        int num = 0;
        StringBuilder sb = new StringBuilder();
        Map<String,StockBasicVo> basicVoMap = new HashMap<>();
        long total = 0;
        for(StockBasicVo vo:basicVoList){
            if(num > 0){
                sb.append(",");
            }
            sb.append(vo.getTsCode());
            num++;
            basicVoMap.put(vo.getTsCode(), vo);
            if(num < batch){
                continue;
            }

            String tsCode = sb.toString();
            sb = new StringBuilder();
            Callable<List<StockDailyVo>> callable = ()->tuShareClient.daily(tsCode, startDate, endDate);
            List<StockDailyVo> dailyVos = Help.tryCall(callable);
            if(!CollectionUtils.isEmpty(dailyVos)){
                /**补充行业**/
                for(StockDailyVo dv:dailyVos){
                    StockBasicVo sbv = basicVoMap.get(dv.getSymbol());
                    dv.setIndustry(sbv.getIndustry());
                    dv.setName(sbv.getName());
                }
                stockDao.batchInsertDaily(dailyVos);
                handleMarketDaily(dailyVos, dailyMap);
                total = total + dailyVos.size();
            }
            num = 0;
        }
        if(num > 0){
            List<StockDailyVo> dailyVos = tuShareClient.daily(sb.toString(), startDate, endDate);
            if(!CollectionUtils.isEmpty(dailyVos)) {
                for(StockDailyVo dv:dailyVos){
                    StockBasicVo sbv = basicVoMap.get(dv.getSymbol());
                    dv.setIndustry(sbv.getIndustry());
                    dv.setName(sbv.getName());
                }
                stockDao.batchInsertDaily(dailyVos);
                handleMarketDaily(dailyVos, dailyMap);
                total = total + dailyVos.size();
            }
        }
        basicVoMap.clear();
        log.info("startDate:{}, endDate:{}, total:{}", startDate, endDate, total);
    }

    private void handleBor(String startDate, String endDate, Map<String, CnMarketDaily> dailyMap){

        List<BorVo> borVoList = tuShareClient.bankOfferedRate(BankOfferedRate.SHIBOR, startDate, endDate);
        TimeSeries.fillValue(borVoList, BorVo::getOn, BorVo::setOn);
        for(BorVo bov:borVoList){
            CnMarketDaily marketDaily = dailyMap.get(bov.getDate());
            if(marketDaily != null){
                marketDaily.setShibor(bov.getOn());
            }
        }

        borVoList = tuShareClient.bankOfferedRate(BankOfferedRate.HIBOR, startDate, endDate);
        TimeSeries.fillValue(borVoList, BorVo::getOn, BorVo::setOn);
        for(BorVo bov:borVoList){
            CnMarketDaily marketDaily = dailyMap.get(bov.getDate());
            if(marketDaily != null){
                marketDaily.setHibor(bov.getOn());
            }
        }

        borVoList = tuShareClient.bankOfferedRate(BankOfferedRate.LIBOR, startDate, endDate);
        TimeSeries.fillValue(borVoList, BorVo::getOn, BorVo::setOn);
        for(BorVo bov:borVoList){
            CnMarketDaily marketDaily = dailyMap.get(bov.getDate());
            if(marketDaily != null){
                marketDaily.setLibor(bov.getOn());
            }
        }
    }

    private void handleMarketDaily(List<StockDailyVo> dailyVos, Map<String, CnMarketDaily> dailyMap){
        BigDecimal rateLevel = new BigDecimal("0.096");
        for(StockDailyVo vo:dailyVos){
            CnMarketDaily marketDaily = dailyMap.get(vo.getDay());
            if(marketDaily == null){
                marketDaily = new CnMarketDaily();
                marketDaily.setAmount(BigDecimal.ZERO);
                marketDaily.setVol(BigDecimal.ZERO);
                marketDaily.setDelisting(0);
                marketDaily.setListing(0);
                marketDaily.setDown(0);
                marketDaily.setUp(0);
                marketDaily.setUplimit(0);
                marketDaily.setDownlimit(0);
                marketDaily.setShibor(BigDecimal.ZERO);
                marketDaily.setHibor(BigDecimal.ZERO);
                marketDaily.setLibor(BigDecimal.ZERO);
                marketDaily.setProfit(BigDecimal.ZERO);
                marketDaily.setTradeDate(DateHelper.strToDate(TU_DATE_FMT, vo.getDay()));
                dailyMap.put(vo.getDay(), marketDaily);
            }
            marketDaily.setAmount(marketDaily.getAmount().add(vo.getAmount()));
            marketDaily.setVol(marketDaily.getVol().add(vo.getVol()));

            BigDecimal diff = vo.getClose().subtract(vo.getOpen());
            BigDecimal rate = diff.divide(vo.getOpen(), 5, RoundingMode.HALF_DOWN).abs();
            if(diff.compareTo(BigDecimal.ZERO) > 0){
                marketDaily.setUp(marketDaily.getUp() + 1);
                if(rate.compareTo(rateLevel) >= 0){
                    marketDaily.setUplimit(marketDaily.getUplimit() + 1);
                }
            }

            if(diff.compareTo(BigDecimal.ZERO) < 0){
                marketDaily.setDown(marketDaily.getDown() + 1);
                if(rate.compareTo(rateLevel) >= 0){
                    marketDaily.setDownlimit(marketDaily.getDownlimit() + 1);
                }
            }

            BigDecimal profit = vo.getClose().subtract(vo.getOpen()).multiply(vo.getVol()).setScale(6, RoundingMode.HALF_DOWN);
            marketDaily.setProfit(marketDaily.getProfit().add(profit));
        }
    }
}
