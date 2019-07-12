package com.nivelle.guide.javacore.thread;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * aqs
 */
public class MySynchronizer extends AbstractQueuedSynchronizer {

    @Override
    protected boolean tryAcquire(int arg) {
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }

    @Override
    protected boolean tryRelease(int arg) {
        setState(0);
        setExclusiveOwnerThread(null);
        return true;
    }

    public void lock() {
        acquire(1);
    }

    public void unlock() {
        release(1);
    }

    public static void main(String[] args) {
        MySynchronizer mySynchronizer = new MySynchronizer();
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

