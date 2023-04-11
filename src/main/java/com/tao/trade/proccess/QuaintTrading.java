package com.tao.trade.proccess;

import com.tao.trade.domain.TaoData;
import com.tao.trade.facade.QuaintTradingDto;
import com.tao.trade.facade.TaoConstants;
import com.tao.trade.infra.CnStockDao;
import com.tao.trade.infra.SinaClient;
import com.tao.trade.infra.vo.QuaintTradingStatus;
import com.tao.trade.infra.vo.SinaRealVo;
import com.tao.trade.utils.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
public class QuaintTrading {
    private final BigDecimal BUY_RATE = new BigDecimal("1.05");
    private final BigDecimal SELL_RATE = new BigDecimal("1.50");
    private final SinaClient sinaClient;
    private final TaoData taoData;
    private final CnStockDao stockDao;

    public QuaintTrading(SinaClient sinaClient, TaoData taoData, CnStockDao stockDao) {
        this.sinaClient = sinaClient;
        this.taoData = taoData;
        this.stockDao = stockDao;
    }

    public void handle(){
        List<QuaintTradingDto> quaintList = taoData.getQuaintTradingList();
        if(CollectionUtils.isEmpty(quaintList)){
            return;
        }
        List<String> tsCode = new LinkedList<>();
        quaintList.forEach(r->tsCode.add(r.getTsCode()));
        Map<String, SinaRealVo> realVoMap = getPrice(tsCode);
        if(CollectionUtils.isEmpty(realVoMap)){
            return;
        }

        Iterator<QuaintTradingDto> it = quaintList.listIterator();
        while (it.hasNext()){
            QuaintTradingDto quaint = it.next();
            if(quaint.getStatus() == QuaintTradingStatus.TRADING.getStatus()){
                handleTrading(quaint, realVoMap);
            }else if(quaint.getStatus() == QuaintTradingStatus.BUY.getStatus()){
                handleBuy(quaint, realVoMap);
            }else{
                it.remove();
            }
            if((quaint.getStatus() == QuaintTradingStatus.CANCEL.getStatus()) ||
                    (quaint.getStatus() == QuaintTradingStatus.SELL.getStatus())){
                it.remove();
            }
        }
        taoData.updateQuaintTradingList(quaintList);
    }

    private void handleTrading(QuaintTradingDto quaint, Map<String, SinaRealVo> realVoMap){
        SinaRealVo realVo = realVoMap.get(quaint.getTsCode());
        if(realVo == null){
            log.warn("Can not find:{} realVo", quaint.getTsCode());
            return;
        }
        BigDecimal buyPrice = BUY_RATE.multiply(quaint.getPrice());
        if(buyPrice.compareTo(realVo.getCurePrice()) > 0){
            log.info("TsCode:{} buy price={} is big than cure price={}", quaint.getTsCode(), buyPrice, realVo.getCurePrice());
            quaint.setBuyPrice(realVo.getCurePrice());
            quaint.setStatus(QuaintTradingStatus.BUY.getStatus());
            quaint.setBuyDate(new Date());
            if(stockDao.insertQuaintTrading(quaint) > 0){
                stockDao.updateQuaintFind(quaint.getTsCode(), quaint.getAlterDate(), QuaintTradingStatus.TRADED.getStatus());
            }else{
                log.info("insert tsCode={} failed", quaint.getTsCode());
                quaint.setStatus(QuaintTradingStatus.TRADING.getStatus());
            }
            return;
        }
        log.info("TsCode:{} buy price={} is less than cure price={}", quaint.getTsCode(), buyPrice, realVo.getCurePrice());
        if(DateHelper.daysDiff(new Date(), quaint.getAlterDate()) >= TaoConstants.MAX_QUAINT_BUY_DAY){
            stockDao.updateQuaintFind(quaint.getTsCode(), quaint.getAlterDate(), QuaintTradingStatus.CANCEL.getStatus());
            quaint.setStatus(QuaintTradingStatus.CANCEL.getStatus());
        }
    }

    private void handleBuy(QuaintTradingDto quaint, Map<String, SinaRealVo> realVoMap){
        SinaRealVo realVo = realVoMap.get(quaint.getTsCode());
        if(realVo == null){
            log.warn("Can not find:{} realVo", quaint.getTsCode());
            return;
        }
        BigDecimal sellPrice = SELL_RATE.multiply(quaint.getPrice());
        int dayDiffs = DateHelper.daysDiff(new Date(), quaint.getBuyDate());
        if((realVo.getCurePrice().compareTo(sellPrice) >= 0)
                || (dayDiffs >= TaoConstants.MAX_QUAINT_SELL_DAY)){
            log.info("TsCode: {}, cure price ={} > sellPrice={} or days diff={} > {}", quaint.getTsCode(),
                    realVo.getCurePrice(), sellPrice, dayDiffs, TaoConstants.MAX_QUAINT_SELL_DAY);
            quaint.setSellPrice(realVo.getCurePrice());
            quaint.setStatus(QuaintTradingStatus.SELL.getStatus());
            quaint.setSellDate(new Date());
            stockDao.updateQuaintTrading(quaint);
        }
    }

    private Map<String, SinaRealVo> getPrice(List<String> tsCode){
        List<String> batch = new ArrayList<>(10);
        Map<String, SinaRealVo> map = new HashMap<>();
        for(String code:tsCode){
            batch.add(code);
            if(batch.size() >= 10){
                List<SinaRealVo> voList = sinaClient.getReal(batch);
                if(CollectionUtils.isEmpty(voList)){
                    log.warn("Can not get price");
                    continue;
                }
                voList.forEach(r->map.put(r.getTsCode(), r));
                batch.clear();
            }
        }
        if(batch.size() > 0){
            List<SinaRealVo> voList = sinaClient.getReal(batch);
            if(CollectionUtils.isEmpty(voList)){
                log.warn("Can not get price");
            }else {
                voList.forEach(r -> map.put(r.getTsCode(), r));
            }
        }
        return map;
    }
}
