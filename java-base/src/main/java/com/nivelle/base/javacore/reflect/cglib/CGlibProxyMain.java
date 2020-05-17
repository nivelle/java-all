package com.nivelle.base.javacore.reflect.cglib;

public class CGlibProxyMain {

    public static void main(String[] args)
    {
        CGlibHosee cGlibHosee = new CGlibHosee();
        CGlibHoseeProxy cGlibHoseeProxy = new CGlibHoseeProxy();
        CGlibHosee proxy = (CGlibHosee) cGlibHoseeProxy.bind(cGlibHosee);
        System.out.println(proxy.sayhi());
    }
}
