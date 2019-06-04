package com.nivelle.guide.java2e.jdk;

/**
 * java运算
 */
public class OperationTest {


    private static long subtraction() {
        int a = 10;
        long b = 4;
        long c = a - b;
        System.out.println(c);
        return c;
    }

    private static int toInteger() {
        String integer = "0x7fffffff";
        return Integer.parseInt(integer,10);
    }


    public static void main(String[] args) {
        //subtraction();
        int result = toInteger();
        System.out.println(result);
    }
}
