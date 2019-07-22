package com.nivelle.guide.javacore;

import java.util.Random;

/**
 * math
 *
 * @author fuxinzhong
 * @date 2019/07/22
 */
public class JavaMathDemo {

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
        System.out.println("科学计数法:" + Math.scalb(2, 2));

        /**
         *  绝对值
         */
        System.out.println("绝对值:" + Math.abs(-98));


        System.out.println("平方根:" + Math.sqrt(4.4));
        System.out.println("平方根:" + Math.sqrt(-4));
        System.out.println("平方根:" + Math.sqrt(0));
        System.out.println("平方根:" + Math.sqrt(4));

        System.out.println("立方根:" + Math.cbrt(8));

        System.out.println("a的b次方:" + Math.pow(2, 2));

        System.out.println("向上取整:" + Math.ceil(-10.8));
        System.out.println("向下取整:" + Math.floor(10.8));

        System.out.println("大于等于0小于1的随机数:" + Math.random());
        System.out.println("大于等于0小于9的随机数:" + new Random().nextInt(10));


        System.out.println("四舍五入:" + Math.rint(4.4));
        /**
         * 注意.5的时候会取偶数
         */
        System.out.println("四舍五入:" + Math.rint(4.5));
        System.out.println("四舍五入:" + Math.rint(4.6));

        /**
         * 参数为float返回int;参数为double返回long
         */
        System.out.println("4.5F 四舍五入:" + Math.round(4.5F));
        System.out.println("4.6F 四舍五入:" + Math.round(4.6F));
        System.out.println("4.44D 四舍五入:" + Math.round(4.44D));


    }
}
