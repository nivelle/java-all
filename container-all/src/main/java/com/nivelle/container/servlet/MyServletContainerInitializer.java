package com.nivelle.container.servlet;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.lang.reflect.Constructor;
import java.util.Set;

/**
 *  ServletContainerInitializer:ServletContainerInitializer是servlet3.0规范中引入的接口，能够让web应用程序在servlet容器启动后做一些自定义的操作。
 *
 *  ServletContainerInitializer 基于服务提供者接口（SPI）概念，因此你需要在你的jar包目录下添加META-INF/services/javax.servlet.ServletContainerInitializer文件，内容就是ServletContainerInitializer实现类的全限定名
 *
 *  ServletContainerInitializer#onStartup方法由Servlet容器调用(必须至少支持Servlet 3.0版本)。我们在这个方法中通过编程的方式去注册Servlet Filter Listenner等组件，代替web.xml。
 *
 *  可以配合 @HandleTypes 注解，通过指定Class，容器会把所有的指定类的子类作为方法onStartup 的参数Set<Class<?>> c传递进来
 *
 */
@HandlesTypes(ContainerInitializerInterface.class)
public class MyServletContainerInitializer implements javax.servlet.ServletContainerInitializer {
    @Override
    public void onStartup( Set<Class<?>> c, ServletContext ctx) throws ServletException {

        for (Class<?> clazz : c) {
            if(!clazz.isInterface()){
                try {
                    System.out.println(clazz);
                    Constructor<?> constructor = clazz.getConstructor();
                    Object instance = constructor.newInstance();
                    ContainerInitializerInterface containerInitalizer = (ContainerInitializerInterface)instance;
                    containerInitalizer.onStartup(ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(">>>>>>");
    }
}

