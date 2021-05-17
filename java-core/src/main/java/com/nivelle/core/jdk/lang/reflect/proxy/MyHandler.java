package com.nivelle.core.jdk.lang.reflect.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MyHandler implements InvocationHandler {

    public DynamicService dynamicService;

    public MyHandler(DynamicService dynamicService) {
        this.dynamicService = dynamicService;
    }

    /**
     * 为object参数创建代理
     *
     * @return
     */
    public DynamicService getProxy() {
        //要代理类的类加载器
        ClassLoader classLoader = this.dynamicService.getClass().getClassLoader();
        //要代理类的接口列表
        Class[] interfaces = this.dynamicService.getClass().getInterfaces();
        //创建代理类
        DynamicService proxyObject = (DynamicService) Proxy.newProxyInstance(classLoader, interfaces, this);
        return proxyObject;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            System.out.println("before");
            Object invoke = method.invoke(dynamicService, args);
            System.out.println(invoke);
            System.out.println("after");
            return invoke;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
}
