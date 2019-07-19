package com.nivelle.guide.datastructures;

/**
 * AQS
 *
 * @author fuxinzhong
 * @date 2019/07/19
 */
public class AQSDemoTest {

    public static void main(String[] args) {
        AQSDemo aqsDemo = new AQSDemo();
        Thread thread1 = new Thread(() -> {
            aqsDemo.lock();
            try {
                System.out.println(" thread1 run");
                System.out.println(" thread1 will sleep 5 secs");
                try {
                    Thread.sleep(100L);
                    System.out.println("thread1 continue");
                } catch (InterruptedException e) {
                    System.out.println(" tread1 interrupted");
                    Thread.currentThread().interrupt();
                }
            } finally {
                aqsDemo.unlock();
                System.out.println("thread1 run end");

            }
        });
        Thread thread2 = new Thread(() -> {
            aqsDemo.lock();
            try {
                System.out.println("tread2 run");
                System.out.println(" thread2 will sleep 4 secs");
                Thread.sleep(1000L);
                System.out.println("thread2 continue");
            } catch (InterruptedException e) {
                System.out.println(" thread2 interrupted");
                Thread.currentThread().interrupt();
            } finally {
                aqsDemo.unlock();
                System.out.println("thread2 run end");

            }
        });
        Thread thread3 = new Thread(() -> {
            aqsDemo.lock();
            try {
                System.out.println(" thread3 run");
                System.out.println(" thread3 will sleep 3 secs");
                try {
                    //若不释放锁，则必须等待当前线程执行完毕，才可能继续执行thread1
                    //aqsDemo.unlock();
                    Thread.sleep(5000L);
                    System.out.println("thread3 continue");
                } catch (Exception e) {
                    System.out.println(" thread3 interrupted");
                    Thread.currentThread().interrupt();
                }
            } finally {
                aqsDemo.unlock();
                System.out.println("thread3 run end");
            }
        });
        thread2.start();
        thread3.start();
        thread1.start();

    }
}
