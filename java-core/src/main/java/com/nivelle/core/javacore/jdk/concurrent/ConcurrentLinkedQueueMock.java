package com.nivelle.core.javacore.jdk.concurrent;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 双端非阻塞队列: ConcurrentLinkedQueue
 *
 * @author nivelle
 * @date 2020/04/12
 */
public class ConcurrentLinkedQueueMock {

    public static void main(String[] args) {
        ConcurrentLinkedQueue concurrentLinkedQueue = new ConcurrentLinkedQueue();
        ConcurrentLinkedDeque concurrentLinkedDeque = new ConcurrentLinkedDeque();
    }
}
