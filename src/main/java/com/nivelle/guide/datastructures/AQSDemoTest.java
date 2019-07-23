package com.nivelle.guide.datastructures;

/**
 * AQS
 *
 * @author fuxinzhong
 * @date 2019/07/19
 */
public class AQSDemoTest {

    public static void main(String[] args) throws Exception {
        AQSDemo aqsDemo = new AQSDemo();

        Thread thread1 = new Thread(() -> {
            Thread.currentThread().setName("thread1");
            aqsDemo.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " ：is run");
                System.out.println(Thread.currentThread().getName() + " ：will sleep 100 ");
                try {
                    Thread.sleep(100L);
                    System.out.println(Thread.currentThread().getName() + " ：continue");
                } catch (InterruptedException e) {
                    System.out.println(Thread.currentThread().getName() + " ：interrupted");
                    Thread.currentThread().interrupt();
                }
            } finally {
                aqsDemo.unlock();
                System.out.println(Thread.currentThread().getName() + "：run end");

            }
        });

        Thread thread2 = new Thread(() -> {
            Thread.currentThread().setName("thread2");
            aqsDemo.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "： is run");
                System.out.println(Thread.currentThread().getName() + " ：will sleep 100 ");
                //可重入锁
                aqsDemo.lock();
                //可重入锁,获取多少次就必须要释放多少次
                Thread.sleep(100);
                aqsDemo.unlock();
                System.out.println(Thread.currentThread().getName() + "： continue");
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + " ：interrupted");
                Thread.currentThread().interrupt();
            } finally {
                aqsDemo.unlock();
                System.out.println(Thread.currentThread().getName() + "：run end");

            }
        });

        Thread thread3 = new Thread(() -> {
            Thread.currentThread().setName("thread3");
            aqsDemo.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " ：is run");
                System.out.println(Thread.currentThread().getName() + " ：will sleep 100 ");
                try {
                    //若不释放锁，则必须等待当前线程执行完毕，才可能继续执行thread1
                    aqsDemo.unlock();
                    Thread.sleep(100L);
                    System.out.println("thread3 continue");
                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName() + " ：interrupted");
                    Thread.currentThread().interrupt();
                }
            } finally {
                aqsDemo.unlock();
                System.out.println(Thread.currentThread().getName() + "：run end");
            }
        });
        thread2.start();
        thread3.start();
        thread1.start();
        Thread.sleep(100L);
    }
}
