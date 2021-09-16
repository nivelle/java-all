package com.nivelle.core.javacore.concurrent;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/05/27
 */

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.Setter;
import lombok.ToString;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * inheritable  实现父子线程参数传递
 *
 * @author fuxinzhong
 * @date 2021/04/21
 */
public  class InheritableThreadLocalMock {
    private static final ThreadLocal<Person> THREAD_LOCAL_0 = new ThreadLocal<>();
    private static final ExecutorService THREAD_POOL_0 = Executors.newFixedThreadPool(1);

    private static final ThreadLocal<Person> THREAD_LOCAL_1 = new InheritableThreadLocal<>();
    private static final ExecutorService THREAD_POOL_1 = Executors.newFixedThreadPool(2);

    // 实现类使用TTL的实现
    private static final ThreadLocal<Person> THREAD_LOCAL_TTL = new TransmittableThreadLocal<>();

    // 线程池使用TTL包装一把
    private static final ExecutorService THREAD_POOL_TTL = TtlExecutors.getTtlExecutorService(Executors.newSingleThreadExecutor());

    @Test
    public void func0() throws InterruptedException {
        Person person = new Person();
        System.out.println("fun0 person:" + person);
        THREAD_LOCAL_0.set(person);
        THREAD_POOL_0.execute(() -> getAndPrintData_0());
        TimeUnit.SECONDS.sleep(2);
        Person newPerson = new Person();
        newPerson.setAge(100);
        THREAD_LOCAL_0.set(newPerson); // 给线程重新绑定值
        THREAD_POOL_0.execute(() -> getAndPrintData_0());
        TimeUnit.SECONDS.sleep(2);
        /**
         * private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();
         *
         * get数据，线程名：pool-1-thread-1，数据为：InheritableThreadLocalMock.Person(age=18)
         * get数据，线程名：pool-1-thread-1，数据为：InheritableThreadLocalMock.Person(age=18)
         */

        /**
         * private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);
         *
         * get数据，线程名：pool-1-thread-1，数据为：InheritableThreadLocalMock.Person(age=18)
         * get数据，线程名：pool-1-thread-2，数据为：InheritableThreadLocalMock.Person(age=100)
         */
        //线程在init初始化的时候，才会去同步一份最新数据过来。
    }

    @Test
    public void func1() throws InterruptedException {
        THREAD_LOCAL_1.set(new Person());
        THREAD_POOL_1.execute(() -> getAndPrintData_1());
        TimeUnit.SECONDS.sleep(2);
        Person newPerson = new Person();
        newPerson.setAge(100);
        THREAD_LOCAL_1.set(newPerson); // 给线程重新绑定值
        THREAD_POOL_1.execute(() -> getAndPrintData_1());
        TimeUnit.SECONDS.sleep(2);
        //线程在init初始化的时候，才会去同步一份最新数据过来。
    }


    @Test
    public void func2() throws InterruptedException {
        THREAD_LOCAL_TTL.set(new Person());
        THREAD_POOL_TTL.execute(() -> getAndPrintData_2());
        TimeUnit.SECONDS.sleep(2);
        Person newPerson = new Person();
        newPerson.setAge(100);
        THREAD_LOCAL_TTL.set(newPerson); // 给线程重新绑定值
        THREAD_POOL_TTL.execute(() -> getAndPrintData_2());
        TimeUnit.SECONDS.sleep(2);
        /**
         * private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor();
         *
         * get数据，线程名：pool-1-thread-1，数据为：InheritableThreadLocalMock.Person(age=18)
         * get数据，线程名：pool-1-thread-1，数据为：InheritableThreadLocalMock.Person(age=18)
         */

        /**
         * private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);
         *
         * get数据，线程名：pool-1-thread-1，数据为：InheritableThreadLocalMock.Person(age=18)
         * get数据，线程名：pool-1-thread-2，数据为：InheritableThreadLocalMock.Person(age=100)
         */
        //线程在init初始化的时候，才会去同步一份最新数据过来。
    }


    private Person getAndPrintData_0() {
        Person person = THREAD_LOCAL_0.get();
        System.out.println(person);
        System.out.println("get数据，线程名：" + Thread.currentThread().getName() + "，数据为：" + person);
        return person;
    }

    private Person getAndPrintData_1() {
        Person person = THREAD_LOCAL_1.get();
        System.out.println("get数据，线程名：" + Thread.currentThread().getName() + "，数据为：" + person);
        return person;
    }

    private Person getAndPrintData_2() {
        Person person = THREAD_LOCAL_TTL.get();
        System.out.println("get数据，线程名：" + Thread.currentThread().getName() + "，数据为：" + person);
        return person;
    }


    @Setter
    @ToString
    private static class Person {
        private Integer age = 18;
    }

}
