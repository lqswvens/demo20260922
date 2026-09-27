package com.zccheng.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 字符串工具类：空值判断、归一化、截断填充、驼峰转换等日常高频操作。
 *
 * <p>设计取舍：
 * <ul>
 *   <li>final + 私有构造 + 全静态方法，纯函数式工具类，不持有任何状态；</li>
 *   <li>只有明确用来处理 null 的判断方法（{@link #isEmpty} / {@link #isBlank} 系列）
 *       允许传 null，其余方法一律抛出带中文说明的 {@link NullPointerException}，
 *       让问题在调用现场就暴露，而不是带着 null 流到下游；</li>
 *   <li>{@link String} 本身不可变，所有方法返回的都是新串，天然线程安全；</li>
 *   <li>不引入 Commons Lang 等第三方库，手写实现的同时讲清楚每个方法容易踩的坑。</li>
 * </ul>
 *
 * <p>典型用法：
 * <pre>{@code
 * if (StringUtils.isBlank(input)) {
 *     input = StringUtils.defaultIfBlank(input, "匿名");
 * }
 * String column = StringUtils.camelToUnderscore("userName");   // user_name
 * String serial = StringUtils.padLeft("7", 4, '0');            // 0007
 * }</pre>
 *
 * <p>本类为工具类，不可实例化。
 */
public final class StringUtils {

    /** 省略号字符：单个 char 即可表达「截断」，比三个点更省空间，中文排版也更自然。 */
    private static final char ELLIPSIS = '…';

    private StringUtils() {
    }

    // ------------------------------------------------------------------
    // 空值判断
    // ------------------------------------------------------------------

    /**
     * 判断字符串是否为 {@code null} 或长度为 0。
     *
     * <p>注意 {@code " "}（一个空格）不算空串，需要把空白也算空时用 {@link #isBlank}。
     *
     * @param text 待判断的字符串，允许为 null
     * @return null 或空串时返回 true
     */
    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    /** 判断字符串是否非空（非 null 且长度大于 0）。 */
    public static boolean isNotEmpty(String text) {
        return !isEmpty(text);
    }

    /**
     * 判断字符串是否为 null、空串或纯空白串。
     *
     * <p>Java 8 的 {@link String} 没有 {@code isBlank()} 方法（Java 11 才加入，
     * 本项目锁定 Java 8 不能用），所以这里手动逐字符判断。
     * 空白字符用 {@link Character#isWhitespace(char)} 识别，涵盖空格、Tab、换行等。
     *
     * @param text 待判断的字符串，允许为 null
     * @return null、空串或全是空白时返回 true
     */
    public static boolean isBlank(String text) {
        if (text == null) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** 判断字符串是否「有实际内容」（非 null 且至少含一个非空白字符）。 */
    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    /**
     * 判断字符串是否全部由数字字符组成。
     *
     * <p>注意一个坑：{@link Character#isDigit(char)} 认的不只是半角数字 {@code 0~9}，
     * 全角数字 {@code １２３}、阿拉伯语数字 {@code ٣} 也会返回 true。
     * 如果只接受半角数字（例如校验手机号），应改用范围比较
     * {@code c >= '0' && c <= '9'}。
     *
     * @param text 待判断的字符串，允许为 null
     * @return 全部是数字字符且非空时返回 true
     */
    public static boolean isNumeric(String text) {
        if (isEmpty(text)) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // 归一化：null 与空白串的统一处理
    // ------------------------------------------------------------------

    /**
     * 去掉首尾空白；结果为空串时归一为 {@code null}。
     *
     * <p>适合「可选项」场景：数据库与业务判断里 {@code null} 比 {@code ""} 更能表达「没填」。
     */
    public static String trimToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 去掉首尾空白；{@code null} 归一为空串。
     *
     * <p>适合「拼串、打印」场景：下游不需要再判 null，直接用即可。
     */
    public static String trimToEmpty(String text) {
        return text == null ? "" : text.trim();
    }

    /**
     * 字符串为空白（含 null）时返回默认值，否则原样返回。
     *
     * @param text        待处理的字符串，允许为 null
     * @param defaultText 默认值，也允许为 null
     * @return text 有实际内容时返回原值，否则返回 defaultText
     */
    public static String defaultIfBlank(String text, String defaultText) {
        return isBlank(text) ? defaultText : text;
    }

    // ------------------------------------------------------------------
    // 大小写
    // ------------------------------------------------------------------

    /** 把首字母转成大写，其余部分保持原样，例如 {@code hello -> Hello}。 */
    public static String capitalize(String text) {
        String s = requireNonNull(text, "text");
        if (s.isEmpty()) {
            return s;
        }
        // 只动第一个 char，其余原样拼回，避免整串 toLowerCase 破坏原有内容
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** 把首字母转成小写，其余部分保持原样，例如 {@code Hello -> hello}。 */
    public static String uncapitalize(String text) {
        String s = requireNonNull(text, "text");
        if (s.isEmpty()) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    // ------------------------------------------------------------------
    // 截取与省略
    // ------------------------------------------------------------------

    /**
     * 把超长字符串截断到指定长度，结尾补省略号。
     *
     * <p>例如 {@code abbreviate("hello world", 8)} 得到 {@code hello w…}：
     * 保留 7 个字符 + 1 个省略号，总长度恰好等于 maxLength。
     *
     * @param text      原字符串
     * @param maxLength 允许的最大长度（含省略号），至少为 1
     * @return 未超长时原样返回；超长时返回「前 maxLength-1 个字符 + 省略号」
     * @throws IllegalArgumentException maxLength 小于 1 时抛出
     */
    public static String abbreviate(String text, int maxLength) {
        String s = requireNonNull(text, "text");
        if (maxLength < 1) {
            throw new IllegalArgumentException("最大长度至少为 1，实际传入：" + maxLength);
        }
        if (s.length() <= maxLength) {
            return s;
        }
        // 预留最后 1 个 char 放省略号，保证结果长度不超过 maxLength
        return s.substring(0, maxLength - 1) + ELLIPSIS;
    }

    /**
     * 取第一个分隔符之前的部分，例如 {@code substringBefore("a=b=c", "=")} 得到 {@code a}。
     *
     * <p>约定：找不到分隔符时返回原字符串（而不是空串），
     * 这样「没有分隔符 = 整体就是内容」的语义更自然。
     */
    public static String substringBefore(String text, String separator) {
        String s = requireNonNull(text, "text");
        Objects.requireNonNull(separator, "参数 separator 不能为 null");
        if (separator.isEmpty()) {
            return "";
        }
        int index = s.indexOf(separator);
        return index < 0 ? s : s.substring(0, index);
    }

    /**
     * 取第一个分隔符之后的部分，例如 {@code substringAfter("a=b=c", "=")} 得到 {@code b=c}。
     *
     * <p>约定：找不到分隔符时返回空串——与 {@link #substringBefore} 相反，
     * 「没有分隔符」意味着「分隔符后面什么都没有」。
     */
    public static String substringAfter(String text, String separator) {
        String s = requireNonNull(text, "text");
        Objects.requireNonNull(separator, "参数 separator 不能为 null");
        if (separator.isEmpty()) {
            return s;
        }
        int index = s.indexOf(separator);
        return index < 0 ? "" : s.substring(index + separator.length());
    }

    // ------------------------------------------------------------------
    // 填充与重复
    // ------------------------------------------------------------------

    /**
     * 在左侧补指定字符直到总长度达到 {@code length}，常见用途是序号补零。
     *
     * <p>例如 {@code padLeft("7", 3, '0')} 得到 {@code 007}。
     * 已达长度或超长的字符串原样返回，不做截断。
     *
     * @param text    原字符串
     * @param length  目标总长度
     * @param padChar 用于填充的字符，补零就是 {@code '0'}
     * @return 长度为 {@code max(length, text.length())} 的结果
     * @throws IllegalArgumentException length 为负数时抛出
     */
    public static String padLeft(String text, int length, char padChar) {
        String s = requireNonNull(text, "text");
        if (length < 0) {
            throw new IllegalArgumentException("目标长度不能为负数，实际传入：" + length);
        }
        if (length <= s.length()) {
            return s;
        }
        // 用 StringBuilder 预分配容量，一次性算出填充串，比循环拼接 String 快得多
        StringBuilder builder = new StringBuilder(length);
        for (int i = s.length(); i < length; i++) {
            builder.append(padChar);
        }
        return builder.append(s).toString();
    }

    /**
     * 重复一个字符指定次数，例如 {@code repeat('-', 3)} 得到 {@code ---}。
     *
     * <p>Java 8 没有 {@code String#repeat()}（Java 11 才加入），所以自己实现。
     *
     * @param c     要重复的字符
     * @param count 重复次数，0 表示返回空串
     * @return 重复 count 次的结果
     * @throws IllegalArgumentException count 为负数时抛出
     */
    public static String repeat(char c, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("重复次数不能为负数，实际传入：" + count);
        }
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    // ------------------------------------------------------------------
    // 驼峰与下划线互转
    // ------------------------------------------------------------------

    /**
     * 把驼峰命名转成下划线命名，例如 {@code userName -> user_name}。
     *
     * <p>规则很简单：遇到大写字母，在它前面插入下划线并转小写。
     * 注意局限：连续大写会被逐个拆开，{@code userID} 得到 {@code user_i_d}，
     * 如果需要 {@code user_id} 的效果，就要维护「上一个字符是否也是大写」的更复杂规则，
     * 这里取简单版，够覆盖大多数字段名转列名的场景。
     *
     * @param text 驼峰命名的字符串
     * @return 下划线命名（小写）的结果
     */
    public static String camelToUnderscore(String text) {
        String s = requireNonNull(text, "text");
        StringBuilder builder = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                // 首字母前面不需要下划线，例如 UserName -> user_name
                if (i > 0) {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /**
     * 把下划线命名转成驼峰命名，例如 {@code user_name -> userName}。
     *
     * <p>这里刻意不用 {@code String#split("_")}：split 按正则处理，
     * 而且对「下划线开头 / 结尾 / 连续出现」会产生空串元素，还得再过滤；
     * 直接一个循环扫过去，遇到下划线就把「下一个字符转大写」的标记立起来，更直观也更稳。
     *
     * @param text 下划线命名的字符串（小写）
     * @return 驼峰命名（首字母小写）的结果
     */
    public static String underscoreToCamel(String text) {
        String s = requireNonNull(text, "text");
        StringBuilder builder = new StringBuilder(s.length());
        boolean upperNext = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '_') {
                // 丢弃下划线本身，只标记「后面是新单词」
                upperNext = true;
            } else if (upperNext && builder.length() > 0) {
                builder.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                // 其余字符统一转小写，兼容传入 USER_NAME 这类大写下划线串
                builder.append(Character.toLowerCase(c));
                upperNext = false;
            }
        }
        return builder.toString();
    }

    // ------------------------------------------------------------------
    // 拆分与连接
    // ------------------------------------------------------------------

    /**
     * 按普通字符串（不是正则）拆分，保留空段。
     *
     * <p>刻意不用 {@code String#split}：它的参数是正则，
     * 拆 {@code "."}、{@code "|"} 这类字符必须先转义，忘了就会拆出错误结果或抛异常；
     * 这里按字面量查找，所见即所得。空段（连续分隔符）会保留，
     * 是否过滤由调用方决定。
     *
     * @param text      原字符串
     * @param separator 分隔符，不能是空串
     * @return 拆分结果，元素顺序与原文一致；至少包含一个元素
     * @throws IllegalArgumentException separator 是空串时抛出
     */
    public static List<String> splitToList(String text, String separator) {
        String s = requireNonNull(text, "text");
        Objects.requireNonNull(separator, "参数 separator 不能为 null");
        if (separator.isEmpty()) {
            throw new IllegalArgumentException("分隔符不能为空字符串");
        }
        List<String> parts = new ArrayList<String>();
        int start = 0;
        int index;
        while ((index = s.indexOf(separator, start)) >= 0) {
            parts.add(s.substring(start, index));
            start = index + separator.length();
        }
        // 收尾：最后一段（可能是空串）也要放进去，与 split 的语义保持一致
        parts.add(s.substring(start));
        return parts;
    }

    /**
     * 用分隔符连接元素，{@code null} 元素按空串处理。
     *
     * <p>{@code String#join} 遇到 null 元素会直接抛 {@link NullPointerException}，
     * 这里改成当空串拼进去，容错更好；如果希望 null 引发报错，请直接用 {@code String#join}。
     *
     * @param items     待连接的元素集合，允许包含 null 元素
     * @param separator 分隔符
     * @return 连接结果；空集合返回空串
     */
    public static String join(Iterable<?> items, String separator) {
        Objects.requireNonNull(items, "参数 items 不能为 null");
        Objects.requireNonNull(separator, "参数 separator 不能为 null");
        StringBuilder builder = new StringBuilder();
        for (Object item : items) {
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(item == null ? "" : item.toString());
        }
        return builder.toString();
    }

    // ------------------------------------------------------------------
    // 其它小工具
    // ------------------------------------------------------------------

    /**
     * 统计子串出现的次数，按「不重叠」计数。
     *
     * <p>例如 {@code countMatches("banana", "ana")} 得到 1：找到第一个 ana 之后，
     * 下一次查找从它末尾开始，第二个 ana 与它重叠，不再计入。
     * 子串为空串时约定返回 0——空串在无穷多个位置「出现」，按 0 处理避免死循环。
     */
    public static int countMatches(String text, String sub) {
        String s = requireNonNull(text, "text");
        Objects.requireNonNull(sub, "参数 sub 不能为 null");
        if (sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = s.indexOf(sub, index)) >= 0) {
            count++;
            index += sub.length();
        }
        return count;
    }

    /**
     * 反转字符串。
     *
     * <p>这里是按「码点」而不是按 char 反转：emoji 这类增补字符在 Java 里占两个 char
     * （一对代理项），直接按下标交换 char 会把它拆成乱码。
     * 顺带一提，{@code new StringBuilder(s).reverse()} 内部其实对代理对做了保护，
     * 结果与本方法一致，手写一遍是为了看清楚 char 与码点的区别。
     */
    public static String reverse(String text) {
        String s = requireNonNull(text, "text");
        StringBuilder builder = new StringBuilder(s.length());
        // 从后往前逐个码点取：codePointBefore 返回上一个完整码点，
        // charCount 告诉我们它占了几个 char（1 或 2），倒着跳过去
        for (int i = s.length(); i > 0; ) {
            int codePoint = s.codePointBefore(i);
            builder.appendCodePoint(codePoint);
            i -= Character.charCount(codePoint);
        }
        return builder.toString();
    }

    // ------------------------------------------------------------------
    // 内部辅助方法
    // ------------------------------------------------------------------

    /** 校验字符串非 null 并原样返回，便于在表达式中直接使用。 */
    private static String requireNonNull(String value, String name) {
        if (value == null) {
            throw new NullPointerException("参数 " + name + " 不能为 null");
        }
        return value;
    }

    /** 断言两个字符串相等，失败时抛出带「期望 / 实际」的 {@link AssertionError}。 */
    private static void assertEquals(String expected, String actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " 失败：期望 [" + expected + "]，实际 [" + actual + "]");
        }
    }

    /** 断言两个整数相等，失败时抛出带「期望 / 实际」的 {@link AssertionError}。 */
    private static void assertEquals(int expected, int actual, String message) {
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

    // ------------------------------------------------------------------
    // main 自测入口：先打印示例建立直觉，再跑标准断言
    // ------------------------------------------------------------------

    /**
     * 自测入口：打印各方法的典型输出，并执行一组断言，
     * 全部通过后输出「通过」字样。
     *
     * @param args 命令行参数，未使用
     */
    public static void main(String[] args) {
        // ------------------ 示例演示 ------------------
        System.out.println("isEmpty(null) = " + isEmpty(null));
        System.out.println("isBlank(\"  \\t\") = " + isBlank("  \t"));
        System.out.println("trimToNull(\"  hello \") = " + trimToNull("  hello "));
        System.out.println("defaultIfBlank(\"  \", \"匿名\") = " + defaultIfBlank("  ", "匿名"));
        System.out.println("capitalize(\"hello\") = " + capitalize("hello"));
        System.out.println("camelToUnderscore(\"userName\") = " + camelToUnderscore("userName"));
        System.out.println("underscoreToCamel(\"user_name\") = " + underscoreToCamel("user_name"));
        System.out.println("padLeft(\"7\", 4, '0') = " + padLeft("7", 4, '0'));
        System.out.println("abbreviate(\"hello world\", 8) = " + abbreviate("hello world", 8));
        System.out.println("substringAfter(\"name=zccheng\", \"=\") = " + substringAfter("name=zccheng", "="));
        System.out.println("join([a, null, b], \",\") = " + join(Arrays.asList("a", null, "b"), ","));
        System.out.println("reverse(\"a\\uD83D\\uDE00\") = " + reverse("a\uD83D\uDE00"));

        // ------------------ 断言自测 ------------------
        assertTrue(isEmpty(null), "isEmpty(null) 应为 true");
        assertTrue(!isEmpty(" "), "isEmpty(\" \") 应为 false：空白串不算空串");
        assertTrue(isNotEmpty("x"), "isNotEmpty 对非空串应为 true");
        assertTrue(isBlank(" \t "), "isBlank 应识别纯空白串");
        assertTrue(!isBlank("x"), "isBlank 对有内容的串应为 false");

        assertTrue(isNumeric("0123"), "isNumeric 应识别纯数字");
        assertTrue(!isNumeric("12a3"), "isNumeric 应拒绝带字母的串");
        assertTrue(!isNumeric(null), "isNumeric 对 null 应为 false");

        assertEquals(null, trimToNull("   "), "trimToNull 应把纯空白串归一为 null");
        assertEquals("hello", trimToNull("  hello "), "trimToNull 应去掉首尾空白");
        assertEquals("", trimToEmpty(null), "trimToEmpty 应把 null 归一为空串");
        assertEquals("匿名", defaultIfBlank("  ", "匿名"), "defaultIfBlank 应在空白时给默认值");
        assertEquals("原文", defaultIfBlank("原文", "默认"), "defaultIfBlank 对有内容的串应原样返回");

        assertEquals("Hello", capitalize("hello"), "capitalize 应把首字母大写");
        assertEquals("hello", uncapitalize("Hello"), "uncapitalize 应把首字母小写");

        assertEquals("hello w…", abbreviate("hello world", 8), "abbreviate 应截断并补省略号");
        assertEquals("hello world", abbreviate("hello world", 20), "abbreviate 对未超长的串应原样返回");

        assertEquals("a", substringBefore("a=b=c", "="), "substringBefore 应取第一个分隔符之前的部分");
        assertEquals("b=c", substringAfter("a=b=c", "="), "substringAfter 应取第一个分隔符之后的部分");
        assertEquals("无分隔符", substringBefore("无分隔符", "="), "substringBefore 找不到分隔符时应返回原串");
        assertEquals("", substringAfter("无分隔符", "="), "substringAfter 找不到分隔符时应返回空串");

        assertEquals("007", padLeft("7", 3, '0'), "padLeft 应在左侧补齐到指定长度");
        assertEquals("7", padLeft("7", 1, '0'), "padLeft 对已达长度的串应原样返回");
        assertEquals("---", repeat('-', 3), "repeat 应重复字符指定次数");

        assertEquals("user_name", camelToUnderscore("userName"), "驼峰转下划线");
        assertEquals("user_name", camelToUnderscore("UserName"), "首字母大写的驼峰转下划线");
        assertEquals("userName", underscoreToCamel("user_name"), "下划线转驼峰");
        assertEquals("userName", underscoreToCamel("USER_NAME"), "大写下划线串也应转成小写驼峰");

        List<String> parts = splitToList("a,,b", ",");
        assertEquals("[a, , b]", parts.toString(), "splitToList 应按字面量拆分并保留空段");

        assertEquals("a,,b", join(Arrays.asList("a", null, "b"), ","), "join 应把 null 元素当空串");

        assertEquals(2, countMatches("banana", "an"), "countMatches 应按不重叠计数");
        // banana 中 ana 出现在位置 1 和位置 3，但两者共用下标 3，按不重叠规则只计入位置 1 的那次
        assertEquals(1, countMatches("banana", "ana"), "重叠出现不计入");

        assertEquals("cba", reverse("abc"), "普通串反转");
        assertEquals("\uD83D\uDE00a", reverse("a\uD83D\uDE00"), "反转不应拆坏 emoji 等增补字符");

        System.out.println("StringUtils 自测通过");
    }
}
