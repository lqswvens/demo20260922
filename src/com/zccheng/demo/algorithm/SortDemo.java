package com.zccheng.demo.algorithm;

import java.util.Arrays;

/**
 * 排序算法总览 Demo：把所有算法放到一起，用一个入口完成正确性验证和性能对比。
 *
 * <p>演示内容：
 * <ol>
 *   <li>正确性：对每个算法跑一遍 {@link SortTestSupport#runAll(String, SortAlgorithm)} 标准用例；</li>
 *   <li>性能：让所有算法排同一份数据，打印耗时，直观感受 O(n^2) 与 O(n log n) 的差距；</li>
 *   <li>数据特征的影响：再用一份「近乎有序」的数据跑一次，可以看到插入排序、冒泡优化版
 *       在近似有序数据上的优势，以及快排在最坏输入下的表现。</li>
 * </ol>
 *
 * <p>面向对象的用法：这里统一用 {@link SortAlgorithm} 数组持有所有算法，
 * 循环里只调用接口方法，新增算法时不需要修改这段遍历逻辑。
 * 计数排序的基础版不接受负数，所以用匿名内部类把「带偏移量版」包装成统一的接口实现来参与对比。
 */
public final class SortDemo {

    /** 性能对比使用的数据规模。 */
    private static final int BENCHMARK_SIZE = 3000;

    /** 固定随机种子，保证每次运行的数据一致。 */
    private static final long BENCHMARK_SEED = 20260925L;

    private SortDemo() {
    }

    public static void main(String[] args) {
        SortAlgorithm[] algorithms = createAlgorithms();

        System.out.println("========== 一、正确性验证：每个算法跑一遍标准用例 ==========");
        for (SortAlgorithm algorithm : algorithms) {
            SortTestSupport.runAll(algorithm.name(), algorithm);
        }

        System.out.println();
        System.out.println("========== 二、性能对比：随机数据 ==========");
        compareWith("随机数据", SortTestSupport.randomArray(BENCHMARK_SIZE, BENCHMARK_SEED), algorithms);

        System.out.println("========== 三、性能对比：近乎有序的数据 ==========");
        compareWith("近乎有序数据", SortTestSupport.nearlySortedArray(BENCHMARK_SIZE, BENCHMARK_SEED), algorithms);
    }

    /**
     * 构造算法列表：一个接口数组装下所有实现，遍历时只依赖接口，不关心具体类型。
     */
    private static SortAlgorithm[] createAlgorithms() {
        CountingSort countingSort = new CountingSort();

        return new SortAlgorithm[]{
                new BubbleSort(),
                new SelectionSort(),
                new InsertionSort(),
                new ShellSort(),
                new QuickSort(),
                new MergeSort(),
                new HeapSort(),
                new CombSort(),
                new BucketSort(),
                new RadixSort(),
                // 匿名内部类：把计数排序的「带偏移量版」适配成统一接口，演示默认方法 name() 的重写
                new SortAlgorithm() {
                    @Override
                    public void sort(int[] array) {
                        countingSort.sortWithOffset(array);
                    }

                    @Override
                    public String name() {
                        return "CountingSort（带偏移量版）";
                    }
                }
        };
    }

    /**
     * 让所有算法排同一份数据，打印耗时；顺带校验结果是否正确，避免「跑得快但结果是错的」。
     *
     * @param title      场景标题
     * @param data       基准数据，每个算法都会拿到它的一份副本
     * @param algorithms 参与对比的算法
     */
    private static void compareWith(String title, int[] data, SortAlgorithm[] algorithms) {
        System.out.println(title + "，数据规模：" + data.length);
        for (SortAlgorithm algorithm : algorithms) {
            int[] target = Arrays.copyOf(data, data.length);
            long startTime = System.nanoTime();
            algorithm.sort(target);
            double costMillis = (System.nanoTime() - startTime) / 1_000_000.0;

            if (!AbstractSortAlgorithm.isSorted(target)) {
                throw new AssertionError(algorithm.name() + " 的排序结果不正确");
            }
            System.out.println("  " + algorithm.name() + " 耗时 " + String.format("%.2f", costMillis) + " ms");
        }
        System.out.println();
    }
}
