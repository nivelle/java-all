package com.nivelle.base.javacore.datastructures.concurrent;

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
 * @author nivell
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
            //executor.execute(new MyTask1(concurrentHashMap));
        }
        ConcurrentHashMap concurrentHashMap1 = new ConcurrentHashMap(8);

        ConcurrentHashMap concurrentHashMap2 = new ConcurrentHashMap(8, 0.75F);
        /**
         * concurrencyLevel: as estimated(估计) threads
         */
        ConcurrentHashMap concurrentHashMap3 = new ConcurrentHashMap(8, 0.75f, 16);

        /**
         * 初始化容量
         */
        int initialCapacity = 10;
        int MAXIMUM_CAPACITY = 10;
        int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY : (initialCapacity + (initialCapacity >>> 1) + 1));
        System.out.println(cap);
        System.out.println(initialCapacity + (initialCapacity >>> 1) + 1);
        System.out.println((initialCapacity >>> 1));

        /**
         * public V put(K key, V value) {
         *         return putVal(key, value, false);// onlyIfAbsent:是否只有在不存在的时候添加
         *  }
         */
        concurrentHashMap3.put(1, 1);
        System.out.println(concurrentHashMap3);

        int h = 1;
        int HASH_BITS = 0x7fffffff;//
        System.out.println("hash值:" + ((h ^ (h >>> 16)) & HASH_BITS));
        System.out.println("hash值:" + ((h ^ (h >>> 16)) & Integer.MAX_VALUE));

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
            System.err.println(e.getCause());

        }
        System.out.println(map);

    }
}
