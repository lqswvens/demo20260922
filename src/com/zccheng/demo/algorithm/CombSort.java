package com.zccheng.demo.algorithm;

/**
 * 梳排序：冒泡排序的改良版，用逐步缩小的「间隔（gap）」代替冒泡中固定的相邻比较。
 *
 * <p>它一开始用较大的间隔把离得很远的元素快速换到附近，间隔不断缩小，
 * 最后间隔变成 1 时退化成一次冒泡排序，起到收尾作用。
 *
 * <p>为什么有效：冒泡排序最怕「小元素排在很靠后的位置」，因为它每次只能向前挪一格；
 * 梳排序先用大间隔比较，小元素可以一次跨越很长距离，因此平均性能比冒泡好得多，代码却几乎一样简单。
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：平均约 O(n^2 / 2^p)（{@code p} 为趟数），实践中接近 O(n log n)；最坏仍为 O(n^2)；</li>
 *   <li>空间：O(1)；</li>
 *   <li>稳定性：不稳定，间隔大于 1 时元素会跨过相等元素。</li>
 * </ul>
 *
 * <p>本类提供 2 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：收缩因子取经典的 1.3（大量实测中的经验最优值）；</li>
 *   <li>{@link #sort(int[], double)}：收缩因子由调用方指定，方便观察它对性能的影响
 *       （因子越小，间隔缩小越慢、比较次数越多；因子越大，行为越接近普通冒泡）。</li>
 * </ol>
 */
public class CombSort extends AbstractSortAlgorithm {

    /** 经典收缩因子：1.3 是大量实测中综合表现最好的取值。 */
    private static final double DEFAULT_SHRINK_FACTOR = 1.3;

    /**
     * 默认版本：收缩因子 1.3。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        sort(array, DEFAULT_SHRINK_FACTOR);
    }

    /**
     * 指定收缩因子的版本。
     *
     * @param array        待排序数组，不能为 {@code null}
     * @param shrinkFactor 收缩因子，必须大于 1
     */
    public void sort(int[] array, double shrinkFactor) {
        checkArray(array);
        if (shrinkFactor <= 1) {
            throw new IllegalArgumentException("收缩因子必须大于 1，当前值：" + shrinkFactor);
        }

        int length = array.length;
        int gap = length;
        boolean swapped = true;

        // 间隔缩小到 1 之后还要再走一轮，只要还有交换就继续，直到彻底有序
        while (gap > 1 || swapped) {
            gap = Math.max(1, (int) (gap / shrinkFactor));
            swapped = false;

            for (int i = 0; i + gap < length; i++) {
                if (array[i] > array[i + gap]) {
                    swap(array, i, i + gap);
                    swapped = true;
                }
            }
        }
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把两种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {49, 38, 65, 97, 76, 13, 27, 49, -5, 0, 100, -100};
        CombSort combSort = new CombSort();

        System.out.println("梳排序示例：");
        SortTestSupport.printSample("收缩因子 1.3（经典取值）", sample, combSort);
        SortTestSupport.printSample("收缩因子 1.5", sample, a -> combSort.sort(a, 1.5));

        System.out.println("梳排序用例测试：");
        SortTestSupport.runAll("梳排序（收缩因子 1.3）", combSort);
        SortTestSupport.runAll("梳排序（收缩因子 1.5）", a -> combSort.sort(a, 1.5));

        System.out.println("梳排序随机压测：");
        SortTestSupport.stress("梳排序（收缩因子 1.3）", combSort, 300, 20260925L);
        SortTestSupport.stress("梳排序（收缩因子 1.5）", a -> combSort.sort(a, 1.5), 300, 20260925L);
    }
}
