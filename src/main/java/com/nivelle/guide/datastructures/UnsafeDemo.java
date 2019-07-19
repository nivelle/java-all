package com.nivelle.guide.datastructures;

import com.nivelle.guide.springboot.pojo.User;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe
 *
 * @author fuxinzhong
 * @date 2019/07/12
 */
public class UnsafeDemo {


    /**
     * Unsafe是位于sun.misc包下的一个类，主要提供一些用于执行低级别、不安全操作的方法，
     * 如直接访问系统内存资源、自主管理内存资源等
     *
     * @param args
     */
    public static void main(String[] args) {
        /**
         * 单例实现;
         * 当且仅当调用getUnsafe方法的类为引导类加载器所加载时才合法；
         */
//        try {
//            Unsafe unsafe = Unsafe.getUnsafe();
//        } catch (Exception e) {
//            System.err.println("securityException: " + e + e.getMessage());
//        }

        /**
         * 1.java -Xbootclasspath/a: ${path}   // 其中path为调用Unsafe相关方法的类所在jar包路径
         * 2.通过反射获取
         */
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe1 = (Unsafe) field.get(null);

            User user = new User(1, "jessy");
            Class userClass = user.getClass();
            Field age = userClass.getDeclaredField("age");
            long ageIndex = unsafe1.objectFieldOffset(age);

            System.out.println("before value:" + user.getAge());
            unsafe1.putInt(user, ageIndex, 2);
            int newAge = user.getAge();
            System.out.println("after value:" + user.getAge());
        } catch (Exception e) {
            System.err.println("反射获取异常: " + e + e.getMessage());
        }
    }
}
