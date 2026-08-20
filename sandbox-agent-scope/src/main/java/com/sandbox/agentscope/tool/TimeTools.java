package com.sandbox.agentscope.tool;

import io.agentscope.core.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @description: 时间工具类
 * @author: 0101
 * @create: 2026/05/22
 */
@Slf4j
public class TimeTools {

    private static final long SEVEN_DAYS_MILLIS = 7 * 24 * 60 * 60 * 1000L; // 7天 = 604,800,000毫秒
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 获取当前日期、星期几和毫秒值
     * 大模型可直接调用此方法获取当前时间信息
     *
     * @return 包含日期、星期、毫秒值的字符串，格式："日期: xxxx-xx-xx, 星期: x, 毫秒值: xxxxx"
     */
    @Tool(description = "获取当前日期、星期几和毫秒值")
    public String getCurrentDateTimeInfo() {
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);
        String date = now.format(DATE_FORMATTER);
        String week = now.format(WEEK_FORMATTER);
        long millis = System.currentTimeMillis();

        log.debug("当前信息 - 日期: {}, 星期: {}, 毫秒值: {}", date, week, millis);
        return String.format("日期: %s, 星期: %s, 毫秒值: %d", date, week, millis);
    }

    /**
     * 获取7天前的毫秒时间戳
     * 用于查询条件的 startTime 参数
     *
     * @return 7天前的毫秒时间戳
     */
    @Tool(description = "获取7天前的毫秒时间戳，用于查询开始时间(startTime)")
    public long getSevenDaysAgoMillis() {
        long sevenDaysAgo = System.currentTimeMillis() - SEVEN_DAYS_MILLIS;
        log.debug("7天前毫秒时间戳: {}", sevenDaysAgo);
        return sevenDaysAgo;
    }

    /**
     * 毫秒时间戳转完整日期时间字符串 (yyyy-MM-dd HH:mm:ss)
     * 将时间戳转换为精确到秒的完整时间
     *
     * @param millis 毫秒时间戳
     * @return 完整日期时间字符串，如 "2026-06-09 14:30:25"
     */
    @Tool(description = "将毫秒时间戳转换为完整日期时间字符串")
    public String convertMillisToDateTime(long millis) {
        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), DEFAULT_ZONE);
            String result = dateTime.format(DATE_TIME_FORMATTER);
            log.debug("毫秒时间戳转换成功 - 输入: {}, 输出: {}", millis, result);
            return result;
        } catch (Exception e) {
            log.error("毫秒时间戳转换失败 - 输入: {}, 错误: {}", millis, e.getMessage(), e);
            return "转换失败，请检查时间戳格式";
        }
    }

    /**
     * 秒级时间戳转完整日期时间字符串 (yyyy-MM-dd HH:mm:ss)
     * 将秒级时间戳转换为精确到秒的完整时间
     *
     * @param seconds 秒级时间戳
     * @return 完整日期时间字符串，如 "2026-06-09 14:30:25"
     */
    @Tool(description = "将秒级时间戳转换为完整日期时间字符串")
    public String convertSecondsToDateTime(long seconds) {
        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), DEFAULT_ZONE);
            String result = dateTime.format(DATE_TIME_FORMATTER);
            log.debug("秒级时间戳转换成功 - 输入: {}, 输出: {}", seconds, result);
            return result;
        } catch (Exception e) {
            log.error("秒级时间戳转换失败 - 输入: {}, 错误: {}", seconds, e.getMessage(), e);
            return "转换失败，请检查时间戳格式";
        }
    }

    /**
     * 日期字符串转毫秒时间戳 (yyyy-MM-dd)
     * 解决大模型自行转换日期计算出错的问题
     *
     * @param dateStr 日期字符串，如 "2026-06-09"
     * @return 该日期零点(00:00:00)的毫秒时间戳，转换失败返回-1
     */
    @Tool(description = "将日期字符串(yyyy-MM-dd格式)转换为毫秒时间戳，返回该日零点的时间戳。例如输入'2026-06-09'返回对应时间戳")
    public long convertDateToMillis(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalDateTime dateTime = date.atStartOfDay();
            long millis = dateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
            log.debug("日期转时间戳 - 日期: {}, 时间戳: {}", dateStr, millis);
            return millis;
        } catch (DateTimeParseException e) {
            log.error("日期格式错误: {}, 正确格式为 yyyy-MM-dd", dateStr, e);
            return -1L;
        }
    }

    /**
     * 完整日期时间字符串转毫秒时间戳 (yyyy-MM-dd HH:mm:ss)
     * 将精确到秒的时间字符串转换为毫秒时间戳
     *
     * @param dateTimeStr 完整日期时间字符串，如 "2026-06-09 14:30:25"
     * @return 毫秒时间戳，转换失败返回-1
     */
    @Tool(description = "将完整日期时间字符串(yyyy-MM-dd HH:mm:ss格式)转换为毫秒时间戳。例如输入'2026-06-09 14:30:25'返回对应时间戳")
    public long convertDateTimeToMillis(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
            long millis = dateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
            log.debug("完整时间转毫秒时间戳 - 时间: {}, 毫秒时间戳: {}", dateTimeStr, millis);
            return millis;
        } catch (DateTimeParseException e) {
            log.error("完整时间格式错误: {}, 正确格式为 yyyy-MM-dd HH:mm:ss", dateTimeStr, e);
            return -1L;
        }
    }

    /**
     * 完整日期时间字符串转秒级时间戳 (yyyy-MM-dd HH:mm:ss)
     * 将精确到秒的时间字符串转换为秒级时间戳
     *
     * @param dateTimeStr 完整日期时间字符串，如 "2026-06-09 14:30:25"
     * @return 秒级时间戳，转换失败返回-1
     */
    @Tool(description = "将完整日期时间字符串(yyyy-MM-dd HH:mm:ss格式)转换为秒级时间戳。例如输入'2026-06-09 14:30:25'返回对应秒级时间戳")
    public long convertDateTimeToSeconds(String dateTimeStr) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
            long seconds = dateTime.atZone(DEFAULT_ZONE).toEpochSecond();
            log.debug("完整时间转秒级时间戳 - 时间: {}, 秒级时间戳: {}", dateTimeStr, seconds);
            return seconds;
        } catch (DateTimeParseException e) {
            log.error("完整时间格式错误: {}, 正确格式为 yyyy-MM-dd HH:mm:ss", dateTimeStr, e);
            return -1L;
        }
    }
}