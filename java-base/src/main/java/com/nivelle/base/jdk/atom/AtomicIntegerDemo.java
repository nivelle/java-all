package com.nivelle.base.jdk.atom;

import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicInteger
 *
 * @author nivelle
 * @date 2020/04/13
 */
public class AtomicIntegerDemo {

//    @Test()
//    public void parallelTest1() {
//        System.out.println("thread id:" + Thread.currentThread().getId());
//        AtomicInteger atomicInteger = new AtomicInteger(1);
//        atomicInteger.incrementAndGet();
//        System.out.println(atomicInteger);
//    }

    @Test()
    public void parallelTest2() {
        System.out.println("thread id:" + Thread.currentThread().getId());
        Integer integer = new Integer(1);
        while (true) {
            integer++;
            System.out.println(integer);
            if (integer>50){
                break;
            }
        }
    }
}
