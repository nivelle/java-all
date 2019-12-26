package com.nivelle.base.datastructures;

/**
 * Integer
 *
 * @author fuxinzhong
 * @date 2019/06/05
 */
public class IntegerDemo {

    public static void main(String[] args) {

        Integer integer = new Integer("128");
        /**
         *向下转型,超出范围会导致精度丢失。byte 范围 -128～127,超出范围的值会因为高位丢失，导致首位变成1,成为负数。
         */
        System.out.println("向下转型精度不会丢失:" + integer.byteValue());
        /**
         *向上转型不会丢失精度
         */
        Byte myByte = new Byte("127");
        System.out.println("向上转型精度不会丢失:" + myByte.intValue());
        System.out.println("向上转型精度不会丢失:" + myByte.doubleValue());

        /**
         * 精度超过36会默认为10
         */
        String integer2 = Integer.toUnsignedString(-300, 80);
        System.out.println("超过36精度默认为10进制:" + integer2);

        /**
         * radix:表面字符串代表的进制
         */
        String intString = "100";
        Integer result = Integer.parseInt(intString, 2);
        System.out.println("2进制intString:" + result);

        Integer resultResult = Integer.valueOf("100", 2);
        System.out.println("2进制intString:" + resultResult);

        /**
         * 静态内部类 IntegerCache 为自动装箱机制中的-128～127提供缓存
         * 1. The cache is initialized on first usage
         *
         * 2. The size of the cache may be controlled by the {@code -XX:AutoBoxCacheMax=<size>} option
         *
         * 3. java.lang.Integer.IntegerCache.high property may be set and saved in the private system properties in the
         *    sun.misc.VM class.
         */
        Integer a = new Integer(127);
        Integer b = new Integer(127);

        System.err.println("a=b is " + a.equals(b));

        /**
         * 这两种hashCode 方法是兼容的
         */
        int hashCode = Integer.hashCode(10);
        System.err.println(hashCode);
        Integer integerHashCode = new Integer(10);
        System.err.println(integerHashCode.hashCode());

        /**
         * 返回正负号 1:正号 0:0 -1:负号
         */
        System.err.println("符号:" + Integer.signum(-100));
        /**
         * 该方法即是将i进行反转，反转就是第1位与第32位对调，第二位与第31位对调 0000 0000 0000 0001 1000 0000 0000 0000 = 98304
         */
        System.err.println("反数:" + Integer.reverse(98304));
        /**
         ** 补码是计算机中用来表示负数，使得负数能够使用加法器参与加法运算的一种码。
         ** 正数的补码即为自己，负数的补码为符号位不变，其余逐位求反再加1。
         */
        System.err.println("二进制表示:" + Integer.toBinaryString(100) + ";1位数量:" + Integer.bitCount(100));

        /**
         * decode方法主要作用是解码字符串转成Integer型
         */
        System.out.println("解码为十进制:" + Integer.decode("0x11") + Integer.decode("#11")+Integer.decode("0001"));

    }
}
