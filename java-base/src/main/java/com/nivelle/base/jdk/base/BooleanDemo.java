package com.nivelle.base.jdk.base;

/**
 * boolean 类型
 *
 * @author nivelle
 * @date 2020/03/21
 */
public class BooleanDemo {

    public static void main(String[] args) {
        boolean a = true;
        boolean b = true;
        System.out.println("与操作:" + Boolean.logicalAnd(a, b));


        Boolean my = new Boolean(true);

        boolean result = Boolean.getBoolean("os");
        System.out.println(result);
    }
}
