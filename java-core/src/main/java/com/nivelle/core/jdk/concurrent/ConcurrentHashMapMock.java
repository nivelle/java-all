package com.nivelle.core.jdk.concurrent;

import java.util.HashMap;
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
 * @author nivelle
 * @date 2019/06/16
 */
public class ConcurrentHashMapMock {

    public static void main(String[] args) {

        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5), new ThreadPoolExecutor.CallerRunsPolicy());
        /**
         * 非线程安全:HashMap
         * 线程安全:concurrentHashMap
         */
        final HashMap<AtomicInteger, String> map = new HashMap(2);
        final ConcurrentHashMap<AtomicInteger, String> concurrentHashMap = new ConcurrentHashMap(2);

        for (int i = 0; i < 1; i++) {
            executor.execute(new MyTask1(concurrentHashMap));
        }
        ConcurrentHashMap concurrentHashMap1 = new ConcurrentHashMap(8);
        concurrentHashMap1.putIfAbsent(1, 1);
        concurrentHashMap1.putIfAbsent(2, 2);
        concurrentHashMap1.putIfAbsent(3, 10);
        ConcurrentHashMap<Integer, Integer> concurrentHashMap2 = new ConcurrentHashMap(8, 0.75F);
        concurrentHashMap2.putAll(concurrentHashMap1);
        System.out.println("直接添加一个集合:" + concurrentHashMap2);
        /**
         * computeIfAbsent 如果key不存在,则处理返回新value
         */
        Object value = concurrentHashMap2.computeIfAbsent(4, key -> {
            return 4;
        });
        System.out.println("computeIfAbsent set value:" + value);
        /**
         * 与put一样，但是返回的是新值。 put返回的是旧值,没有则返回null
         *
         * 当key不存在时，执行value计算方法，计算value
         */
        Integer newValue = concurrentHashMap2.compute(4, (k, v) -> {
            return k + v;
        });
        System.out.println("compute set value:" + newValue);

        System.err.println("concurrentHashMap2 is:" + concurrentHashMap2);
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


        System.out.println("获取元素:" + concurrentHashMap3.get(1));
        System.out.println("删除元素:" + concurrentHashMap3.remove(1));

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
