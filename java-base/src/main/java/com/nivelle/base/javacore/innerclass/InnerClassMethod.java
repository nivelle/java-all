package com.nivelle.base.javacore.innerclass;

/**
 * @author fuxinzhong
 * @date 2019/12/15
 */
public class InnerClassMethod {

    private int x;
    private int y;

    public InnerClassMethod(int x, int y) {
        System.err.println("内部类测试,父类内部属性===>" + "x:" + x + "++"+"y:" + y);
        this.x = x;
        this.y = y;
    }

    public int innerClassMethodValue() {
        return this.x + this.y;
    }
}
