package com.nivelle.core.jdk.lang;

import com.nivelle.core.pojo.User;
import org.springframework.stereotype.Service;

/**
 * ThreadLocal
 *
 * @author nivelle
 * @date 2019/06/16
 */
@Service
public class ThreadLocalMock {
    /**
     * This class provides thread-local variables【线程局部变量】These variables differ from
     * their normal counterparts【同行】 in that each thread that accesses one (via its
     * {@code get} or {@code set} method) has its own, independently initialized
     * copy of the variable.  {@code ThreadLocal} instances are typically private
     * static fields in classes that wish to associate state with a thread (e.g.,
     * a user ID or Transaction ID).
     * <p>
     * ThreadLocal 实例通常是类中的 private static 字段,它们希望将状态与某一个线程（例如，用户 ID 或事务 ID）相关联。
     */
    /**
     * 1. 线程有个 threadLocal.ThreadLocalMap threadLocals = null; 的成员变量,这个成员变量是map结构
     * <p>
     * 2. threadLocal作为key, 要保存的线程变量为值
     */

    public static void main(String[] args) {
        /**
         * 每个线程保留一个副本,线程安全。
         */
        ThreadLocal threadLocal1 = ThreadLocal.withInitial(() -> 0);
        ThreadLocal threadLocal2 = ThreadLocal.withInitial(() -> "one");
        new Thread(() -> {
            //获取当前线程的ThreadLocalMap ,将 threadLocal作为key , 3作为value 设置到thread的成员变量里
            threadLocal1.set(3);
            threadLocal2.set("three");
            //get()方法会先获取调用者线程,然后获取调用者线程中 key 为 当前threadLocal的实例
            System.out.println(Thread.currentThread().getName() + ":" + threadLocal1.get());
            System.out.println(Thread.currentThread().getName() + ":" + threadLocal2.get());

        }, "thread3").start();
        System.out.println(Thread.currentThread().getName() + ":" + threadLocal1.get());
        System.out.println(Thread.currentThread().getName() + ":" + threadLocal2.get());
        System.out.println("threadlocal 传递参数");
        changeParams();
    }

    /**
     * Thread 类依赖了 ThreadLocal 类的内部静态类:ThreadLocalMap,（一个Thread只对应一个 ThreadLocalMap）ThreadLocalMap 通过Thread.currentThread获取当前使用它的线程;
     *
     * 实现原理:每个Thread 维护一个 ThreadLocalMap 映射表，这个映射表的 key 是 ThreadLocal实例本身，value 是真正需要存储的 Object。(ThreadLocalMap 的key是弱引用的 ThreadLocal,可以对应多个)
     */

    /**
     * 使用弱应用的风险:ThreadLocalMap使用ThreadLocal的弱引用作为key,如果一个ThreadLocal没有外部强引用来引用它,那么系统 GC 的时候，这个ThreadLocal势必会被回收，这样一来，ThreadLocalMap中就会出现key为null的Entry,
     * 也就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话,这些key为null的Entry的value就会一直存在一条强引用链:
     * Thread Ref -> Thread -> ThreadLocalMap -> Entry -> value永远无法回收，造成内存泄漏;
     *
     * 当把 threadLocal 实例置为null以后，没有任何强引用指向 ThreadLocal 实例，所以 ThreadLocal 将会被gc回收
     */


    /**
     *  使用弱应用的目的:Entry继承WeakReference,使用弱引用，可以将ThreadLocal对象的生命周期和线程生命周期解绑,持有对ThreadLocal的弱引用,可以使得ThreadLocal在没有其他强引用的时候被回收掉，这样可以避免因为线程得不到销毁导致ThreadLocal对象无法被回收。
     */

    private static final ThreadLocal<User> THREAD_LOCAL = new ThreadLocal<>();

    private static void changeParams() {


        // 方法入口处，设置一个变量和当前线程绑定
        setData(new User(12,"jessy"));
        // 调用其它方法，其它方法内部也能获取到刚放进去的变量
        getAndPrintData();

        System.out.println("======== Finish =========");


    }

    private static void setData(User user) {
        System.out.println("set数据，线程名：" + Thread.currentThread().getName());
        THREAD_LOCAL.set(user);
    }

    private static void getAndPrintData() {
        // 拿到当前线程绑定的一个变量，然后做逻辑（本处只打印）
        User user = THREAD_LOCAL.get();
        System.out.println("get数据，线程名：" + Thread.currentThread().getName() + "，数据为：" + user);
    }


}

