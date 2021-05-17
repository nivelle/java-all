package com.nivelle.core.jvm;


import com.nivelle.core.pojo.Parent;
import com.nivelle.core.pojo.Son;

/**
 * 构造函数初始化顺序
 */
public class ConstructorInitializerOrderDemo {


    /**
     * 子类->父类成员变量->父类的代码块->父类构造函数->
     * 子类复写的方法(被复写的方法在父类内部依然是子类为上下文)
     * ->子类成员变量->子类代码块->子类构造函数
     */

    public static void main(String... args) {
        Son son = new Son();
        /**
         * 此时返回的是子类的方法，返回此时子类的j值:2
         */
        System.out.println("子类实例化后调用复写后的方法:" + son.getValue());

        System.out.println("================");
        Parent parent = new Parent();
        System.out.println("直接实例化后父类调用方法:" + parent.getValue());

        System.out.println("具体值要看当前的上下文");

    }

}
