package com.tao.trade.infra;

import com.alibaba.fastjson.JSON;
import com.tao.trade.infra.db.mapper.*;
import com.tao.trade.infra.db.model.*;
import com.tao.trade.infra.vo.StockBasicVo;
import com.tao.trade.infra.vo.StockDailyVo;
import com.tao.trade.infra.vo.StockIpoVo;
import com.tao.trade.utils.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CnStockDao {

    @Autowired
    private CnMarketDailyMapper marketDailyMapper;
    @Autowired
    private CnStockDailyMapper dailyMapper;
    @Autowired
    private CnStockDailyStatMapper dailyStatMapper;
    @Autowired
    private CnStockInfoMapper stockInfoMapper;
    @Autowired
    private CnStockIpoMapper stockIpoMapper;
    @Autowired
    private CnCustomMapper customMapper;
    @Autowired
    private SysStatusMapper sysStatusMapper;
    @Autowired
    private DataDeltaDateMapper deltaMapper;

    public boolean hasLocked(String name){
        SysStatusExample example = new SysStatusExample();
        example.createCriteria().andNameEqualTo(name).andStatusEqualTo("L");
        List<SysStatus> statusList = sysStatusMapper.selectByExample(example);
        return ((statusList != null) && (statusList.size() > 0));
    }

    public void lockSys(String name){
        SysStatus sysStatus = new SysStatus();
        sysStatus.setName(name);
        sysStatus.setStatus("L");
        customMapper.insertOrUpdate(sysStatus);
    }

    @Transactional
    public int batchInsertStockBasic(List<StockBasicVo> basicVoList){
        List<CnStockInfo> batchList = new ArrayList<>(50);
        Date now = new Date();
        for(StockBasicVo vo:basicVoList){
            CnStockInfo info = DtoConvert.INSTANCE.fromBasic(vo);
            info.setCreateDate(now);
            batchList.add(info);
            if(batchList.size() >= 50){
                customMapper.batchInsertUpdateBasic(batchList);
                batchList.clear();
            }
        }
        if(batchList.size() > 0){
            customMapper.batchInsertUpdateBasic(batchList);
            batchList.clear();
        }
        return basicVoList.size();
    }

    @Transactional
    public int batchInsertDaily(List<StockDailyVo> dailyVos){
        List<CnStockDaily> batchList = new ArrayList<>(50);
        for(StockDailyVo vo:dailyVos){
            CnStockDaily daily = DtoConvert.INSTANCE.fromDaily(vo);
            BigDecimal profit = vo.getClose().subtract(vo.getOpen())
                    .multiply(vo.getVol()).setScale(6, RoundingMode.HALF_DOWN);
            daily.setProfit(profit);
            batchList.add(daily);
            if(batchList.size() >= 50){
                customMapper.batchInsertUpdateDaily(batchList);
                batchList.clear();
            }
        }
        if(batchList.size() > 0){
            customMapper.batchInsertUpdateDaily(batchList);
            batchList.clear();
        }
        return dailyVos.size();
    }

    @Transactional
    public int batchInsertIpo(List<StockIpoVo> ipoVos){
        List<CnStockIpo> batchList = new ArrayList<>(50);
        for(StockIpoVo ipo:ipoVos){
            if(StringUtils.isEmpty(ipo.getIssueDate())){
                ipo.setIssueDate(ipo.getIpoDate());
            }
            try {
                batchList.add(DtoConvert.INSTANCE.fromIpo(ipo));
            }catch (Throwable t){
                log.info("ipo:{}", JSON.toJSONString(ipo));
                throw new RuntimeException(t);
            }
            if(batchList.size() >= 50){
                customMapper.batchInsertIpo(batchList);
                batchList.clear();
            }
        }
        if(batchList.size() > 0){
            customMapper.batchInsertIpo(batchList);
            batchList.clear();
        }
        return ipoVos.size();
    }

    @Transactional
    public int batchInsertMarket(List<CnMarketDaily> marketDailyList){
        List<CnMarketDaily> batchList = new ArrayList<>(50);
        for(CnMarketDaily md:marketDailyList){
            batchList.add(md);
            if(batchList.size() >= 50){
                customMapper.batchInsertMarket(batchList);
                batchList.clear();
            }
        }
        if(batchList.size() > 0){
            customMapper.batchInsertMarket(batchList);
            batchList.clear();
        }
        return marketDailyList.size();
    }

    @Transactional
    public int batchInsertDailyStat(List<CnStockDailyStat> dailyStatList){
        List<CnStockDailyStat> batchList = new ArrayList<>(50);
        for(CnStockDailyStat ds:dailyStatList){
            batchList.add(ds);
            if(batchList.size() >= 50){
                customMapper.batchInsertDailyStat(batchList);
                batchList.clear();
            }
        }
        if(batchList.size() >= 50){
            customMapper.batchInsertDailyStat(batchList);
            batchList.clear();
        }

        return dailyStatList.size();
    }

    public int updateDeltaDate(String name, Date date){
        DataDeltaDate deltaDate = new DataDeltaDate();
        deltaDate.setName(name);
        deltaDate.setDataDate(date);
        return customMapper.insertOrUpdateDelta(deltaDate);
    }

    public List<CnStockDaily> getSymbolDailyBetween(String symbol, Date startDate, Date endDate){
        CnStockDailyExample example = new CnStockDailyExample();
        example.createCriteria().andSymbolEqualTo(symbol)
                .andTradeDateBetween(startDate, endDate);
        example.setOrderByClause("trade_date ASC");
        return dailyMapper.selectByExample(example);
    }

    public List<CnStockDaily> getDailyBetween(Date startDate, Date endDate){
        CnStockDailyExample example = new CnStockDailyExample();
        example.createCriteria().andTradeDateBetween(startDate, endDate);
        example.setOrderByClause("trade_date ASC");
        return dailyMapper.selectByExample(example);
    }

    public CnStockInfo findStock(String symbol){
        CnStockInfoExample example = new CnStockInfoExample();
        example.createCriteria().andSymbolEqualTo(symbol);
        List<CnStockInfo> list = stockInfoMapper.selectByExample(example);
        if(!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        }else{
            return null;
        }
    }

    public List<CnStockDailyStat> getSymbolStat(String tsCode, int limit){
        Date lowDate = DateHelper.beforeNDays(new Date(), limit);
        CnStockDailyStatExample example = new CnStockDailyStatExample();
        example.createCriteria().andSymbolEqualTo(tsCode).andTradeDateGreaterThan(lowDate);
        example.setOrderByClause("trade_date asc");
        List<CnStockDailyStat> statList = dailyStatMapper.selectByExample(example);
        return statList;
    }

    public Date getDeltaDate(String symbol){
        DataDeltaDate daily = deltaMapper.selectByPrimaryKey(symbol);
        if(daily != null){
            return daily.getDataDate();
        }
        return null;
    }

    public List<CnMarketDaily> loadDailyMarket(Date last){
        CnMarketDailyExample example = new CnMarketDailyExample();
        example.createCriteria().andTradeDateGreaterThanOrEqualTo(last);
        example.setOrderByClause("trade_date asc");
        List<CnMarketDaily> cmd = marketDailyMapper.selectByExample(example);
        return cmd;
    }
}
