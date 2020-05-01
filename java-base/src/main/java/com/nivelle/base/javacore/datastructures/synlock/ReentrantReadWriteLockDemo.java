package com.nivelle.base.javacore.datastructures.synlock;


import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReentrantReadWriteLock 读写锁
 *
 * @author nivell
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

        readLock.lock();
        System.out.println();

    }
}
