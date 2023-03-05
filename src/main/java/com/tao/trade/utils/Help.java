package com.tao.trade.utils;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.util.DateUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public class Help {
    public static boolean isHourAfter(int hours){
        return (DateUtils.hour(new Date()) > hours);
    }
    public static void sleep(int s){
        try{
            Thread.sleep(s * 1000);
        }catch (Throwable t){

        }
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

    public static <T> Object call(Callable<Object> callable){
        try{
            return callable.call();
        }catch (Throwable t){
            log.info("call:{}", t.getMessage());
            return null;
        }
    }
}
