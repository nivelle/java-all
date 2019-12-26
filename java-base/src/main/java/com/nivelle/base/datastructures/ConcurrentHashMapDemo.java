package com.nivelle.base.datastructures;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ConcurrentHashMap
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class ConcurrentHashMapDemo {

    public static void main(String[] args) {

        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5), new ThreadPoolExecutor.CallerRunsPolicy());
        /**
         * 非线程安全:HashMap
         * 线程安全:concurrentHashMap
         */
        //final HashMap<AtomicInteger, String> map = new HashMap(2);
        final ConcurrentHashMap<AtomicInteger, String> concurrentHashMap = new ConcurrentHashMap(2);

        for (int i = 0; i < 1000; i++) {
            executor.execute(new MyTask1(concurrentHashMap));
        }

    }
}



/**
 * 线程安全异常模拟
 */
class MyTask1 implements Runnable {
    Map<AtomicInteger, String> map;

    AtomicInteger atomicInteger = new AtomicInteger(1);

    public MyTask1(Map<AtomicInteger, String> map) {
        this.map = map;
    }
    @Override
    public void run() {
        try {
            Random random = new Random();
            int rand = random.nextInt(100);

            if (rand < 50) {
                map.put(atomicInteger, UUID.randomUUID().toString());
                atomicInteger.incrementAndGet();
            } else {
                map.get(UUID.randomUUID().toString());
            }
        } catch (Exception e) {
            System.err.println(e.getStackTrace());
            System.err.println();
            System.err.println(e.getMessage());
            System.err.println(e.getCause());

        }
        System.out.println(map);

    }
}
