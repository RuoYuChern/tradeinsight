package com.tao.trade.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

public class DateHelper {
    public static boolean dateEqual(Date first, Date second){
       return DateUtils.isSameDay(first, second);
    }

    public static String dateToStr(String fmt, Date date){
        return DateFormatUtils.format(date, fmt);
    }

    public static Date strToDate(String fmt, String str){
        try{
            return DateUtils.parseDate(str, fmt);
        }catch (Throwable t){
            throw new RuntimeException(t.getMessage());
        }
    }

    public static Date afterNDays(Date now, int days){
        return DateUtils.addDays(now, days);
    }
}
