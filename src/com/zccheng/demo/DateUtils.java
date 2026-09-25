package com.zccheng.demo;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 {@link LocalDate} 与 {@link LocalDateTime} 的日期时间工具类。
 *
 * <p>相比传统的 {@code Date} + {@code SimpleDateFormat}，这里全部使用 java.time 的不可变对象：
 * <ul>
 *   <li>对象不可变，天然线程安全，{@link DateTimeFormatter} 也可以放心共享；</li>
 *   <li>所有方法都是静态的纯函数，不修改传入的参数，返回的永远是新的实例；</li>
 *   <li>月份、星期等取值都是人可读的 1~12、MONDAY~SUNDAY，不需要再做偏移换算。</li>
 * </ul>
 *
 * <p>典型用法：
 * <pre>{@code
 * String text = DateUtils.format(LocalDateTime.now());
 * LocalDate date = DateUtils.parseDate("2026-09-25");
 * long days = DateUtils.daysBetween(date, DateUtils.today());
 * LocalDateTime end = DateUtils.endOfDay(date);
 * }</pre>
 *
 * <p>本类为工具类，不可实例化，所有方法入参均不允许为 {@code null}。
 */
public final class DateUtils {

    /** 默认日期格式：{@code yyyy-MM-dd}。 */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    /** 默认日期时间格式：{@code yyyy-MM-dd HH:mm:ss}。 */
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /** 紧凑的日期时间格式：{@code yyyyMMddHHmmss}，常用于生成流水号。 */
    public static final String COMPACT_DATETIME_PATTERN = "yyyyMMddHHmmss";

    /** 纯时间格式：{@code HH:mm:ss}。 */
    public static final String TIME_PATTERN = "HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern(TIME_PATTERN);

    /**
     * {@link DateTimeFormatter} 是不可变且线程安全的，这里按格式串缓存，
     * 避免每次格式化都重新解析 pattern 造成不必要的开销。
     */
    private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<String, DateTimeFormatter>();

    private DateUtils() {
    }

    // ------------------------------------------------------------------
    // 获取当前时间
    // ------------------------------------------------------------------

    /** 返回当前日期（系统默认时区）。 */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /** 返回当前日期时间（系统默认时区，精确到纳秒）。 */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /** 返回指定时区的当前日期时间。 */
    public static LocalDateTime now(ZoneId zone) {
        return LocalDateTime.now(Objects.requireNonNull(zone, "zone must not be null"));
    }

    // ------------------------------------------------------------------
    // 格式化
    // ------------------------------------------------------------------

    /**
     * 按默认格式 {@value #DEFAULT_DATE_PATTERN} 格式化日期，例如 {@code 2026-09-25}。
     */
    public static String format(LocalDate date) {
        return requireNonNull(date, "date").format(DATE_FORMATTER);
    }

    /**
     * 按指定格式格式化日期，例如 {@code format(date, "yyyy年MM月dd日")}。
     */
    public static String format(LocalDate date, String pattern) {
        return requireNonNull(date, "date").format(formatterOf(pattern));
    }

    /**
     * 按默认格式 {@value #DEFAULT_DATETIME_PATTERN} 格式化日期时间，例如 {@code 2026-09-25 13:20:30}。
     */
    public static String format(LocalDateTime dateTime) {
        return requireNonNull(dateTime, "dateTime").format(DATETIME_FORMATTER);
    }

    /**
     * 按指定格式格式化日期时间，例如 {@code format(dateTime, "yyyyMMdd")}。
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        return requireNonNull(dateTime, "dateTime").format(formatterOf(pattern));
    }

    /**
     * 按默认格式 {@value #COMPACT_DATETIME_PATTERN} 格式化日期时间，适合做时间戳后缀。
     */
    public static String formatCompact(LocalDateTime dateTime) {
        return format(dateTime, COMPACT_DATETIME_PATTERN);
    }

    /**
     * 只格式化时间部分，格式为 {@value #TIME_PATTERN}。
     */
    public static String formatTime(LocalDateTime dateTime) {
        return requireNonNull(dateTime, "dateTime").format(TIME_FORMATTER);
    }

    // ------------------------------------------------------------------
    // 解析
    // ------------------------------------------------------------------

    /**
     * 按默认格式 {@value #DEFAULT_DATE_PATTERN} 解析日期。
     *
     * @throws java.time.format.DateTimeParseException 字符串与格式不匹配时抛出
     */
    public static LocalDate parseDate(String text) {
        return parseDate(text, DEFAULT_DATE_PATTERN);
    }

    /**
     * 按指定格式解析日期，自动去掉首尾空格，例如 {@code parseDate("2026/09/25", "yyyy/MM/dd")}。
     *
     * @throws java.time.format.DateTimeParseException 字符串与格式不匹配时抛出
     */
    public static LocalDate parseDate(String text, String pattern) {
        return LocalDate.parse(trimAndRequire(text, "text"), formatterOf(pattern));
    }

    /**
     * 按默认格式 {@value #DEFAULT_DATETIME_PATTERN} 解析日期时间。
     *
     * @throws java.time.format.DateTimeParseException 字符串与格式不匹配时抛出
     */
    public static LocalDateTime parseDateTime(String text) {
        return parseDateTime(text, DEFAULT_DATETIME_PATTERN);
    }

    /**
     * 按指定格式解析日期时间，自动去掉首尾空格。
     *
     * @throws java.time.format.DateTimeParseException 字符串与格式不匹配时抛出
     */
    public static LocalDateTime parseDateTime(String text, String pattern) {
        return LocalDateTime.parse(trimAndRequire(text, "text"), formatterOf(pattern));
    }

    /**
     * 把日期时间按默认格式解析成 {@link LocalDate}，只保留日期部分。
     */
    public static LocalDate parseDateTimeToDate(String text) {
        return parseDateTime(text).toLocalDate();
    }

    // ------------------------------------------------------------------
    // 时间戳互转（基于系统默认时区）
    // ------------------------------------------------------------------

    /** 日期时间转毫秒时间戳。 */
    public static long toEpochMilli(LocalDateTime dateTime) {
        return requireNonNull(dateTime, "dateTime").atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /** 日期时间转秒级时间戳。 */
    public static long toEpochSecond(LocalDateTime dateTime) {
        return requireNonNull(dateTime, "dateTime").atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /** 毫秒时间戳转日期时间。 */
    public static LocalDateTime ofEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    /** 秒级时间戳转日期时间。 */
    public static LocalDateTime ofEpochSecond(long epochSecond) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZoneId.systemDefault());
    }

    // ------------------------------------------------------------------
    // 一天的起止时刻
    // ------------------------------------------------------------------

    /** 取某天的起始时刻，即 {@code 00:00:00.000000000}。 */
    public static LocalDateTime startOfDay(LocalDate date) {
        return requireNonNull(date, "date").atStartOfDay();
    }

    /**
     * 取某天的结束时刻，即 {@code 23:59:59.999999999}。
     *
     * <p>用于「日期 &gt;= startOfDay 且 日期 &lt;= endOfDay」这类闭区间查询，
     * 注意它比 {@code 23:59:59.999} 更靠后，写入数据库时可能需要按精度截断。
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return requireNonNull(date, "date").atTime(LocalTime.MAX);
    }

    /**
     * 取某天的结束时刻，精确到毫秒，即 {@code 23:59:59.999}，适合毫秒精度的存储。
     */
    public static LocalDateTime endOfDayOfMilli(LocalDate date) {
        return requireNonNull(date, "date").atTime(23, 59, 59, 999_000_000);
    }

    /** 取某天的最后一分钟，即 {@code 23:59}，常用于按月/日做统计分桶。 */
    public static LocalDateTime endOfDayOfMinute(LocalDate date) {
        return requireNonNull(date, "date").atTime(23, 59);
    }

    // ------------------------------------------------------------------
    // 月、周、年的边界
    // ------------------------------------------------------------------

    /** 取某月第一天的起始时刻。 */
    public static LocalDateTime startOfMonth(LocalDate date) {
        return requireNonNull(date, "date").withDayOfMonth(1).atStartOfDay();
    }

    /** 取某月最后一天的结束时刻。 */
    public static LocalDateTime endOfMonth(LocalDate date) {
        return endOfDay(requireNonNull(date, "date").with(TemporalAdjusters.lastDayOfMonth()));
    }

    /** 取所在周的周一（以周一为一周的第一天）。 */
    public static LocalDate firstDayOfWeek(LocalDate date) {
        return requireNonNull(date, "date").with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** 取所在周的周日（以周日为一周的结束日）。 */
    public static LocalDate lastDayOfWeek(LocalDate date) {
        return requireNonNull(date, "date").with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    /** 取所在季度的第一天。 */
    public static LocalDate firstDayOfQuarter(LocalDate date) {
        return requireNonNull(date, "date")
                .with(date.getMonth().firstMonthOfQuarter())
                .with(TemporalAdjusters.firstDayOfMonth());
    }

    /** 取所在年的第一天。 */
    public static LocalDate firstDayOfYear(LocalDate date) {
        return requireNonNull(date, "date").with(TemporalAdjusters.firstDayOfYear());
    }

    /** 取所在年的最后一天。 */
    public static LocalDate lastDayOfYear(LocalDate date) {
        return requireNonNull(date, "date").with(TemporalAdjusters.lastDayOfYear());
    }

    // ------------------------------------------------------------------
    // 比较与差值
    // ------------------------------------------------------------------

    /** 判断两个日期是否为同一天，两者均为 null 时也视为相同。 */
    public static boolean isSameDay(LocalDate first, LocalDate second) {
        return Objects.equals(first, second);
    }

    /** 判断两个日期时间是否为同一天。 */
    public static boolean isSameDay(LocalDateTime first, LocalDateTime second) {
        if (first == null || second == null) {
            return first == second;
        }
        return first.toLocalDate().isEqual(second.toLocalDate());
    }

    /** 判断 {@code dateTime} 是否落在 {@code [start, end]} 闭区间内。 */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /** 判断目标日期是否在起始日期之后、结束日期之前（含首尾）。 */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
    }

    /**
     * 计算两个日期相差的天数，{@code end - start}。
     *
     * <p>结果为负表示 {@code end} 早于 {@code start}。
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(requireNonNull(start, "start"), requireNonNull(end, "end"));
    }

    /** 计算两个日期相差的月数，{@code end - start}。 */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.MONTHS.between(requireNonNull(start, "start"), requireNonNull(end, "end"));
    }

    /** 计算两个日期相差的年数，{@code end - start}。 */
    public static long yearsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.YEARS.between(requireNonNull(start, "start"), requireNonNull(end, "end"));
    }

    /** 计算两个日期时间的间隔时长。 */
    public static Duration durationBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(requireNonNull(start, "start"), requireNonNull(end, "end"));
    }

    /** 计算两个日期时间相差的毫秒数，{@code end - start}。 */
    public static long millisBetween(LocalDateTime start, LocalDateTime end) {
        return durationBetween(start, end).toMillis();
    }

    /**
     * 按周岁计算年龄，规则为「生日已过则加一岁」。
     */
    public static int age(LocalDate birthday) {
        return age(birthday, today());
    }

    /**
     * 按指定基准日期计算周岁年龄。
     */
    public static int age(LocalDate birthday, LocalDate referenceDate) {
        return Period.between(requireNonNull(birthday, "birthday"), requireNonNull(referenceDate, "referenceDate"))
                .getYears();
    }

    // ------------------------------------------------------------------
    // 其它小工具
    // ------------------------------------------------------------------

    /** 判断某个日期所在年份是否为闰年。 */
    public static boolean isLeapYear(LocalDate date) {
        return requireNonNull(date, "date").isLeapYear();
    }

    /** 判断指定年份是否为闰年。 */
    public static boolean isLeapYear(int year) {
        return java.time.Year.isLeap(year);
    }

    /** 取某个月的天数，例如 2 月在闰年是 29 天。 */
    public static int lengthOfMonth(LocalDate date) {
        return requireNonNull(date, "date").lengthOfMonth();
    }

    /**
     * 判断两个时间段是否有交集（闭区间，首尾相接也算相交）。
     */
    public static boolean isOverlap(LocalDateTime startOne, LocalDateTime endOne,
                                    LocalDateTime startTwo, LocalDateTime endTwo) {
        Objects.requireNonNull(startOne, "startOne must not be null");
        Objects.requireNonNull(endOne, "endOne must not be null");
        Objects.requireNonNull(startTwo, "startTwo must not be null");
        Objects.requireNonNull(endTwo, "endTwo must not be null");
        if (startOne.isAfter(endOne) || startTwo.isAfter(endTwo)) {
            throw new IllegalArgumentException("时间段的起始时刻不能晚于结束时刻");
        }
        return !startOne.isAfter(endTwo) && !endOne.isBefore(startTwo);
    }

    /**
     * 把秒数格式化成 {@code HH:mm:ss}，超过 24 小时的部分会继续累加小时。
     */
    public static String formatSeconds(long seconds) {
        long hours = seconds / 3600;
        long minutes = seconds % 3600 / 60;
        long remainSeconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, remainSeconds);
    }

    /** 把秒和纳秒清零，只保留到分钟，例如 {@code 2026-09-25T13:20:30.123} 变为 {@code 2026-09-25T13:20}。 */
    public static LocalDateTime truncateToMinute(LocalDateTime dateTime) {
        return requireNonNull(dateTime, "dateTime").withSecond(0).withNano(0);
    }

    /**
     * 返回 {@code dateTime} 加上指定分钟数后，再截断到分钟的结果。
     *
     * <p>典型场景：按 {@code 30} 分钟做时间分桶。
     */
    public static LocalDateTime plusMinutesAndTruncate(LocalDateTime dateTime, long minutesToAdd) {
        return truncateToMinute(requireNonNull(dateTime, "dateTime").plusMinutes(minutesToAdd));
    }

    // ------------------------------------------------------------------
    // 内部辅助方法
    // ------------------------------------------------------------------

    /** 按格式串取（并缓存）{@link DateTimeFormatter}。 */
    private static DateTimeFormatter formatterOf(String pattern) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        DateTimeFormatter cached = FORMATTER_CACHE.get(pattern);
        if (cached != null) {
            return cached;
        }
        DateTimeFormatter created = DateTimeFormatter.ofPattern(pattern);
        DateTimeFormatter previous = FORMATTER_CACHE.putIfAbsent(pattern, created);
        return previous == null ? created : previous;
    }

    /** 校验非空并原样返回，便于在表达式中直接使用。 */
    private static <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new NullPointerException(name + " must not be null");
        }
        return value;
    }

    /** 校验字符串非空并去掉首尾空格。 */
    private static String trimAndRequire(String text, String name) {
        if (text == null) {
            throw new NullPointerException(name + " must not be null");
        }
        return text.trim();
    }
}
