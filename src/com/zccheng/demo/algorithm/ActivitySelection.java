package com.zccheng.demo.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 活动选择问题（Activity Selection Problem，也叫「区间调度问题」）的贪心算法实现。
 *
 * <p>问题描述：给定 {@code n} 个活动，每个活动用半开区间 {@code [start, end)} 表示，
 * 两个活动「相容」当且仅当它们的区间不重叠（端点相接不算重叠）。要求选出一组两两相容的活动，
 * 使得数量最多。
 *
 * <p>贪心策略：<strong>每次选择「结束时间最早」且与已选活动相容的活动。</strong>
 * 直觉是：结束得越早，给后面的活动留下的时间就越多，越不容易挡住别的活动。
 *
 * <p>为什么贪心在这里是对的（简版证明，也叫「交换论证」）：
 * <ol>
 *   <li>设所有活动中结束时间最早的是活动 {@code x}。任何一个最优解里，把它的第一个活动换成
 *       {@code x}，相容性不会被破坏（因为 {@code x} 结束得最早），数量也不变，所以一定存在一个
 *       「包含 {@code x}」的最优解——这就是「贪心选择性质」；</li>
 *   <li>选掉 {@code x} 之后，剩下的「从 {@code x} 结束之后才开始的活动里挑」仍是同构的子问题，
 *       这就是「最优子结构」。</li>
 * </ol>
 * 两者合起来，用归纳法即可证明贪心得到的是全局最优解。
 *
 * <p>复杂度：
 * <ul>
 *   <li>时间：O(n log n)，排序占主导，扫描是 O(n)；</li>
 *   <li>空间：O(n)，排序前需要复制一份列表。</li>
 * </ul>
 *
 * <p>重要提醒：贪心不是万能钥匙，关键在于「贪心选择的依据」要选对。
 * 例如把策略改成「每次选开始时间最早的活动」就是错的（本类用 {@link #selectByEarliestStart(List)}
 * 演示了反例），因为开始得早的活动可能拖得很长、挡掉很多短活动。
 *
 * <p>设计取舍：
 * <ul>
 *   <li>本类写成工具类（{@code final} + 私有构造 + 全静态方法），因为活动选择没有需要保存的状态；</li>
 *   <li>{@link Activity} 设计成不可变值对象，排序、多处引用都安全；</li>
 *   <li>对照实现用暴力枚举（指数级），只在数据规模很小（{@code n <= 20}）时用于自测，
 *       用来证明「贪心的结果确实是最优的」，而不是另写一份贪心当参考答案。</li>
 * </ul>
 *
 * @see #select(List)
 * @see #selectByEarliestStart(List)
 */
public final class ActivitySelection {

    /** 对照实现允许的最大活动数，超过后暴力枚举会指数爆炸、不划算。 */
    private static final int MAX_BRUTE_FORCE_SIZE = 20;

    /** 工具类不允许实例化。 */
    private ActivitySelection() {
    }

    /**
     * 一个活动：用半开区间 {@code [start, end)} 表示，不可变。
     *
     * <p>设计成不可变值对象，排序、在多处引用都不会被意外修改；两个活动的相容性
     * 只由它们的时间区间决定，因此不需要任何可变状态。
     */
    public static final class Activity {

        /** 开始时间（含）。 */
        private final int start;

        /** 结束时间（不含）。 */
        private final int end;

        /**
         * 构造一个活动。
         *
         * @param start 开始时间，要求 {@code start >= 0}
         * @param end   结束时间，要求 {@code end > start}
         * @throws IllegalArgumentException 时间不合法时抛出
         */
        public Activity(int start, int end) {
            if (start < 0 || end <= start) {
                throw new IllegalArgumentException(
                        "活动时间不合法：要求 start >= 0 且 end > start，实际 start=" + start + "，end=" + end);
            }
            this.start = start;
            this.end = end;
        }

        /** 返回开始时间。 */
        public int getStart() {
            return start;
        }

        /** 返回结束时间。 */
        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + ")";
        }
    }

    // ------------------------------------------------------------------
    // 对外方法
    // ------------------------------------------------------------------

    /**
     * 贪心求解活动选择问题：返回数量最多的两两相容活动。
     *
     * <p>实现步骤：先按结束时间升序排序，再从头扫描，只要当前活动的开始时间不早于
     * 上一个已选活动的结束时间（即 {@code start >= 上一个 end}），就把它选进来并更新边界。
     * 端点相接（一个活动恰好在另一个结束时开始）视为相容。
     *
     * @param activities 候选活动列表，不能为 {@code null}，也不会被修改
     * @return 选中的活动（按结束时间升序），数量为全局最优解
     */
    public static List<Activity> select(List<Activity> activities) {
        Objects.requireNonNull(activities, "活动列表不能为 null");
        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort(Comparator.comparingInt(Activity::getEnd));

        List<Activity> chosen = new ArrayList<>();
        int lastEnd = Integer.MIN_VALUE;
        for (Activity activity : sorted) {
            if (activity.getStart() >= lastEnd) {
                chosen.add(activity);
                lastEnd = activity.getEnd();
            }
        }
        return chosen;
    }

    /**
     * 按「开始时间最早」贪心（错误示范）：每次选开始最早且与已选相容的活动。
     *
     * <p>这个策略并不保证最优。反例 {@code [0, 10)}、{@code [1, 2)}、{@code [2, 3)}：
     * 按开始时间选会先拿 {@code [0, 10)}，结果只能选 1 个；而按结束时间选可以拿到
     * {@code [1, 2)} 和 {@code [2, 3)} 共 2 个。
     *
     * <p>保留这个方法纯粹是为了教学：把两种「看似都合理」的策略放到一起对比，
     * 说明贪心必须选对「当前最优」的依据，策略错了就得不到最优解。
     *
     * @param activities 候选活动列表，不能为 {@code null}，也不会被修改
     * @return 按开始时间贪心选出的活动，可能不是最优解
     */
    public static List<Activity> selectByEarliestStart(List<Activity> activities) {
        Objects.requireNonNull(activities, "活动列表不能为 null");
        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort(Comparator.comparingInt(Activity::getStart));

        List<Activity> chosen = new ArrayList<>();
        int lastEnd = Integer.MIN_VALUE;
        for (Activity activity : sorted) {
            if (activity.getStart() >= lastEnd) {
                chosen.add(activity);
                lastEnd = activity.getEnd();
            }
        }
        return chosen;
    }

    /**
     * 暴力枚举参照实现：返回「最大相容子集」的规模，用来对照验证贪心结果确实是最优的。
     *
     * <p>做法是枚举全部 {@code 2^n} 个子集，对每个子集检查是否两两相容，
     * 记录其中规模最大的一个。时间 O(n · 2^n)，因此只适用于小规模数据。
     *
     * @param activities 候选活动列表，不能为 {@code null}
     * @return 最大相容子集的活动数量
     * @throws IllegalArgumentException 活动数超过 {@value #MAX_BRUTE_FORCE_SIZE} 时抛出
     */
    public static int maxCompatibleCount(List<Activity> activities) {
        Objects.requireNonNull(activities, "活动列表不能为 null");
        if (activities.size() > MAX_BRUTE_FORCE_SIZE) {
            throw new IllegalArgumentException(
                    "暴力枚举只支持不超过 " + MAX_BRUTE_FORCE_SIZE + " 个活动，实际 " + activities.size() + " 个");
        }
        int best = 0;
        for (int mask = 0; mask < (1 << activities.size()); mask++) {
            if (isCompatibleSubset(activities, mask)) {
                best = Math.max(best, Integer.bitCount(mask));
            }
        }
        return best;
    }

    // ------------------------------------------------------------------
    // 内部实现
    // ------------------------------------------------------------------

    /** 判断两个半开区间 {@code [start, end)} 是否重叠（端点相接不算重叠）。 */
    private static boolean overlaps(Activity a, Activity b) {
        return a.getStart() < b.getEnd() && b.getStart() < a.getEnd();
    }

    /** 判断某个子集（用位掩码表示）内的活动是否两两相容。 */
    private static boolean isCompatibleSubset(List<Activity> activities, int mask) {
        for (int i = 0; i < activities.size(); i++) {
            if ((mask & (1 << i)) == 0) {
                continue;
            }
            for (int j = i + 1; j < activities.size(); j++) {
                if ((mask & (1 << j)) != 0 && overlaps(activities.get(i), activities.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 判断一组活动是否两两相容，用作对贪心结果的额外检查。 */
    private static boolean isPairwiseCompatible(List<Activity> activities) {
        for (int i = 0; i < activities.size(); i++) {
            for (int j = i + 1; j < activities.size(); j++) {
                if (overlaps(activities.get(i), activities.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 把活动列表格式化成一行字符串。 */
    private static String format(List<Activity> activities) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < activities.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(activities.get(i));
        }
        return builder.append(']').toString();
    }

    /**
     * 校验贪心结果：既要比得上暴力枚举的最优规模，又要保证选出的活动确实两两相容。
     *
     * @param caseName   用例名称
     * @param activities 输入活动列表
     */
    private static void verify(String caseName, List<Activity> activities) {
        List<Activity> chosen = select(activities);
        int greedyCount = chosen.size();
        int optimalCount = maxCompatibleCount(activities);
        if (greedyCount != optimalCount) {
            throw new AssertionError("用例「" + caseName + "」贪心结果不是最优：贪心选 " + greedyCount
                    + " 个，最优应为 " + optimalCount + " 个");
        }
        if (!isPairwiseCompatible(chosen)) {
            throw new AssertionError("用例「" + caseName + "」选出的活动存在重叠：" + format(chosen));
        }
    }

    // ------------------------------------------------------------------
    // 自测入口
    // ------------------------------------------------------------------

    /**
     * 自测入口：先打印经典示例与反例对比，再跑标准用例，最后用随机数据 + 暴力枚举对照压测。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        // 一、经典示例（《算法导论》16.1 的活动选择例子，最优解规模为 4）
        List<Activity> sample = Arrays.asList(
                new Activity(1, 4), new Activity(3, 5), new Activity(0, 6),
                new Activity(5, 7), new Activity(3, 9), new Activity(5, 9),
                new Activity(6, 10), new Activity(8, 11), new Activity(8, 12),
                new Activity(2, 14), new Activity(12, 16));

        System.out.println("活动选择问题示例：");
        System.out.println("  全部活动（乱序）：" + format(sample));
        List<Activity> chosen = select(sample);
        System.out.println("  按结束时间贪心选中 " + chosen.size() + " 个：" + format(chosen));

        // 二、反例对比：按开始时间贪心会失败
        List<Activity> counterExample = Arrays.asList(
                new Activity(0, 10), new Activity(1, 2), new Activity(2, 3));
        System.out.println("  反例演示（" + format(counterExample) + "）：");
        System.out.println("    按开始时间贪心选 " + selectByEarliestStart(counterExample).size()
                + " 个：" + format(selectByEarliestStart(counterExample)));
        System.out.println("    按结束时间贪心选 " + select(counterExample).size()
                + " 个：" + format(select(counterExample)));

        // 三、标准用例：每个都用暴力枚举对照
        System.out.println("活动选择问题用例测试：");
        verify("空列表", new ArrayList<Activity>());
        verify("单个活动", Arrays.asList(new Activity(1, 4)));
        verify("全部相容（可全选）", Arrays.asList(
                new Activity(0, 2), new Activity(2, 4), new Activity(4, 6)));
        verify("全部重叠（只能选 1 个）", Arrays.asList(
                new Activity(0, 10), new Activity(1, 9), new Activity(2, 8)));
        verify("端点相接", Arrays.asList(
                new Activity(5, 7), new Activity(1, 3), new Activity(3, 5)));
        verify("经典例子", sample);
        verify("交错重叠", Arrays.asList(
                new Activity(1, 3), new Activity(2, 5), new Activity(4, 6),
                new Activity(5, 8), new Activity(7, 9)));
        System.out.println("  [通过] 活动选择：7 个标准用例全部通过（含暴力枚举对照）");

        // 四、随机压测：随机生成小规模活动，与暴力枚举逐一对照
        System.out.println("活动选择问题随机压测：");
        stress(300, 20260925L);
    }

    /**
     * 随机压测：随机生成活动列表，用暴力枚举对照验证贪心结果最优。
     *
     * @param rounds 压测轮数
     * @param seed   随机种子，固定种子便于复现问题
     */
    private static void stress(int rounds, long seed) {
        Random random = new Random(seed);
        for (int round = 1; round <= rounds; round++) {
            int size = random.nextInt(17); // 0 ~ 16，保证暴力枚举可行
            List<Activity> activities = randomActivities(size, random.nextLong());
            verify("随机压测第 " + round + " 轮（" + size +





                    " 个活动）", activities);
        }
        System.out.println("  [通过] 活动选择：随机压测 " + rounds + " 轮全部通过（含暴力枚举对照）");
    }

    /** 生成一组随机活动：开始时间在 {@code [0, 100)}，持续长度在 {@code [1, 20]}。 */
    private static List<Activity> randomActivities(int size, long seed) {
        Random random = new Random(seed);
        List<Activity> activities = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int start = random.nextInt(100);
            int length = random.nextInt(20) + 1;
            activities.add(new Activity(start, start + length));
        }
        return activities;
    }
}
