package com.nivelle.guide.datastructures;

import com.nivelle.guide.springboot.pojo.User;
import org.springframework.stereotype.Service;

/**
 * ThreadLocal
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
@Service
public class ThreadLocalData {

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

        System.out.println(Thread.currentThread().getName()+":"+threadLocal.get());

    }

}

