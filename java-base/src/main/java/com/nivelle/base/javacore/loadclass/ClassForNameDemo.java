package com.nivelle.base.javacore.loadclass;

import java.lang.reflect.Constructor;

/**
 * 通过类路径获取类对象
 */
public class ClassForNameDemo {


    public static void main(String args[]) {

        try {

            System.out.println(System.getProperty("java.class.path"));
            //获取类对象
            Class classObject = Class.forName(TestBean.class.getName());

            System.out.println("classLoader:" + classObject.getClassLoader());
            //获取构造函数
            Constructor<?>[] constructor = classObject.getConstructors();
            //通过构造函数构造实例
            TestBean testBeans = (TestBean) constructor[0].newInstance("nivelle");

            System.out.println(testBeans.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
