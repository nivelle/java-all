package com.nivelle.base.javacore.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MyProxy implements InvocationHandler {

    Object object;

    public Object bind(Object object){
        this.object = object;

        return Proxy.newProxyInstance(object.getClass().getClassLoader(),object.getClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method,Object[]args)throws Throwable{

        System.out.println("I'm proxy!!!");
        Object res = method.invoke(proxy, args);
        return res;

    }
}
