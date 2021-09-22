package com.nivelle.core.javacore.base;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HashMap线程不安全
 *
 * @author fuxinzhong
 * @date 2020/11/22
 */
public class HashMapResizeCycleMock {


    public static void main(String[] args) {

        while (true) {
            TestThread testThread = new TestThread();
            testThread.start();
        }

    }
}

class TestThread extends Thread {
    private static HashMap ourHashMap = new HashMap(4);
    private static AtomicInteger ai = new AtomicInteger();

    @Override
    public void run() {
        while (true) {
            ourHashMap.put(ai.get(), ai.get());
            ai.incrementAndGet();
            System.out.println(ourHashMap);
        }
    }
}
