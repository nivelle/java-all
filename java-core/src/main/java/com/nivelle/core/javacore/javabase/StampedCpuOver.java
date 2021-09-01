package com.nivelle.core.javacore.javabase;

import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.StampedLock;

/**
 * StampedLock 的中断操作导致 cpu飙升
 *
 * @author fuxinzhong
 * @date 2021/01/31
 */
public class StampedCpuOver {

    public static void main(String[] args) throws Exception {
        final StampedLock lock = new StampedLock();
        Thread T1 = new Thread(() -> {
            // 获取写锁
            lock.writeLock();
            // 永远阻塞在此处，不释放写锁
            LockSupport.park();
        });
        T1.start();
        // 保证T1获取写锁
        Thread.sleep(100);
        Thread t2 = new Thread(() ->
                //阻塞在悲观读锁
                lock.readLock()
        );
        t2.start();
        // 保证T2阻塞在读锁
        Thread.sleep(100);
        //中断线程T2
        //会导致线程T2所在CPU飙升
        System.out.println("主线程调用t2的中断操作");
        t2.interrupt();
        t2.join();
    }

}
