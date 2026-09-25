package com.zccheng.demo.algorithm;

import java.util.Arrays;

/**
 * 排序算法统一接口：所有的排序算法都实现它，调用方只依赖这一个抽象。
 *
 * <p>这样设计的好处：
 * <ul>
 *   <li>算法之间可以互相替换，客户端代码不用改，方便做「同一份数据换算法跑」的横向对比；</li>
 *   <li>{@link #sort(int[])} 是唯一的抽象方法，因此本接口天然是函数式接口，
 *       可以直接用 Lambda 表达式描述「某个算法的某一种实现」，测试代码写起来很短；</li>
 *   <li>公共能力（算法名、排序结果副本）用默认方法提供，实现类不用重复写。</li>
 * </ul>
 *
 * <p>接口约定：
 * <ol>
 *   <li>{@link #sort(int[])} 直接原地修改传入的数组，不再返回新数组；</li>
 *   <li>参数为 {@code null} 时抛出 {@link IllegalArgumentException}；</li>
 *   <li>统一按「升序（非递减）」排列，且只处理 {@code int} 类型；</li>
 *   <li>每个实现类都可以提供多种实现方式（最多 3 种）以及自己的 {@code main} 自测入口。</li>
 * </ol>
 *
 * @see AbstractSortAlgorithm
 * @see SortTestSupport
 */
public interface SortAlgorithm {

    /**
     * 对数组做升序排序，直接修改传入的数组。
     *
     * @param array 待排序数组，不能为 {@code null}
     */
    void sort(int[] array);

    /**
     * 算法名称，默认取实现类的类名。
     *
     * <p>之所以做成默认方法，是因为绝大多数实现类直接用类名就已经足够清晰，
     * 只有需要更友好名字的类才去重写它。
     */
    default String name() {
        return getClass().getSimpleName();
    }

    /**
     * 返回「排好序的副本」，原数组保持不变，适合不想改动调用方数据的场景。
     *
     * @param source 原始数组，不能为 {@code null}
     * @return 排序后的新数组
     */
    default int[] sortCopy(int[] source) {
        int[] copy = Arrays.copyOf(source, source.length);
        sort(copy);
        return copy;
    }
}
