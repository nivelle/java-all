package com.nivelle.base.datastructures;

import java.util.concurrent.CountDownLatch;

/**
 * 自己实现一个 CountDownLatch
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class CountDownLatchDemo {

    /**多个线程共享一个state状态值,countDow**/
    public static void main(String[] args) throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(2);

        new Thread(() -> {
            System.out.println("第一次执行前,当前的count:" + countDownLatch.getCount());

            try {
                Thread.sleep(1000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("第一次执行后,当前的count:" + countDownLatch.getCount());
        }).start();

        new Thread(() -> {
            System.out.println("第二次执行前,当前的count:" + countDownLatch.getCount());
            try {
                Thread.sleep(1000);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("第二次执行后,当前的count:" + countDownLatch.getCount());
        }).start();

        long start = System.currentTimeMillis();
        /**
         * 主线程都会等待state等于0的时候才会继续往下执行，否则阻塞
         */
        System.out.println("主线程执行到这里,被阻塞了" + Thread.currentThread().getState().name());
        boolean interruptedStatueBefore = Thread.currentThread().isInterrupted();
        System.out.println("中断前的状态:" + interruptedStatueBefore);

        /**
         * 阻塞状态可以被中断异常取消。
         */
        Thread.sleep(5000);
        if (countDownLatch.getCount() > 1) {
            Thread.currentThread().interrupt();
            boolean interruptedStatueAfter = Thread.currentThread().isInterrupted();
            System.out.println("中断后的状态:" + interruptedStatueAfter);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        long end = System.currentTimeMillis();
        System.out.println("被阻挡了:" + (end - start));
        System.out.println(Thread.currentThread().getName() + "执行完了");

    }

}
