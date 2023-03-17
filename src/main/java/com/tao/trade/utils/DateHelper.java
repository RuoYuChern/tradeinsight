package com.tao.trade.utils;

import com.tao.trade.facade.TaoConstants;
import com.tao.trade.infra.TuShareClient;
import com.tao.trade.infra.vo.TradeDateVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
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

    public static int hours(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static Date getBiggerDate(TuShareClient tuShareClient, Date start, Date now){
        String startDate = DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, start);
        List<TradeDateVo> list = tuShareClient.trade_cal(startDate, DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, now));
        /**逆序**/
        for(int i = 0; i < list.size(); i++){
            TradeDateVo vo = list.get(i);
            if(vo.getIsOpen() == 0){
                continue;
            }
            return DateHelper.strToDate("yyyyMMdd", vo.getDate());
        }
        return null;
    }

    public static Date getLessDay(TuShareClient tuShareClient, Date start, Date now){
        String startDate = DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, start);
        List<TradeDateVo> list = tuShareClient.trade_cal(startDate, DateHelper.dateToStr(TaoConstants.TU_DATE_FMT, now));
        /**逆序**/
        int offset = 0;
        Date last = null;
        for(TradeDateVo vo:list){
            if(vo.getIsOpen() == 0){
                continue;
            }
            offset++;
            last = DateHelper.strToDate("yyyyMMdd", vo.getDate());
            if(offset >= 200){
                break;
            }
        }
        log.info("offset:{}", offset);
        return last;
    }

    public static Date afterNDays(Date now, int days){
        return DateUtils.addDays(now, days);
    }

    public static Date beforeNDays(Date now, int days){
        return DateUtils.addDays(now, -days);
    }

    public static Date getCureDataDate(){
        Date date = new Date();
        if(isHourAfter(17)){
            return date;
        }else{
            return beforeNDays(date, 1);
        }
    }

    public static int daysDiff(Date now, Date before){
        long diff = now.getTime() - before.getTime();
        return (int)(diff/(1000*60*60*24));
    }

    public static boolean isHourAfter(int h){
        return (hours(new Date()) > h);
    }

    public static boolean isHourBefore(int h){
        return (hours(new Date()) < h);
    }

    public static Date getDataDate(){
        Date today = new Date();
        if(isHourBefore(17)){
            /**还没收盘，今天的数据放到增量同步**/
            today = DateHelper.beforeNDays(today, 1);
        }
        return today;
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
        Date lastDay = DateUtils.addYears(now, -1);
        /****quarter = 3, Q4Q1Q2Q3***/
        /****quarter = 1, Q2Q3Q4Q1***/
        startQ = String.format("%sQ%d", DateFormatUtils.format(lastDay, "yyyy"), quarter);
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
        String endM = DateFormatUtils.format(now, "yyyyMM");
        Date lastDay;
        lastDay = DateUtils.addYears(now, -1);
        String startM = DateFormatUtils.format(lastDay, "yyyyMM");
        return Pair.of(startM, endM);
    }
}
