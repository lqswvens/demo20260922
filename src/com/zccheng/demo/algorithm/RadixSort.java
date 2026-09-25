package com.zccheng.demo.algorithm;

/**
 * 基数排序：按「位」排序，先按个位把数据分到 10 个桶里，再按十位、百位……重复，
 * 每一轮分桶都是稳定排序，跑完最高位之后整体就有序了。
 *
 * <p>为什么按位排完就整体有序：所有数据都按「低位到高位」的顺序参与排序，
 * 高位相同的元素会保留上一轮（更低一位）已经排好的相对顺序，这就是逐位稳定带来的叠加效果。
 *
 * <p>复杂度（{@code d} 为最大数的位数，{@code n} 为元素个数，{@code r} 为基数，这里取 10）：
 * <ul>
 *   <li>时间：O(d * (n + r))，对位数固定的整数几乎可以看作线性；</li>
 *   <li>空间：O(n + r)，需要等长的桶缓冲区以及 10 个计数器；</li>
 *   <li>稳定性：每轮分桶必须稳定，否则结果会错，这是基数排序的关键前提。</li>
 * </ul>
 *
 * <p>本类提供 2 种实现方式：
 * <ol>
 *   <li>{@link #sort(int[])}：LSD（最低位优先），从个位一直排到最高位，写法统一、不用递归，工程上最常用；</li>
 *   <li>{@link #sortMsd(int[])}：MSD（最高位优先），先按最高位分成 10 个桶，再对每个桶递归处理下一位，
 *       思路和「字典序」一致，桶内元素变少时能提前结束递归。</li>
 * </ol>
 *
 * <p>负数怎么处理：排序前统一加上 {@code -min} 这个偏移量，把整个值域平移到非负区间，
 * 排完再减回去。这样不必为符号位单独写分支，也避免了「负数取模得到负值」的坑。
 */
public class RadixSort extends AbstractSortAlgorithm {

    /** 十进制基数：每一位取值 0~9，共 10 个桶。 */
    private static final int RADIX = 10;

    /**
     * LSD（最低位优先）基数排序：从个位开始，依次按每一位做一轮「计数 + 稳定分桶」。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    @Override
    public void sort(int[] array) {
        checkArray(array);
        if (array.length < 2) {
            return;
        }

        long offset = offsetOf(array);
        long maxShifted = shiftedMax(array, offset);

        long[] source = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            source[i] = array[i] + offset;
        }
        long[] target = new long[array.length];

        for (long exp = 1; exp <= maxShifted; exp *= RADIX) {
            distributeByDigit(source, target, exp);
            // 交换两个数组的引用，下一轮直接往另一个数组写，省去每轮的数据拷贝
            long[] temp = source;
            source = target;
            target = temp;
        }

        for (int i = 0; i < array.length; i++) {
            array[i] = (int) (source[i] - offset);
        }
    }

    /**
     * MSD（最高位优先）基数排序：先按最高位分桶，同一桶内元素的高位已经相同，
     * 于是对每个桶继续按「下一位」递归分桶，直到所有位都比完。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    public void sortMsd(int[] array) {
        checkArray(array);
        if (array.length < 2) {
            return;
        }

        long offset = offsetOf(array);
        long maxShifted = shiftedMax(array, offset);

        long[] data = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            data[i] = array[i] + offset;
        }

        // 找到最高位对应的权值：最大值是 638 时，exp 为 100
        long exp = 1;
        while (exp * RADIX <= maxShifted) {
            exp *= RADIX;
        }

        msdSort(data, new long[data.length], 0, data.length - 1, exp);

        for (int i = 0; i < array.length; i++) {
            array[i] = (int) (data[i] - offset);
        }
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    /**
     * LSD 的一轮分桶：按 {@code exp} 指定的那一位，把 {@code source} 的元素稳定地放入 {@code target}。
     *
     * @param exp 当前处理的权值：1 表示个位，10 表示十位，以此类推
     */
    private static void distributeByDigit(long[] source, long[] target, long exp) {
        int[] count = new int[RADIX];
        for (long value : source) {
            count[digitOf(value, exp)]++;
        }

        // 把「出现次数」转换成「每个桶在目标数组中的起始下标」
        int[] start = new int[RADIX];
        for (int digit = 1; digit < RADIX; digit++) {
            start[digit] = start[digit - 1] + count[digit - 1];
        }

        for (long value : source) {
            target[start[digitOf(value, exp)]++] = value;
        }
    }

    /**
     * MSD 的递归分桶：对区间 {@code [low, high]} 按 {@code exp} 位分桶，再逐个桶递归处理下一位。
     *
     * @param data 数据数组（已平移到非负区间）
     * @param aux  辅助数组，长度与 {@code data} 相同
     * @param low  区间左边界（含）
     * @param high 区间右边界（含）
     * @param exp  当前处理的权值，为 0 表示所有位都处理完了
     */
    private static void msdSort(long[] data, long[] aux, int low, int high, long exp) {
        if (high - low < 1 || exp == 0) {
            // 区间只剩 0~1 个元素，或者所有位都比完了
            return;
        }

        int[] start = new int[RADIX + 1];
        for (int i = low; i <= high; i++) {
            start[digitOf(data[i], exp) + 1]++;
        }
        for (int digit = 0; digit < RADIX; digit++) {
            start[digit + 1] += start[digit];
        }

        int[] cursor = new int[RADIX];
        for (int i = low; i <= high; i++) {
            int bucket = digitOf(data[i], exp);
            aux[cursor[bucket] + start[bucket]] = data[i];
            cursor[bucket]++;
        }
        // 辅助数组里这段数据是从下标 0 开始排好的，整体搬回原数组
        System.arraycopy(aux, 0, data, low, high - low + 1);

        for (int digit = 0; digit < RADIX; digit++) {
            int bucketLow = low + start[digit];
            int bucketHigh = low + start[digit + 1] - 1;
            msdSort(data, aux, bucketLow, bucketHigh, exp / RADIX);
        }
    }

    /**
     * 计算把数组平移到非负区间所需的偏移量。
     *
     * @return 最小值为负数时返回 {@code -min}，否则返回 0
     */
    private static long offsetOf(int[] array) {
        int min = array[0];
        for (int value : array) {
            if (value < min) {
                min = value;
            }
        }
        return min < 0 ? -(long) min : 0L;
    }

    /** 计算平移之后的最大值，用来决定需要处理多少位。 */
    private static long shiftedMax(int[] array, long offset) {
        int max = array[0];
        for (int value : array) {
            if (value > max) {
                max = value;
            }
        }
        return (long) max + offset;
    }

    /**
     * 取出数值在 {@code exp} 这一位上的数字。
     *
     * @param value 已平移到非负区间的数值
     * @param exp   权值（1、10、100……）
     * @return 0~9 之间的数字
     */
    private static int digitOf(long value, long exp) {
        return (int) (value / exp % RADIX);
    }

    /**
     * 自测入口：先打印示例的排序前后对比，再把两种实现都跑一遍标准用例。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        int[] sample = {170, 45, 75, 90, 802, 24, 2, 66, -13, 0};
        RadixSort radixSort = new RadixSort();

        System.out.println("基数排序示例：");
        SortTestSupport.printSample("LSD 最低位优先版", sample, radixSort);
        SortTestSupport.printSample("MSD 最高位优先版", sample, a -> radixSort.sortMsd(a));

        System.out.println("基数排序用例测试：");
        SortTestSupport.runAll("基数排序（LSD 最低位优先版）", radixSort);
        SortTestSupport.runAll("基数排序（MSD 最高位优先版）", a -> radixSort.sortMsd(a));

        System.out.println("基数排序随机压测：");
        SortTestSupport.stress("基数排序（LSD 最低位优先版）", radixSort, 300, 20260925L);
        SortTestSupport.stress("基数排序（MSD 最高位优先版）", a -> radixSort.sortMsd(a), 300, 20260925L);
    }
}
