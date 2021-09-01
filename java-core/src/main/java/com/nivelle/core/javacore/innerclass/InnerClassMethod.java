package com.nivelle.core.javacore.innerclass;

/**
 * @author nivelle
 * @date 2019/12/15
 */
public class InnerClassMethod {

    private int x;
    private int y;

    public InnerClassMethod(int x, int y) {
        System.out.println("内部类测试,父类内部属性===>" + "x:" + x + "&" + "y:" + y);
        this.x = x;
        this.y = y;
    }

    public int innerClassMethodValue() {
        System.out.println("内部类方法，x:" + x + "&" + "y:" + y);
        return this.x + this.y;
    }
}
