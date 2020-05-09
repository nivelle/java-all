package com.nivelle.base.javacore.datastructures.base;

/**
 * System 系统默认方法
 *
 * @author nivell
 * @date 2020/04/13
 */
public class SystemDemo {

    public static void main(String[] args) {
        System.out.println("system的默认方法");

        int[] array = new int[]{1, 2, 3, 4, 5};

        int[] copy = new int[10];
        /**
         * @param      src      the source array. 数据源
         * @param      srcPos   starting position in the source array.数据源开始复制起点 index
         * @param      dest     the destination array. 目标数组
         * @param      destPos  starting position in the destination data. 目标数组设置数据起点
         * @param      length   the number of array elements to be copied. 要复制的数组数据长度
         */
        System.arraycopy(array, 3, copy, 0, 2);
        for (int i = 0; i < copy.length; i++) {
            System.err.print(copy[i]);
        }

    }
}
