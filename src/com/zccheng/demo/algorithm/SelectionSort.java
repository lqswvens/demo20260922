package com.zccheng.demo.algorithm;

/**
 * 选择排序：每一轮从待处理区间里「选」出最小（或最大）的元素，放到区间边界上。
 *
 * <p>核心思想：把数组分成「已排序区」和「未排序区」，每轮在未排序区里找最小值，
 * 与未排序区的第一个元素交换，已排序区就增长一个元素。
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：无论数据如何都是 O(n^2)，比较次数固定为 {@code n * (n - 1) / 2}；
 *       但交换次数最多只有 {@code n - 1} 次，比冒泡排序少得多；</li>
 *   <li>空间：O(1)；</li>
 *   <li>稳定性：基础版不稳定（交换会跨过与最小值相等的元素），双向版同样不稳定。</li>
 * </ul>
 *
 * <p>本类提供 2 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：简单选择排序，每轮只找最小值；</li>
 *   <li>{@link #sortBidirectional(int[])}：双端选择排序，每轮同时找最小值和最大值，
 *       一次确定两个元素的最终位置，循环轮数减半。</li>
 * </ol>
 */
public class SelectionSort extends AbstractSortAlgorithm {

    /**
     * 简单选择排序：每轮在 {@code [i, n - 1]} 中找出最小值，与下标 {@code i} 交换。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        for (int i = 0; i < array.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] < array[minIndex]) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                swap(array, i, minIndex);
            }
        }
    }

    /**
     * 双端选择排序：每轮同时找出区间内的最小值和最大值，
     * 最小值换到左端、最大值换到右端，然后区间两端各收缩一格。
     *
     * <p>注意一个细节：如果最大值原本就在左端 {@code left} 下标上，
     * 那么「最小值换到左端」这一步会把它挪到 {@code minIndex} 位置，
     * 所以交换前必须修正最大值的下标，否则会把数据放错位置。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortBidirectional(int[] array) {
        checkArray(array);
        int left = 0;
        int right = array.length - 1;

        while (left < right) {
            int minIndex = left;
            int maxIndex = left;
            for (int i = left; i <= right; i++) {
                if (array[i] < array[minIndex]) {
                    minIndex = i;
                }
                if (array[i] > array[maxIndex]) {
                    maxIndex = i;
                }
            }

            swap(array, left, minIndex);
            if (maxIndex == left) {
                // 最大值原在 left 位置，被上一步换到了 minIndex 位置
                maxIndex = minIndex;
            }
            swap(array, right, maxIndex);

            left++;
            right--;
        }
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把两种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {49, 38, 65, 97, 76, 13, 27, 49, -5, 0, 100, -100};
        SelectionSort selectionSort = new SelectionSort();

        System.out.println("选择排序示例：");
        SortTestSupport.printSample("简单选择版", sample, selectionSort);
        SortTestSupport.printSample("双端选择版", sample, a -> selectionSort.sortBidirectional(a));

        System.out.println("选择排序用例测试：");
        SortTestSupport.runAll("选择排序（简单选择版）", selectionSort);
        SortTestSupport.runAll("选择排序（双端选择版）", a -> selectionSort.sortBidirectional(a));

        System.out.println("选择排序随机压测：");
        SortTestSupport.stress("选择排序（简单选择版）", selectionSort, 300, 20260925L);
        SortTestSupport.stress("选择排序（双端选择版）", a -> selectionSort.sortBidirectional(a), 300, 20260925L);
    }
}
