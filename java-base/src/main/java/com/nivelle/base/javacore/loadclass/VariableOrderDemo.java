package com.nivelle.base.javacore.loadclass;

/**
 * 成员变量初始化顺序
 */
public class VariableOrderDemo {

    /**
     * 成员变量->代码块->构造函数
     */

    private int i = 1;
    private int j = i + 1;

    public VariableOrderDemo(int var) {
        System.out.println(i);
        System.out.println(j);
        this.i = var;
        System.out.println(i);
        System.out.println(j);
    }

    {
        j += 3;

    }

    public static void main(String args[]) {
        new VariableOrderDemo(8);
    }

}
