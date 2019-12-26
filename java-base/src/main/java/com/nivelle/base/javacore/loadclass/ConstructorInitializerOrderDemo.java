package com.nivelle.base.javacore.loadclass;


import com.nivelle.base.pojo.Bar;

/**
 * 构造函数初始化顺序
 */
public class ConstructorInitializerOrderDemo {


    /**
     * 子类->父类成员变量->父类的代码块->父类构造函数->子类复写的方法->子类成员变量->子类代码块->子类构造函数
     */

    public static void main(String... args) {
        Bar bar = new Bar();
        /**
         * 此时返回的是子类的方法，返回此时子类的j值:2
         */
        System.out.println(bar.getValue());
    }

}
