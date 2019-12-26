package com.nivelle.base.algorithms.common;

/**
 * 斐波那契数列
 *
 * @author fuxinzhong
 * @date 2019/08/20
 */
public class Fibonacci {

    public static void main(String[] args) {

        System.out.println(fib(8));
        System.out.println(fib2(8));

    }

    /**
     * 可能会sof
     *
     * @param n
     * @return
     */
    private static int fib(int n) {

        if (n <= 1) {
            return n;
        } else {
            return fib(n - 1) + fib(n - 2);
        }
    }

    /**
     * 动态规划
     * @param n
     * @return
     */
    private static int fib2(int n) {
        if (n == 0 || n == 1) {
            return n;
        }
        int fn1 = 0;
        int fn2 = 1;
        for (int i = 2; i <= n; i++) {
            fn2 += fn1;
            fn1 = fn2 - fn1;
        }
        return fn2;
    }
}
