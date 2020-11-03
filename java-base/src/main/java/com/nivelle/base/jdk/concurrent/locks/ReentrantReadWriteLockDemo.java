package com.nivelle.base.jdk.concurrent.locks;


import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReentrantReadWriteLock 读写锁
 *
 * @author nivelle
 * @date 2019/06/16
 */
public class ReentrantReadWriteLockDemo {
    public static void main(String[] args) {

        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();

        /**
         * 读写锁构造函初始化的时候构建一个Sync 然后构建 readLock 和 writeLock
         *
         * public ReentrantReadWriteLock(boolean fair) {
         *         sync = fair ? new FairSync() : new NonfairSync();
         *         readerLock = new ReadLock(this);
         *         writerLock = new WriteLock(this);
         *     }
         */
        ReentrantReadWriteLock reentrantReadWriteLock1 = new ReentrantReadWriteLock(true);
        System.out.println(reentrantReadWriteLock1.getQueueLength());
        System.out.println(reentrantReadWriteLock1.getReadHoldCount());
        System.out.println(reentrantReadWriteLock1.getReadLockCount());
        System.out.println(reentrantReadWriteLock1.getWriteHoldCount());
        /**
         *  读锁的获取过程:
         *
         * （1）先尝试获取读锁；
         *
         * （2）如果成功了直接结束；
         *
         * （3）如果失败了，进入doAcquireShared()方法；
         *
         * （4）doAcquireShared()方法中首先会生成一个新节点并进入AQS队列中；
         *
         * （5）如果头节点正好是当前节点的上一个节点，再次尝试获取锁；
         *
         * （6）如果成功了，则设置头节点为新节点，并传播；
         *
         * （7）传播即唤醒下一个读节点（如果下一个节点是读节点的话）；
         *
         * （8）如果头节点不是当前节点的上一个节点或者（5）失败，则阻塞当前线程等待被唤醒；
         *
         * （9）唤醒之后继续走（5）的逻辑；
         */
        readLock.lock();
        readLock.unlock();
        System.out.println();
        /**
         *  写锁获取过程:
         *
         * （1）尝试获取锁；
         *
         * （2）如果有读者占有着读锁，尝试获取写锁失败；
         *
         * （3）如果有其它线程占有着写锁，尝试获取写锁失败；
         *
         * （4）如果是当前线程占有着写锁，尝试获取写锁成功，state值加1；
         *
         * （5）如果没有线程占有着锁（state==0），当前线程尝试更新state的值，成功了表示尝试获取锁成功，否则失败；
         *
         * （6）尝试获取锁失败以后，进入队列排队，等待被唤醒；
         *
         * （7）后续逻辑跟ReentrantLock是一致；
         */
        writeLock.lock();
        writeLock.unlock();

    }
}
