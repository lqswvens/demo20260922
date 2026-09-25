package com.zccheng.demo.algorithm;

/**
 * 冒泡排序：每一轮从头到尾比较相邻两个元素，把较大的一路「冒」到本轮的末尾。
 *
 * <p>核心思想：一次完整的扫描必然把当前范围内的最大值换到最右边，
 * 因此每轮结束后待处理区间缩小 1，重复 {@code n - 1} 轮即可有序。
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：平均和最坏 O(n^2)，基础版最好也是 O(n^2)；加了提前退出后，最好情况（已有序）降为 O(n)；</li>
 *   <li>空间：O(1)，只在交换时使用一个临时变量；</li>
 *   <li>稳定性：稳定，只有「严格大于」才交换，相等元素不会跨过对方。</li>
 * </ul>
 *
 * <p>本类提供 3 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：基础版冒泡，最容易理解和书写，适合教学演示；</li>
 *   <li>{@link #sortWithEarlyExit(int[])}：加「本轮没有发生交换就结束」的优化，近似有序数据上接近 O(n)；</li>
 *   <li>{@link #sortBidirectional(int[])}：鸡尾酒排序（双向冒泡），正向一趟把最大值送到右端、反向一趟把最小值送到左端，
 *       对「大部分有序、个别小元素在末尾」的数据比单向冒泡快很多。</li>
 * </ol>
 */
public class BubbleSort extends AbstractSortAlgorithm {

    /**
     * 基础版冒泡排序：每轮把当前区间内的最大值冒到右端。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        for (int end = array.length - 1; end > 0; end--) {
            for (int i = 0; i < end; i++) {
                if (array[i] > array[i + 1]) {
                    swap(array, i, i + 1);
                }
            }
        }
    }

    /**
     * 提前退出优化版：如果某一轮从头到尾都没有发生交换，说明数组已经有序，可以直接结束。
     *
     * <p>这是冒泡排序最实用的一个优化，对「已经基本有序」的数据效果非常明显。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortWithEarlyExit(int[] array) {
        checkArray(array);
        for (int end = array.length - 1; end > 0; end--) {
            boolean swapped = false;
            for (int i = 0; i < end; i++) {
                if (array[i] > array[i + 1]) {
                    swap(array, i, i + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                // 本轮没有任何交换，区间内已全部有序
                return;
            }
        }
    }

    /**
     * 鸡尾酒排序（双向冒泡）：正向一趟把最大值送到右边界（右边界左移），
     * 反向一趟把最小值送到左边界（左边界右移），两头同时收敛。
     *
     * <p>对「小元素恰好堆在数组末尾」这类数据，单向冒泡要花 O(n^2) 把它挪到最前面，
     * 双向冒泡只需要一趟反向扫描。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortBidirectional(int[] array) {
        checkArray(array);
        int left = 0;
        int right = array.length - 1;
        boolean swapped = true;

        while (swapped && left < right) {
            swapped = false;

            // 正向：把当前区间最大值送到 right 位置，随后右侧边界已经就位
            for (int i = left; i < right; i++) {
                if (array[i] > array[i + 1]) {
                    swap(array, i, i + 1);
                    swapped = true;
                }
            }
            right--;

            // 反向：把当前区间最小值送到 left 位置，随后左侧边界已经就位
            for (int i = right; i > left; i--) {
                if (array[i - 1] > array[i]) {
                    swap(array, i - 1, i);
                    swapped = true;
                }
            }
            left++;
        }
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把三种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {49, 38, 65, 97, 76, 13, 27, 49, -5, 0, 100, -100};
        BubbleSort bubbleSort = new BubbleSort();

        System.out.println("冒泡排序示例：");
        SortTestSupport.printSample("基础版", sample, bubbleSort);
        SortTestSupport.printSample("提前退出优化版", sample, a -> bubbleSort.sortWithEarlyExit(a));
        SortTestSupport.printSample("鸡尾酒双向版", sample, a -> bubbleSort.sortBidirectional(a));

        System.out.println("冒泡排序用例测试：");
        SortTestSupport.runAll("冒泡排序（基础版）", bubbleSort);
        SortTestSupport.runAll("冒泡排序（提前退出优化版）", a -> bubbleSort.sortWithEarlyExit(a));
        SortTestSupport.runAll("冒泡排序（鸡尾酒双向版）", a -> bubbleSort.sortBidirectional(a));

        System.out.println("冒泡排序随机压测：");
        SortTestSupport.stress("冒泡排序（基础版）", bubbleSort, 300, 20260925L);
        SortTestSupport.stress("冒泡排序（提前退出优化版）", a -> bubbleSort.sortWithEarlyExit(a), 300, 20260925L);
        SortTestSupport.stress("冒泡排序（鸡尾酒双向版）", a -> bubbleSort.sortBidirectional(a), 300, 20260925L);
    }
}
