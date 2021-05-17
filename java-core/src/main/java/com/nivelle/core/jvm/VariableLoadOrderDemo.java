package com.nivelle.core.jvm;

/**
 * 成员变量初始化顺序
 */
public class VariableLoadOrderDemo {

    /**
     * 成员变量->代码块->构造函数
     */

    private int i = 1;
    private int j = i + 1;

    public VariableLoadOrderDemo(int var) {
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
        new VariableLoadOrderDemo(8);
    }

}
