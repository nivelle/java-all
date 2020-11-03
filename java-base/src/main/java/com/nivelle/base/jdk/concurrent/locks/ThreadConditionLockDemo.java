package com.nivelle.base.jdk.concurrent.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * condition
 *
 * @author nivellefu
 */
public class ThreadConditionLockDemo {

    public static void main(String args[]) {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        Thread thread0 = new Thread(() -> {

            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + ":->run");
                System.out.println(Thread.currentThread().getName() + ":->wait for condition");
                try {
                    condition.await();
                    //中断状态只是个标识，如果没有处理则不会对任务产生影响
                    Thread.currentThread().interrupt();
                    System.out.println(Thread.currentThread().getName() + ":continue run");
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread().getName() + ":interrupted");
                }

            } finally {
                System.out.println(Thread.currentThread().isInterrupted());
                lock.unlock();
            }

        });

        Thread thread1 = new Thread(() -> {

            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + ":condition run");
                System.out.println(Thread.currentThread().getName() + ":condition sleep 5 secs");
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    System.err.println(Thread.currentThread().getName() + ":interrupted");
                }
                Thread.currentThread().interrupt();
                condition.signalAll();
            } finally {
                lock.unlock();
                System.out.println("thread1 " + Thread.currentThread().isInterrupted());
            }

        });

        Thread thread2 = new Thread(() -> {

            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + ":拿到锁且不释放");
            } finally {
                lock.unlock();
                System.out.println("thread2 " + Thread.currentThread().isInterrupted());
            }

        });

        thread0.start();

        thread1.start();

        thread2.start();

    }
}
