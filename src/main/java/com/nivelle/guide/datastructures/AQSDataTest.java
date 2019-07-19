package com.nivelle.guide.datastructures;

/**
 * AQS
 *
 * @author fuxinzhong
 * @date 2019/07/19
 */
public class AQSDataTest {

    public static void main(String[] args) {
        AQSDemo mySynchronizer = new AQSDemo();
        Thread thread1 = new Thread(() -> {

            mySynchronizer.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " run");
                System.out.println(Thread.currentThread().getName() + " will sleep 5 secs");
                try {
                    Thread.sleep(5000l);
                    System.out.println(Thread.currentThread().getName() + " continue");
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread().getName() + " interrupted");
                    Thread.currentThread().interrupt();
                }
            } finally {
                mySynchronizer.unlock();
            }
        });
        Thread thread2 = new Thread(() -> {
            mySynchronizer.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " run");
            } finally {
                mySynchronizer.unlock();
            }

        });
        thread1.start();
        thread2.start();
    }
}
