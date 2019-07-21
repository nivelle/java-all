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
    public static void main(String[] args) throws Exception {

        /**
         * ## 内存操纵
         *
         * 主要包含堆外内存的分配、拷贝、释放、给定地址值操作等方法
         *
         * unSafe操纵的是堆外内存,堆内内存由JVM控制
         */
        User user = new User(2, "Jessy");

        Unsafe unsafe = reflectGetUnsafe();
        System.out.println("before value = " + user);
        Class userClass = user.getClass();
        Field age = userClass.getDeclaredField("age");
        //从内存中直接获取指定属性的值
        long userAgeOffset = unsafe.objectFieldOffset(age);
        System.out.println("userAgeOffset = " + userAgeOffset);
        int memoryAge = (int) unsafe.getObject(user, userAgeOffset);
        System.out.println("memory value = " + memoryAge);
        //直接操纵指定元素的值
        unsafe.putObject(user, userAgeOffset, 11);
        System.out.println("userAgeOffset = " + userAgeOffset);
        System.out.println("after value = " + user.getAge());
        //释放元素内存
        //unsafe.freeMemory(ageAddress);
        System.out.println("free after = " + user.getAge());
        /**
         * ## CAS
         */
        System.out.println("user2 before value = " + user.getAge());
        int memoryAge2 = (int) unsafe.getObject(user, userAgeOffset);
        System.out.println("memory age2 " + memoryAge2);
        System.out.println("user2 memory value = " + user.getAge());
        //修改值
        boolean swapResult = unsafe.compareAndSwapObject(user, userAgeOffset, 11, 20);
        System.out.println("cas操纵结果:" + swapResult);
        System.out.println("userOffset= " + userAgeOffset);
        System.out.println("user2 age is = " + user.getAge());


        new Thread(() -> {
            synchronized (user) {
                try {
                    System.out.println("获取到了user1的对象锁！" + Thread.currentThread().getName());
                } catch (Exception e) {
                    System.out.println(e + e.getMessage());
                }

            }
            unsafe.park(true,5000);
        }).start();

        /**
         * ## 线程调度
         *
         * 包括线程挂起、恢复、锁机制
         */
        //可重入锁
        boolean monitorEnter = unsafe.tryMonitorEnter(user);
        System.out.println("获取对象锁,monitorEnter:" + monitorEnter);
        boolean monitorEnter2 = unsafe.tryMonitorEnter(user);
        System.out.println("获取对象锁,monitorEnter2:" + monitorEnter2);
        System.out.println("当前锁:" + Thread.currentThread().getName());
        Thread.sleep(1000);

        /**
         * ## Class相关
         *
         * 提供Class和它的静态字段的操作相关方法，包含静态字段内存定位、定义类、定义匿名类、检验&确保初始化等
         */
        User userAllocateInstance = (User) unsafe.allocateInstance(userClass);
        userAllocateInstance.setAge(20);
        userAllocateInstance.setName("AllocateUser");
        System.out.println("userAllocateInstance is:" + userAllocateInstance);

    }


    /**
     * 单例实现;
     * 当且仅当调用getUnsafe方法的类为引导类加载器所加载时才合法;
     * <p>
     * 1.java -Xbootclasspath/a: ${path}   // 其中path为调用Unsafe相关方法的类所在jar包路径
     * 2.通过反射获取Unsafe实例
     */
    private static Unsafe reflectGetUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            System.out.println(e + e.getMessage());
            return null;
        }
    }


}
