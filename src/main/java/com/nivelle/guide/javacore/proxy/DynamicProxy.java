package com.nivelle.guide.javacore.proxy;

public class DynamicProxy {

    public static void main(String args[]){
        MyProxy myproxy = new MyProxy();
        HoseeDynamicimpl dynamicimpl = new HoseeDynamicimpl();
        HoseeDynamic proxy = (HoseeDynamic)myproxy.bind(dynamicimpl);
        System.out.println(proxy.sayhi());
    }
}
