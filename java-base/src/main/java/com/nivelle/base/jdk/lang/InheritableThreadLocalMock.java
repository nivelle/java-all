package com.nivelle.base.jdk.lang;

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
public class InheritableThreadLocalMock {
    private static final ThreadLocal<Person> THREAD_LOCAL = new InheritableThreadLocal<>();
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2);

    @Test
    public void fun1() throws InterruptedException {
        THREAD_LOCAL.set(new Person());
        THREAD_POOL.execute(() -> getAndPrintData());
        TimeUnit.SECONDS.sleep(2);
        Person newPerson = new Person();
        newPerson.setAge(100);
        THREAD_LOCAL.set(newPerson); // 给线程重新绑定值
        THREAD_POOL.execute(() -> getAndPrintData());
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
    // 实现类使用TTL的实现
    private static final ThreadLocal<Person> THREAD_LOCAL_TTL = new TransmittableThreadLocal<>();

    // 线程池使用TTL包装一把
    private static final ExecutorService THREAD_POOL_TTL = TtlExecutors.getTtlExecutorService(Executors.newSingleThreadExecutor());


    @Test
    public void fun2() throws InterruptedException {
        THREAD_LOCAL_TTL.set(new Person());
        THREAD_POOL_TTL.execute(() -> getAndPrintDataLocal());
        TimeUnit.SECONDS.sleep(2);
        Person newPerson = new Person();
        newPerson.setAge(100);
        THREAD_LOCAL_TTL.set(newPerson); // 给线程重新绑定值
        THREAD_POOL_TTL.execute(() -> getAndPrintDataLocal());
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


    private void setData(Person person) {
        System.out.println("set数据，线程名：" + Thread.currentThread().getName());
        THREAD_LOCAL.set(person);
    }

    private Person getAndPrintData() {
        Person person = THREAD_LOCAL.get();
        System.out.println("get数据，线程名：" + Thread.currentThread().getName() + "，数据为：" + person);
        return person;
    }

    private Person getAndPrintDataLocal(){
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
