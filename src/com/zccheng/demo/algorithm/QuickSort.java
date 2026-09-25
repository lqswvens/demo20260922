package com.zccheng.demo.algorithm;

/**
 * 快速排序：分治思想的代表算法，选一个基准值，把数组分成「小于基准」和「大于等于基准」两部分，再分别递归。
 *
 * <p>核心步骤（分治三部曲）：
 * <ol>
 *   <li>分区（partition）：选定基准，把元素按与基准的大小关系交换到基准两侧，返回基准的最终下标；</li>
 *   <li>递归：对基准左侧和右侧的子数组分别排序；</li>
 *   <li>递归出口：子数组长度为 0 或 1 时天然有序，直接返回。</li>
 * </ol>
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：平均 O(n log n)；最坏 O(n^2)（分区极不平衡，例如每次都选到最值作为基准）；</li>
 *   <li>空间：O(log n) 的递归栈（采用「先递归较短一侧」的技巧后，栈深度可以控制在 O(log n)）；</li>
 *   <li>稳定性：不稳定，分区过程中的交换会跨过相等元素。</li>
 * </ul>
 *
 * <p>本类提供 3 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：单路分区（Lomuto 方案），以末尾元素为基准，最容易理解，
 *       配合「三数取中」避免在已排序数据上退化；</li>
 *   <li>{@link #sortHoare(int[])}：双指针分区（Hoare 方案），左右指针向中间夹逼并交换，
 *       交换次数更少，分区更均衡，是很多标准库的实现思路；</li>
 *   <li>{@link #sortThreeWay(int[])}：三路分区（荷兰国旗问题），把区间分成「小于、等于、大于」三段，
 *       大量重复元素时接近 O(n)，正好补上单路分区的短板。</li>
 * </ol>
 */
public class QuickSort extends AbstractSortAlgorithm {

    /**
     * 单路分区版（Lomuto）：以末尾元素为基准，小于基准的往前挪，最后把基准放到分界点上。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        quickSortLomuto(array, 0, array.length - 1);
    }

    /**
     * 双指针分区版（Hoare）：左右指针分别从两端向中间扫描，
     * 左边遇到不小于基准的、右边遇到不大于基准的就交换，相遇处即为分界点。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortHoare(int[] array) {
        checkArray(array);
        quickSortHoare(array, 0, array.length - 1);
    }

    /**
     * 三路分区版（荷兰国旗）：把区间分成「小于基准」「等于基准」「大于基准」三段，
     * 等于基准的一批元素会被一次性放到最终位置上，不再参与后续递归。
     *
     * <p>当数组中重复元素很多时，单路和双指针分区都会退化成 O(n^2)，
     * 而三路分区每轮只处理严格小于和严格大于的部分，性能接近 O(n)。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortThreeWay(int[] array) {
        checkArray(array);
        quickSortThreeWay(array, 0, array.length - 1);
    }

    // ------------------------------------------------------------------
    // 单路分区（Lomuto）
    // ------------------------------------------------------------------

    /**
     * 单路分区的递归实现：每轮确定一个基准元素的最终位置，
     * 并且先递归较长的一侧、后处理较短的一侧，把递归转化为循环来压栈深。
     */
    private static void quickSortLomuto(int[] array, int low, int high) {
        while (low < high) {
            int pivotIndex = partitionLomuto(array, low, high);
            if (pivotIndex - low < high - pivotIndex) {
                // 左侧更短：先递归左侧，再循环处理右侧
                quickSortLomuto(array, low, pivotIndex - 1);
                low = pivotIndex + 1;
            } else {
                // 右侧更短：先递归右侧，再循环处理左侧
                quickSortLomuto(array, pivotIndex + 1, high);
                high = pivotIndex - 1;
            }
        }
    }

    /**
     * Lomuto 分区：把小于基准的元素依次换到区间左侧，最后把基准换到分界点。
     *
     * @return 基准元素的最终下标
     */
    private static int partitionLomuto(int[] array, int low, int high) {
        int mid = (low + high) >>> 1;
        medianOfThree(array, low, mid, high);
        swap(array, mid, high);

        int pivot = array[high];
        int storeIndex = low;
        for (int i = low; i < high; i++) {
            if (array[i] < pivot) {
                swap(array, i, storeIndex);
                storeIndex++;
            }
        }
        swap(array, storeIndex, high);
        return storeIndex;
    }

    // ------------------------------------------------------------------
    // 双指针分区（Hoare）
    // ------------------------------------------------------------------

    /** 双指针分区的递归实现，同样采用「先递归较短一侧」来限制递归深度。 */
    private static void quickSortHoare(int[] array, int low, int high) {
        while (low < high) {
            int splitIndex = partitionHoare(array, low, high);
            if (splitIndex - low < high - splitIndex - 1) {
                quickSortHoare(array, low, splitIndex);
                low = splitIndex + 1;
            } else {
                quickSortHoare(array, splitIndex + 1, high);
                high = splitIndex;
            }
        }
    }

    /**
     * Hoare 分区：取中间元素的值作为基准（值本身留在原处也可以），
     * 左右指针分别向中间扫描并交换，返回左半部分的最后一个下标。
     *
     * <p>由于基准值一定存在于数组中，两侧扫描最远只会停在基准元素所在的位置，
     * 不会出现数组越界，因此不需要额外判断边界的哨兵。
     *
     * @return 分界点下标，左半部分为 {@code [low, splitIndex]}，右半部分为 {@code [splitIndex + 1, high]}
     */
    private static int partitionHoare(int[] array, int low, int high) {
        int pivot = array[(low + high) >>> 1];
        int i = low - 1;
        int j = high + 1;

        while (true) {
            do {
                i++;
            } while (array[i] < pivot);
            do {
                j--;
            } while (array[j] > pivot);

            if (i >= j) {
                return j;
            }
            swap(array, i, j);
        }
    }

    // ------------------------------------------------------------------
    // 三路分区（荷兰国旗）
    // ------------------------------------------------------------------

    /**
     * 三路分区：维护三个指针，把区间整理成「小于基准 | 等于基准 | 大于基准」三段，
     * 其中「等于基准」的一段已经处于最终位置，只需递归剩下两段。
     *
     * @param array 目标数组
     * @param low   区间左边界（含）
     * @param high  区间右边界（含）
     */
    private static void quickSortThreeWay(int[] array, int low, int high) {
        if (low >= high) {
            return;
        }

        int pivot = array[(low + high) >>> 1];
        // lt 指向「小于区」的下一个空位，gt 指向「大于区」的前一个空位，i 是当前扫描位置
        int lt = low;
        int i = low;
        int gt = high;

        while (i <= gt) {
            if (array[i] < pivot) {
                swap(array, lt, i);
                lt++;
                i++;
            } else if (array[i] > pivot) {
                swap(array, i, gt);
                gt--;
                // 换过来的元素还没检查过，i 不能自增
            } else {
                i++;
            }
        }

        quickSortThreeWay(array, low, lt - 1);
        quickSortThreeWay(array, gt + 1, high);
    }

    // ------------------------------------------------------------------
    // 公共小工具
    // ------------------------------------------------------------------

    /**
     * 三数取中：把 {@code low}、{@code mid}、{@code high} 三个位置上的值排成
     * {@code array[low] <= array[mid] <= array[high]}，这样 {@code mid} 上就是中位数。
     *
     * <p>作用：避免对「已经有序」或「完全逆序」的数组总是选到最值作为基准，从而退化到 O(n^2)。
     */
    private static void medianOfThree(int[] array, int low, int mid, int high) {
        if (array[mid] < array[low]) {
            swap(array, low, mid);
        }
        if (array[high] < array[mid]) {
            swap(array, mid, high);
        }
        if (array[mid] < array[low]) {
            swap(array, low, mid);
        }
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把三种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {49, 38, 65, 97, 76, 13, 27, 49, -5, 0, 100, -100};
        QuickSort quickSort = new QuickSort();

        System.out.println("快速排序示例：");
        SortTestSupport.printSample("单路分区版（Lomuto）", sample, quickSort);
        SortTestSupport.printSample("双指针分区版（Hoare）", sample, a -> quickSort.sortHoare(a));
        SortTestSupport.printSample("三路分区版（荷兰国旗）", sample, a -> quickSort.sortThreeWay(a));

        System.out.println("快速排序用例测试：");
        SortTestSupport.runAll("快速排序（单路分区版）", quickSort);
        SortTestSupport.runAll("快速排序（双指针分区版）", a -> quickSort.sortHoare(a));
        SortTestSupport.runAll("快速排序（三路分区版）", a -> quickSort.sortThreeWay(a));

        System.out.println("快速排序随机压测：");
        SortTestSupport.stress("快速排序（单路分区版）", quickSort, 300, 20260925L);
        SortTestSupport.stress("快速排序（双指针分区版）", a -> quickSort.sortHoare(a), 300, 20260925L);
        SortTestSupport.stress("快速排序（三路分区版）", a -> quickSort.sortThreeWay(a), 300, 20260925L);
    }
}
