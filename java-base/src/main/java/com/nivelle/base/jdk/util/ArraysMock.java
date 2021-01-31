package com.nivelle.base.jdk.util;

import java.util.Arrays;
import java.util.List;

/**
 * Arrays
 *
 * @author nivelle
 * @date 2020/04/14
 */
public class ArraysMock {
    public static void main(String[] args) {
        Integer[] test1 = new Integer[]{1, 3, 9, 10, 2};
        /**
         * 排序
         */
        Arrays.sort(test1);
        /**
         * 转流
         */
        Arrays.stream(test1).forEach(System.out::println);
        System.out.println("========");
        /**
         * 转集合
         */
        List<Integer> list = Arrays.asList(test1);
        System.out.println(list);

        Integer[] test2 = new Integer[]{1, 3, 9, 10, 2};
        /**
         * 比较数组是否相等
         */
        boolean equals = Arrays.equals(test1, test2);
        System.out.println(equals);
        /**
         * 数组拷贝
         */
        Integer[] copyArray = Arrays.copyOfRange(test1, 1, test1.length);
        System.out.println(copyArray);
        System.out.println(Arrays.asList(copyArray));
    }
}
