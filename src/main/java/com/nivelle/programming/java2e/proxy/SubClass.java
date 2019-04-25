package com.nivelle.programming.java2e.proxy;

public class SubClass extends SClass {

    static{
        System.out.println("SubClass init");
    }

    static int a;

    public SubClass(){
        System.out.println("init SubClass");
    }
}
