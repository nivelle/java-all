package com.nivelle.base.javacore.datastructures.concurrent;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延时队列
 *
 * @author nivell
 * @date 2020/04/14
 */
public class DelayQueueDemo {

    public static void main(String[] args) {
        /**
         * public class DelayQueue<E extends Delayed> extends AbstractQueue<E> implements BlockingQueue<E>
         *
         * public interface Delayed extends Comparable<Delayed>
         */

        /**
         * 主要属性:
         *
         * ## 用于控制并发的锁
         * 1.private final transient ReentrantLock lock = new ReentrantLock();
         * ## 优先级队列
         * 2.private final PriorityQueue<E> q = new PriorityQueue<E>();
         * ## 用于标记当前是否有线程在排队（仅用于取元素时）
         * 3.private Thread leader = null;
         * ## 条件，用于表示现在是否有可取的元素
         * 4.private final Condition available = lock.newCondition();
         */
        DelayQueue delayQueue = new DelayQueue();
        DelayElement delayElement1 = new DelayElement(1L);
        DelayElement delayElement2 = new DelayElement(2L);
        DelayElement delayElement3 = new DelayElement(3L);

        delayQueue.add(delayElement1);
        delayQueue.add(delayElement2);
        delayQueue.add(delayElement3);

        System.out.println(delayQueue);
        DelayElement delayElement4 = new DelayElement(4L);
        delayQueue.put(delayElement4);
        System.out.println(delayQueue);

        DelayElement delayElement5 = new DelayElement(5L);

        /**
         * public boolean offer(E e) {
         *
         *         final ReentrantLock lock = this.lock;
         *         ## 加锁
         *         lock.lock();
         *         try {
         *             ## 添加元素
         *             q.offer(e);
         *             if (q.peek() == e) {
         *                 leader = null;
         *                 available.signal();
         *             }
         *             return true;
         *         } finally {
         *             lock.unlock();
         *         }
         *     }
         */
        delayQueue.offer(delayElement5,123L,TimeUnit.SECONDS);
        System.out.println(delayQueue);

    }
}

