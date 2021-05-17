package com.nivelle.core.jdk.concurrent.locks;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * 在Java 8中引入了一种锁的新机制——StampedLock，它可以看成是读写锁的一个改进版本。
 * <p>
 * 1. StampedLock提供了一种乐观读锁的实现，这种乐观读锁类似于无锁的操作，完全不会阻塞写线程获取写锁，从而缓解读多写少时写线程“饥饿”现象。
 * 2. 由于StampedLock提供的乐观读锁不阻塞写线程获取读锁，当线程共享变量从主内存load到线程工作内存时，会存在数据不一致问题。
 * 3. StampedLock 不支持重入
 * 4. StampedLock 的悲观读锁、写锁都不支持条件变量
 * 5. 如果线程阻塞在 StampedLock 的 readLock() 或者 writeLock() 上时，此时调用该阻塞线程的 interrupt() 方法，会导致 CPU 飙升
 * 6. 使用 StampedLock 一定不要调用中断操作，如果需要支持中断功能，一定使用可中断的悲观读锁 readLockInterruptibly() 和写锁 writeLockInterruptibly()。
 */
public class StampedLockMock {

    private int x;
    private int y;
    public StampedLock stampedLock = null;

    {
        /**
         * 1. state的初始值为 ORIGIN（256）,它的二进制是 1 0000 0000,也就是初始版本号。
         *
         * 2. state的高24位存储的是版本号,低8位存储的是是否有加锁,第8位存储的是写锁,
         *
         * 3. 低7位存储的是读锁被获取的次数,而且如果只有第8位存储写锁的话,那么写锁只能被获取一次,也就不可能重入了。
         *
         */
        stampedLock = new StampedLock();
    }

    /**
     * 1. 写模式
     *
     * @param deltaX
     * @param deltaY
     */
    public void move(int deltaX, int deltaY) {
        // 获取写锁,返回一个版本号（戳）
        long stamp = stampedLock.writeLock();
        System.out.println("获取悲观写锁:" + stampedLock.isWriteLocked());
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            // 释放写锁，需要传入上面获取的版本号
            stampedLock.unlockWrite(stamp);
        }
        System.out.println("写锁执行之后x:" + x + "写锁执行之后y:" + y);
    }

    /**
     * 2. 乐观读:乐观读操作是无锁的
     *
     * @return
     */
    public double distanceFromOrigin() {
        // 乐观读
        long stamp = stampedLock.tryOptimisticRead();
        int currentX = x;
        int currentY = y;
        // 验证版本号是否有变化，验证下是否有写操作修改了版本号
        if (!stampedLock.validate(stamp)) {
            System.err.println("乐观读获取锁失败");
            // 版本号变了，乐观读转悲观读
            stamp = stampedLock.readLock();
            System.out.println("乐观读失败,重新获取悲观读锁:" + stampedLock.isReadLocked());
            try {
                // 重新读取x、y的值
                currentX = x + x;
                currentY = y + y;
            } finally {
                // 释放读锁，需要传入上面获取的版本号
                stampedLock.unlockRead(stamp);
            }
        }
        System.out.println("悲观读校验版本号：" + stampedLock.validate(stamp));
        x = currentX * currentX;
        y = currentY * currentY;
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }

    /**
     * 3. 悲观读
     *
     * @param newX
     * @param newY
     */
    public void moveIfAtOrigin(int newX, int newY) {
        // 获取悲观读锁
        long stamp = stampedLock.readLock();
        System.out.println("悲观读锁，获取的悲观读锁的版本号，stamp:" + stamp);
        try {
            while (x == 0 && y == 0) {
                // 转为写锁
                long wStamp = stampedLock.tryConvertToWriteLock(stamp);
                System.out.println("转换为悲观读锁的版本号，wStamp:" + wStamp);
                // 转换成功
                if (wStamp != 0) {
                    stamp = wStamp;
                    x = newX;
                    y = newY;
                    break;
                } else {
                    // 转换失败
                    stampedLock.unlockRead(stamp);
                    // 获取写锁
                    stamp = stampedLock.writeLock();
                }
            }
        } finally {
            // 释放锁
            stampedLock.unlock(stamp);
        }
        System.out.println("悲观读之后的值，x:=" + x + "y:=" + y);
    }


    /**
     * StampedLock具有三种模式:写模式、悲观读模式、乐观读模式。
     * <p>
     * 1. 写锁、悲观读锁的语义和 ReadWriteLock 的写锁、读锁的语义非常类似，允许多个线程同时获取悲观读锁，但是只允许一个线程获取写锁，写锁和悲观读锁是互斥的
     * <p>
     * 2. StampedLock 里的写锁和悲观读锁加锁成功之后，都会返回一个 stamp；然后解锁的时候，需要传入这个 stamp
     */
    public static void main(String[] args) throws Exception {

        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5));

        StampedLockMock stampedLockDemo = new StampedLockMock();
        stampedLockDemo.move(1, 3);
        double result = stampedLockDemo.distanceFromOrigin();
        System.err.println(result);
        stampedLockDemo.moveIfAtOrigin(4, 5 / 1);
        for (int i = 0; i < 10; i++) {
            executor.execute(new Lock1(stampedLockDemo));
            Thread.sleep(200);
        }
        for (int j = 0; j < 10; j++) {
            executor.execute(new Lock2(stampedLockDemo));
        }
    }
}

class Lock1 implements Runnable {

    private StampedLockMock stampedLockDemo;

    public Lock1(StampedLockMock stampedLockDemo) {
        this.stampedLockDemo = stampedLockDemo;
    }

    @Override
    public void run() {
        stampedLockDemo.distanceFromOrigin();
    }
}

class Lock2 implements Runnable {

    private StampedLockMock stampedLockDemo;

    public Lock2(StampedLockMock stampedLockDemo) {
        this.stampedLockDemo = stampedLockDemo;
    }

    @Override
    public void run() {
        stampedLockDemo.move(10, 11);
    }
}
