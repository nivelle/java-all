package com.nivelle.core.javacore.concurrent;

import java.util.concurrent.CyclicBarrier;

/**
 * 自己实现一个 CyclicBarrier
 *
 * @author nivelle
 * @date 2019/06/16
 */
public class CyclicBarrierMock {

    /**
     * 1. CyclicBarrier 是一组线程之间互相等待
     * <p>
     * 2. CyclicBarrier 的计数器是可以循环利用的，而且具备自动重置的功能，一旦计数器减到 0 会自动重置到你设置的初始值
     */

    public static void main(String[] args) throws Exception {
        /**
         * 栅栏,
         */
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + ":CyclicBarrier 执行前" + cyclicBarrier.getParties());
            try {
                System.out.println(Thread.currentThread().getName() + ":等待其他线程");
                cyclicBarrier.await();
                Thread.sleep(500);
                System.out.println(Thread.currentThread().getName() + ":执行完毕:" + cyclicBarrier.getParties());
            } catch (Exception e) {
                System.out.println(e);
            }
        }).start();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "->CyclicBarrier 执行前" + cyclicBarrier.getParties());
            try {
                System.out.println(Thread.currentThread().getName() + ":等待其他线程");
                cyclicBarrier.await();
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + ":执行完毕" + cyclicBarrier.getParties());
            } catch (Exception e) {
                System.out.println(e);
            }
        }).start();
        System.out.println("主线程等待其他线程执行完毕:" + Thread.currentThread().getName() + "执行完毕:" + cyclicBarrier.getParties());
        cyclicBarrier.await();
        System.out.println(Thread.currentThread().getName() + "执行完毕");
    }
}
