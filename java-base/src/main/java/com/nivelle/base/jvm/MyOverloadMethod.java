package com.nivelle.base.jvm;

/**
 * 方法重载
 *
 * @author fuxinzhong
 * @date 2021/02/20
 */
public class MyOverloadMethod {

    public int myMethod(String s, int i) {
        System.out.println("myMethod(String s, int i)");
        return Integer.valueOf(s) + i;
    }

    public long myMethod(String s1, Integer j) {
        System.out.println("myMethod(String s1, Integer j)");
        return Long.valueOf(s1) + j;
    }

    public void myMethod1(Object obj, String... strings) {
        System.out.println("myMethod1(Object obj, String... strings)");
        return;
    }

    public void myMethod1(String s, Object obj, String... strings) {
        System.out.println("myMethod1(String s, Object obj,String... strings)");
        return;
    }
}


