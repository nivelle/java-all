package com.nivelle.base.jdk.jvm;

import java.io.File;

/**
 * 类加载器
 */
public class ClassLoaderInfo {
    public static void main(String[] args) {

        /**
         *  当前类加载器:sun.misc.Launcher$AppClassLoader@18b4aac2
         *
         *  当前类的父亲加载器:sun.misc.Launcher$ExtClassLoader@7e774085
         *
         *  当前类父亲的父亲加载器:null,用C++实现，其他的类加载器是:java.lang.ClassLoader
         *
         */
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        System.out.println("当前类加载器:" + loader);
        System.out.println("当前类的父亲加载器:" + loader.getParent());
        System.out.println("当前类父亲的父亲加载器:" + loader.getParent().getParent());

        System.out.println("bootstrap classLoad path:========");
        final String s = System.getProperty("sun.boot.class.path");
        System.out.println(s);
        sun.misc.Launcher launcher = sun.misc.Launcher.getLauncher();
        System.out.println(launcher.getClass().getClassLoader());
        System.out.println("ext classLoad path:=======");
        final String s1 = System.getProperty("java.ext.dirs");
        System.out.println(s1);
        System.out.println("app classLoad path:========");
        final String s2 = System.getProperty("java.class.path");
        System.out.println(s2);
    }
}
