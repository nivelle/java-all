package com.nivelle.base.jdk.lang;

/**
 * boolean 类型
 *
 * @author nivelle
 * @date 2020/03/21
 */
public class BooleanMock {
    //成员变量堆中 占用1个字节
    static boolean booleanValue;

    public static void main(String[] args) {
        //局部变量，栈中计算时被映射成 4个字节 也就是 int
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

        System.out.println(Integer.toBinaryString(2));
        System.out.println(Integer.toBinaryString(3));

    }
}
