package com.tao.trade.infra;

import lombok.extern.slf4j.Slf4j;

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
}
