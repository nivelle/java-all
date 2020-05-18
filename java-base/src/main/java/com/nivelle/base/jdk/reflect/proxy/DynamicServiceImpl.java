package com.nivelle.base.jdk.reflect.proxy;

public class DynamicServiceImpl implements DynamicService {
    @Override
    public String sayHello() {
        return "hello: DynamicServiceImpl";
    }

    @Override
    public int add(int a, int b) {
        System.out.println("a:"+a+"  "+"b:"+b);
        return a + b;
    }

}
