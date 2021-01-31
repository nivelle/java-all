package com.nivelle.base.jdk.lang;

/**
 * boolean 类型
 *
 * @author nivelle
 * @date 2020/03/21
 */
public class BooleanMock {

    public static void main(String[] args) {
        boolean a = true;
        boolean b = true;
        System.out.println("&&操作:" + Boolean.logicalAnd(a, b));
        System.out.println("^操作：" + Boolean.logicalXor(a, b));

        System.out.println("true 的hashCode值:" + Boolean.hashCode(true));
        System.out.println("false 的hashCode值:" + Boolean.hashCode(false));


        Boolean my = new Boolean(true);
        System.out.println(my.hashCode());

        boolean result = Boolean.getBoolean("user");
        System.out.println(result);

        System.out.println("获取虚拟机boolean类型:" + Boolean.TYPE);


    }
}
