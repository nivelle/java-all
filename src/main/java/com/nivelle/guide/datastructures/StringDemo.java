package com.nivelle.guide.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * String
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class StringDemo {


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
         * 忽略大小写等于比较
         * **默认都是转化为大写字母比较**
         */
        System.out.println("字符串忽略大小写相等判断:" + string1.equalsIgnoreCase(string2));

        /**
         * The comparison is based on the Unicode value of each character in the strings
         */
        String compare1 = "A";
        String compare2 = "C";
        System.out.println("字符串字典比较:" + compare1.compareTo(compare2));

        /**
         * 大小写不敏感比较，分别转为大写字母和小写字母分别比较一次
         */
        String compare3 = "100";
        String compare4 = "97";
        System.out.println("字符串忽略大小写大小比较:" + compare3.compareToIgnoreCase(compare4));

        /**
         * 切割字符串
         * 底层利用了: public String(char value[], int offset, int count) 构造函数
         */
        System.out.println("切割字符串:" + string2.substring(0, 1));

        /**
         * 字符串拼接
         * 需要经过两次字符串复制:
         * 1. char buf[] = Arrays.copyOf(value, len + otherLen);//buf[] 字符数组包括 string2字符串+加上新字符串的长度
         *
         * 2.str.getChars(buf, len);//"love jessy!".getChars(buf,len);
         *
         * 3.System.arraycopy(value, 0, dst, dstBegin, value.length);//love jessy! 这个字符串调用复制函数
         */
        System.out.println("拼接字符串:" + string2.concat(" love jessy!"));

        /**
         * 字符串替换
         */
        System.out.println("字符串替换:" + string2.replace("l", "j"));

        /**
         * 字符串匹配
         */
        System.out.println("字符串匹配:" + string2.matches("nivelle"));

        /**
         * 字符串分割成数组
         */
        String string5 = "n,i,v,e,l,l,e";
        for (int i = 0; i < string5.split(",").length; i++) {
            System.out.print("字符分割:" + string5.split(",")[i] + ";");
        }
        System.out.println();

        /**
         * 集合转指定字符拼接的字符串
         */
        List list = new ArrayList();
        list.add("n");
        list.add("i");
        list.add("v");
        list.add("e");
        list.add("l");
        list.add("l");
        list.add("e");
        String joinResult = String.join(",", list);
        System.out.println("集合元素通过指定符号拼接:" + joinResult);

        StringJoiner joiner = new StringJoiner(";");
        for (Object cs : list) {
            joiner.add((CharSequence) cs);
        }
        System.out.println("集合元素通过指定符号拼接原理:" + joiner.toString());

        /**
         * 字符串字母转换:大写->小写
         */
        System.out.println("字符串转小写:" + "NIVELLE".toLowerCase(Locale.US));
        /**
         * 字符串字母转换:小写->大写
         */
        System.out.println("字符串转大写:" + "nivellE".toUpperCase(Locale.US));

        /**
         * 去掉字符串首尾中的空格
         */
        System.out.println("去掉字符串首尾中的空格:" + " nive  ll e ".trim());

        /**
         * 底层原理:通过复制当前字符窜的字符串数组实现：
         * System.arraycopy(value, 0, result, 0, value.length);
         */
        for (int i = 0; i < "nivelle".length(); i++) {
            System.out.println("转为字符串数组:" + ("nivelle".toCharArray()[i]));
        }

        System.out.println("格式化字符串:" + String.format("nivell%s", "e"));

        String str = new String("aaa");
        System.out.println(str);

        referenceStr(str);
        test01();
        test02();
        internTest();
        stringInit();

    }

    public static void changeStr(String str) {
        String s = str;
        str += "welcome";
        System.out.println(s);
        System.out.println(str);
    }


    public static void referenceStr(String str) {
        String strCopy = str;
        strCopy.toUpperCase();
        System.out.println(str);
    }

    private static void test01() {
        String s1 = "nivelle";
        String s2 = "nivelle";
        String s3 = "nive" + "lle";
        System.out.println("s1==s2:" + (s1 == s2));
        System.out.println("s1==s3:" + (s1 == s3));
    }

    private static void test02() {
        String s0 = "nivelle";
        String s1 = new String("nivelle");
        String s2 = "nive" + new String("lle");

        String s4 = "lle";
        String s3 = "nive";


        System.out.println("s0==s1:" + (s0 == s1));
        System.out.println("s0==s2:" + (s0 == s2));
        System.out.println("s1==s2:" + (s1 == s2));
        System.out.println("s0==s3:" + (s0 == s3));
        System.out.println("s1==s3:" + (s1 == s3));
        System.out.println("s2==s3:" + (s2 == s3));


    }

    private static void internTest() {
        String str1 = "abc";
        String str2 = new String("abc");

        String str3 = str2.intern();

        System.out.println("str1==str2:" + (str1 == str2));
        System.out.println("str1==str3:" + (str1 == str3));
    }

    private static void stringInit() {
        String s1 = "abc";
        String s3 = new String("abc");
        //创建了两个对象，一个存放在字符串池中，一个存在与堆区中；
        //还有一个对象引用s3存放在栈中
        //字符串池中已经存在“abc”对象，所以只在堆中创建了一个对象
        String s4 = new String("abc");
        System.out.println("字符串比较");

        //false   s3和s4栈区的地址不同，指向堆区的不同地址；
        System.out.println("s3 == s4 : " + (s3 == s4));

        //true  s3和s4的值相同
        System.out.println("s3.equals(s4) : " + (s3.equals(s4)));

        //false 存放的地区都不同，一个方法区，一个堆区
        System.out.println("s1 == s3 : " + (s1 == s3));

        //true  值相同
        System.out.println("s1.equals(s3) : " + (s1.equals(s3)));

    }
}
