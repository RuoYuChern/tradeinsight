package com.tao.trade.ml;

import com.tao.trade.facade.IndicatorDto;
import com.tao.trade.infra.db.model.CnStockDaily;
import com.tao.trade.utils.DateHelper;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
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

    public BigDecimal MA(List<BigDecimal> data){
        double sum = 0;
        for(BigDecimal d:data){
            sum = sum + d.doubleValue();
        }
        if(data.isEmpty()){
            return BigDecimal.ZERO;
        }else{
            BigDecimal d = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(data.size()), 2, RoundingMode.DOWN);
            return d;
        }
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

    public static <T> List<IndicatorDto> SMA(int period, List<T> dailyList, Function<T,BigDecimal> valueFunc,  Function<T, Date> dateFun){
        List<IndicatorDto> dtoList = new ArrayList<>();
        double sum = 0;
        int offset = 0;
        T pre = null;
        for(T d:dailyList){
            sum += valueFunc.apply(d).doubleValue();
            if(offset >= (period -1)){
                IndicatorDto ind = new IndicatorDto();
                ind.setValue(new BigDecimal(sum/period).setScale(2, RoundingMode.HALF_DOWN));
                ind.setDay(DateHelper.dateToStr("yyyyMMdd", dateFun.apply(d)));
                ind.setPrice(valueFunc.apply(d));
                dtoList.add(ind);
            }
            if(pre != null){
                sum -= valueFunc.apply(pre).doubleValue();
            }
            pre = d;
        }
        return dtoList;
    }

    public static List<IndicatorDto> ADX_CALC(int period, int delta, List<CnStockDaily> dailyList){
        List<IndicatorDto> dtoList = new ArrayList<>();
        int twoPeriod = 2 * period;
        for(int i = (twoPeriod + delta); i < dailyList.size(); i++){
            BigDecimal adx = ADX(period, delta, dailyList, i);
            IndicatorDto ind = new IndicatorDto();
            ind.setPrice(dailyList.get(i).getClosePrice());
            ind.setValue(adx);
            ind.setDay(DateHelper.dateToStr("yyyyMMdd", dailyList.get(i).getTradeDate()));

            dtoList.add(ind);
        }
        return dtoList;
    }

    public static BigDecimal ADX(int period, int delta, List<CnStockDaily> dailyList, int end){
        int twoPeriod = 2 * period;
        if(end < (twoPeriod + delta)){
            return BigDecimal.ZERO;
        }
        int len = twoPeriod + delta;
        double []dmPlus = new double[len];
        double []dmMinus = new double[len];
        double []tr = new double[len];
        double []diPlus = new double[len];
        double []diMinus = new double[len];
        double []dx = new double[len];
        double []adx = new double[len];

        /**initialize**/
        dmPlus[0] = 0;
        dmMinus[0] = 0;
        tr[0] = 0;
        diPlus[0] = 0;
        diMinus[0] = 0;
        int start = end - len + 1;

        for(int i = 1; i < len; i++){
            int pos = (start + i);
            CnStockDaily pre = dailyList.get(pos - 1);
            CnStockDaily cure = dailyList.get(pos);
            double upMove = cure.getHighPrice().subtract(pre.getHighPrice()).doubleValue();
            double downMove = pre.getLowPrice().subtract(cure.getLowPrice()).doubleValue();
            /**dmPlus**/
            if((upMove > downMove) && (upMove > 0)){
                dmPlus[i] = upMove;
            }else{
                dmPlus[i] = 0;
            }
            /**dmMinus**/
            if((downMove > upMove) && (downMove > 0)){
                dmMinus[i] = downMove;
            }else {
                dmMinus[i] = 0;
            }
            /**abs(h(i) - close(i-1))**/
            double hC = cure.getHighPrice().subtract(pre.getClosePrice()).abs().doubleValue();
            /**max(h(i) - l(i), abs(h(i) -close(i - 1)))***/
            double HlHc = Math.max(cure.getHighPrice().subtract(cure.getLowPrice()).doubleValue(), hC);
            /**abs(l(i) - close(i - 1))***/
            double lc = cure.getLowPrice().subtract(pre.getClosePrice()).abs().doubleValue();
            tr[i] = Math.max(lc, HlHc);
        }
        /*****/
        double dmPlusSmooth = 0;
        double dmMinusSmooth = 0;
        double trSmooth = 0;
        for(int i = 0; i < period; i++){
            dmPlusSmooth += dmPlus[i];
            dmMinusSmooth += dmMinus[i];
            trSmooth += tr[i];
            diPlus[i] = 0;
            diMinus[i] = 0;
        }
        diPlus[period - 1] = dmPlusSmooth/period;
        diMinus[period - 1] = dmMinusSmooth/period;
        double atr = trSmooth/period;
        for(int i = period; i < len; i++){
            diPlus[i] = ((diPlus[i-1] * (period-1)) + dmPlus[i]) / period;
            diMinus[i] = ((diMinus[i-1] * (period-1)) + dmMinus[i]) / period;
            atr = ((atr * (period-1)) + tr[i]) / period;
            double plusDI = 100 * (diPlus[i] / atr);
            double minusDI = 100 * (diMinus[i] / atr);
            dx[i] = 100 * (Math.abs(plusDI - minusDI) / (plusDI + minusDI));
            if (i >= twoPeriod-1) {
                double dxSmooth = 0;
                for (int j = i-period+1; j <= i; j++) {
                    dxSmooth += dx[j];
                }
                adx[i] = dxSmooth / period;
            }else{
                adx[i] = 0;
            }
        }
        try{
            return new BigDecimal(adx[len - 1]).setScale(2, RoundingMode.HALF_DOWN);
        }catch (Throwable t){
            log.warn("len = {}, period={}, adx={}, twoPeriod={}", len, period, adx[len - 1], twoPeriod);
            throw t;
        }
    }
}
