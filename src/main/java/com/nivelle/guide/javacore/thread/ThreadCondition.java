package com.nivelle.guide.javacore.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * condition
 */
public class ThreadCondition {

    public static void main(String args[]) {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Thread thread1 = new Thread(() -> {

            lock.lock();

            try {
                System.out.println(Thread.currentThread().getName() + "run");
                System.out.println(Thread.currentThread().getName() + "wait for condition");

                try {
                    condition.await();
                    System.out.println(Thread.currentThread().getName() + "continue");
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread().getName() + " interrupted");
                    Thread.currentThread().interrupt();
                }

            } finally {
                lock.unlock();
            }

        });


        Thread thread2 = new Thread(() -> {

            lock.lock();

            try {
                System.out.println(Thread.currentThread().getName() + "condition run");
                System.out.println(Thread.currentThread().getName() + "condition sleep 5 secs");

                try {
                    Thread.sleep(5000l);
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread().getName() + " interrupted");
                    Thread.currentThread().interrupt();
                }
                condition.signalAll();

            } finally {
                lock.unlock();
            }

        });
        thread1.start();
        thread2.start();

    }
}
