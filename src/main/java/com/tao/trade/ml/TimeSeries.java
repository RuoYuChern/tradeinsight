package com.tao.trade.ml;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TimeSeries {

    public static <T> void fillValue(List<T> data, Function<T,BigDecimal> function, BiConsumer<T,BigDecimal> setter){
        BigDecimal ma = AVG(data, function);
        for(int cur = 0; cur < data.size(); cur++){
            BigDecimal now = function.apply(data.get(cur));
            if(now != null){
                continue;
            }
            setter.accept(data.get(cur), ma);
        }
    }

    public static <T> BigDecimal AVG(List<T> data, Function<T,BigDecimal> function){
        double sum = 0;
        int count = 0;
        for(int i = 0; i < data.size(); i++){
            BigDecimal v = function.apply(data.get(i));
            if(v == null){
                continue;
            }
            count += 1;
            sum += v.doubleValue();
        }
        if(count == 0){
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
    }

    public static <T> BigDecimal MA(int period, int end, List<T> data, Function<T,BigDecimal> function){
        return SMA(period, end, data, function);
    }

    public static <T> BigDecimal SMA(int period, int end, List<T> data, Function<T,BigDecimal> function){
        int start = (end - period) + 1;
        if((start < 0) || (end >= data.size())){
            return null;
        }
        double sum = 0;
        for(int i = start; i <= end; i++){
            sum = sum + function.apply(data.get(i)).doubleValue();
        }
        BigDecimal d = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(period), 2, RoundingMode.DOWN);
        return d;
    }

    public static <T> BigDecimal EMA(int period, int end, List<T> data, Function<T,BigDecimal> function){
        int start = (end - period) + 1;
        if((start < 0) || (end >= data.size())){
            return null;
        }

        double rate = 2.0/(period + 1);
        double ema = 0;
        for (int i = start; i <= end; i++){
            double cure = function.apply(data.get(i)).doubleValue();
            if(i == start){
                ema = cure;
            }else{
                ema = cure * rate + (1 - rate)*ema;
            }
        }
        return new BigDecimal(ema).setScale(2, RoundingMode.HALF_DOWN);
    }

    public static <T> BigDecimal WMA(int period, int end, List<T> data, Function<T,BigDecimal> function){
        int start = (end - period) + 1;
        if((start < 0) || (end >= data.size())){
            return null;
        }
        double wma = 0;
        double sum = 0;
        for(int i = 0; i < period; i++){
            double cure = function.apply(data.get(i + start)).doubleValue() * (i + 1);
            sum = sum + (i + 1);
            wma = wma + cure;
        }
        return new BigDecimal((wma/sum)).setScale(2, RoundingMode.HALF_DOWN);
    }
}
