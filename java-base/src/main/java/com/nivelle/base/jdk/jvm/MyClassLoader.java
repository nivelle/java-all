package com.nivelle.base.jdk.jvm;

import java.io.InputStream;

/**
 * 自定义类加载器
 * @author nivellefu
 */
public class MyClassLoader {

    /**
     * 自定义类加载器
     *
     * @param args
     */
    public static void main(String args[]) {

        ClassLoader selfClassLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) {

                try {
                    String filename = name;
                    System.out.println("class name:" + filename);
                    InputStream is = getClass().getResourceAsStream(filename);
                    if (is == null) {
                        System.out.println("类文件流为空");
                        return super.loadClass(name);
                    }
                    System.out.println("ReflectionMain 不为空");
                    byte[] b = new byte[is.available()];

                    is.read(b);
                    return defineClass("com.nivelle.base.javacore.jvm.ReflectionMain", b, 0, b.length);
                } catch (Exception e) {
                    System.err.println(e);
                }
                return null;
            }
        };

        try {
            Object obj = selfClassLoader.loadClass("/com/nivelle/base/jdk/jvm/ReflectionMain.class");
            System.out.println(obj.getClass().getName());
            //不同类加载器加载进来的类，视为不同类，故不能强转
            System.out.println(obj.getClass().getMethods()[0]);
        } catch (Exception e) {
            System.err.println(e);
        }

    }
}
