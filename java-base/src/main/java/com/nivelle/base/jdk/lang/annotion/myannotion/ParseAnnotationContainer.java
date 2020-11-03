package com.nivelle.base.jdk.lang.annotion.myannotion;

import java.lang.reflect.Method;

public class ParseAnnotationContainer {

    public static UserLogin getBean() {
        UserLogin userLogin = new UserLogin();
        if (UserLogin.class.isAnnotationPresent(MyAnnotation.class)) {
            System.out.println("类上的注解：" + UserLogin.class.isAnnotationPresent(MyAnnotation.class));
            Method[] methods = UserLogin.class.getDeclaredMethods();
            for (Method method : methods) {
                System.out.println(method);
                if (method.isAnnotationPresent(MyAnnotation.class)) {
                    System.out.println("方法上的注解：" + method.isAnnotationPresent(MyAnnotation.class));
                    MyAnnotation annotationTest = method.getAnnotation(MyAnnotation.class);
                    System.out.println("MyAnnotation(field=" + method.getName() + ",nation=" + annotationTest.nation() + ")");
                    IUser userService;
                    try {
                        System.out.println("当前注解值" + annotationTest.nation());
                        System.err.println(IUser.class.getPackage().getName());
                        //根据不通的注解内容,获取不同的实例
                        userService = (IUser) Class.forName(IUser.class.getPackage().getName() + "." + annotationTest.nation()).newInstance();
                        System.out.println("userService is:" + userService);
                        userLogin.setUserService(userService);
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

