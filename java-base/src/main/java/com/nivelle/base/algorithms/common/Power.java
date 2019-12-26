package com.nivelle.base.algorithms.common;

/**
 * 数值的整数次方
 *
 * @author fuxinzhong
 * @date 2019/08/21
 */
public class Power {

    public static void main(String[] args) {
        double result = Power(2, 5);
        System.out.println("result is:" + result);
    }

    public static double Power(double base, int exponent) {
        int n = exponent;
        if (exponent == 0) {
            // 当指数为0底数为0时，没有意义，返回0或者返回1都可以
            return 1;
        } else if (exponent < 0) {
            if (base == 0) {
                throw new RuntimeException("分母不能为0");
            }
            n = -exponent;
        }
        double res = PowerUnsignedExponent(base, n);
        //double res = PowerUnsignedExponent2(base, n);
        return exponent < 0 ? 1 / res : res;
    }

    public static double PowerUnsignedExponent(double base, int n) {
        if (n == 0)
            return 1;
        if (n == 1) {
            return base;
        }
        //递归
        double res = PowerUnsignedExponent(base, n / 2);
        res *= res;
        if (n % 2 == 1) {//奇数
            res *= base;
        }
        return res;
    }

    public static double PowerUnsignedExponent2(double base, int n) {
        if (n == 0)
            return 1;
        if (n == 1)
            return base;
        //递归
        double res = PowerUnsignedExponent(base, n >> 1);
        res *= res;
        if ((n & 0x1) == 1)
            res *= base;
        return res;
    }
}
