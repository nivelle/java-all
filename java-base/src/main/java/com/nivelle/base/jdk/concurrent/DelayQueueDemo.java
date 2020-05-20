package com.nivelle.base.jdk.concurrent;

import com.nivelle.base.pojo.DelayElement;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * 延时队列
 *
 * @author nivell
 * @date 2020/04/14
 */
public class DelayQueueDemo {
    /**
     * DelayQueue是java并发包下的延时阻塞队列，常用于实现定时任务。
     *
     * 延时队列主要使用优先级队列来实现，并辅以重入锁和条件来控制并发安全
     */
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
        DelayQueue<DelayElement> delayQueue = new DelayQueue();
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
         *             ## 添加元素到 PriorityQueue
         *             q.offer(e);
         *             ## 如果添加的元素是堆顶元素,就把leader置为空，并唤醒等待在条件avaliable上的线程
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
        delayQueue.offer(delayElement5, 123L, TimeUnit.SECONDS);
        DelayElement delayElement6 = new DelayElement(6L);
        delayQueue.add(delayElement6);
        System.out.println("offer element:" + delayQueue);

        /**
         *
         * （1）加锁；
         *
         * （2）检查第一个元素，如果为空或者还没到期，就返回null；
         *
         * （3）如果第一个元素到期了就调用优先级队列的poll()弹出第一个元素；
         *
         * （4）解锁。
         *
         * public E poll() {
         *         final ReentrantLock lock = this.lock;
         *         ## 加锁
         *         lock.lock();
         *         try {
         *             ## 获取 PriorityQueue 队列首个元素
         *             E first = q.peek();
         *             ## 队列首元素为空或者
         *             if (first == null || first.getDelay(NANOSECONDS) > 0){
         *                 return null;
         *             } else{
         *                 ## 获取第一个元素:PriorityQueue
         *                 return q.poll();
         *             }
         *         } finally {
         *             ## 解锁
         *             lock.unlock();
         *         }
         *     }
         */
        delayElement1.setIndex(-1);
        DelayElement delayElement = delayQueue.poll();
        System.out.println("poll element:" + delayElement);

        try {
            delayElement2.setIndex(-2);
            /**
             *
             * （1）加锁；
             *
             * （2）判断堆顶元素是否为空，为空的话直接阻塞等待；
             *
             * （3）判断堆顶元素是否到期，到期了直接调用优先级队列的poll()弹出元素；
             *
             * （4）没到期，再判断前面是否有其它线程在等待，有则直接等待；
             *
             * （5）前面没有其它线程在等待，则把自己当作第一个线程等待delay时间后唤醒，再尝试获取元素；
             *
             * （6）获取到元素之后再唤醒下一个等待的线程；
             *
             * （7）解锁；
             *
             * public E take() throws InterruptedException {
             *         final ReentrantLock lock = this.lock;
             *         lock.lockInterruptibly();
             *         try {
             *             for (;;) {
             *                 ## 堆顶元素
             *                 E first = q.peek();
             *                 if (first == null){
             *                     ## 如果堆顶元素为空，说明队列中还没有元素，直接阻塞等待
             *                     available.await();
             *                 }
             *                 else {
             *                     ## 堆顶元素的到期时间
             *                     long delay = first.getDelay(NANOSECONDS);
             *                     if (delay <= 0){
             *                         ## 获取第一个元素:PriorityQueue
             *                         return q.poll();
             *                     }
             *                     ## 如果delay大于0 ，则下面要阻塞了;
             *                     ## 将first置为空方便gc，因为有可能其它元素弹出了这个元素,这里还持有着引用不会被清理
             *                     first = null; // don't retain ref while waiting
             *                     ## 如果前面有其它线程在等待，直接进入等待
             *                     if (leader != null)
             *                         available.await();
             *                     else {
             *                         ## 如果leader为null，把当前线程赋值给它
             *                         Thread thisThread = Thread.currentThread();
             *                         leader = thisThread;
             *                         try {
             *                             ## 等待delay时间后自动醒过来，醒过来后把leader置空并重新进入循环判断堆顶元素是否到期。
             *                             ## 这里即使醒过来后也不一定能获取到元素,因为有可能其它线程先一步获取了锁并弹出了堆顶元素
             *                             ## 条件锁的唤醒分成两步，先从Condition的队列里出队,再入队到AQS的队列中，当其它线程调用LockSupport.unpark(t)的时候才会真正唤醒
             *                             available.awaitNanos(delay);
             *                         } finally {
             *                             if (leader == thisThread){
             *                                 leader = null;
             *                             }
             *                         }
             *                     }
             *                 }
             *             }
             *         } finally {
             *             ## 成功出队后，如果leader为空且堆顶还有元素，就唤醒下一个等待的线程
             *             if (leader == null && q.peek() != null){
             *                 ## signal()只是把等待的线程放到AQS的队列里面，并不是真正的唤醒
             *                 available.signal();
             *             }
             *             lock.unlock();
             *         }
             *     }
             */
            DelayElement takeDelayElement = delayQueue.take();
            System.out.println("take element:" + takeDelayElement);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        System.out.println("offer element:" + delayQueue);
    }
}

