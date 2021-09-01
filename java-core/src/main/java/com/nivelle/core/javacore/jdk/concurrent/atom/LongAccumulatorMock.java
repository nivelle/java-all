package com.nivelle.core.javacore.jdk.concurrent.atom;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAccumulator;

/**
 * LongAccumulator
 *
 * @author nivelle
 * @date 2020/04/13
 */
public class LongAccumulatorMock {
    /**
     * LongAccumulator是LongAdder的功能增强版。LongAdder的API只有对数值的加减，而LongAccumulator提供了自定义的函数操作
     *
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        LongAccumulator accumulator = new LongAccumulator(Long::max, Long.MIN_VALUE);

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        CountDownLatch countDownLatch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                Random random = new Random();
                long value = random.nextInt(10);
                //比较value和上一次的比较值，然后存储较大的值
                System.out.println(value);
                accumulator.accumulate(value);
            });
            countDownLatch.countDown();
        }
        countDownLatch.await();
        System.out.println("最大值：" + accumulator.longValue());
    }
}
