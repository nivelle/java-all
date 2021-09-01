package com.nivelle.core.javacore.jdk.lang.reflect.cglib;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGlibProxy {

    Object obj;

    public Object bind(final Object target) {
        this.obj = target;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(obj.getClass());
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args,
                                    MethodProxy proxy) throws Throwable {
                System.out.println("I'm proxy! before");
                Object res = method.invoke(target, args);
                System.out.println("I'm proxy! after");

                return res;
            }
        });
        return enhancer.create();
    }

}
