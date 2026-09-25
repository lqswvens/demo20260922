package com.zccheng.demo.algorithm;

/**
 * 归并排序：分治思想，把数组一分为二分别排序，再把两个有序子数组合并成一个有序数组。
 *
 * <p>核心步骤：
 * <ol>
 *   <li>分割：把区间二分，直到每个子区间只剩一个元素（单个元素天然有序）；</li>
 *   <li>归并：把两个有序子区间「合并」成一个有序区间，合并时借助一个辅助数组暂存数据；</li>
 *   <li>合并时相等元素优先取左边，从而保证排序是稳定的。</li>
 * </ol>
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：任何情况下都是 O(n log n)，分治层数固定为 log n，每层合并总共扫描 n 个元素；</li>
 *   <li>空间：O(n)，需要一份和原数组等长的辅助数组（这是它相对快排的主要代价）；</li>
 *   <li>稳定性：稳定，是常见 O(n log n) 算法里少数稳定的实现。</li>
 * </ul>
 *
 * <p>本类提供 2 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：自顶向下递归，从大区间不断二分，最常见、最贴合分治描述；</li>
 *   <li>{@link #sortBottomUp(int[])}：自底向上迭代，先合并长度为 1 的相邻区间，再合并长度为 2、4、8……，
 *       没有递归调用开销，也不受递归深度限制。</li>
 * </ol>
 */
public class MergeSort extends AbstractSortAlgorithm {

    /**
     * 自顶向下递归版归并排序。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        if (array.length < 2) {
            return;
        }
        int[] auxiliary = new int[array.length];
        sortTopDown(array, auxiliary, 0, array.length - 1);
    }

    /**
     * 自底向上迭代版归并排序：区间长度从 1 开始成倍增长，每一轮把相邻两个有序区间合并。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortBottomUp(int[] array) {
        checkArray(array);
        int length = array.length;
        if (length < 2) {
            return;
        }
        int[] auxiliary = new int[length];

        for (int width = 1; width < length; width *= 2) {
            for (int low = 0; low + width < length; low += 2 * width) {
                int mid = low + width - 1;
                // 最后一组可能凑不满两个区间，右边界需要截断到数组末尾
                int high = Math.min(low + 2 * width - 1, length - 1);
                merge(array, auxiliary, low, mid, high);
            }
        }
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    /** 递归排序区间 {@code [low, high]}。 */
    private static void sortTopDown(int[] array, int[] auxiliary, int low, int high) {
        if (low >= high) {
            // 区间长度不超过 1，天然有序
            return;
        }
        int mid = (low + high) >>> 1;
        sortTopDown(array, auxiliary, low, mid);
        sortTopDown(array, auxiliary, mid + 1, high);
        if (array[mid] <= array[mid + 1]) {
            // 左右两段已经整体有序（左段最大值不超过右段最小值），无需合并
            return;
        }
        merge(array, auxiliary, low, mid, high);
    }

    /**
     * 合并两个相邻的有序区间：{@code [low, mid]} 与 {@code [mid + 1, high]}。
     *
     * <p>先把整段数据复制到辅助数组，再按大小「二路归并」回原数组。
     * 当两个元素相等时优先取左段的元素，这一条保证了排序的稳定性。
     */
    private static void merge(int[] array, int[] auxiliary, int low, int mid, int high) {
        System.arraycopy(array, low, auxiliary, low, high - low + 1);

        int left = low;
        int right = mid + 1;
        for (int k = low; k <= high; k++) {
            if (left > mid) {
                // 左段取完，只能取右段
                array[k] = auxiliary[right++];
            } else if (right > high) {
                // 右段取完，只能取左段
                array[k] = auxiliary[left++];
            } else if (auxiliary[right] < auxiliary[left]) {
                array[k] = auxiliary[right++];
            } else {
                // 相等时先取左段，保证稳定
                array[k] = auxiliary[left++];
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
        MergeSort mergeSort = new MergeSort();

        System.out.println("归并排序示例：");
        SortTestSupport.printSample("自顶向下递归版", sample, mergeSort);
        SortTestSupport.printSample("自底向上迭代版", sample, a -> mergeSort.sortBottomUp(a));

        System.out.println("归并排序用例测试：");
        SortTestSupport.runAll("归并排序（自顶向下递归版）", mergeSort);
        SortTestSupport.runAll("归并排序（自底向上迭代版）", a -> mergeSort.sortBottomUp(a));

        System.out.println("归并排序随机压测：");
        SortTestSupport.stress("归并排序（自顶向下递归版）", mergeSort, 300, 20260925L);
        SortTestSupport.stress("归并排序（自底向上迭代版）", a -> mergeSort.sortBottomUp(a), 300, 20260925L);
    }
}
