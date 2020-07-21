package com.nivelle.base.jdk.concurrent;

import java.util.Arrays;

/**
 * Arrays
 *
 * @author nivelle
 * @date 2020/04/14
 */
public class ArraysTest {
    public static void main(String[] args) {
        int[] test = new int[]{1, 3, 9, 10, 2};
        Arrays.sort(test);
        Arrays.stream(test).forEach(System.out::println);
    }
}
