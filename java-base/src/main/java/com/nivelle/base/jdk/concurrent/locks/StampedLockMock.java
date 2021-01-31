package com.nivelle.base.jdk.concurrent.locks;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

/**
 * 在Java 8中引入了一种锁的新机制——StampedLock，它可以看成是读写锁的一个改进版本。
 * <p>
 * StampedLock提供了一种乐观读锁的实现，这种乐观读锁类似于无锁的操作，完全不会阻塞写线程获取写锁，从而缓解读多写少时写线程“饥饿”现象。
 * 由于StampedLock提供的乐观读锁不阻塞写线程获取读锁，当线程共享变量从主内存load到线程工作内存时，会存在数据不一致问题，所以当使用StampedLock的乐观读锁时，
 * 需要遵从如下图用例中使用的模式来确保数据的一致性。
 */
public class StampedLockMock {

    private double x;
    private double y;
    public StampedLock stampedLock = null;

    {
        /**
         * state的初始值为 ORIGIN（256）,它的二进制是 1 0000 0000,也就是初始版本号。
         *
         * state的高24位存储的是版本号,低8位存储的是是否有加锁,第8位存储的是写锁,
         *
         * 低7位存储的是读锁被获取的次数,而且如果只有第8位存储写锁的话,那么写锁只能被获取一次,也就不可能重入了。
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
    public void move(double deltaX, double deltaY) {
        // 获取写锁,返回一个版本号（戳）
        long stamp = stampedLock.writeLock();
        System.out.println("move is readLock:" + stampedLock.isReadLocked());
        System.out.println("move is writeLock:" + stampedLock.isWriteLocked());
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            // 释放写锁，需要传入上面获取的版本号
            stampedLock.unlockWrite(stamp);
        }
        System.out.println("move x is=" + x + ";y is =" + y);
    }

    /**
     * 2. 乐观读
     *
     * @return
     */
    public double distanceFromOrigin() {
        // 乐观读
        long stamp = stampedLock.tryOptimisticRead();
        double currentX = x;
        double currentY = y;
        // 验证版本号是否有变化
        if (!stampedLock.validate(stamp)) {
            System.err.println("乐观读获取锁失败");
            // 版本号变了，乐观读转悲观读
            stamp = stampedLock.readLock();
            System.out.println("distanceFromOrigin 乐观读失败 is readLock:" + stampedLock.isReadLocked());
            System.out.println("distanceFromOrigin 乐观读失败 is writeLock:" + stampedLock.isWriteLocked());
            try {
                // 重新读取x、y的值
                currentX = x + x;
                currentY = y + y;
            } finally {
                // 释放读锁，需要传入上面获取的版本号
                stampedLock.unlockRead(stamp);
            }
        }
        System.out.println("distanceFromOrigin is readLock:" + stampedLock.isReadLocked());
        System.out.println("distanceFromOrigin is writeLock:" + stampedLock.isWriteLocked());
        System.out.println("distanceFromOrigin is optimistic lock:" + stampedLock.validate(stamp));
        System.out.println("distanceFromOrigin x is=" + x + ";y is =" + y);
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
    public void moveIfAtOrigin(double newX, double newY) {
        // 获取悲观读锁
        long stamp = stampedLock.readLock();
        try {
            while (x == 0.0 && y == 0.0) {
                // 转为写锁
                long ws = stampedLock.tryConvertToWriteLock(stamp);
                // 转换成功
                if (ws != 0L) {
                    stamp = ws;
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
        System.out.println("moveIfAtOrigin x is=" + x + ";y is =" + y);

    }


    /**
     * StampedLock具有三种模式:写模式、读模式、乐观读模式。
     */
    public static void main(String[] args) throws Exception {

        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5));

        StampedLockMock stampedLockDemo = new StampedLockMock();
        stampedLockDemo.move(2.0D, 3.0D);
        double result = stampedLockDemo.distanceFromOrigin();
        System.err.println(result);
        stampedLockDemo.moveIfAtOrigin(4.0D, 5 / 0D);
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
        stampedLockDemo.move(10.0D, 11.0D);
    }
}
