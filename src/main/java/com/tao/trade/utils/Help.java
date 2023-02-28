package com.tao.trade.utils;

import org.thymeleaf.util.DateUtils;

import java.util.Date;

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
}
