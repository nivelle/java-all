package com.nivelle.base.javacore.datastructures.synlock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock 源码分析
 *
 * @author nivell
 * @date 2019/06/16
 */
public class ReentrantLockDemo {

    public static void main(String[] args) {
        ReentrantLock reentrantLock = new ReentrantLock();

        reentrantLock.lock();
        reentrantLock.unlock();
        reentrantLock.newCondition();
    }
}
