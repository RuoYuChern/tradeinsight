package com.tao.trade.ml;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MACD {
    private final int shortPeriod;
    private final int longPeriod;
    private final int signalPeriod;

    public MACD(int shortPeriod, int longPeriod, int signalPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
        this.signalPeriod = signalPeriod;
    }

    public <T> void calculate(List<T> data, Function<T, BigDecimal> function, BiConsumer<T,BigDecimal> setter){
        double [] emaShort = calculateEMA(data, function, shortPeriod);
        double [] emaLong  = calculateEMA(data, function, longPeriod);
        double [] macdLine = new double[data.size()];
        for (int i = 0; i < data.size(); i++){
            macdLine[i] = emaShort[i] - emaLong[i];
        }
        double[] signalLine = calculateEMA(macdLine, signalPeriod);
        for(int i = 0; i < data.size(); i++){
            double v = macdLine[i] - signalLine[i];
            T item = data.get(i);
            setter.accept(item, new BigDecimal(v).setScale(2, RoundingMode.HALF_DOWN));
        }
    }

    private double [] calculateEMA(double [] values, int period){
        double[] ema = new double[values.length];
        double multiplier = 2.0/(period + 1);
        ema[0] = values[0];
        for(int i = 1; i < values.length; i++){
            ema[i] = values[i] * multiplier + (1 - multiplier)*ema[i - 1];
        }
        return ema;
    }

    private <T> double [] calculateEMA(List<T> data, Function<T, BigDecimal> function, int period){
        double[] ema = new double[data.size()];
        double multiplier = 2.0/(period + 1);
        ema[0] = function.apply(data.get(0)).doubleValue();
        for(int i = 1; i < data.size(); i++){
            double value = function.apply(data.get(i)).doubleValue();
            ema[i] = value * multiplier + (1 - multiplier)*ema[i - 1];
        }
        return ema;
    }
}
