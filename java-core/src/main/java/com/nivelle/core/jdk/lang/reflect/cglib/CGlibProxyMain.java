package com.nivelle.core.jdk.lang.reflect.cglib;

public class CGlibProxyMain {

    public static void main(String[] args) {
        CGlibTarget cGlibTarget = new CGlibTarget();
        CGlibProxy cGlibProxy = new CGlibProxy();
        CGlibTarget proxy = (CGlibTarget) cGlibProxy.bind(cGlibTarget);
        System.out.println(proxy.sayHello());
    }
}
