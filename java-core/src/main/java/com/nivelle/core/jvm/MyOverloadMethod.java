package com.nivelle.core.jvm;

/**
 * 方法重载
 *
 * @author fuxinzhong
 * @date 2021/02/20
 */
public class MyOverloadMethod extends MyOverloadMethodParent {

//    public int myMethod(String s, int i) {
//        System.out.println("myMethod(String s, int i)");
//        return Integer.valueOf(s) + i;
//    }

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

    @Override
    public int myMethod3(long i, int j, int k) {
        System.out.println("MyOverloadMethod==> Overload myMethod3");
        return 1;
    }

    public void myMethod5(long i, int j) {
        System.out.println("MyOverloadMethod==>myMethod4");
    }

//    public int myMethod5(long i, int j) {
//        System.out.println("MyOverloadMethod==>myMethod4");
//        return 10;
//    }

}


