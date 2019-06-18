package com.nivelle.guide.java2e.core;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class StringData {


    /**
     * 1. String 代表的是一个字符数组,所有的java中的文字都是通过String类实现的。
     * 2. Strings are constant; their values cannot be changed after they are created. String buffers support mutable strings.
     * 3. Because String objects are immutable they can be shared
     *
     * @param args
     */
    public static void main(String[] args) {

        /**
         * 字符串比较
         */
        String string1 = new String("nivelle");
        String string2 = "nivelle";
        System.out.print("常量池和对象比较:");
        System.out.println(string1 == string2);
        System.out.print("内容比较:");
        System.out.println(string1.equals(string2));
        System.out.print("强制比较池常量:");
        System.out.println(string1.intern() == string2);

        /**
         * 字符串底层数据结构是字符串数组
         */
        char[] string3 = {'n', 'i', 'v', 'e', 'l', 'l', 'e'};
        System.out.println("string1对比string3:" + string1.equals(string3));
        System.out.println("string1对比string3:" + string1.equals(new String(string3)));

        /**
         * 由字符数组构造字符串
         * Allocates a new {@code String} that contains characters from a subarray of the character array argument.
         * offset:子字符串的初始位置
         * count:子字符串的长度
         */
        System.out.println("通过字符数组构造字符串:" + new String(string3, 1, string3.length - 1));
        /**
         * 通过字符ASCII码构造字符串
         */
        int[] postArray = {110, 105, 118, 101, 108, 108, 101};
        System.out.println("通过字符数组构造字符串:" + new String(postArray, 1, string3.length - 1));


        /**
         * 空构造函数也就是字符串数组="";
         */
        String string4 = new String();
        System.out.println("空构造函数：" + string4.equals(""));

        /**
         * hash码算法
         * 算法：hashCode = s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]， n 是字符串长度
         * 特例：The hash value of the empty string is zero
         */
        System.out.println("字符串string1的hash码:" + string1.hashCode());
        System.out.println("字符串string2的hash码:" + string1.hashCode());
        System.out.println("字符串string4的hash码:" + string4.hashCode());

        /**
         * 字符转字节编码数组
         */
        byte[] bytes = string1.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            System.out.print("字符串ASCII码:" + bytes[i] + ";");
        }
        System.out.println();
        /**
         * 获取指定位置的字符
         */
        System.out.println("指定位置的字符:" + string1.charAt(1));
        /**
         * 返回指定位置的字节编码
         */
        System.out.println("指定位置的字符:" + string1.codePointAt(1));
        /**
         * 忽略大小写话比较
         * **默认都是转化为大写字母比较**
         */
        System.out.println("字符串忽略大小写比较:" + string1.equalsIgnoreCase(string2));

        /**
         * The comparison is based on the Unicode value of each character in the strings
         */
        System.out.println("字符串字典比较:" + string1.compareTo(string2));


    }
}
