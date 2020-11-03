package com.nivelle.base.jdk.lang.reflect.proxy;

public class DynamicProxyMain {

    public static void main(String args[]) throws Exception {
        DynamicService dynamicService = new DynamicServiceImpl();
        MyHandler myProxy = new MyHandler(dynamicService);
        DynamicService dynamicServiceProxy = myProxy.getProxy();
        int c = dynamicServiceProxy.add(1, 2);
        System.out.println("c = " + c);
        String string = dynamicService.sayHello();
        System.out.println(string);
    }
}
