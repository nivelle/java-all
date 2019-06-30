package com.nivelle.guide.javacore.classloader;

import java.lang.reflect.Constructor;

public class ClassForNameTest {


    public static void main(String args[]) {

        try {

            System.out.println(System.getProperty("java.class.path"));
            //获取类对象
            Class typeLoaded = Class.forName("com.nivelle.guide.javacore.classloader.TestBean");
            System.out.println(typeLoaded.getClassLoader());
            //获取构造函数
            Constructor<?>[] constructor = typeLoaded.getConstructors();
            //通过构造函数构造实例
            TestBean testBeans = (TestBean) constructor[0].newInstance("nivelle");
            System.out.println(testBeans.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
