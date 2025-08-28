package top.harrylei.community.core.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 日期工具类
 *
 * @author harry
 */
public class DateUtil {
    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

    // 标准格式
    public static final DateTimeFormatter STANDARD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 已有的特殊格式
    public static final DateTimeFormatter UTC_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter BLOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
    public static final DateTimeFormatter BLOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    // 支持的日期格式列表
    private static final List<DateTimeFormatter> SUPPORTED_FORMATTERS =
            Arrays.asList(
                    STANDARD_FORMAT,
                    DATE_FORMAT,
                    UTC_FORMAT,
                    DB_FORMAT,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );

    /**
     * 时间常量
     */
    public static final Long ONE_DAY_MILL = 86400_000L;
    public static final Long ONE_DAY_SECONDS = 86400L;
    public static final Long ONE_MONTH_SECONDS = 31 * 86400L;
    public static final Long THREE_DAY_MILL = 3 * ONE_DAY_MILL;

    /**
     * 字符串转LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }

        for (DateTimeFormatter formatter : SUPPORTED_FORMATTERS) {
            try {
                // yyyy-MM-dd
                if (dateStr.length() == 10) {
                    return LocalDate.parse(dateStr, formatter).atStartOfDay();
                } else {
                    return LocalDateTime.parse(dateStr, formatter);
                }
            } catch (Exception e) {
                // 尝试下一个格式
            }
        }

        log.warn("Cannot parse date: {}", dateStr);
        return null;
    }

    /**
     * 毫秒转LocalDateTime
     */
    public static LocalDateTime time2LocalTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转字符串(标准格式)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return STANDARD_FORMAT.format(dateTime);
    }

    /**
     * 毫秒转日期时间
     */
    public static String time2day(long timestamp) {
        return format(BLOG_TIME_FORMAT, timestamp);
    }

    public static String time2day(Timestamp timestamp) {
        return time2day(timestamp.getTime());
    }

    /**
     * 毫秒转UTC格式
     */
    public static String time2utc(long timestamp) {
        return format(UTC_FORMAT, timestamp);
    }

    /**
     * 毫秒转日期
     */
    public static String time2date(long timestamp) {
        return format(BLOG_DATE_FORMAT, timestamp);
    }

    public static String time2date(Timestamp timestamp) {
        return time2date(timestamp.getTime());
    }

    /**
     * 格式化时间戳
     */
    public static String format(DateTimeFormatter format, long timestamp) {
        LocalDateTime time = time2LocalTime(timestamp);
        return format.format(time);
    }

    /**
     * 获取当前日期的开始时间 (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay();
    }

    /**
     * 获取当前日期的结束时间 (23:59:59.999999999)
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atTime(23, 59, 59, 999999999);
    }

    /**
     * 判断是否跨天
     */
    public static boolean skipDay(long last, long now) {
        last = last / ONE_DAY_MILL;
        now = now / ONE_DAY_MILL;
        return last != now;
    }
}
