package com.tao.trade.domain;

import com.tao.trade.infra.vo.StockBasicVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CnStockBaseData {
    private AtomicReference<List<StockBasicVo>> basicVoList;
    public CnStockBaseData(){
        basicVoList = new AtomicReference<>();
    }

    public void updateBasic(List<StockBasicVo> voList){
        basicVoList.set(voList);
    }

    public List<StockBasicVo> getBasic(){
        return basicVoList.get();
    }

}
