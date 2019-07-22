package com.nivelle.guide.datastructures;

/**
 * math
 *
 * @author fuxinzhong
 * @date 2019/07/22
 */
public class MathDemo {

    public static void main(String[] args) {


        /**
         * 返回 d × 2scaleFactor 舍入好像由一个单一的执行正确舍入浮点乘法的double 值集合的成员。它包括以下情况：
         *
         *
         * 		如果第一个参数为NaN，则返回NaN。 
         *
         * 		如果第一个参数是无限的，那么同样的符号无穷大返回。
         *
         * 		如果第一个参数为0，则返回一个相同于零的符号
         */
        System.out.println(Math.scalb(2, 2));

        /**
         *  绝对值
         */
        System.out.println(Math.abs(-98));

    }
}
