package com.nivelle.base.javacore.thread;

import java.util.concurrent.locks.StampedLock;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/04/12
 */
public class StampedLockDemo {

    public static void main(String[] args) {
        StampedLock stampedLock = new StampedLock();
        System.out.println(stampedLock);
    }
}
