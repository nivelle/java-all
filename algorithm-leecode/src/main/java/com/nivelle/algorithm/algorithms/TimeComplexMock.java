package com.nivelle.algorithm.algorithms;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/03/24
 */
public class TimeComplexMock {

    public static void main(String[] args) {

    }

    /**
     * O(1) 时间复杂度
     */
    private void O1() {
        int n = 1000;
        System.out.println(n);
        System.out.println(n);
        System.out.println(n);
    }

    /**
     * O(n)时间复杂度
     */
    private void On() {
        int n = 100;
        for (int i = 1; i <= n; i++) {
            System.out.println(n);
        }
    }

    /**
     * O(n*m)时间复杂度
     */
    private void On2() {
        int n = 100;
        int m = 100;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                System.out.println(n * m);
            }
        }
    }

    /**
     * O(log(n))时间复杂度
     */
    private void OLongN() {
        int n = 100;
        for (int i = 1; i <= n; i = i * 2) {
            System.out.println(i);
        }
    }

    /**
     * O(k^n)时间复杂度
     */
    private void OKn() {
        int n = 100;
        for (int i = 1; i <= Math.pow(2, n); i++) {
            System.out.println(i);
        }
    }

    /**
     * O(n!)时间复杂度
     */
    private void ON() {
        int n = 100;
        for (int i = 1; i <= factorial(n); i++) {
            System.out.println(i);
        }
    }

    private int factorial(int n) {
        return n;
    }


}
