package com.nivelle.base.datastructures;

import java.util.concurrent.CyclicBarrier;

/**
 * 自己实现一个 CyclicBarrier
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class CyclicBarrierDemo {

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
                System.err.println(Thread.currentThread().getName() + ":执行完毕");
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
                System.err.println(Thread.currentThread().getName() + ":执行完毕");
            } catch (Exception e) {
                System.out.println(e);
            }
        }).start();

        System.out.println("主线程等待其他线程执行完毕:" + Thread.currentThread().getName());
        cyclicBarrier.await();
        System.err.println(Thread.currentThread().getName() + "执行完毕");
    }
}
