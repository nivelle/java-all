package com.nivelle.base.jdk.sun;

import com.nivelle.base.pojo.User;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe
 *
 * @author nivelle
 * @date 2019/07/12
 */
public class UnsafeClassDemo {
    /**
     * Unsafe是位于sun.misc包下的一个类，主要提供一些用于执行低级别、不安全操作的方法，
     * 如直接访问系统内存资源、自主管理内存资源等
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        //getUnSafe();

        /**
         * 1. 内存操纵
         *
         * 主要包含堆外内存的分配、拷贝、释放、给定地址值操作等方法
         *
         * unSafe操纵的是堆外内存,堆内内存由JVM控制
         *
         * Unsafe申请的内存的使用将直接脱离jvm，gc将无法管理Unsafe申请的内存，所以使用之后一定要手动释放内存，避免内存溢出
         */
        User user = new User(2, "Jessy");

        Unsafe unsafe = reflectGetUnsafe();
        System.out.println("unsafe:" + unsafe);
        System.out.println("before value:" + user);
        Field ageField = User.class.getDeclaredField("age");
        System.out.println("declared ageField:" + ageField);
        Field flagField = User.class.getDeclaredField("flag");
        System.out.println("declared  flagField:" + flagField);
        //静态字段偏移量获取
        long staticFieldOffset = unsafe.staticFieldOffset(flagField);
        System.out.println("staticFieldOffset:" + staticFieldOffset);
        long userAgeOffset = unsafe.objectFieldOffset(ageField);
        System.out.println("userAgeOffset:" + userAgeOffset);
        //修改基本数据类型的值
        unsafe.putInt(user, userAgeOffset, 120);
        unsafe.putInt(User.class, staticFieldOffset, 4);
        long numberOffSet = unsafe.objectFieldOffset(User.class.getDeclaredField("number"));
        //修改非基本数据类型的值
        unsafe.putObject(user, numberOffSet, new Integer(90));

        System.out.println("after modify instance value:" + user.getAge());
        System.out.println("after modify static value:" + User.flag);
        System.out.println("after modify number value:" + user.getNumber());

        //从内存中直接获取指定属性的值
        Object numberValue = unsafe.getObject(User.class, numberOffSet);
        System.out.println("numberValue:" + numberValue);
        //直接操纵指定元素的值
        unsafe.putObject(user, userAgeOffset, 11);
        System.out.println("userAgeOffset:" + userAgeOffset);
        System.out.println("after value:" + user.getAge());
        //释放元素内存
        //unsafe.freeMemory(ageAddress);
        System.out.println("free after:" + user.getAge());

        //该内存的使用将直接脱离jvm，gc将无法管理以下方式申请的内存，以用于一定要手动释放内存，避免内存溢出
        //向本地系统申请一块内存地址； 使用方法allocateMemory(long capacity) ,该方法将返回内存地址的起始地址
        long address = unsafe.allocateMemory(8);
        System.out.println("allocate memory address : " + address);
        //向指定地址设置值
        unsafe.putByte(address, (byte) 1);
        byte b = unsafe.getByte(address);
        System.out.println("从指定内存获取值:" + b);


//        unsafe.putObject(User.class, address + 2, new User());
//        User memUser = (User) unsafe.getObject(User.class, address + 2);
//        System.out.println("从指定内存获取对象:" + memUser);

        // 重新分配内存 reallocateMemory(内存地址 ，大小) ,该方法说明:该方法将释放掉给定内存地址所使用的内存,并重新申请给定大小的内存;
        long newAddress = unsafe.reallocateMemory(address, 32);
        System.out.println("new address = " + newAddress);
        //释放内存
        unsafe.freeMemory(newAddress);
        /**
         * 2. CAS操作
         */
        System.out.println("user2 before value :" + user.getAge());
        int memoryAge2 = unsafe.getInt(user, userAgeOffset);
        System.out.println("memory age2: " + memoryAge2);
        System.out.println("user2 memory value : " + user.getAge());
        //修改值
        boolean swapResult = unsafe.compareAndSwapObject(user, userAgeOffset, 11, 20);
        System.out.println("cas操纵结果:" + swapResult);
        System.out.println("userOffset:" + userAgeOffset);
        System.out.println("user2 age is:" + user.getAge());


        new Thread(() -> {
            synchronized (user) {
                try {
                    System.out.println("获取到了user1的对象锁！" + Thread.currentThread().getName());
                } catch (Exception e) {
                    System.out.println(e + e.getMessage());
                }

            }
            unsafe.park(true, 5000);
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
        User userAllocateInstance = (User) unsafe.allocateInstance(User.class);
        userAllocateInstance.setAge(20);
        userAllocateInstance.setName("AllocateUser");
        System.out.println("userAllocateInstance is:" + userAllocateInstance);

    }


    /**
     * //构造函数是private的，所以不能使用new创建，而使用单例模式创建
     * <p>
     * 当且仅当调用getUnsafe方法的类为引导类加载器所加载时才合法;
     * <p>
     * 1.java -Xbootclasspath/a: ${path}   // 其中path为调用Unsafe相关方法的类所在jar包路径
     * <p>
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

    /**
     * java -Xbootclasspath/a: ${path}   // 其中path为调用Unsafe相关方法的类所在jar包路径
     * java -Xbootclasspath: /Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/rt.jar :. com.nivelle.base.jdk.sun.UnsafeClassDemo
     *
     * @return
     */
    public static Unsafe getUnSafe() {
        Unsafe unsafe = Unsafe.getUnsafe();
        return unsafe;
    }


}
