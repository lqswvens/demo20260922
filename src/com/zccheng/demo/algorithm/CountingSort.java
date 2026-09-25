package com.zccheng.demo.algorithm;

/**
 * 计数排序：不做元素之间的比较，而是把「元素的值」直接当作下标，统计每个值出现的次数，再按值的大小顺序写回数组。
 *
 * <p>核心思想：
 * <ol>
 *   <li>找出数据的值域（最小值到最大值）；</li>
 *   <li>开一个长度为「值域跨度」的计数数组，遍历原数组完成计数；</li>
 *   <li>按计数数组的下标从小到大依次写回数据，得到的自然就是升序结果。</li>
 * </ol>
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：O(n + k)，{@code n} 是元素个数，{@code k} 是值域跨度；元素少而值域大时并不划算；</li>
 *   <li>空间：O(k)，需要额外的计数数组；</li>
 *   <li>稳定性：本写法按值输出，相同值本来就不区分先后，天然稳定；</li>
 *   <li>适用前提：元素是整数（或可映射成整数的键），且值域跨度不大。</li>
 * </ul>
 *
 * <p>本类提供 2 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：基础版，只统计 {@code 0 ~ max}，适合成绩、年龄、状态码这类非负且值域小的数据；</li>
 *   <li>{@link #sortWithOffset(int[])}：带偏移量的版本，统计 {@code min ~ max}，因此可以处理负数。</li>
 * </ol>
 *
 * <p>注意：计数数组长度等于值域跨度，所以值域异常大时必须提前拦截。
 * 本类用 {@link #MAX_RANGE} 做保护，超过上限直接抛异常并提示改用基数排序等更合适的算法。
 */
public class CountingSort extends AbstractSortAlgorithm {

    /** 计数数组允许的最大长度（约 1600 万），值域超过这个范围就不再适合用计数排序。 */
    public static final int MAX_RANGE = 1 << 24;

    /**
     * 基础版计数排序：只支持非负整数，统计范围是 {@code 0 ~ max}。
     *
     * @param array 待排序数组，不能为 {@code null}
     * @throws IllegalArgumentException 数组中出现负数，或值域过大时抛出
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        if (array.length < 2) {
            return;
        }

        int max = array[0];
        for (int value : array) {
            if (value < 0) {
                throw new IllegalArgumentException("基础版计数排序只支持非负整数，但遇到了 " + value
                        + "；可改用 sortWithOffset(int[]) 或 RadixSort（基数排序）");
            }
            if (value > max) {
                max = value;
            }
        }

        requireRange((long) max + 1, "值域 0 ~ " + max);

        int[] count = new int[max + 1];
        for (int value : array) {
            count[value]++;
        }
        fillBack(array, count, 0);
    }

    /**
     * 带偏移量的计数排序：先求出最小值 {@code min}，用 {@code value - min} 作为计数下标，
     * 这样负数也能参与统计，需要申请的数组长度只要 {@code max - min + 1}。
     *
     * @param array 待排序数组，不能为 {@code null}
     * @throws IllegalArgumentException 值域过大时抛出
     */
    public void sortWithOffset(int[] array) {
        checkArray(array);
        if (array.length < 2) {
            return;
        }

        int min = array[0];
        int max = array[0];
        for (int value : array) {
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
        }

        long range = (long) max - min + 1;
        requireRange(range, "值域 " + min + " ~ " + max);

        int[] count = new int[(int) range];
        for (int value : array) {
            // value - min 一定落在 [0, range) 内，且 range 已做过上限校验，所以不会溢出
            count[value - min]++;
        }
        fillBack(array, count, min);
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    /**
     * 把计数数组还原成有序数据。
     *
     * @param array  输出数组
     * @param count  计数数组，下标 {@code i} 表示值 {@code offset + i} 出现的次数
     * @param offset 下标到真实值的偏移量（基础版为 0，带偏移版为 min）
     */
    private static void fillBack(int[] array, int[] count, int offset) {
        int index = 0;
        for (int value = 0; value < count.length; value++) {
            for (int times = count[value]; times > 0; times--) {
                array[index++] = offset + value;
            }
        }
    }

    /** 校验值域跨度是否可以接受，避免申请过大的数组。 */
    private static void requireRange(long range, String description) {
        if (range > MAX_RANGE) {
            throw new IllegalArgumentException("计数排序需要长度为 " + range + " 的计数数组，"
                    + description + " 过大；这类数据更适合基数排序或基于比较的排序");
        }
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把两种实现都跑一遍标准用例，
     * 最后演示两条异常路径（基础版遇到负数、值域过大）。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {4, 2, 2, 8, 3, 3, 1};
        CountingSort countingSort = new CountingSort();

        System.out.println("计数排序示例：");
        SortTestSupport.printSample("基础版（非负整数）", sample, countingSort);
        SortTestSupport.printSample("带偏移量版（支持负数）",
                new int[]{49, 38, -65, 97, -76, 13, 0, 49}, a -> countingSort.sortWithOffset(a));

        System.out.println("计数排序用例测试：");
        // 基础版不支持负数，所以用「非负整数」用例集合
        SortTestSupport.runNonNegativeAll("计数排序（基础版）", countingSort);
        SortTestSupport.runAll("计数排序（带偏移量版）", a -> countingSort.sortWithOffset(a));

        try {
            // 异常路径 1：基础版不支持负数
            countingSort.sort(new int[]{1, -1});
        } catch (IllegalArgumentException e) {
            System.out.println("  预期异常（负数）：" + e.getMessage());
        }

        try {
            // 异常路径 2：值域约 ±21 亿，远超计数数组上限
            countingSort.sortWithOffset(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE});
        } catch (IllegalArgumentException e) {
            System.out.println("  预期异常（值域过大）：" + e.getMessage());
        }

        System.out.println("计数排序随机压测（值域必须受控，所以不用 int 全域）：");
        SortTestSupport.stressWithValueRange("计数排序（基础版，值域 0 ~ 1000）", countingSort, 300, 20260925L, 0, 1000);
        SortTestSupport.stressWithValueRange("计数排序（带偏移量版，值域 -1000 ~ 1000）",
                a -> countingSort.sortWithOffset(a), 300, 20260925L, -1000, 1000);
    }
}
