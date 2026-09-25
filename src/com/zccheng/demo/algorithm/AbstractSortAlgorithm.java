package com.zccheng.demo.algorithm;

/**
 * 排序算法的抽象父类：实现 {@link SortAlgorithm}，并把各算法都会用到的公共能力集中在这里。
 *
 * <p>这是「面向对象」中最常见的复用方式：把相同代码下沉到父类，把不同的逻辑留给子类。
 * 子类只需要关心自己的排序过程，数组校验、元素交换、有序性判断等通用代码都不必重复写。
 *
 * <p>本类声明为 {@code abstract}，故意不允许直接实例化，必须由具体算法类继承后使用。
 *
 * @see SortAlgorithm
 */
public abstract class AbstractSortAlgorithm implements SortAlgorithm {

    /**
     * 校验数组参数，避免每个实现里都重复写非空判断。
     *
     * @param array 待校验数组
     * @throws IllegalArgumentException 数组为 {@code null} 时抛出
     */
    protected static void checkArray(int[] array) {
        if (array == null) {
            throw new IllegalArgumentException("待排序数组不能为 null");
        }
    }

    /**
     * 交换数组中两个下标对应的元素。
     *
     * @param array 目标数组
     * @param i     第一个下标
     * @param j     第二个下标
     */
    protected static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    /**
     * 判断数组是否已经按升序（非递减）排好，测试与调试时很常用。
     *
     * @param array 待判断数组，可以为 {@code null}
     * @return 已升序返回 {@code true}；{@code null} 或存在逆序对返回 {@code false}
     */
    public static boolean isSorted(int[] array) {
        if (array == null) {
            return false;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }
        return true;
    }
}
