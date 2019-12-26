package com.nivelle.base.datastructures;

import com.nivelle.base.pojo.User;
import org.springframework.stereotype.Service;

/**
 * ThreadLocal
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
@Service
public class ThreadLocalDemo {


    /**
     * This class provides thread-local variables【线程局部变量】These variables differ from
     * their normal counterparts【同行】 in that each thread that accesses one (via its
     * {@code get} or {@code set} method) has its own, independently initialized
     * copy of the variable.  {@code ThreadLocal} instances are typically private
     * static fields in classes that wish to associate state with a thread (e.g.,
     * a user ID or Transaction ID).
     * <p>
     * ThreadLocal 实例通常是类中的 private static 字段，它们希望将状态与某一个线程（例如，用户 ID 或事务 ID）相关联。
     */

    public static void main(String[] args) {

        /**
         * 每个线程保留一个副本,线程安全。
         */
        User user = new User(1, "nivelle");
        ThreadLocal threadLocal = ThreadLocal.withInitial(() -> user.getAge());

        new Thread(() -> {
            threadLocal.set(1);
            System.out.println(Thread.currentThread().getName() + ":" + threadLocal.get());
        }, "thread1").start();
        new Thread(() -> {
            threadLocal.set(2);
            System.out.println(Thread.currentThread().getName() + ":" + threadLocal.get());
        }, "thread2").start();
        new Thread(() -> {
            threadLocal.set(3);
            System.out.println(Thread.currentThread().getName() + ":" + threadLocal.get());
        }, "thread3").start();

        System.out.println(Thread.currentThread().getName() + ":" + threadLocal.get());

    }

    /**
     * Thread 类依赖了 ThreadLocal 类的内部静态类:ThreadLocalMap,（一个Thread只对应一个 ThreadLocalMap）
     * ThreadLocalMap 通过Thread.currentThread获取当前使用它的线程;
     *
     * 实现原理:每个Thread 维护一个 ThreadLocalMap 映射表，这个映射表的 key 是 ThreadLocal实例本身，
     * value 是真正需要存储的 Object。(ThreadLocalMap 的key是弱引用的 ThreadLocal,可以对应多个)
     */

    /**
     * ThreadLocalMap使用ThreadLocal的弱引用作为key，如果一个ThreadLocal没有外部强引用来引用它，
     * 那么系统 GC 的时候，这个ThreadLocal势必会被回收，这样一来，ThreadLocalMap中就会出现key为null的Entry，
     * 就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话，
     * 这些key为null的Entry的value就会一直存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value永远无法回收，
     * 造成内存泄漏;
     * ThreadLocal里面使用了一个存在弱引用的map, map的类型是ThreadLocal.ThreadLocalMap. Map中的key为一个threadlocal实例。
     * 这个Map的确使用了弱引用，不过弱引用只是针对key。每个key都弱引用指向 ThreadLocal。
     *
     * 当把threadlocal实例置为null以后，没有任何强引用指向 ThreadLocal 实例，所以 ThreadLocal 将会被gc回收
     */


}

