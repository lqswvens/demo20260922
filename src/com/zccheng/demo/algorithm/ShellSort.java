package com.zccheng.demo.algorithm;

/**
 * 希尔排序：插入排序的改进版，先把数组按「增量（gap）」分成若干子序列分别做插入排序，
 * 再不断缩小增量，最后增量变成 1 时做一次完整插入排序。
 *
 * <p>为什么有效：增量较大时，元素可以一次跨越很远的位置，能快速把「离最终位置很远」的元素挪过去；
 * 等到增量变小时，数组已经「基本有序」，而插入排序在基本有序的数据上接近 O(n)。
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：取决于增量序列。用 Knuth 序列（1, 4, 13, 40, ...）大约 O(n^1.5)；
 *       折半增量最坏 O(n^2)（某些输入下会退化），但实现简单；</li>
 *   <li>空间：O(1)，是就地排序；</li>
 *   <li>稳定性：不稳定，同一元素可能在不同增量的子序列中跨过其他元素。</li>
 * </ul>
 *
 * <p>本类提供 2 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：Knuth 增量序列，{@code h = 3h + 1}，性能更稳；</li>
 *   <li>{@link #sortByHalfGap(int[])}：折半缩小增量（{@code gap /= 2}），是希尔排序最初提出的写法，代码最直观。</li>
 * </ol>
 */
public class ShellSort extends AbstractSortAlgorithm {

    /**
     * Knuth 增量序列版：先用 {@code h = 3h + 1} 找到不超过 {@code n / 3} 的最大增量，
     * 之后按 {@code h = h / 3} 依次缩小到 1。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);

        int h = 1;
        while (h < array.length / 3) {
            h = 3 * h + 1;
        }
        for (; h >= 1; h /= 3) {
            insertionSortWithGap(array, h);
        }
    }

    /**
     * 折半增量版：增量从 {@code n / 2} 开始，每次减半，直到增量为 1 结束。
     *
     * <p>写法最贴近希尔排序的原始论文，便于理解「增量」的作用。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortByHalfGap(int[] array) {
        checkArray(array);
        for (int gap = array.length / 2; gap > 0; gap /= 2) {
            insertionSortWithGap(array, gap);
        }
    }

    /**
     * 对「按 gap 分出来的每个子序列」做插入排序。
     *
     * <p>下标 {@code 0, gap, 2gap, ...} 构成第一个子序列，{@code 1, 1 + gap, ...} 构成第二个，
     * 依此类推。每个子序列各自做插入排序，代码上就是「步长从 1 变成 gap」的插入排序。
     *
     * @param array 目标数组
     * @param gap   当前增量，必须大于 0
     */
    private static void insertionSortWithGap(int[] array, int gap) {
        for (int i = gap; i < array.length; i++) {
            int current = array[i];
            int j = i - gap;
            while (j >= 0 && array[j] > current) {
                array[j + gap] = array[j];
                j -= gap;
            }
            array[j + gap] = current;
        }
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把两种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {49, 38, 65, 97, 76, 13, 27, 49, -5, 0, 100, -100};
        ShellSort shellSort = new ShellSort();

        System.out.println("希尔排序示例：");
        SortTestSupport.printSample("Knuth 增量版", sample, shellSort);
        SortTestSupport.printSample("折半增量版", sample, a -> shellSort.sortByHalfGap(a));

        System.out.println("希尔排序用例测试：");
        SortTestSupport.runAll("希尔排序（Knuth 增量版）", shellSort);
        SortTestSupport.runAll("希尔排序（折半增量版）", a -> shellSort.sortByHalfGap(a));

        System.out.println("希尔排序随机压测：");
        SortTestSupport.stress("希尔排序（Knuth 增量版）", shellSort, 300, 20260925L);
        SortTestSupport.stress("希尔排序（折半增量版）", a -> shellSort.sortByHalfGap(a), 300, 20260925L);
    }
}
