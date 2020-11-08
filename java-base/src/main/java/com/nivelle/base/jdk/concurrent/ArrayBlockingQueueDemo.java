package com.nivelle.base.jdk.concurrent;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * ArrayBlockingQueue
 *
 * @author nivelle
 * @date 2020/04/16
 */
public class ArrayBlockingQueueDemo {
    /**
     * (1). BlockingQueue 不接受 null 元素
     *
     * (2). BlockingQueue 可以是限定容量的 没有的话就是Integer.MAX_VALUE
     *
     * (3). BlockingQueue 实现主要用于生产者-使用者队列，但它另外还支持 Collection 接口
     *
     * (4). BlockingQueue 实现是线程安全的
     */

    /**
     * //底层存储元素的数组。为final说明一旦初始化，容量不可变，所以是有界的。
     * final Object[] items;
     * <p>
     * //下一个take, poll, peek or remove操作的index位置
     * int takeIndex;
     * <p>
     * //下一个put, offer, or add操作的index位置
     * int putIndex;
     * <p>
     * // 元素数量
     * int count;
     * 用于并发控制：使用经典的双Condition算法
     * final ReentrantLock lock;
     * 获取操作等待条件
     * private final Condition notEmpty;
     * 插入操作等待条件
     * private final Condition notFull;
     **/


    public static void main(String[] args) {
        ArrayBlockingQueue arrayBlockingQueue1 = new ArrayBlockingQueue(3, true);
        arrayBlockingQueue1.add(1);
        arrayBlockingQueue1.add(2);
        arrayBlockingQueue1.add(3);
        System.out.println(arrayBlockingQueue1);
        Iterator iterable1 = arrayBlockingQueue1.iterator();
        while (iterable1.hasNext()) {
            System.out.println(iterable1.next());
        }

        System.out.println("=========");
        ArrayBlockingQueue arrayBlockingQueue2 = new ArrayBlockingQueue(3, false);
        arrayBlockingQueue2.add(2);
        arrayBlockingQueue2.add(3);
        arrayBlockingQueue2.add(4);
        System.out.println(arrayBlockingQueue2);
        Iterator iterable2 = arrayBlockingQueue2.iterator();
        while (iterable2.hasNext()) {
            System.out.println(iterable2.next());
        }


    }
}
