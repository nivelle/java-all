package com.nivelle.base.javacore.loadclass;

/**
 * 静态常量初始化不会触发类的初始化，静态属性和静态代码块按照顺序依次初始化
 */
class FinalFieldClass {

    public static final String CONSRANR = "CONSRANR";

    static {
        System.out.println("FinalFieldClass init");
    }

    public static String name = "nivelle";
}

public class StaticVariableOrderDemo {

    public static void main(String[] args) {
        /**
         *打印结果：CONSRANR
         *
         * 1.直接访问静态常量不触发类的初始化
         */
       System.out.println(FinalFieldClass.CONSRANR);

        /**
         * 打印结果：
         * FinalFieldClass init
         * nivelle
         */
        System.out.println(FinalFieldClass.name);

    }
}
