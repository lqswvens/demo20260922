package com.zccheng.demo.algorithm;

import java.util.Arrays;
import java.util.Random;

/**
 * {@link BubbleSort} 的独立单元测试入口。
 *
 * <p>本项目没有 Maven、Gradle 与第三方测试依赖，因此沿用仓库约定的自测方式：
 * 使用普通 {@code main} 方法执行用例，失败时直接抛出带中文说明的 {@link AssertionError}。
 * 这样测试只依赖 JDK，能够和业务源码一起使用 {@code javac} 编译。
 *
 * <p>测试覆盖三种实现方式的典型数据、边界数据、随机数据以及 {@code null} 参数约定：
 * <ul>
 *   <li>基础版：{@link BubbleSort#sort(int[])}；</li>
 *   <li>提前退出版：{@link BubbleSort#sortWithEarlyExit(int[])}；</li>
 *   <li>鸡尾酒双向版：{@link BubbleSort#sortBidirectional(int[])}。</li>
 * </ul>
 */
public final class BubbleSortTest {

    /** 随机测试的固定种子，保证失败后可以稳定复现同一批数据。 */
    private static final long RANDOM_SEED = 20261101L;

    /** 每种实现方式执行的随机数组轮数。 */
    private static final int RANDOM_ROUNDS = 50;

    /** 测试类只提供静态入口，不允许实例化。 */
    private BubbleSortTest() {
    }

    /**
     * 执行全部单元测试；任何一条失败都会终止程序并抛出 {@link AssertionError}。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        BubbleSort bubbleSort = new BubbleSort();
        SortAlgorithm basic = bubbleSort::sort;
        SortAlgorithm earlyExit = bubbleSort::sortWithEarlyExit;
        SortAlgorithm bidirectional = bubbleSort::sortBidirectional;

        System.out.println("BubbleSort 单元测试开始：");
        verifyCommonCases("基础版", basic);
        verifyCommonCases("提前退出版", earlyExit);
        verifyCommonCases("鸡尾酒双向版", bidirectional);

        verifyRandomCases("基础版", basic, RANDOM_SEED);
        verifyRandomCases("提前退出版", earlyExit, RANDOM_SEED + 1);
        verifyRandomCases("鸡尾酒双向版", bidirectional, RANDOM_SEED + 2);

        verifyNullRejection("基础版", basic);
        verifyNullRejection("提前退出版", earlyExit);
        verifyNullRejection("鸡尾酒双向版", bidirectional);

        System.out.println("BubbleSort 单元测试全部通过");
    }

    // ------------------------------------------------------------------
    // 固定用例
    // ------------------------------------------------------------------

    /**
     * 用固定数据覆盖空数组、单元素、逆序、重复值和整数极值等场景。
     *
     * @param label     实现方式名称
     * @param algorithm 当前待验证的排序实现
     */
    private static void verifyCommonCases(String label, SortAlgorithm algorithm) {
        assertSorts(label, "空数组", new int[0], algorithm);
        assertSorts(label, "单个元素", new int[]{42}, algorithm);
        assertSorts(label, "两个元素逆序", new int[]{2, 1}, algorithm);
        assertSorts(label, "已升序但含重复值", new int[]{-5, -5, 0, 1, 1, 9}, algorithm);
        assertSorts(label, "完全逆序", new int[]{5, 4, 3, 2, 1}, algorithm);
        assertSorts(label, "普通乱序", new int[]{49, 38, 65, 97, 76, 13, 27, 49}, algorithm);
        assertSorts(label, "整数极值", new int[]{
                Integer.MAX_VALUE, 0, Integer.MIN_VALUE, -1, Integer.MAX_VALUE, Integer.MIN_VALUE
        }, algorithm);
    }

    /**
     * 对固定种子生成的整数数组执行多轮随机测试，补充固定用例未覆盖的排列组合。
     *
     * @param label     实现方式名称
     * @param algorithm 当前待验证的排序实现
     * @param seed      随机种子
     */
    private static void verifyRandomCases(String label, SortAlgorithm algorithm, long seed) {
        Random random = new Random(seed);
        for (int round = 1; round <= RANDOM_ROUNDS; round++) {
            int[] input = new int[random.nextInt(101)];
            for (int i = 0; i < input.length; i++) {
                input[i] = random.nextInt();
            }
            assertSorts(label, "随机用例第 " + round + " 轮", input, algorithm);
        }
    }

    /**
     * 验证实现遵循数组为 {@code null} 时抛出 {@link IllegalArgumentException} 的公共约定。
     *
     * @param label     实现方式名称
     * @param algorithm 当前待验证的排序实现
     */
    private static void verifyNullRejection(String label, SortAlgorithm algorithm) {
        try {
            algorithm.sort(null);
        } catch (IllegalArgumentException exception) {
            String expectedMessage = "待排序数组不能为 null";
            if (!expectedMessage.equals(exception.getMessage())) {
                throw new AssertionError("[" + label + "] null 参数异常消息不正确，期望："
                        + expectedMessage + "，实际：" + exception.getMessage(), exception);
            }
            return;
        }
        throw new AssertionError("[" + label + "] 传入 null 时没有抛出 IllegalArgumentException");
    }

    // ------------------------------------------------------------------
    // 断言与辅助方法
    // ------------------------------------------------------------------

    /**
     * 排序输入副本并和 JDK 参考实现的结果逐位比较。
     *
     * <p>使用副本是为了确认当前实现确实原地修改传入数组，而不是返回一个容易误用的新数组。
     *
     * @param label     实现方式名称
     * @param caseName  用例名称
     * @param input     原始数据
     * @param algorithm 待验证的排序实现
     */
    private static void assertSorts(String label, String caseName, int[] input, SortAlgorithm algorithm) {
        int[] expected = Arrays.copyOf(input, input.length);
        int[] actual = Arrays.copyOf(input, input.length);
        Arrays.sort(expected);

        try {
            algorithm.sort(actual);
        } catch (RuntimeException exception) {
            throw new AssertionError("[" + label + "] 用例「" + caseName + "」排序时抛出异常："
                    + exception.getMessage(), exception);
        }

        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError("[" + label + "] 用例「" + caseName + "」排序结果不正确"
                    + System.lineSeparator() + "  输入：" + Arrays.toString(input)
                    + System.lineSeparator() + "  期望：" + Arrays.toString(expected)
                    + System.lineSeparator() + "  实际：" + Arrays.toString(actual));
        }
    }
}
