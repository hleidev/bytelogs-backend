package top.harrylei.community.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日期工具类测试
 *
 * @author harry
 */
@DisplayName("DateUtil 测试")
class DateUtilTest {

    @Nested
    @DisplayName("parseDateTime 方法测试")
    class ParseDateTimeTest {

        @Test
        @DisplayName("标准格式应正确解析")
        void shouldParseStandardFormat() {
            String dateStr = "2024-01-15 10:30:45";

            LocalDateTime result = DateUtil.parseDateTime(dateStr);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(1);
            assertThat(result.getDayOfMonth()).isEqualTo(15);
            assertThat(result.getHour()).isEqualTo(10);
            assertThat(result.getMinute()).isEqualTo(30);
            assertThat(result.getSecond()).isEqualTo(45);
        }

        @Test
        @DisplayName("仅日期格式应正确解析（时间为 00:00:00）")
        void shouldParseDateOnlyFormat() {
            String dateStr = "2024-06-20";

            LocalDateTime result = DateUtil.parseDateTime(dateStr);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(6);
            assertThat(result.getDayOfMonth()).isEqualTo(20);
            assertThat(result.getHour()).isEqualTo(0);
            assertThat(result.getMinute()).isEqualTo(0);
            assertThat(result.getSecond()).isEqualTo(0);
        }

        @Test
        @DisplayName("ISO 格式应正确解析")
        void shouldParseIsoFormat() {
            String dateStr = "2024-03-10T14:25:30";

            LocalDateTime result = DateUtil.parseDateTime(dateStr);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(3);
            assertThat(result.getDayOfMonth()).isEqualTo(10);
            assertThat(result.getHour()).isEqualTo(14);
            assertThat(result.getMinute()).isEqualTo(25);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("空或 null 应返回 null")
        void shouldReturnNullForBlankInput(String dateStr) {
            LocalDateTime result = DateUtil.parseDateTime(dateStr);

            assertThat(result).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "2024/01/01", "01-01-2024"})
        @DisplayName("无效格式应返回 null")
        void shouldReturnNullForInvalidFormat(String dateStr) {
            LocalDateTime result = DateUtil.parseDateTime(dateStr);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("formatDateTime 方法测试")
    class FormatDateTimeTest {

        @Test
        @DisplayName("LocalDateTime 应格式化为标准格式")
        void shouldFormatToStandardFormat() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 5, 20, 15, 30, 45);

            String result = DateUtil.formatDateTime(dateTime);

            assertThat(result).isEqualTo("2024-05-20 15:30:45");
        }

        @Test
        @DisplayName("null 应返回 null")
        void shouldReturnNullForNullInput() {
            String result = DateUtil.formatDateTime(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("日期边界值应正确格式化")
        void shouldFormatBoundaryDates() {
            LocalDateTime startOfYear = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
            LocalDateTime endOfYear = LocalDateTime.of(2024, 12, 31, 23, 59, 59);

            assertThat(DateUtil.formatDateTime(startOfYear)).isEqualTo("2024-01-01 00:00:00");
            assertThat(DateUtil.formatDateTime(endOfYear)).isEqualTo("2024-12-31 23:59:59");
        }
    }

    @Nested
    @DisplayName("startOfDay 方法测试")
    class StartOfDayTest {

        @Test
        @DisplayName("应返回当天 00:00:00")
        void shouldReturnStartOfDay() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 7, 15, 14, 30, 45);

            LocalDateTime result = DateUtil.startOfDay(dateTime);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(7);
            assertThat(result.getDayOfMonth()).isEqualTo(15);
            assertThat(result.getHour()).isEqualTo(0);
            assertThat(result.getMinute()).isEqualTo(0);
            assertThat(result.getSecond()).isEqualTo(0);
            assertThat(result.getNano()).isEqualTo(0);
        }

        @Test
        @DisplayName("null 应返回 null")
        void shouldReturnNullForNullInput() {
            LocalDateTime result = DateUtil.startOfDay(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("endOfDay 方法测试")
    class EndOfDayTest {

        @Test
        @DisplayName("应返回当天 23:59:59.999999999")
        void shouldReturnEndOfDay() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 7, 15, 14, 30, 45);

            LocalDateTime result = DateUtil.endOfDay(dateTime);

            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(7);
            assertThat(result.getDayOfMonth()).isEqualTo(15);
            assertThat(result.getHour()).isEqualTo(23);
            assertThat(result.getMinute()).isEqualTo(59);
            assertThat(result.getSecond()).isEqualTo(59);
            assertThat(result.getNano()).isEqualTo(999999999);
        }

        @Test
        @DisplayName("null 应返回 null")
        void shouldReturnNullForNullInput() {
            LocalDateTime result = DateUtil.endOfDay(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("time2LocalTime 方法测试")
    class Time2LocalTimeTest {

        @Test
        @DisplayName("毫秒时间戳应正确转换")
        void shouldConvertTimestampToLocalDateTime() {
            // 2024-01-01 00:00:00 UTC = 1704067200000L
            long timestamp = 1704067200000L;

            LocalDateTime result = DateUtil.time2LocalTime(timestamp);

            assertThat(result).isNotNull();
            // 注意：结果依赖于系统时区
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonthValue()).isEqualTo(1);
            assertThat(result.getDayOfMonth()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("skipDay 方法测试")
    class SkipDayTest {

        @ParameterizedTest
        @CsvSource({
                "1704067200000, 1704153600000, true",  // 相差一天
                "1704067200000, 1704067200001, false", // 同一天（相差 1 毫秒）
                "1704067200000, 1704153599999, false", // 同一天（差 1 毫秒满一天）
        })
        @DisplayName("跨天判断测试")
        void shouldCorrectlyDetermineSkipDay(long last, long now, boolean expected) {
            boolean result = DateUtil.skipDay(last, now);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("相同时间戳应返回 false")
        void sameTimestampShouldReturnFalse() {
            long timestamp = 1704067200000L;

            boolean result = DateUtil.skipDay(timestamp, timestamp);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("时间常量测试")
    class TimeConstantsTest {

        @Test
        @DisplayName("ONE_DAY_MILL 应为 86400000 毫秒")
        void oneDayMillShouldBeCorrect() {
            assertThat(DateUtil.ONE_DAY_MILL).isEqualTo(86400_000L);
        }

        @Test
        @DisplayName("ONE_DAY_SECONDS 应为 86400 秒")
        void oneDaySecondsShouldBeCorrect() {
            assertThat(DateUtil.ONE_DAY_SECONDS).isEqualTo(86400L);
        }

        @Test
        @DisplayName("ONE_MONTH_SECONDS 应为 31 天的秒数")
        void oneMonthSecondsShouldBeCorrect() {
            assertThat(DateUtil.ONE_MONTH_SECONDS).isEqualTo(31 * 86400L);
        }

        @Test
        @DisplayName("THREE_DAY_MILL 应为 3 天的毫秒数")
        void threeDayMillShouldBeCorrect() {
            assertThat(DateUtil.THREE_DAY_MILL).isEqualTo(3 * 86400_000L);
        }
    }

    @Nested
    @DisplayName("格式化方法测试")
    class FormatMethodsTest {

        @Test
        @DisplayName("time2day 应返回博客时间格式")
        void time2DayShouldReturnBlogTimeFormat() {
            // 使用一个已知的时间戳进行测试
            long timestamp = 1704067200000L;

            String result = DateUtil.time2day(timestamp);

            // 格式应为 yyyy年MM月dd日 HH:mm
            assertThat(result).matches("\\d{4}年\\d{2}月\\d{2}日 \\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("time2date 应返回博客日期格式")
        void time2DateShouldReturnBlogDateFormat() {
            long timestamp = 1704067200000L;

            String result = DateUtil.time2date(timestamp);

            // 格式应为 yyyy年MM月dd日
            assertThat(result).matches("\\d{4}年\\d{2}月\\d{2}日");
        }

        @Test
        @DisplayName("time2utc 应返回 UTC 格式")
        void time2UtcShouldReturnUtcFormat() {
            long timestamp = 1704067200000L;

            String result = DateUtil.time2utc(timestamp);

            // 格式应为 yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
            assertThat(result).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
        }
    }

    @Nested
    @DisplayName("解析和格式化一致性测试")
    class RoundTripTest {

        @Test
        @DisplayName("格式化后再解析应保持一致")
        void shouldMaintainConsistencyAfterRoundTrip() {
            LocalDateTime original = LocalDateTime.of(2024, 8, 15, 12, 30, 45);

            String formatted = DateUtil.formatDateTime(original);
            LocalDateTime parsed = DateUtil.parseDateTime(formatted);

            assertThat(parsed).isEqualTo(original);
        }
    }
}
