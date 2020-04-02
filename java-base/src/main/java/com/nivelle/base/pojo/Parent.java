package com.nivelle.base.pojo;

/**
 * 父类
 */
public class Parent {

    int i = 1;
    String name;


    Parent() {
        System.out.println(i);
        /**
         * 子类复写了该方法,这导致会调用子类方法，返回此时子类的i值：0
         */
        int x = getValue();
        System.out.println(x);
    }

    {
        i = 2;
    }


    public Parent(String name) {
        this.name = name;
    }


    protected int getValue() {
        return i;
    }

}


