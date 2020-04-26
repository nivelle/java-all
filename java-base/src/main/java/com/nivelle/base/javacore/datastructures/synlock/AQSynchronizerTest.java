package com.nivelle.base.javacore.datastructures.synlock;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * AQS
 *
 * @author nivell
 * @date 2019/07/19
 */
public class AQSynchronizerTest {

    public static void main(String[] args) throws Exception {

        MyAQSynchronizer myAQSynchronizer = new MyAQSynchronizer();

        Thread thread1 = new Thread(() -> {
            Thread.currentThread().setName("thread1");
            myAQSynchronizer.lock();
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
                myAQSynchronizer.unlock();
                System.out.println(Thread.currentThread().getName() + "：run end");

            }
        });

        Thread thread2 = new Thread(() -> {
            Thread.currentThread().setName("thread2");
            myAQSynchronizer.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "： is run");
                System.out.println(Thread.currentThread().getName() + " ：will sleep 100 ");
                //可重入锁
                myAQSynchronizer.lock();
                //可重入锁,获取多少次就必须要释放多少次
                Thread.sleep(100);
                myAQSynchronizer.unlock();
                System.out.println(Thread.currentThread().getName() + "： continue");
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + " ：interrupted");
                Thread.currentThread().interrupt();
            } finally {
                myAQSynchronizer.unlock();
                System.out.println(Thread.currentThread().getName() + "：run end");

            }
        });

        Thread thread3 = new Thread(() -> {
            Thread.currentThread().setName("thread3");
            myAQSynchronizer.lock();
            try {
                System.out.println(Thread.currentThread().getName() + " ：is run");
                System.out.println(Thread.currentThread().getName() + " ：will sleep 100 ");
                try {
                    //若不释放锁，则必须等待当前线程执行完毕，才可能继续执行thread1
                    myAQSynchronizer.unlock();
                    Thread.sleep(100L);
                    System.out.println("thread3 continue");
                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName() + " ：interrupted");
                    Thread.currentThread().interrupt();
                }
            } finally {
                myAQSynchronizer.unlock();
                System.out.println(Thread.currentThread().getName() + "：run end");
            }
        });
        thread2.start();
        thread3.start();
        thread1.start();
        Thread.sleep(100L);
    }
}

/**
 * AbstractQueuedSynchronizer简称AQS，抽象的队列式的同步器
 *
 * 1. AQS定义了两种资源共享方式：
 * 1. Exclusive（独占，只有一个线程能获取锁，其他线程都必须等到拥有锁的线程释放掉锁之后才可以争夺锁，如ReentrantLock）
 * <p>
 * 2. Share（共享，多个线程可同时获得锁，同时执行。如 Semaphore/ CountDownLatch）
 */
class MyAQSynchronizer extends AbstractQueuedSynchronizer {

    @Override
    protected boolean tryAcquire(int arg) {
        final Thread current = Thread.currentThread();
        Thread exclusiveThread = getExclusiveOwnerThread();
        //同一个线程,实现可重入锁
        if (exclusiveThread != null) {
            if (current.getId() == exclusiveThread.getId()) {
                if (compareAndSetState(getState(), getState() + arg)) {
                    setExclusiveOwnerThread(current);
                    System.out.println("tryAcquire again true:" + Thread.currentThread().getName() + ":state:" + getState());
                    System.out.println("tryAcquire again true:" + Thread.currentThread().getName() + ":isHeldExclusively:" + isHeldExclusively());
                    return true;
                }
            }
        }
        if (compareAndSetState(0, getState() + arg)) {
            //设置独占锁拥有者线程（模版方法）
            setExclusiveOwnerThread(current);
            System.out.println("tryAcquire true:" + Thread.currentThread().getName() + ":state:" + getState());
            System.out.println("tryAcquire true:" + Thread.currentThread().getName() + ":isHeldExclusively:" + isHeldExclusively());
            return true;
        }
        return false;
    }

    @Override
    protected boolean tryRelease(int arg) {
        System.out.println("tryRelease before:" + Thread.currentThread().getName() + ":getState() is:" + getState());
        setState(getState() - 1);
        if (getState() == 0) {
            setExclusiveOwnerThread(null);
        }
        System.out.println("tryRelease after:" + Thread.currentThread().getName() + ":isHeldExclusively:" + isHeldExclusively());
        return true;
    }

    @Override
    protected boolean isHeldExclusively() {

        Thread thread = super.getExclusiveOwnerThread();
        if (thread != null) {
            return Thread.currentThread().getId() == thread.getId() ? true : false;
        }
        return false;

    }

    public void lock() {
        acquire(1);
    }

    public void unlock() {
        release(1);
    }

}
