package com.zccheng.demo.algorithm;

/**
 * 插入排序：把每个元素插入到「前面已经有序的部分」中的正确位置，像打扑克时一张张理牌。
 *
 * <p>核心思想：把第一个元素看作已排序区，从第二个元素开始，依次把当前元素插到左侧有序区的合适位置，
 * 有序区不断向右扩张，处理完所有元素即完成排序。
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：最坏和平均 O(n^2)（数组逆序时每个元素都要移到最前面），最好 O(n)（数组已有序，每次只比较一次）；</li>
 *   <li>空间：O(1)；</li>
 *   <li>稳定性：稳定，遇到相等元素就停止移动，不会打乱相同元素的相对次序。</li>
 * </ul>
 *
 * <p>本类提供 3 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：直接插入排序，边比较边把较大元素整体后移，是工程上最推荐的写法；</li>
 *   <li>{@link #sortBySwap(int[])}：交换式插入，用相邻交换代替整体后移，代码最短但赋值次数多；</li>
 *   <li>{@link #sortByBinarySearch(int[])}：二分插入排序，用二分查找定位插入点，把比较次数从 O(n) 降到 O(log n)，
 *       但元素移动次数不变，所以整体复杂度仍是 O(n^2)（适合「比较很贵、移动很便宜」的场景）。</li>
 * </ol>
 *
 * <p>另外，插入排序是很多高级算法的小数组优化手段（例如快排在区间很小时改用插入排序），
 * 因为它在小规模、近乎有序的数据上常数极小。
 */
public class InsertionSort extends AbstractSortAlgorithm {

    /**
     * 直接插入排序：记录当前元素，把左侧比它大的元素依次后移一格，再把它放到空出的位置。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        for (int i = 1; i < array.length; i++) {
            int current = array[i];
            int j = i - 1;
            while (j >= 0 && array[j] > current) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = current;
        }
    }

    /**
     * 交换式插入排序：如果前一个元素比当前元素大，就交换两者，
     * 相当于让当前元素一路「冒」到自己的位置。
     *
     * <p>实现最简单，容易和冒泡排序对比理解，但每次交换要写 3 次数组，效率略低于整体后移的写法。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortBySwap(int[] array) {
        checkArray(array);
        for (int i = 1; i < array.length; i++) {
            for (int j = i; j > 0 && array[j - 1] > array[j]; j--) {
                swap(array, j - 1, j);
            }
        }
    }

    /**
     * 二分插入排序：因为左侧区间已经有序，所以可以用二分查找快速定位插入点，
     * 再用 {@link System#arraycopy(Object, int, Object, int, int)} 一次性把后面的元素整体后移。
     *
     * <p>查找的是「第一个大于等于当前元素的位置」，这样相等元素会排在当前元素后面，排序依然是稳定的。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortByBinarySearch(int[] array) {
        checkArray(array);
        for (int i = 1; i < array.length; i++) {
            int current = array[i];
            int insertIndex = lowerBound(array, 0, i, current);
            if (insertIndex == i) {
                // 已经比左侧所有元素都大，不需要移动
                continue;
            }
            System.arraycopy(array, insertIndex, array, insertIndex + 1, i - insertIndex);
            array[insertIndex] = current;
        }
    }

    /**
     * 在有序区间 {@code [from, to)} 中二分查找「第一个大于等于 key 的下标」。
     *
     * @return 插入位置，取值区间为 {@code [from, to]}
     */
    private static int lowerBound(int[] array, int from, int to, int key) {
        int low = from;
        int high = to;
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (array[mid] < key) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把三种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {49, 38, 65, 97, 76, 13, 27, 49, -5, 0, 100, -100};
        InsertionSort insertionSort = new InsertionSort();

        System.out.println("插入排序示例：");
        SortTestSupport.printSample("直接插入版", sample, insertionSort);
        SortTestSupport.printSample("交换式插入版", sample, a -> insertionSort.sortBySwap(a));
        SortTestSupport.printSample("二分插入版", sample, a -> insertionSort.sortByBinarySearch(a));

        System.out.println("插入排序用例测试：");
        SortTestSupport.runAll("插入排序（直接插入版）", insertionSort);
        SortTestSupport.runAll("插入排序（交换式插入版）", a -> insertionSort.sortBySwap(a));
        SortTestSupport.runAll("插入排序（二分插入版）", a -> insertionSort.sortByBinarySearch(a));

        System.out.println("插入排序随机压测：");
        SortTestSupport.stress("插入排序（直接插入版）", insertionSort, 300, 20260925L);
        SortTestSupport.stress("插入排序（交换式插入版）", a -> insertionSort.sortBySwap(a), 300, 20260925L);
        SortTestSupport.stress("插入排序（二分插入版）", a -> insertionSort.sortByBinarySearch(a), 300, 20260925L);
    }
}
