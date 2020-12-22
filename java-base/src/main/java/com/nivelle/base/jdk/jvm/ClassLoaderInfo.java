package com.nivelle.base.jdk.jvm;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 类加载器
 */
public class ClassLoaderInfo {
    public static void main(String[] args) throws Exception {

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
        System.out.println("================================");
        //调用加载当前类的类加载器（这里即为系统类加载器）加载TestBean
        //  jar cvf ./ClassLoaderBean.jar ClassLoaderBean.class 打包指令
        // /Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext
       // Class typeLoaded = loader.loadClass("com.nivelle.base.jdk.jvm.ClassLoaderBean");
        //classPath:sun.misc.Launcher$AppClassLoader@18b4aac2
        //classPath:sun.misc.Launcher$ExtClassloader@15db9742
        //System.out.println("classPath:" + typeLoaded.getClassLoader());
        //Class myClass = Class.forName("com.nivelle.base.jdk.jvm.ClassLoaderBean");
        //System.out.println(myClass);

        try {
            URL[] extURLs = ((URLClassLoader) ClassLoader.getSystemClassLoader().getParent()).getURLs();
            for (int i = 0; i < extURLs.length; i++) {
                System.out.println(extURLs[i]);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
