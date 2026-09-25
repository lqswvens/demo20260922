package com.zccheng.demo.algorithm;

/**
 * 堆排序：利用「二叉堆」这种数据结构完成排序，是唯一能同时做到最坏 O(n log n) 且只需 O(1) 额外空间的常用排序。
 *
 * <p>核心思路（升序排序使用大顶堆）：
 * <ol>
 *   <li>建堆：把数组整理成大顶堆，即每个节点都不小于它的两个孩子，此时堆顶是全局最大值；</li>
 *   <li>交换：把堆顶与当前堆的最后一个元素交换，最大值就落到了最终位置；</li>
 *   <li>下沉：堆的有效范围缩小 1，再让新的堆顶「下沉」到合适位置，恢复堆性质；</li>
 *   <li>重复第 2、3 步，直到堆里只剩一个元素。</li>
 * </ol>
 *
 * <p>数组表示二叉树的下标关系：下标 {@code i} 的左孩子是 {@code 2i + 1}，右孩子是 {@code 2i + 2}，
 * 父节点是 {@code (i - 1) / 2}，因此不需要真的建树。
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：建堆 O(n)，之后 n - 1 次下沉每次 O(log n)，整体 O(n log n)（最好、平均、最坏都一样）；</li>
 *   <li>空间：O(1)，完全就地排序；</li>
 *   <li>稳定性：不稳定，堆顶与末尾元素的长距离交换会打乱相等元素的相对次序。</li>
 * </ul>
 *
 * <p>本类提供 2 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：下沉建堆（从最后一个非叶子节点往前逐个下沉），建堆只需 O(n)，是标准写法；</li>
 *   <li>{@link #sortBuildBySiftUp(int[])}：上浮建堆（把元素逐个插入并上浮），建堆需要 O(n log n)，
 *       但思路更接近「向堆里不断插入数据」，适合和优先队列的用法对照理解。</li>
 * </ol>
 */
public class HeapSort extends AbstractSortAlgorithm {

    /**
     * 标准堆排序：下沉建堆 + 反复「交换堆顶到末尾、再下沉」。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        int length = array.length;

        // 从最后一个非叶子节点开始向前下沉建堆，时间复杂度 O(n)
        for (int i = length / 2 - 1; i >= 0; i--) {
            siftDown(array, i, length);
        }

        for (int end = length - 1; end > 0; end--) {
            // 堆顶是当前最大值，换到区间末尾即到达最终位置
            swap(array, 0, end);
            // 堆的有效范围缩小到 [0, end)，修复堆顶
            siftDown(array, 0, end);
        }
    }

    /**
     * 上浮建堆版堆排序：把元素从前往后逐个「插入」到堆中并上浮，
     * 建堆完成后，后续排序过程与标准版完全一致。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortBuildBySiftUp(int[] array) {
        checkArray(array);
        int length = array.length;

        for (int i = 1; i < length; i++) {
            siftUp(array, i);
        }

        for (int end = length - 1; end > 0; end--) {
            swap(array, 0, end);
            siftDown(array, 0, end);
        }
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    /**
     * 下沉：让下标 {@code start} 处的元素与较大的孩子交换，一直往下调整到合适位置。
     *
     * @param array 目标数组
     * @param start 下沉起点
     * @param end   堆的有效范围右边界（不含），即堆只覆盖 {@code [0, end)}
     */
    private static void siftDown(int[] array, int start, int end) {
        int current = start;
        while (true) {
            int leftChild = 2 * current + 1;
            int rightChild = leftChild + 1;
            int largest = current;

            if (leftChild < end && array[leftChild] > array[largest]) {
                largest = leftChild;
            }
            if (rightChild < end && array[rightChild] > array[largest]) {
                largest = rightChild;
            }
            if (largest == current) {
                // 已经比两个孩子都大，堆性质恢复
                return;
            }
            swap(array, current, largest);
            current = largest;
        }
    }

    /**
     * 上浮：把下标 {@code index} 处的元素与父节点比较，比父节点大就交换，直到满足堆性质。
     *
     * @param array 目标数组，调用前假设 {@code [0, index - 1]} 已经是大顶堆
     * @param index 新插入元素的下标
     */
    private static void siftUp(int[] array, int index) {
        int current = index;
        while (current > 0) {
            int parent = (current - 1) >>> 1;
            if (array[parent] >= array[current]) {
                return;
            }
            swap(array, parent, current);
            current = parent;
        }
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把两种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {49, 38, 65, 97, 76, 13, 27, 49, -5, 0, 100, -100};
        HeapSort heapSort = new HeapSort();

        System.out.println("堆排序示例：");
        SortTestSupport.printSample("下沉建堆版", sample, heapSort);
        SortTestSupport.printSample("上浮建堆版", sample, a -> heapSort.sortBuildBySiftUp(a));

        System.out.println("堆排序用例测试：");
        SortTestSupport.runAll("堆排序（下沉建堆版）", heapSort);
        SortTestSupport.runAll("堆排序（上浮建堆版）", a -> heapSort.sortBuildBySiftUp(a));

        System.out.println("堆排序随机压测：");
        SortTestSupport.stress("堆排序（下沉建堆版）", heapSort, 300, 20260925L);
        SortTestSupport.stress("堆排序（上浮建堆版）", a -> heapSort.sortBuildBySiftUp(a), 300, 20260925L);
    }
}
