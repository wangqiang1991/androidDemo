/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.hande.goochao.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yanshen on 2015/12/21.
 */
public class DateUtils {

    /**
     * 第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static Date frist(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static Date last(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month + 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 一天开始
     * @param date
     * @return
     */
    public static Date dayFrist(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 一天结束
     * @param date
     * @return
     */
    public static Date dayLast(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    /**
     * 相差多少天
     * @param start
     * @param end
     * @return
     */
    public static int diffDay(Date start, Date end){
        long milliseconds1 = start.getTime();
        long milliseconds2 = end.getTime();
        long diff = milliseconds2 - milliseconds1;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        if (diffDays < 0) return 0;
        return (int) diffDays;
    }

    /**
     * 添加多少年
     * @param now
     * @param amount
     * @return
     */
    public static Date addHowYear(Date now, int amount){
        return addHow(now, Calendar.YEAR, amount);
    }

    /**
     * 添加多少月
     * @param now
     * @param amount
     * @return
     */
    public static Date addHowMonth(Date now, int amount){
        return addHow(now, Calendar.MONTH, amount);
    }

    /**
     * 添加多少周
     * @param now
     * @param amount
     * @return
     */
    public static Date addHowWeek(Date now, int amount){
        return addHow(now, Calendar.WEEK_OF_MONTH, amount);
    }

    /**
     * 添加多少天
     * @param now
     * @param amount
     * @return
     */
    public static Date addHowDay(Date now, int amount){
        return addHow(now, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 添加多少小时
     * @param now
     * @param amount
     * @return
     */
    public static Date addHowHour(Date now, int amount){
        return addHow(now, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 添加多少分钟
     * @param now
     * @param amount
     * @return
     */
    public static Date addHowMinute(Date now, int amount){
        return addHow(now, Calendar.MINUTE, amount);
    }

    /**
     * 在当前时间添加多少数量
     * @param field
     * @param amount
     * @return
     */
    public static Date addHow(int field, int amount){
        return addHow(new Date(), field, amount);
    }

    public static Date addHow(Date now, int field, int amount){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static boolean isDay(Date edate, Date date) {
        int eDay = edate.getDate();
        int day = date.getDate();
        if (isMonth(edate, date) && eDay == day) {
            return true;
        }
        return false;
    }

    public static boolean isMonth(Date edate, Date date) {
        int eYear = edate.getYear();
        int year = date.getYear();
        int eMonth = edate.getMonth();
        int month = date.getMonth();
        if (eYear == year && eMonth == month) {
            return true;
        }
        return false;
    }

    public static String format(Date date, String format){
        return new SimpleDateFormat(format).format(date);
    }

    public static String format(Date date){
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * 日期字符串转换为Date
     * @param date
     * @param format
     * @return
     */
    public static Date parse(String date, String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 日期字符串转换为毫秒
     * @param date
     * @param format
     * @return
     */
    public static long parseMilliseconds(String date, String format) {
        Date dat = parse(date, format);
        if (dat == null) {
            return 0;
        }
        return dat.getTime();
    }

    /**
     * 去掉日期字符串中的时间部分
     * @param date
     * @return
     */
    public static String removeTime(String date){
        if(!TextUtils.isEmpty(date) && date.length() > 10){
            return date.substring(0, 10);
        }
        return date;
    }


    /**
     * 功能:获取时间距离现在间隔多久
     * @param dateStr
     * @return
     */
    public static String timeInfoWithDateString(String dateStr) {
        Date date = DateUtils.parse(dateStr,"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date dateNow = new Date();

        int dayNow = dateNow.getDay();
        int dayCurrent = date.getDay();

        int timeNow = (int) (dateNow.getTime() / 1000);
        int timeCurrent = (int) (date.getTime() / 1000);

        int diffSeconds = timeNow - timeCurrent; //两个时间相差的秒数

        if (diffSeconds < 60) { //60秒以内
            if (dayNow == dayCurrent) {
                return "刚刚";
            } else {
                return "昨天";
            }
        }

        if (diffSeconds > 60 && diffSeconds < 60 * 60 *24) { //1-24小时之间
            if (dayNow == dayCurrent) {
                return DateUtils.format(date,"HH:mm");
            } else {
                return "昨天";
            }
        }

        if (diffSeconds > 60 * 60 *24 && diffSeconds < 60 * 60 * 24 * 2) { // 24小时-48小时之间
            if (dayNow == dayCurrent) {
                return DateUtils.format(date,"HH:mm");
            } else {
                if (dayNow - dayCurrent > 1) {
                    return DateUtils.format(date,"yyyy-MM-dd");
                }
                return "昨天";
            }
        }
        return DateUtils.format(date,"yyyy-MM-dd");
    }

    /**
     * 功能：将时间戳转换为Date对象
     * @param timeStamp
     * @return
     */
    public static Date timeStampToDate(long timeStamp) {
        Date date = new Date(timeStamp);
        return date;
    }

    /**
     * 功能：时间戳
     * @param timeStamp
     * @param format
     * @return
     */
    public static String timeStampToStr(long timeStamp,String format) {
        Date date = timeStampToDate(timeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
}
