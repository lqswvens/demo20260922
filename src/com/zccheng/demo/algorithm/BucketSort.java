package com.zccheng.demo.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 桶排序：按数值大小把元素分到若干「桶」里，桶内各自排序，最后按桶的顺序依次拼回数组。
 *
 * <p>核心步骤：
 * <ol>
 *   <li>找出值域 {@code [min, max]}，把它均匀切成 {@code k} 段，每段对应一个桶；</li>
 *   <li>把元素按 {@code (value - min) * k / (max - min + 1)} 放进对应桶里；</li>
 *   <li>桶内单独排序（桶内元素通常很少，插入排序就足够快）；</li>
 *   <li>按桶的先后顺序写回原数组，结果自然是升序。</li>
 * </ol>
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：数据在值域上分布均匀时接近 O(n + k)；如果所有数据都落进同一个桶，
 *       就退化成桶内排序的复杂度（用插入排序即 O(n^2)）；</li>
 *   <li>空间：O(n + k)，需要额外的桶来存放数据；</li>
 *   <li>稳定性：取决于桶内排序算法，本类默认使用的插入排序是稳定的。</li>
 * </ul>
 *
 * <p>本类提供 2 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：桶数量取 {@code sqrt(n)} 自适应，桶内使用插入排序，是推荐写法；</li>
 *   <li>{@link #sort(int[], int)}：桶数量由调用方指定，桶内直接使用 JDK 的 {@link Arrays#sort(int[])}，
 *       写法最简单，也方便做「桶数量对性能影响」的对比实验。</li>
 * </ol>
 */
public class BucketSort extends AbstractSortAlgorithm {

    /** 桶数量上限，防止调用方传入过大的值造成内存浪费。 */
    public static final int MAX_BUCKET_COUNT = 1_000_000;

    /**
     * 默认桶排序：桶数量取 {@code sqrt(n)}，桶内使用插入排序。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        if (array.length < 2) {
            return;
        }

        int bucketCount = Math.max(1, (int) Math.sqrt(array.length));
        List<int[]> buckets = scatter(array, bucketCount);

        // 桶内元素一般很少，插入排序在小数组上没有递归开销，实际比通用排序更快
        for (int[] bucket : buckets) {
            insertionSort(bucket);
        }
        gather(array, buckets);
    }

    /**
     * 指定桶数量的桶排序：桶内直接调用 JDK 的排序实现。
     *
     * @param array       待排序数组，不能为 {@code null}
     * @param bucketCount 桶数量，必须位于 {@code 1 ~ MAX_BUCKET_COUNT}
     */
    public void sort(int[] array, int bucketCount) {
        checkArray(array);
        if (bucketCount <= 0 || bucketCount > MAX_BUCKET_COUNT) {
            throw new IllegalArgumentException("桶数量必须在 1 ~ " + MAX_BUCKET_COUNT + " 之间，当前值：" + bucketCount);
        }
        if (array.length < 2) {
            return;
        }

        List<int[]> buckets = scatter(array, bucketCount);
        for (int[] bucket : buckets) {
            Arrays.sort(bucket);
        }
        gather(array, buckets);
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    /**
     * 分桶：按值域把元素均匀映射到 {@code bucketCount} 个桶中。
     *
     * <p>用「计数 + 回填」两步完成分配，这样桶内元素能保持原有相对顺序，整体也就保持了稳定性。
     *
     * @return 桶列表
     */
    private static List<int[]> scatter(int[] array, int bucketCount) {
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
        int[] counters = new int[bucketCount];
        int[] bucketIndexes = new int[array.length];

        for (int i = 0; i < array.length; i++) {
            // 用 long 参与运算，避免大值域下 value - min 溢出；结果一定落在 [0, bucketCount)
            int bucketIndex = (int) (((long) array[i] - min) * bucketCount / range);
            bucketIndexes[i] = bucketIndex;
            counters[bucketIndex]++;
        }

        List<int[]> buckets = new ArrayList<>(bucketCount);
        for (int i = 0; i < bucketCount; i++) {
            buckets.add(new int[counters[i]]);
        }

        int[] cursors = new int[bucketCount];
        for (int i = 0; i < array.length; i++) {
            int bucketIndex = bucketIndexes[i];
            buckets.get(bucketIndex)[cursors[bucketIndex]++] = array[i];
        }
        return buckets;
    }

    /** 按桶的顺序把数据写回原数组。 */
    private static void gather(int[] array, List<int[]> buckets) {
        int index = 0;
        for (int[] bucket : buckets) {
            System.arraycopy(bucket, 0, array, index, bucket.length);
            index += bucket.length;
        }
    }

    /** 桶内插入排序：小数组上常数很小，非常适合作为桶内排序。 */
    private static void insertionSort(int[] array) {
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
     * 自测入口：先打印示例的排序前后对比，再把两种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {49, 38, 65, 97, 76, 13, 27, 49, -5, 0, 100, -100};
        BucketSort bucketSort = new BucketSort();

        System.out.println("桶排序示例：");
        SortTestSupport.printSample("默认桶数 sqrt(n) + 桶内插入排序", sample, bucketSort);
        SortTestSupport.printSample("指定 5 个桶 + 桶内 JDK 排序", sample, a -> bucketSort.sort(a, 5));

        System.out.println("桶排序用例测试：");
        SortTestSupport.runAll("桶排序（默认桶数 + 桶内插入排序）", bucketSort);
        SortTestSupport.runAll("桶排序（指定桶数 + 桶内 JDK 排序）", a -> bucketSort.sort(a, 64));

        System.out.println("桶排序随机压测：");
        SortTestSupport.stress("桶排序（默认桶数 + 桶内插入排序）", bucketSort, 300, 20260925L);
        SortTestSupport.stress("桶排序（指定桶数 + 桶内 JDK 排序）", a -> bucketSort.sort(a, 64), 300, 20260925L);
    }
}
