package com.nivelle.core.javacore.concurrent;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 单端阻塞队列:ArrayBlockingQueue
 *
 * @author nivelle
 * @date 2020/04/16
 */
public class ArrayBlockingQueueMock {
    /**
     * (1). BlockingQueue 不接受 null 元素
     *
     * (2). BlockingQueue 可以是限定容量的 没有的话就是Integer.MAX_VALUE
     *
     * (3). BlockingQueue 实现主要用于生产者-使用者队列，但它另外还支持 Collection 接口
     *
     * (4). BlockingQueue 实现是线程安全的
     *
     * (5). ArrayBlockingQueue初始化时必须传入容量，也就是数组的大小；
     *
     * (6). 可以通过构造方法控制重入锁的类型是公平锁还是非公平锁；
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
     * //获取操作等待条件
     * private final Condition notEmpty;
     * //插入操作等待条件
     * private final Condition notFull;
     **/


    public static void main(String[] args) {
        ArrayBlockingQueue arrayBlockingQueue1 = new ArrayBlockingQueue(2, true);
        arrayBlockingQueue1.add(1);
        arrayBlockingQueue1.add(2);
        //arrayBlockingQueue1.add(3);
        System.out.println(arrayBlockingQueue1);
        Iterator iterable1 = arrayBlockingQueue1.iterator();
        while (iterable1.hasNext()) {
            System.out.println(iterable1.next());
            System.out.println("peek:" + arrayBlockingQueue1.peek());
            try {
                System.out.println("take:" + arrayBlockingQueue1.take());
            } catch (InterruptedException e) {

                System.err.println(e);
            }
        }
        System.out.println("end queue:" + arrayBlockingQueue1);
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

    /**
     * ## 添加元素
     * public void put(E e) throws InterruptedException {
     *         checkNotNull(e);//添加的元素不能为null
     *         final ReentrantLock lock = this.lock;
     *         lock.lockInterruptibly();//加锁
     *         try {
     *             //如果数组满了,使用notFull等待 notFull等待的意思是说现在队列满了
     *             //只有取走一个元素后，队列才不满 然后唤醒notFull，然后继续现在的逻辑
     *             //这里之所以使用while而不是if是因为有可能多个线程阻塞在lock上
     *             //即使唤醒了,可能其它线程先一步修改了队列又变成满的了这时候需要再次等待
     *             while (count == items.length){
     *                   notFull.await();
     *             }
     *             enqueue(e);
     *         } finally {
     *             lock.unlock();
     *         }
     *     }
     *  ## 添加元素到数组
     *  private void enqueue(E x) {
     *         // assert lock.getHoldCount() == 1;
     *         // assert items[putIndex] == null;
     *         final Object[] items = this.items;
     *         items[putIndex] = x;
     *         // 如果放指针到数组尽头了，就返回头部
     *         if (++putIndex == items.length)
     *             putIndex = 0;
     *         count++;
     *         notEmpty.signal();
     *     }
     *
     *  ## 从数组获取元素
     *  private E dequeue() {
     *         // assert lock.getHoldCount() == 1;
     *         // assert items[takeIndex] != null;
     *         final Object[] items = this.items;
     *         @SuppressWarnings("unchecked")
     *         E x = (E) items[takeIndex];
     *         items[takeIndex] = null;
     *         //如果从数组末尾获取，则takeIndex 置为0
     *         if (++takeIndex == items.length)
     *             takeIndex = 0;
     *         count--;
     *         if (itrs != null)
     *             itrs.elementDequeued();
     *         notFull.signal();
     *         return x;
     *     }
     *
     * ## 入队：
     * 1. offer(E e)：如果队列没满，立即返回true； 如果队列满了，立即返回false-->不阻塞
     * 2. put(E e)：如果队列满了，一直阻塞，直到数组不满了或者线程被中断-->阻塞
     * 3. offer(E e, long timeout, TimeUnit unit)：在队尾插入一个元素,，如果数组已满，则进入等待，直到出现以下三种情况：-->阻塞
     *    3.1 被唤醒
     *    3.2 等待时间超时
     *    3.3 当前线程被中断
     *
     *
     * ## 出队
     *
     * 1. poll()：如果没有元素，直接返回null；如果有元素，出队
     * 2. take()：如果队列空了，一直阻塞，直到数组不为空或者线程被中断-->阻塞
     * 3. poll(long timeout, TimeUnit unit)：如果数组不空，出队；如果数组已空且已经超时，返回null；如果数组已空且时间未超时，则进入等待，直到出现以下三种情况：
     *    3.1 被唤醒
     *    3.2 等待时间超时
     *    3.3 当前线程被中断
     */
}
