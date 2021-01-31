package com.nivelle.base.jdk.concurrent.atom;

import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicInteger
 *
 * @author nivelle
 * @date 2020/04/13
 */
public class AtomicIntegerMock {

    Integer integer = new Integer(1);

    AtomicInteger atomicInteger = new AtomicInteger(1);

    @Test(threadPoolSize = 4, invocationCount = 4)
    public void parallelTest1() {
        while (true) {
            atomicInteger.incrementAndGet();
            int value = atomicInteger.get();
            System.out.println("parallelTest1 id " + Thread.currentThread().getId() + "-->value:" + value);
            if (atomicInteger.get() > 5) {
                break;
            }
        }
    }

    @Test(threadPoolSize = 10, invocationCount = 10)
    public void parallelTest2() {
        /**
         * parallelTest2 id 12-->value:2
         * parallelTest2 id 12-->value:3
         * parallelTest2 id 12-->value:4
         * parallelTest2 id 12-->value:5
         * parallelTest2 id 12-->value:6
         * parallelTest2 id 11-->value:7
         * parallelTest2 id 13-->value:8
         * parallelTest2 id 10-->value:9
         * parallelTest2 id 15-->value:10
         * parallelTest2 id 14-->value:10
         * parallelTest2 id 16-->value:11
         * parallelTest2 id 17-->value:12
         * parallelTest2 id 18-->value:13
         * parallelTest2 id 19-->value:14
         */
        while (true) {
            integer++;
            System.out.println("parallelTest2 id " + Thread.currentThread().getId() + "-->value:" + integer);
            if (integer > 5) {
                break;
            }
        }
    }
}
