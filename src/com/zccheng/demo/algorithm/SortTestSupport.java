package com.zccheng.demo.algorithm;

import java.util.Arrays;
import java.util.Random;

/**
 * 排序算法的测试工具：生成测试数据、校验排序结果、打印用例报告。
 *
 * <p>每个算法类的 {@code main} 都直接调用这里的方法完成自测，主要包含三类能力：
 * <ul>
 *   <li>{@link #runAll(String, SortAlgorithm)}：跑一遍标准用例集合，覆盖空数组、单元素、已排序、
 *       逆序、大量重复值、负数、大数值等边界场景；</li>
 *   <li>{@link #printSample(String, int[], SortAlgorithm)}：打印一个具体例子排序前后的对比，方便肉眼观察；</li>
 *   <li>{@link #verify(String, String, int[], SortAlgorithm)}：断言排序结果等于期望结果，不一致直接抛
 *       {@link AssertionError}，让错误立刻暴露而不是「看着像对的」。</li>
 * </ul>
 *
 * <p>因为 {@link SortAlgorithm} 是函数式接口，这里也大量使用 Lambda 表达式传入「算法的某一种实现」，
 * 例如：{@code SortTestSupport.runAll("鸡尾酒排序", a -> new BubbleSort().sortBidirectional(a))}。
 *
 * <p>期望结果统一用 {@link Arrays#sort(int[])} 计算：JDK 自带的排序是经过充分验证的实现，
 * 把它当作参照物（参考实现）可以少写一份「正确答案」。
 */
public final class SortTestSupport {

    /** 打印数组时最多展示的元素个数，超出部分折叠显示，避免刷屏。 */
    private static final int MAX_PRINT_SIZE = 24;

    /** 标准用例中「随机数组」的值域下限。 */
    private static final int RANDOM_MIN = -1000;

    /** 标准用例中「随机数组」的值域上限。 */
    private static final int RANDOM_MAX = 1000;

    /** 工具类不允许实例化。 */
    private SortTestSupport() {
    }

    // ------------------------------------------------------------------
    // 用例执行与断言
    // ------------------------------------------------------------------

    /**
     * 跑一遍标准用例集合，全部通过后打印一行汇总；任何一条失败都会抛出 {@link AssertionError}。
     *
     * @param label     用例标签，一般写成「算法名（实现方式）」
     * @param algorithm 被测试的排序算法，可以是实现类实例，也可以是 Lambda 表达式
     */
    public static void runAll(String label, SortAlgorithm algorithm) {
        runCases(label, standardCases(), algorithm);
    }

    /**
     * 只跑「非负整数」用例集合：给计数排序基础版这种不接受负数的算法使用，
     * 其余检查逻辑与 {@link #runAll(String, SortAlgorithm)} 完全一致。
     *
     * @param label     用例标签
     * @param algorithm 被测试的排序算法
     */
    public static void runNonNegativeAll(String label, SortAlgorithm algorithm) {
        runCases(label, nonNegativeCases(), algorithm);
    }

    /** 执行给定用例集合，并在全部通过后打印汇总。 */
    private static void runCases(String label, Case[] cases, SortAlgorithm algorithm) {
        for (Case item : cases) {
            verify(label, item.name, item.input, algorithm);
        }
        System.out.println("  [通过] " + label + "：" + cases.length + " 个用例全部通过");
    }

    /**
     * 随机压测：反复用「随机长度 + 随机值域」的数组验证算法，比固定用例更容易发现边界问题。
     *
     * <p>默认同时跑两种值域，正好覆盖两类常见事故：
     * <ul>
     *   <li>小值域（{@code -100 ~ 100}）会产生大量重复元素，专门暴露分区、偏移量相关的 bug；</li>
     *   <li>int 全域（含 {@link Integer#MIN_VALUE}、{@link Integer#MAX_VALUE}）专门暴露溢出、
     *       取模、偏移量计算相关的 bug。</li>
     * </ul>
     *
     * @param label     用例标签
     * @param algorithm 被测试的算法
     * @param rounds    压测总轮数，会平均分给上面两种值域
     * @param seed      随机种子，固定种子便于复现问题
     */
    public static void stress(String label, SortAlgorithm algorithm, int rounds, long seed) {
        int half = Math.max(1, rounds / 2);
        stressWithValueRange(label + "，小值域（大量重复元素）", algorithm, half, seed, -100, 100);
        stressWithValueRange(label + "，int 全域（含极值）", algorithm, Math.max(1, rounds - half), seed + 1,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    /**
     * 指定值域的随机压测，适合不能处理全域整数的算法（例如计数排序基础版只能处理非负且值域小的数据）。
     *
     * @param label     用例标签
     * @param algorithm 被测试的算法
     * @param rounds    压测轮数
     * @param seed      随机种子
     * @param minValue  值域下限（含）
     * @param maxValue  值域上限（含）
     */
    public static void stressWithValueRange(String label, SortAlgorithm algorithm, int rounds, long seed,
                                            int minValue, int maxValue) {
        Random random = new Random(seed);
        for (int round = 1; round <= rounds; round++) {
            // 长度取 0 ~ 79：小数组更容易踩到「只有一个元素」「全部相同」这类边界
            int size = random.nextInt(80);
            int[] input = randomArrayInRange(size, minValue, maxValue, random.nextLong());
            verify(label, "随机压测第 " + round + " 轮（长度 " + size + "）", input, algorithm);
        }
        System.out.println("  [通过] " + label + "：随机压测 " + rounds + " 轮全部通过");
    }

    /**
     * 校验某个算法对给定输入数组的排序结果是否正确。
     *
     * <p>做法是先在副本上排序，再和 {@link Arrays#sort(int[])} 的结果逐位比较；
     * 排序过程中抛异常、或者改变了数组长度，同样算失败。
     *
     * @param label     用例标签（算法名 + 实现方式）
     * @param caseName  用例名称
     * @param input     原始输入数据（方法内部不会改动它）
     * @param algorithm 被测试的算法
     */
    public static void verify(String label, String caseName, int[] input, SortAlgorithm algorithm) {
        int[] expected = expectedOf(input);
        int[] actual = Arrays.copyOf(input, input.length);

        try {
            algorithm.sort(actual);
        } catch (RuntimeException e) {
            throw new AssertionError("[" + label + "] 用例「" + caseName + "」排序时抛出异常：" + e, e);
        }

        if (actual.length != input.length) {
            throw new AssertionError("[" + label + "] 用例「" + caseName + "」改变了数组长度："
                    + input.length + " -> " + actual.length);
        }

        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(String.format("[%s] 用例「%s」排序结果不正确%n  输入：%s%n  期望：%s%n  实际：%s",
                    label, caseName, format(input), format(expected), format(actual)));
        }
    }

    /**
     * 打印一个具体例子的排序前后对比，用于直观演示。
     *
     * <p>这里调用的是接口默认方法 {@link SortAlgorithm#sortCopy(int[])}，顺便演示「不改动原数组」的用法。
     *
     * @param label     展示标题，一般写成「算法名（实现方式）」
     * @param sample    示例数据
     * @param algorithm 被演示的算法
     */
    public static void printSample(String label, int[] sample, SortAlgorithm algorithm) {
        int[] result = algorithm.sortCopy(sample);
        System.out.println("  " + label);
        System.out.println("    排序前：" + format(sample));
        System.out.println("    排序后：" + format(result));
        if (!Arrays.equals(expectedOf(sample), result)) {
            throw new AssertionError("[" + label + "] 示例排序结果不正确，期望：" + format(expectedOf(sample)));
        }
    }

    // ------------------------------------------------------------------
    // 测试数据
    // ------------------------------------------------------------------

    /** 生成升序数组：{@code [0, 1, 2, ..., size - 1]}。 */
    public static int[] ascendingArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        return array;
    }

    /** 生成降序数组：{@code [size - 1, ..., 2, 1, 0]}，这是很多排序算法的最坏输入。 */
    public static int[] reversedArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = size - 1 - i;
        }
        return array;
    }

    /**
     * 生成随机数组，值域为 {@code [-1000, 1000]}，含负数以便顺带验证负值处理。
     *
     * @param size 数组长度
     * @param seed 随机种子，固定种子可以让每次运行拿到同一份数据，便于复现问题
     */
    public static int[] randomArray(int size, long seed) {
        return randomArrayInRange(size, RANDOM_MIN, RANDOM_MAX, seed);
    }

    /**
     * 生成指定值域的随机数组。
     *
     * @param size 数组长度
     * @param min  值域下限（含）
     * @param max  值域上限（含）
     * @param seed 随机种子
     */
    public static int[] randomArrayInRange(int size, int min, int max, long seed) {
        if (min > max) {
            throw new IllegalArgumentException("min 不能大于 max：" + min + " > " + max);
        }
        int[] array = new int[size];
        Random random = new Random(seed);
        long range = (long) max - min + 1;
        for (int i = 0; i < size; i++) {
            // 值域不超过 int 范围时直接用 nextInt；超过时退化成对 nextLong 取模
            // 这里用「与 Long.MAX_VALUE 按位与」而不是 Math.abs，因为 Math.abs(Long.MIN_VALUE) 仍然是负数
            long offset = range <= Integer.MAX_VALUE
                    ? random.nextInt((int) range)
                    : (random.nextLong() & Long.MAX_VALUE) % range;
            array[i] = (int) (min + offset);
        }
        return array;
    }

    /** 生成「整体有序、只有少量元素错位」的数组，用来验证插入排序、冒泡优化版这类算法在近似有序数据上的优势。 */
    public static int[] nearlySortedArray(int size, long seed) {
        int[] array = ascendingArray(size);
        if (size < 2) {
            return array;
        }
        Random random = new Random(seed);
        int swapTimes = Math.max(1, size / 20);
        for (int i = 0; i < swapTimes; i++) {
            int first = random.nextInt(size);
            int second = random.nextInt(size);
            int temp = array[first];
            array[first] = array[second];
            array[second] = temp;
        }
        return array;
    }

    /** 生成「大量重复值」的数组（只有 0~5 这 6 个不同取值），用来暴露单路快排在重复数据上的性能退化。 */
    public static int[] fewDistinctValuesArray(int size, long seed) {
        return randomArrayInRange(size, 0, 5, seed);
    }

    /** 计算期望结果：直接用 JDK 的排序结果作为参照。 */
    public static int[] expectedOf(int[] source) {
        int[] expected = Arrays.copyOf(source, source.length);
        Arrays.sort(expected);
        return expected;
    }

    // ------------------------------------------------------------------
    // 输出格式化
    // ------------------------------------------------------------------

    /** 把数组格式化成一行字符串，元素过多时折叠中间部分。 */
    public static String format(int[] array) {
        if (array == null) {
            return "null";
        }
        if (array.length == 0) {
            return "[]";
        }
        StringBuilder builder = new StringBuilder("[");
        int limit = Math.min(array.length, MAX_PRINT_SIZE);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(array[i]);
        }
        if (array.length > limit) {
            builder.append(", ... 共 ").append(array.length).append(" 个元素");
        }
        return builder.append(']').toString();
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    /** 标准用例集合：覆盖各种边界与典型场景。 */
    private static Case[] standardCases() {
        return new Case[]{
                new Case("空数组", new int[0]),
                new Case("单个元素", new int[]{42}),
                new Case("两个元素（逆序）", new int[]{2, 1}),
                new Case("两个元素（已序）", new int[]{1, 2}),
                new Case("全部元素相同", new int[]{7, 7, 7, 7, 7, 7}),
                new Case("已升序（小数组）", ascendingArray(20)),
                new Case("已降序（小数组）", reversedArray(20)),
                new Case("含负数和零", new int[]{-3, 0, -1, 5, -3, 2, 0, -10, 7}),
                new Case("随机数组", randomArray(300, 20260925L)),
                new Case("近乎有序数组", nearlySortedArray(300, 20260925L)),
                new Case("大量重复值", fewDistinctValuesArray(500, 20260925L)),
                new Case("较大随机数组", randomArray(1000, 7L)),
                new Case("大数值随机数组（约 ±50 万）", randomArrayInRange(500, -500000, 500000, 99L))
        };
    }

    /** 非负整数用例集合：去掉所有含负数的场景，值域也控制在计数排序可接受的范围内。 */
    private static Case[] nonNegativeCases() {
        return new Case[]{
                new Case("空数组", new int[0]),
                new Case("单个元素", new int[]{0}),
                new Case("两个元素（逆序）", new int[]{2, 1}),
                new Case("两个元素（已序）", new int[]{1, 2}),
                new Case("全部元素相同", new int[]{7, 7, 7, 7, 7, 7}),
                new Case("已升序（小数组）", ascendingArray(20)),
                new Case("已降序（小数组）", reversedArray(20)),
                new Case("含零和重复值", new int[]{0, 0, 5, 3, 5, 1, 0, 9, 3}),
                new Case("随机数组", randomArrayInRange(300, 0, 1000, 20260925L)),
                new Case("近乎有序数组", nearlySortedArray(300, 20260925L)),
                new Case("大量重复值", fewDistinctValuesArray(500, 20260925L)),
                new Case("较大随机数组", randomArrayInRange(1000, 0, 2000, 7L)),
                new Case("大数值随机数组（0 ~ 100 万）", randomArrayInRange(500, 0, 1000000, 99L))
        };
    }

    /** 一条测试用例：名称 + 输入数据。 */
    private static final class Case {

        private final String name;

        private final int[] input;

        private Case(String name, int[] input) {
            this.name = name;
            this.input = input;
        }
    }
}
