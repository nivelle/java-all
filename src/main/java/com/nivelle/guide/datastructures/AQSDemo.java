package com.nivelle.guide.datastructures;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * AbstractQueuedSynchronizer
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */


/**
 * AbstractQueuedSynchronizer简称AQS，抽象的队列式的同步器
 * <p>
 * 1. AQS定义了两种资源共享方式：Exclusive（独占，只有一个线程能获取锁，其他线程都必须等到拥有锁的线程释放掉锁之后才可以争夺锁，如ReentrantLock）
 * <p>
 * 和Share（共享，多个线程可同时获得锁，同时执行。如Semaphore/CountDownLatch）
 */
public class AQSDemo extends AbstractQueuedSynchronizer {

    @Override
    protected boolean tryAcquire(int arg) {
        if (compareAndSetState(getState(), getState() + arg)) {
            //设置独占锁拥有者线程（模版方法）
            setExclusiveOwnerThread(Thread.currentThread());
            System.out.println("tryAcquire after:" + Thread.currentThread().getName() + ":state:" + getState());
            System.out.println("tryAcquire after:" + Thread.currentThread().getName() + ":isHeldExclusively:" + isHeldExclusively());
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

