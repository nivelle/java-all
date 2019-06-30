package com.nivelle.guide.javacore.annotion.myannotion;

import java.lang.reflect.Method;

public class Container {

    public static UserLogin getBean() {
        UserLogin userLogin = new UserLogin();
        if (UserLogin.class.isAnnotationPresent(MyAnnotation.class)) {
            System.out.println("类上的注解：" + UserLogin.class.isAnnotationPresent(MyAnnotation.class));
            Method[] methods = UserLogin.class.getDeclaredMethods();
            for (Method method : methods) {
                System.out.println(method);
                if (method.isAnnotationPresent(MyAnnotation.class)) {
                    System.out.println("方法上的注解：" + method.isAnnotationPresent(MyAnnotation.class));
                    MyAnnotation annotest = method.getAnnotation(MyAnnotation.class);
                    System.out.println("MyAnnotation(field=" + method.getName() + ",nation=" + annotest.nation() + ")");
                    IUser userdao;
                    try {
                        System.out.println("当前注解值" + annotest.nation());
                        userdao = (IUser) Class.forName("com.nivelle.programming.java2e.annotion."+annotest.nation()).newInstance();
                        System.out.println("userdao is"+userdao);
                        userLogin.setUserdao(userdao);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            }
        } else {
            System.out.println("没有注解标记！");
        }
        return userLogin;
    }
}

