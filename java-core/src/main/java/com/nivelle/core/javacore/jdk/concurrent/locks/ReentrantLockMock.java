package com.nivelle.core.javacore.jdk.concurrent.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock 源码分析
 *
 * @author nivelle
 * @date 2019/06/16
 */
public class ReentrantLockMock {
    /**
     * public class ReentrantLock implements Lock, java.io.Serializable
     * <p>
     * 1. ReentrantLock 主要属性:private final Sync sync;
     */
    public static void main(String[] args) {

        /**
         * public ReentrantLock() {
         *     sync = new NonfairSync(); ## 默认构造方法实现的是非公平锁
         * }
         *
         * 非公平锁的获取过程:
         *
         * （1）一开始就尝试CAS更新状态变量state的值，如果成功了就获取到锁了；
         *
         * （2）在tryAcquire()的时候没有检查是否前面有排队的线程，直接上去获取锁才不管别人有没有排队呢；
         *
         *
         *
         * 为什么ReentrantLock默认采用的是非公平模式？
         *
         * 答：因为非公平模式效率比较高。
         *
         * 为什么非公平模式效率比较高？
         *
         * 答：因为非公平模式会在一开始就尝试两次获取锁，如果当时正好state的值为0，它就会成功获取到锁，少了排队导致的阻塞/唤醒过程，并且减少了线程频繁的切换带来的性能损耗。
         *
         * 非公平模式有什么弊端？
         *
         * 答：非公平模式有可能会导致一开始排队的线程一直获取不到锁，导致线程饿死。
         **/
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        reentrantLock.unlock();
        reentrantLock.getHoldCount();
        reentrantLock.getQueueLength();

        /**
         *  公平锁的获取过程:
         *
         * （1）尝试获取锁，如果获取到了就直接返回了；
         *
         * （2）尝试获取锁失败，再调用addWaiter()构建新节点并把新节点入队；
         *
         * （3）然后调用acquireQueued()再次尝试获取锁,如果成功了,直接返回；
         *
         * （4）如果再次失败,再调用shouldParkAfterFailedAcquire()将节点的等待状态置为等待唤醒（SIGNAL）；
         *
         * （5）调用parkAndCheckInterrupt()阻塞当前线程；
         *
         * （6）如果被唤醒了,会继续在acquireQueued()的for()循环再次尝试获取锁，如果成功了就返回；
         *
         * （7）如果不成功，再次阻塞，重复（3）（4）（5）直到成功获取到锁。
         */
        ReentrantLock reentrantLock1 = new ReentrantLock(true);

        try {
            /**
             * 支持线程中断，它与lock()方法的主要区别在于lockInterruptibly()获取锁的时候如果线程中断了,会抛出一个异常;
             * 而lock()不会管线程是否中断都会一直尝试获取锁,获取锁之后把自己标记为已中断,继续执行自己的逻辑,后面也会正常释放锁。也就是 lockInterruptibly 会关注中断状态
             *
             * 线程中断，只是在线程上打一个中断标志，并不会对运行中的线程有什么影响，具体需要根据这个中断标志干些什么，用户自己去决定。比如，如果用户在调用lock()获取锁后，发现线程中断了，就直接返回了，而导致没有释放锁，这也是允许的，但是会导致这个锁一直得不到释放，就出现了死锁。
             */
            reentrantLock1.lockInterruptibly();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        /**
         * tryLock()方法比较简单，直接以非公平的模式去尝试获取一次锁，获取到了或者锁本来就是当前线程占有着就返回true，否则返回false。
         */
        reentrantLock1.tryLock();

        try {
            reentrantLock1.tryLock(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        reentrantLock1.unlock();

    }
}
