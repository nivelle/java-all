package com.nivelle.core.jdk.concurrent;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.Setter;
import lombok.ToString;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 自己实现一个 CountDownLatch
 *
 * @author nivelle
 * @date 2019/06/16
 */
public class CountDownLatchMock {

    /**
     * 多个线程共享一个state状态值,countDow
     **/
    public static void main(String[] args) throws Exception {
        /**
         *  public CountDownLatch(int count) {
         *         if (count < 0) throw new IllegalArgumentException("count < 0");
         *         this.sync = new Sync(count);
         *     }
         *
         *
         *     private static final class Sync extends AbstractQueuedSynchronizer {
         *         private static final long serialVersionUID = 4982264981922014374L;
         *         ## 传入初始次数
         *         Sync(int count) {
         *             setState(count);
         *         }
         *         ## 获取还剩的次数
         *         int getCount() {
         *             return getState();
         *         }
         *         ## 尝试获取共享锁
         *         protected int tryAcquireShared(int acquires) {
         *              ## 这里state等于0的时候返回1,也就是count减为0的时候总是获取成功，state不为0时返回-1，也就是count不为0的时候总需要排队
         *             return (getState() == 0) ? 1 : -1;
         *         }
         *         ## 尝试释放锁
         *         protected boolean tryReleaseShared(int releases) {
         *
         *             for (;;) {
         *                 ## state的值
         *                 int c = getState();
         *                 ## 等于0了，则无法再释放了
         *                 if (c == 0){
         *                     return false;
         *                 }
         *                 ## 将count的值-1
         *                 int nextc = c-1;
         *                 ## 原子更新state的值
         *                 if (compareAndSetState(c, nextc)){
         *                     ## 减为0的时候返回true，这时会唤醒后面排队的线程
         *                     return nextc == 0;
         *                 }
         *             }
         *         }
         *     }
         *
         *
         */
        CountDownLatch countDownLatch = new CountDownLatch(2);

        new Thread(() -> {
            System.out.println("第一次执行前,当前的count:" + countDownLatch.getCount());

            try {
                Thread.sleep(1000);
                /**
                 * public void countDown() {
                 *         sync.releaseShared(1);
                 *     }
                 *
                 * public final boolean releaseShared(int arg) {
                 *         if (tryReleaseShared(arg)) {
                 *             doReleaseShared();
                 *             return true;
                 *         }
                 *         return false;
                 *  }
                 *
                 *  private void doReleaseShared() {
                 *
                 *         for (;;) {
                 *             ## 获取head
                 *             Node h = head;
                 *             if (h != null && h != tail) {
                 *                 int ws = h.waitStatus;
                 *                 if (ws == Node.SIGNAL) {
                 *                     if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                 *                         continue;            // loop to recheck cases
                 *                     unparkSuccessor(h);
                 *                 }
                 *                 else if (ws == 0 &&
                 *                          !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                 *                     continue;                // loop on failed CAS
                 *             }
                 *             if (h == head)                   // loop if head changed
                 *                 break;
                 *         }
                 *     }
                 *
                 */
                countDownLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("第一次执行后,当前的count:" + countDownLatch.getCount());
        }).start();

        new Thread(() -> {
            System.out.println("第二次执行前,当前的count:" + countDownLatch.getCount());
            try {
                Thread.sleep(1000);
                //countDownLatch.countDown();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("第二次执行后,当前的count:" + countDownLatch.getCount());
        }).start();

        long start = System.currentTimeMillis();
        /**
         * 主线程都会等待state等于0的时候才会继续往下执行，否则阻塞
         */
        System.out.println("主线程执行到这里,被阻塞了" + Thread.currentThread().getState().name());
        boolean interruptedStatueBefore = Thread.currentThread().isInterrupted();
        System.out.println("中断前的状态:" + interruptedStatueBefore);

        /**
         * 阻塞状态可以被中断异常取消。
         */
        Thread.sleep(5000);
        System.out.println(countDownLatch.getCount());
        if (countDownLatch.getCount() > 0) {
            Thread.currentThread().interrupt();
            boolean interruptedStatueAfter = Thread.currentThread().isInterrupted();
            System.out.println("中断后的状态:" + interruptedStatueAfter);
        }
        try {
            /**
             * ## java.util.concurrent.CountDownLatch.await()
             * public void await() throws InterruptedException {
             *         ## 调用AQS的acquireSharedInterruptibly()方法
             *         sync.acquireSharedInterruptibly(1);
             *     }
             *
             * ## java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireSharedInterruptibly()
             * public final void acquireSharedInterruptibly(int arg)
             *             throws InterruptedException {
             *         if (Thread.interrupted()){
             *             throw new InterruptedException();
             *         }
             *         ## 0或者-1,-1 代表在排队
             *         if (tryAcquireShared(arg) < 0){
             *             doAcquireSharedInterruptibly(arg);
             *         }
             *     }
             *
             *
             *     private void doAcquireSharedInterruptibly(int arg)
             *         throws InterruptedException {
             *         ## 添加共享尾节点
             *         final Node node = addWaiter(Node.SHARED);
             *         boolean failed = true;
             *         try {
             *             for (;;) {
             *                 ## 尾节点的前置节点
             *                 final Node p = node.predecessor();
             *                 if (p == head) {
             *                     ## 如果尾节点的前置节点是head节点
             *                     int r = tryAcquireShared(arg);
             *                     if (r >= 0) {
             *                         setHeadAndPropagate(node, r);
             *                         p.next = null; // help GC
             *                         failed = false;
             *                         return;
             *                     }
             *                 }
             *                 if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
             *                     throw new InterruptedException();
             *             }
             *         } finally {
             *             if (failed)
             *                 cancelAcquire(node);
             *         }
             *     }
             */
            countDownLatch.await();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        long end = System.currentTimeMillis();
        System.out.println("被阻挡了:" + (end - start));
        System.out.println(Thread.currentThread().getName() + "执行完了");

    }

    /**
     * （1）CountDownLatch的初始次数是否可以调整？
     *
     * 答：前面我们学习Semaphore的时候发现，它的许可次数是可以随时调整的，那么，CountDownLatch的初始次数能随时调整吗？答案是不能的，它没有提供修改（增加或减少）次数的方法，除非使用反射作弊。
     *
     * （2）CountDownLatch为什么使用共享锁？
     *
     * 答：前面我们分析ReentrantReadWriteLock的时候学习过AQS的共享锁模式,比如当前锁是由一个线程获取为互斥锁，那么这时候所有需要获取共享锁的线程都要进入AQS队列中进行排队，当这个互斥锁释放的时候，会一个接着一个地唤醒这些连续的排队的等待获取共享锁的线程，注意，这里的用语是“一个接着一个地唤醒”，也就是说这些等待获取共享锁的线程不是一次性唤醒的。
     *
     * 因为CountDownLatch的await()多个线程可以调用多次，当调用多次的时候这些线程都要进入AQS队列中排队,当count次数减为0的时候,它们都需要被唤醒,继续执行任务,如果使用互斥锁则不行,互斥锁在多个线程之间是互斥的,一次只能唤醒一个,不能保证当count减为0的时候这些调用了await()方法等待的线程都被唤醒。
     *
     * （3）CountDownLatch与Thread.join()有何不同？
     *
     * 答：Thread.join()是在主线程中调用的，它只能等待被调用的线程结束了才会通知主线程，而CountDownLatch则不同，它的countDown()方法可以在线程执行的任意时刻调用，灵活性更大。
     *
     */


}
