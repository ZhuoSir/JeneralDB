package com.chen.JeneralDB.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * Created by sunny-chen on 2017/3/26.
 */
public class DateUtil {

    /**
     * 默认时间格式
     * */
    private static final String defaultPattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取当前时间
     * */
    public static String currentTime() {
        return currentTime(null);
    }

    /**
     * 获取当前时间
     *
     * @param pattern 时间格式
     * */
    public static String currentTime(String pattern) {
        return dateToString(new Date(), pattern);
    }

    /**
     * 昨天时间
     *
     * */
    public static String yesterday() {
        return yesterday(null);
    }

    /**
     * 昨天时间
     *
     * @param pattern 时间格式
     * */
    public static String yesterday(String pattern) {
        return coupleOfDaysAgo(1, pattern);
    }

    /**
     * 明天时间
     *
     * */
    public static String tomorrow() {
        return tomorrow(null);
    }

    /**
     * 明天时间
     *
     * @param pattern 时间格式
     * */
    public static String tomorrow(String pattern) {
        return coupleOfDaysLater(1, pattern);
    }

    /**
     * 获取几天之前的时间
     *
     * @param countOfDay 天数
     * */
    public static String coupleOfDaysAgo(int countOfDay) {
        return coupleOfDaysAgo(countOfDay, null);
    }

    /**
     * 获取几天之前的时间
     *
     * @param countOfDay 天数
     * @param pattern 时间格式
     * */
    public static String coupleOfDaysAgo(int countOfDay, String pattern) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -countOfDay);
        Date date = calendar.getTime();

        return dateToString(date, pattern);
    }

    /**
     * 获取几天之后的时间
     *
     * @param countOfDay 天数
     * */
    public static String coupleOfDaysLater(int countOfDay) {
        return coupleOfDaysLater(countOfDay, null);
    }

    /**
     * 获取几天之后的时间
     *
     * @param countOfDay 天数
     * @param pattern 时间格式
     * */
    public static String coupleOfDaysLater(int countOfDay, String pattern) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, countOfDay);
        Date date = calendar.getTime();

        return dateToString(date, pattern);
    }

    /**
     * 一个星期前时间
     *
     * */
    public static String oneWeekAgo() {
        return oneWeekAgo(null);
    }

    /**
     * 一个星期前时间
     *
     * */
    public static String oneWeekAgo(String pattern) {
        return coupleOfWeeksAgo(1, pattern);
    }

    /**
     * 几个星期前时间
     *
     * @param countOfWeek 周数
     * */
    public static String coupleOfWeeksAgo(int countOfWeek) {
        return coupleOfWeeksAgo(countOfWeek, null);
    }

    /**
     * 几个星期前时间
     *
     * @param countOfWeek 周数
     * @param pattern 时间格式
     * */
    public static String coupleOfWeeksAgo(int countOfWeek, String pattern) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -7 * countOfWeek);
        Date date = calendar.getTime();

        return dateToString(date, pattern);
    }

    /**
     * 几个月前时间
     *
     * @param countOfMonth 月数
     * */
    public static String coupleOfMonthsAgo(int countOfMonth) {
        return coupleOfMonthsAgo(countOfMonth, null);
    }

    /**
     * 几个月前时间
     *
     * @param countOfMonth 月数
     * @param pattern 时间格式
     * */
    public static String coupleOfMonthsAgo(int countOfMonth, String pattern) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1 * countOfMonth);
        Date date = calendar.getTime();

        return dateToString(date, pattern);
    }

    /**
     * 几年前时间
     *
     * @param countOfYear 月数
     * */
    public static String coupleOfYearsAgo(int countOfYear) {
        return coupleOfYearsAgo(countOfYear, null);
    }

    /**
     * 几年前时间
     *
     * @param countOfYear 月数
     * @param pattern 时间格式
     * */
    public static String coupleOfYearsAgo(int countOfYear, String pattern) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1 * countOfYear);
        Date date = calendar.getTime();

        return dateToString(date, pattern);
    }

    /**
     * 日期类转字符串
     *
     * @param date 日期
     *
     * @return 结果字符串
     * */
    public static String dateToString(Date date) {
        return dateToString(date, null);
    }

    /**
     * 日期类转字符串
     *
     * @param date 日期
     * @param pattern 时间格式
     *
     * @return 结果字符串
     * */
    public static String dateToString(Date date, String pattern) {
        if (!StringUtil.isNotNullOrEmpty(pattern))
            pattern = defaultPattern;

        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 字符串转日期
     * */
    public static Date strToDate(String str) throws ParseException {
        return new SimpleDateFormat(defaultPattern).parse(str);
    }
}
