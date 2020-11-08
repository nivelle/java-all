package com.nivelle.base.jdk.concurrent.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition 条件锁
 *
 * @author fuxinzhong
 * @date 2020/04/30
 */
public class ReentrantConditionDemo {

    /**
     * ConditionObject 是 AbstractQueuedSynchronizer 的内部类
     *
     * 条件锁,是指在获取锁之后发现当前业务场景自己无法处理,而需要等待某个条件的出现才可以继续处理时使用的一种锁。
     */
    public static void main(String[] args) throws InterruptedException {
        //声明一个重入锁
        ReentrantLock lock = new ReentrantLock();
        //声明一个条件锁
        Condition condition = lock.newCondition();
        new Thread(() -> {
            try {
                lock.lock();
                System.out.println("before await");//2
                System.out.println("1:" + lock.isLocked());
                /**
                 * （1）新建一个节点加入到条件队列中去；
                 *
                 * （2）完全释放当前线程占有的锁；
                 *
                 * （3）阻塞当前线程，并等待条件的出现；
                 *
                 * （4）条件已出现（此时节点已经移到AQS的队列中），尝试获取锁；
                 *
                 *  也就是说await()方法内部其实是 先释放锁->等待条件->再次获取锁的过程。
                 */
                condition.await();//3 条件需要在获取锁之后才能等待
                System.out.println("2:" + lock.isLocked());
                System.out.println("after await");//10
            } catch (InterruptedException e) {
                lock.unlock();//11
            }

        }).start();

        Thread.sleep(1000);
        lock.lock();//4
        try {
            Thread.sleep(2000);//5
            System.out.println("3:" + lock.isLocked());

            System.out.println("before signal");//6
            /**
             * signal()方法的大致流程为：
             *
             * （1）从条件队列的头节点开始寻找一个非取消状态的节点；
             *
             * （2）把它从条件队列移到AQS队列；
             *
             * （3）且只移动一个节点；
             */
            condition.signal();//7
            System.out.println("4:" + lock.isLocked());
            //lock.unlock();
            System.out.println("after signal");//8
            System.out.println("5:" + lock.isLocked());
        } catch (Exception e) {
            lock.unlock();//9
        }
        Thread.sleep(1000);
        //lock.unlock();
    }
    /**
     * （1）重入锁是指可重复获取的锁，即一个线程获取锁之后再尝试获取锁时会自动获取锁；
     *
     * （2）在ReentrantLock中重入锁是通过不断累加state变量的值实现的；
     *
     * （3）ReentrantLock的释放要跟获取匹配，即获取了几次也要释放几次；
     *
     * （4）ReentrantLock默认是非公平模式，因为非公平模式效率更高；
     *
     * （5）条件锁是指为了等待某个条件出现而使用的一种锁；
     *
     * （6）条件锁比较经典的使用场景就是队列为空时阻塞在条件notEmpty上；
     *
     * （7）ReentrantLock中的条件锁是通过AQS的ConditionObject内部类实现的；
     *
     * （8）await()和signal()方法都必须在获取锁之后释放锁之前使用；
     *
     * （9）await()方法会新建一个节点放到条件队列中，接着完全释放锁，然后阻塞当前线程并等待条件的出现；
     *
     * （10）signal()方法会寻找条件队列中第一个可用节点移到AQS队列中；
     *
     * （11）在调用signal()方法的线程调用unlock()方法才真正唤醒阻塞在条件上的节点（此时节点已经在AQS队列中）；
     *
     * （12）之后该节点会再次尝试获取锁，后面的逻辑与lock()的逻辑基本一致了。
     */
}
