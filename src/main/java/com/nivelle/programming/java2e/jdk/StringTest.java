package com.nivelle.programming.java2e.jdk;

public class StringTest {


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
        System.out.println(s1 == s2);
        System.out.println(s1 == s3);
    }

    private static void test02() {
        String s0 = "nivelle";
        String s1 = new String("nivelle");
        String s2 = "nive" + new String("lle");

        String s = "lle";
        String s3 = "nive";


        System.out.println(s0 == s1);
        System.out.println(s0 == s2);
        System.out.println(s1 == s2);
        System.out.println(s0 == s3);
        System.out.println(s1 == s3);
        System.out.println(s2 == s3);


    }

    private static void internTest(){
        String str1= "abc";
        String str2 = new String("abc");

        String str3 = str2.intern();

        System.out.println(str1==str2);
        System.out.println(str1==str3);
    }

    private static void stringInit(){
        String s1= "abc";
        String s3 = new String("abc");
        //↑ 创建了两个对象，一个存放在字符串池中，一个存在与堆区中；
        //↑ 还有一个对象引用s3存放在栈中
        String s4 = new String("abc");
        //↑ 字符串池中已经存在“abc”对象，所以只在堆中创建了一个对象
        System.out.println("s3 == s4 : "+(s3==s4));
        //↑false   s3和s4栈区的地址不同，指向堆区的不同地址；
        System.out.println("s3.equals(s4) : "+(s3.equals(s4)));
        //↑true  s3和s4的值相同
        System.out.println("s1 == s3 : "+(s1==s3));
        //↑false 存放的地区都不同，一个方法区，一个堆区
        System.out.println("s1.equals(s3) : "+(s1.equals(s3)));
        //↑true  值相同
    }

    public static void main(String args[]) {
        //String str = new String("aaa");
        //System.out.println(str);
        //referenceStr(str);
       // test01();
       // test02();
        // internTest();
        stringInit();
    }
}
