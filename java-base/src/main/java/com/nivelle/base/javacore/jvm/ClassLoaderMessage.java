package com.nivelle.base.javacore.jvm;

/**
 * 类加载器
 */
public class ClassLoaderMessage {
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
    }
}
