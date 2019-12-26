package com.nivelle.base.algorithms.common;

import java.util.Arrays;
import java.util.Vector;

/**
 * 输入一个整数数组，实现一个函数来调整该数组中数字的顺序，使得所有的奇数位于数组的前半部分，所有的偶数位于位于数组的后半部分，并保证奇数和奇数，偶数和偶数之间的相对位置不变。
 *
 * @author fuxinzhong
 * @date 2019/09/03
 */
public class reOrderArray {

    public static void main(String[] args) {
        Integer[] arrary = new Integer[]{3, 6, 7, 9, 10, 12, 12, 23};
        reOrderArray(arrary);
    }


    public static void reOrderArray(Integer[] array) {
        Vector<Integer> odd = new Vector();
        Vector<Integer> even = new Vector();

        for (int i = 0; i < array.length; i++) {
            if ((array[i] & 0x1) == 0) {
                even.add(array[i]);
            } else {
                odd.add(array[i]);
            }
        }
        odd.addAll(even);
        for (int i = 0; i < array.length; i++) {
            array[i] = odd.get(i);
        }
        Arrays.asList(array).stream().forEach(System.out::println);
    }

    /**
     * 相邻的两个奇偶不同则交换位置
     *
     * @param array
     */
    public static void reOrderArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length - 1; j++) {
                if (array[j] % 2 == 0 && array[j + 1] % 2 != 0) {
                    int temp = array[j + 1];
                    array[j + 1] = array[j];
                    array[j] = temp;
                }
            }
        }
    }
}
