package com.yunkesoftware.www.utils;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class LocalDateUtils {

    private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    private static final int PATTERN_CACHE_SIZE = 100;

    public static final String PATTERN_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_YYYYMMDD_BIAS = "yyyy/MM/dd";
    public static final String PATTERN_YYYYMMDDHHMMSS_S = "yyyyMMddHHmmss";
    public static final String PATTERN_YYYYMMDD = "yyyy-MM-dd";
    public static final String PATTERN_YYYYMM = "yyyy-MM";
    public static final String PATTERN_MMDD = "MM-dd";
    public static final String PATTERN_MM_YEAR_DD_MOTH = "M月d日";
    public static final String PATTERN_MM_YEAR_DD_MOTH_PAT = "MM月dd日";
    public static final String PATTERN_HHMM = "HH:mm";


    /**
     * 在缓存中创建DateTimeFormatter
     *
     * @param pattern 格式
     */
    private static DateTimeFormatter createCacheFormatter(String pattern) {
        if (pattern == null || pattern.length() == 0) {
            throw new IllegalArgumentException("Invalid pattern specification");
        }
        DateTimeFormatter formatter = FORMATTER_CACHE.get(pattern);
        if (formatter == null) {
            if (FORMATTER_CACHE.size() < PATTERN_CACHE_SIZE) {
                formatter = DateTimeFormatter.ofPattern(pattern);
                DateTimeFormatter oldFormatter = FORMATTER_CACHE.putIfAbsent(pattern, formatter);
                if (oldFormatter != null) {
                    formatter = oldFormatter;
                }
            }
        }
        return formatter;
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate dateToLocalDate(Date date) {
        return dateToLocalDateTime(date).toLocalDate();
    }

    public static LocalTime dateToLocalTime(Date date) {
        return dateToLocalDateTime(date).toLocalTime();
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date localDateToDate(LocalDate localDate) {
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Date localTimeToDate(LocalTime localTime) {
        LocalDate localDate = LocalDate.now();
        Instant instant = LocalDateTime.of(localDate, localTime).atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * localDateTime转换为格式化时间
     *
     * @param localDateTime localDateTime
     * @param pattern       格式
     */
    public static String format(LocalDateTime localDateTime, String pattern) {
        DateTimeFormatter formatter = createCacheFormatter(pattern);
        return localDateTime.format(formatter);
    }

    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, PATTERN_YYYYMMDDHHMMSS);
    }

    public static String format(LocalDate localDate, String pattern) {
        DateTimeFormatter formatter = createCacheFormatter(pattern);
        return localDate.format(formatter);
    }

    public static String format(LocalDate localDate) {
        return format(localDate, PATTERN_YYYYMMDD);
    }

    /**
     * 格式化字符串转为LocalDateTime
     *
     * @param time    格式化时间
     * @param pattern 格式
     */
    public static LocalDateTime parseLocalDateTime(String time, String pattern) {
        DateTimeFormatter formatter = createCacheFormatter(pattern);
        return LocalDateTime.parse(time, formatter);
    }

    public static LocalDate parseLocalDate(String time, String pattern) {
        DateTimeFormatter formatter = createCacheFormatter(pattern);
        return LocalDate.parse(time, formatter);
    }

    public static LocalDateTime getFirstDayOfMonth(String yearMonthDateStr, int monthNum) {
        YearMonth yearMonth = YearMonth.parse(yearMonthDateStr);
        LocalDate localDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1).plusMonths(monthNum);
        return localDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String getDayNameOfWeek(LocalDateTime localDateTime) {
        String[] daysOfWeek = new String[]{"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        return daysOfWeek[localDateTime.getDayOfWeek().getValue() - 1];
    }


    /**
     *  检查是否在时间区间
     *
     */
    public static   Boolean ckeckTimeBetween (LocalDateTime startTime,LocalDateTime endTime,LocalDateTime nowTime){
        if ( nowTime.isBefore(endTime)&&nowTime.isAfter(startTime)) {
            return true;
        }
         return false;

    }


}
