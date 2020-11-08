package com.nivelle.base.jdk.concurrent;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * ArrayBlockingQueue
 *
 * @author nivelle
 * @date 2020/04/16
 */
public class ArrayBlockingQueueDemo {

    public static void main(String[] args) {
        ArrayBlockingQueue arrayBlockingQueue = new ArrayBlockingQueue(1);
        arrayBlockingQueue.add(1);

    }
}
