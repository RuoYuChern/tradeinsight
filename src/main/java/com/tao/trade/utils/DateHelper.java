package com.tao.trade.utils;

import org.apache.commons.lang3.time.CalendarUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.weaver.ast.Call;

import java.util.Calendar;
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

    public static Pair<String,String> getOneYearQ(){
        Date now = new Date();
        int month = DateUtils.toCalendar(now).get(Calendar.MONTH) + 1;
        int quarter = 1;
        if(month <= 3){
            quarter = 1;
        }else if((month <= 6) && (quarter > 3)){
            quarter = 2;
        } else if ((month <= 9) && (quarter > 6)) {
            quarter = 3;
        }else{
            quarter = 4;
        }

        String endQ = String.format("%sQ%d", DateFormatUtils.format(now, "yyyy"), quarter);
        String startQ;
        if(quarter == 4){
            /**quarter = 4, Q1Q2Q3Q4**/
            startQ = String.format("%sQ%d", DateFormatUtils.format(now, "yyyy"), 1);
        }else{
            /****quarter = 3, Q4Q1Q2Q3***/
            /****quarter = 1, Q2Q3Q4Q1***/
            Date lastDay = DateUtils.addYears(now, -1);
            startQ = String.format("%sQ%d", DateFormatUtils.format(lastDay, "yyyy"), (quarter + 1));
        }
        return Pair.of(startQ, endQ);
    }

    public static Pair<String,String> getPreMonth(Date date){
        int month = DateUtils.toCalendar(date).get(Calendar.MONTH) + 1;
        Date lastDay;
        if(month > 1){
            lastDay = DateUtils.addMonths(date, -1);
        }else{
            lastDay = DateUtils.addYears(date, -1);
            lastDay = DateUtils.addMonths(lastDay, 11);
        }
        String endM = DateFormatUtils.format(date, "yyyyMM");
        String startM = DateFormatUtils.format(lastDay, "yyyyMM");
        return Pair.of(startM, endM);
    }

    public static Pair<String, String> getOneYearM(){
        Date now = new Date();
        int month = DateUtils.toCalendar(now).get(Calendar.MONTH) + 1;
        String endM = DateFormatUtils.format(now, "yyyyMM");
        Date lastDay;
        if(month == 12){
            lastDay = DateUtils.addMonths(now, -11);
        }else{
            /***-year + month**/
            lastDay = DateUtils.addYears(now, -1);
            lastDay = DateUtils.addMonths(lastDay, 1);
        }
        String startM = DateFormatUtils.format(lastDay, "yyyyMM");
        return Pair.of(startM, endM);
    }
}
