package com.tao.trade.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

@Slf4j
public class Help {
    private static final BigDecimal BASE_W = new BigDecimal("10000");
    public static void sleep(int s){
        try{
            Thread.sleep(s * 1000);
        }catch (Throwable t){

        }
    }

    public static BigDecimal baseWan(BigDecimal value){
        return value.divide(BASE_W, 5, RoundingMode.HALF_DOWN);
    }


    public static <T,U> void inverted(List<T> list, U obj, BiConsumer<U,List<T>> setter){
        List<T> nl = new LinkedList<>();
        for(int i = list.size(); i > 0; i--){
            nl.add(list.get(i - 1));
        }
        setter.accept(obj, nl);
    }

    public static <T,U> void inverted(int offset, int size, List<T> list, U obj, BiConsumer<U,List<T>> setter){
        List<T> nl = new LinkedList<>();
        int skip = 0;
        for(int i = list.size(); i > 0; i--){
            if(skip < offset){
                skip ++;
                continue;
            }
            if(nl.size() >= size){
                break;
            }
            nl.add(list.get(i - 1));
        }
        setter.accept(obj, nl);
    }

    public static Object call(Callable<Object> callable){
        try{
            return callable.call();
        }catch (Throwable t){
            log.info("call:{}", t.getMessage());
            return null;
        }
    }

    public static <T> T tryCall(Callable<T> callable){
        Throwable exp = null;
        for(int i = 0; i < 5; i++){
            try{
                return callable.call();
            }catch (Throwable t){
                exp = t;
                log.info("call:{}", t.getMessage());
                Help.sleep(3);
            }
        }
        throw new RuntimeException(exp);
    }
}
