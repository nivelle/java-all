package com.nivelle.core.javacore.jdk.concurrent.atom;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AtomicBoolean
 *
 * @author nivelle
 * @date 2020/04/13
 */
public class AtomicBooleanMock {


    @Test(threadPoolSize = 10, invocationCount = 10, timeOut = 1000)
    public void parallelTest() {
        System.out.println(Thread.currentThread().getName());
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        atomicBoolean.set(false);
        Assert.assertFalse(atomicBoolean.get());
    }

    @Test(threadPoolSize = 20, invocationCount = 10, timeOut = 1000)
    public void parallelTest2() {
        System.out.println("thread id:" + Thread.currentThread().getId());
        Boolean baseBoolean = Boolean.FALSE;
        baseBoolean = Boolean.TRUE;
        while (baseBoolean) {
            System.out.println("true:" + baseBoolean);
        }
        System.out.println("false:" + baseBoolean);
    }


}
