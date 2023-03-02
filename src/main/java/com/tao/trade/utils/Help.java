package com.tao.trade.utils;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.util.DateUtils;

import java.util.Date;
import java.util.concurrent.Callable;
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

    public static <T> Object call(Callable<Object> callable){
        try{
            return callable.call();
        }catch (Throwable t){
            log.info("call:{}", t.getMessage());
            return null;
        }
    }
}
