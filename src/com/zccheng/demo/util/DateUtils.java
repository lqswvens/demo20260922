package com.zccheng.demo.util;

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
     *
     * <p>注意：缓存没有容量上限。本项目里 pattern 都是代码内的常量，量级有限没有问题；
     * 如果 pattern 来自外部输入，就要警惕被恶意构造的大量不同 pattern 撑爆内存，
     * 那时应换成有界缓存（如 LRU）。
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
        return LocalDateTime.now(requireNonNull(zone, "zone"));
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
        return LocalDate.parse(requireNonNull(text, "text").trim(), formatterOf(pattern));
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
        return LocalDateTime.parse(requireNonNull(text, "text").trim(), formatterOf(pattern));
    }

    /**
     * 把日期时间按默认格式解析成 {@link LocalDate}，只保留日期部分。
     */
    public static LocalDate parseDateTimeToDate(String text) {
        return parseDateTime(text).toLocalDate();
    }

    // ------------------------------------------------------------------
    // 时间戳互转（基于系统默认时区）
    //
    // 注意：时间戳本身是世界统一的「时刻」，但时刻要落到某个时区才有年月日，
    // 本节全部使用系统默认时区，同一台机器上来回转换是可逆的；
    // 跨时区部署或多时区数据请显式传 ZoneId（如 dateTime.atZone(zone).toInstant()），
    // 不要依赖默认时区——换了台机器结果就可能不同。
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
        requireNonNull(dateTime, "dateTime");
        requireNonNull(start, "start");
        requireNonNull(end, "end");
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /** 判断目标日期是否在起始日期之后、结束日期之前（含首尾）。 */
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        requireNonNull(date, "date");
        requireNonNull(start, "start");
        requireNonNull(end, "end");
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
     * 按指定基准日期计算周岁年龄，规则为「生日已过则加一岁」，由 {@link Period} 自动处理。
     *
     * @param birthday      生日
     * @param referenceDate 基准日期，通常是当天
     * @return 周岁年龄
     * @throws IllegalArgumentException 生日晚于基准日期时抛出：
     *         未来人的年龄是负数，只会把错误悄悄带进下游统计，不如在调用现场暴露
     */
    public static int age(LocalDate birthday, LocalDate referenceDate) {
        Period period = Period.between(requireNonNull(birthday, "birthday"),
                requireNonNull(referenceDate, "referenceDate"));
        if (period.isNegative()) {
            throw new IllegalArgumentException(
                    "生日 " + birthday + " 不能晚于基准日期 " + referenceDate);
        }
        return period.getYears();
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
        requireNonNull(startOne, "startOne");
        requireNonNull(endOne, "endOne");
        requireNonNull(startTwo, "startTwo");
        requireNonNull(endTwo, "endTwo");
        if (startOne.isAfter(endOne) || startTwo.isAfter(endTwo)) {
            throw new IllegalArgumentException("时间段的起始时刻不能晚于结束时刻");
        }
        return !startOne.isAfter(endTwo) && !endOne.isBefore(startTwo);
    }

    /**
     * 把秒数格式化成 {@code HH:mm:ss}，超过 24 小时的部分会继续累加小时。
     *
     * <p>不接受负数：负数的整除与取余都向零取整，{@code -10} 会拼出
     * {@code 00:00:-10} 这种畸形结果，直接拒绝比悄悄出错好。
     *
     * @param seconds 秒数，不能为负
     * @return {@code HH:mm:ss} 形式的时长文本
     * @throws IllegalArgumentException seconds 为负数时抛出
     */
    public static String formatSeconds(long seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("秒数不能为负数，实际传入：" + seconds);
        }
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

    /**
     * 按格式串取（并缓存）{@link DateTimeFormatter}。
     *
     * <p>用 {@code computeIfAbsent} 而不是 get + putIfAbsent 的组合：
     * 一次原子调用完成「查缓存，缺失时创建并放入」，并发竞争时不会白白创建
     * 一个随后被丢弃的 formatter，代码也更短。pattern 非法时 {@code ofPattern}
     * 抛出的 {@link IllegalArgumentException} 会让条目不进缓存，异常原样向上传播，
     * 行为正好正确。
     */
    private static DateTimeFormatter formatterOf(String pattern) {
        requireNonNull(pattern, "pattern");
        if (pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("参数 pattern 不能为空白串");
        }
        return FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }

    /** 校验非空并原样返回，便于在表达式中直接使用。 */
    private static <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new NullPointerException("参数 " + name + " 不能为 null");
        }
        return value;
    }

    /** 断言两个对象相等（含 null），失败时抛出带「期望 / 实际」的 {@link AssertionError}。 */
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " 失败：期望 [" + expected + "]，实际 [" + actual + "]");
        }
    }

    /** 断言两个整数相等，失败时抛出带「期望 / 实际」的 {@link AssertionError}。 */
    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " 失败：期望 [" + expected + "]，实际 [" + actual + "]");
        }
    }

    /** 断言条件成立，失败时抛出 {@link AssertionError}。 */
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " 失败");
        }
    }

    /**
     * 断言执行体抛出指定类型的运行时异常。
     *
     * <p>不引入测试框架时的轻量替代：抛对了直接通过，抛错类型或没抛都给出中文说明。
     */
    private static void assertThrows(Class<? extends Exception> expectedType, Runnable action, String message) {
        try {
            action.run();
        } catch (Exception e) {
            if (expectedType.isInstance(e)) {
                return;
            }
            throw new AssertionError(message + " 失败：期望抛出 " + expectedType.getSimpleName()
                    + "，实际抛出 " + e.getClass().getName(), e);
        }
        throw new AssertionError(message + " 失败：期望抛出 " + expectedType.getSimpleName() + "，但没有抛出任何异常");
    }

    // ------------------------------------------------------------------
    // main 自测入口
    // ------------------------------------------------------------------

    /**
     * 自测入口：断言用例均使用固定日期（2026-09-25 是星期五、2026 年 9 月共 30 天），
     * 不依赖「今天」，任何时间运行结果都一致、问题可复现。
     *
     * @param args 命令行参数，未使用
     */
    public static void main(String[] args) {
        LocalDate date = LocalDate.of(2026, 9, 25);
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 25, 13, 20, 30);

        // ------------------ 示例演示 ------------------
        System.out.println("format(date) = " + format(date));
        System.out.println("format(dateTime) = " + format(dateTime));
        System.out.println("formatCompact(dateTime) = " + formatCompact(dateTime));
        System.out.println("formatTime(dateTime) = " + formatTime(dateTime));
        System.out.println("format(dateTime, \"yyyy年MM月dd日\") = " + format(dateTime, "yyyy年MM月dd日"));
        System.out.println("formatSeconds(3725) = " + formatSeconds(3725));
        System.out.println("age(1990-06-15) = " + age(LocalDate.of(1990, 6, 15)));

        // ------------------ 断言自测 ------------------
        // 格式化与解析
        assertEquals("2026-09-25", format(date), "默认格式化日期");
        assertEquals("2026-09-25 13:20:30", format(dateTime), "默认格式化日期时间");
        assertEquals("20260925132030", formatCompact(dateTime), "紧凑格式化");
        assertEquals("13:20:30", formatTime(dateTime), "只格式化时间部分");
        assertEquals("2026年09月25日", format(date, "yyyy年MM月dd日"), "按指定格式格式化");
        assertEquals(date, parseDate("2026-09-25"), "按默认格式解析日期");
        assertEquals(date, parseDate(" 2026/09/25 ", "yyyy/MM/dd"), "按指定格式解析并自动去掉首尾空格");
        assertEquals(dateTime, parseDateTime("2026-09-25 13:20:30"), "按默认格式解析日期时间");
        assertEquals(date, parseDateTimeToDate("2026-09-25 13:20:30"), "解析日期时间并取日期部分");
        // 先格式化再解析，应还原出同一个日期（往返一致性）
        assertEquals(date, parseDate(format(date)), "日期格式化解析往返一致");

        // 时间戳互转：只断言「转过去再转回来」结果不变，这样不依赖运行机器的时区设置
        assertEquals(dateTime, ofEpochMilli(toEpochMilli(dateTime)), "毫秒时间戳往返");
        assertEquals(dateTime, ofEpochSecond(toEpochSecond(dateTime)), "秒级时间戳往返");

        // 一天的起止时刻
        assertEquals(LocalDateTime.of(2026, 9, 25, 0, 0), startOfDay(date), "一天起点");
        assertEquals(LocalDateTime.of(2026, 9, 25, 23, 59, 59, 999_999_999), endOfDay(date), "一天终点（纳秒精度）");
        assertEquals(LocalDateTime.of(2026, 9, 25, 23, 59, 59, 999_000_000), endOfDayOfMilli(date), "一天终点（毫秒精度）");
        assertEquals(LocalDateTime.of(2026, 9, 25, 23, 59), endOfDayOfMinute(date), "一天终点（分钟精度）");

        // 月、周、年的边界
        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0), startOfMonth(date), "月初起点");
        assertEquals(LocalDateTime.of(2026, 9, 30, 23, 59, 59, 999_999_999), endOfMonth(date), "月末终点");
        assertEquals(LocalDate.of(2026, 9, 21), firstDayOfWeek(date), "所在周周一（2026-09-25 是周五）");
        assertEquals(LocalDate.of(2026, 9, 27), lastDayOfWeek(date), "所在周周日");
        assertEquals(LocalDate.of(2026, 7, 1), firstDayOfQuarter(date), "所在季度首日");
        assertEquals(LocalDate.of(2026, 1, 1), firstDayOfYear(date), "所在年首日");
        assertEquals(LocalDate.of(2026, 12, 31), lastDayOfYear(date), "所在年末日");

        // 比较与差值
        assertTrue(isSameDay(date, LocalDate.of(2026, 9, 25)), "同一天判断");
        assertTrue(!isSameDay(dateTime, dateTime.plusDays(1)), "不同天判断");
        assertTrue(isBetween(dateTime, startOfDay(date), endOfDayOfMinute(date)), "闭区间之内");
        assertTrue(!isBetween(startOfDay(date).minusNanos(1), startOfDay(date), endOfDay(date)), "闭区间之外");
        assertTrue(isOverlap(dateTime, dateTime.plusHours(2), dateTime.plusHours(1), dateTime.plusHours(3)), "时间段相交");
        assertTrue(isOverlap(dateTime, dateTime.plusHours(1), dateTime.plusHours(1), dateTime.plusHours(2)), "首尾相接也算相交");
        assertTrue(!isOverlap(dateTime, dateTime.plusHours(1), dateTime.plusHours(2), dateTime.plusHours(3)), "时间段分离");
        assertEquals(24L, daysBetween(LocalDate.of(2026, 9, 1), date), "相差天数");
        assertEquals(3L, monthsBetween(LocalDate.of(2026, 6, 25), date), "相差月数");
        assertEquals(6L, yearsBetween(LocalDate.of(2020, 9, 25), date), "相差年数");
        assertEquals(3_600_000L, millisBetween(dateTime, dateTime.plusHours(1)), "相差毫秒数");
        assertEquals(36, age(LocalDate.of(1990, 6, 15), date), "周岁年龄");
        assertThrows(IllegalArgumentException.class, () -> age(LocalDate.of(2027, 1, 1), date), "未来生日应拒绝");

        // 其它小工具
        assertTrue(isLeapYear(2024), "2024 是闰年");
        assertTrue(!isLeapYear(2026), "2026 不是闰年");
        assertTrue(isLeapYear(date.withYear(2024)), "isLeapYear(LocalDate) 重载");
        assertEquals(29, lengthOfMonth(LocalDate.of(2024, 2, 1)), "闰年 2 月有 29 天");
        assertEquals("01:01:01", formatSeconds(3661), "秒数格式化");
        assertEquals("25:00:00", formatSeconds(90_000), "超过 24 小时继续累加小时");
        assertThrows(IllegalArgumentException.class, () -> formatSeconds(-1), "负数秒应拒绝");
        assertEquals(LocalDateTime.of(2026, 9, 25, 13, 20), truncateToMinute(dateTime), "截断到分钟");
        assertEquals(LocalDateTime.of(2026, 9, 25, 13, 50), plusMinutesAndTruncate(dateTime, 30), "加 30 分钟再截断");

        // 参数校验
        assertThrows(NullPointerException.class, () -> format(date, null), "格式串为 null 应拒绝");
        assertThrows(IllegalArgumentException.class, () -> format(date, "  "), "空白格式串应拒绝");
        assertThrows(java.time.format.DateTimeParseException.class, () -> parseDate("不是日期"), "非法日期串应抛出解析异常");

        System.out.println("DateUtils 自测通过");
    }
}
