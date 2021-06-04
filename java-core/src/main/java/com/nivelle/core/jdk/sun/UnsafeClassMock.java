package com.nivelle.core.jdk.sun;

import com.nivelle.core.pojo.User;
import com.nivelle.core.utils.GsonUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe
 *
 * @author nivelle
 * @date 2019/07/12
 */
public class UnsafeClassMock {
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

        /**
         * 3. 线程调度
         *
         * 包括线程挂起、恢复、锁机制
         */
//        boolean monitorEnter = unsafe.tryMonitorEnter(user);
//        System.out.println("获取对象锁,monitorEnter:" + monitorEnter);
//        boolean monitorEnter2 = unsafe.tryMonitorEnter(user);
//        System.out.println("获取对象锁,monitorEnter2:" + monitorEnter2);
//        System.out.println("当前锁:" + Thread.currentThread().getName());
        Thread.sleep(1000);
        /**
         * public native void park(boolean isAbsolute, long time); // 第一个参数是是否是绝对时间，第二个参数是等待时间值。如果isAbsolute是true则会实现ms定时。如果isAbsolute是false则会实现ns定时。
         *
         * 1. isAbsolute = false,time=0 【LockSupport 的park操作就是此种情况】 :
         * (1) 在调用park()之前调用了unPark或者interrupt则park直接返回,不会挂起
         * (2) 未调用则park会挂起当前线程
         * (3) park未知原因调用则直接返回
         *
         * 2. isAbsolute = false,time>0
         * (1) 在调用park()之前调用了unPark或者interrupt则park直接返回,不会挂起
         * (2) 如果未调用则会挂起当前线程，但是在挂起time ns时如果未收到唤醒信号也会返回继续执行
         * (3) park未知原因调用出错则直接返回（一般不会出现）
         *
         * 3. isAbsolute = true,time为任意数值
         * (1) 在调用park()之前调用了unPark或者interrupt则park直接返回,不会挂起
         * (2) 如果time <= 0则直接返回
         * (3) 如果之前未调用park unpark并且time > 0,则会挂起当前线程，但是在挂起time ms时如果未收到唤醒信号也会返回继续执行
         * (4) park未知原因调用出错则直接返回
         */
        new Thread(() -> {
            synchronized (user) {
                try {
                    System.out.println("获取到了user1的对象锁！" + Thread.currentThread().getName());
                } catch (Exception e) {
                    System.out.println(e + e.getMessage());
                }
            }
            System.out.println("线程：" + Thread.currentThread().getName() + "被挂起！");
            unsafe.park(false, 0);
            unsafe.unpark(Thread.currentThread());
            System.out.println("挂起超时");
        }).start();

        Thread.sleep(600);
        /**
         * 4. Class相关
         *
         * 提供Class和它的静态字段的操作相关方法，包含静态字段内存定位、定义类、定义匿名类、检验&确保初始化等
         *
         *
         */
        /**
         * 而Unsafe中提供allocateInstance方法，仅通过Class对象就可以创建此类的实例对象，而且不需要调用其构造函数、初始化代码、JVM安全检查等。
         * 它抑制修饰符检测，也就是即使构造器是private修饰的也能通过此方法实例化，只需提类对象即可创建相应的对象。由于这种特性，allocateInstance在java.lang.invoke、Objenesis（提供绕过类构造器的对象生成方式）、Gson（反序列化时用到）中都有相应的应用
         */
        User userAllocateInstance = (User) unsafe.allocateInstance(User.class);
        System.out.println("userAllocateInstance before is:" + userAllocateInstance);
        userAllocateInstance.setAge(20);
        userAllocateInstance.setName("AllocateUser");
        System.out.println("userAllocateInstance after is:" + userAllocateInstance);
        //定义一个类，此方法会跳过JVM的所有安全检查，默认情况下，ClassLoader（类加载器）和ProtectionDomain（保护域）实例来源于调用者
        //unsafe.defineClass(className, classFile, 0, classFile.length,BoundMethodHandle.class.getClassLoader(), null)
        final byte[] classFile = GsonUtils.toJson(userAllocateInstance).getBytes();
        //Class<?> myDefineClass = unsafe.defineClass("myDefineClassName", classFile, 0, classFile.length, UnsafeClassDemo.class.getClassLoader(), null);
        //System.out.println("自定义Class:" + myDefineClass.getName());
        //检测给定的类是否已经初始化。通常在获取一个类的静态属性的时候（因为一个类如果没初始化，它的静态属性也不会初始化）使用
        unsafe.ensureClassInitialized(User.class);

        /**
         * 5. 数组相关
         */
        Integer[] array = new Integer[]{1, 2, 3, 4, 7, 8, 9};
        //返回数组中第一个元素的偏移地址
        long startElementAddress = unsafe.arrayBaseOffset(Integer[].class);
        System.out.println("数组首元素地址:" + startElementAddress);
        //返回数组中一个元素占用的大小
        int elementScale = unsafe.arrayIndexScale(Integer[].class);
        System.out.println("数组元素的大小:" + elementScale);

        /**
         * 6.内存屏障相关 例如：stampedLock
         */
        //内存屏障:禁止load操作重排序。屏障前的load操作不能被重排序到屏障后，屏障后的load操作不能被重排序到屏障前
        unsafe.loadFence();
        //内存屏障:禁止store操作重排序。屏障前的store操作不能被重排序到屏障后，屏障后的store操作不能被重排序到屏障前
        unsafe.storeFence();
        //内存屏障: 禁止load、store操作重排序
        unsafe.fullFence();


        /**
         * 7.系统相关
         */
        //返回系统指针的大小。返回值为4（32位系统）或 8（64位系统）。
        int addressSize = unsafe.addressSize();
        System.out.println("系统指针大小：" + addressSize);

        //内存页的大小，此值为2的幂次方
        int pageSize = unsafe.pageSize();
        System.out.println("内存页大小：" + pageSize);

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
