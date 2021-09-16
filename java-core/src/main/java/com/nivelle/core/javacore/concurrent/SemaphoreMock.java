package com.nivelle.core.javacore.concurrent;

import java.util.concurrent.Semaphore;

/**
 * Semaphore:信号量
 *
 * @author fuxinzhong
 * @date 2020/04/24
 */
public class SemaphoreMock {

    public static void main(String[] args) throws Exception {
        /**
         * // 构造方法,传入许可次数,放入state中
         * public Semaphore(int permits) {
         *         // 构造方法，调用父类的构造方法
         *         sync = new NonfairSync(permits);
         *     }
         *
         *
         * static final class NonfairSync extends Sync {
         *         private static final long serialVersionUID = -2694183684443567898L;
         *         // 调用父类的构造方法
         *         NonfairSync(int permits) {
         *             super(permits);
         *         }
         *         // 尝试获取许可，调用父类nonfairTryAcquireShared
         *         protected int tryAcquireShared(int acquires) {
         *             // 非公平方式获取锁
         *             return nonfairTryAcquireShared(acquires);
         *         }
         *     }
         *
         * // java.util.concurrent.Semaphore.Sync
         * abstract static class Sync extends AbstractQueuedSynchronizer {
         *         private static final long serialVersionUID = 1192457210091910933L;
         *
         *         Sync(int permits) {
         *             // 构造方法，传入许可次数,放入state中
         *             setState(permits);
         *         }
         *         // 获取许可次数
         *         final int getPermits() {
         *             return getState();
         *         }
         *         // 非公平模式获取许可
         *         final int nonfairTryAcquireShared(int acquires) {
         *             for (;;) {
         *                 // 查看还有几个许可
         *                 int available = getState();
         *                 // 减去这次需要获取的许可还剩下几个许可
         *                 int remaining = available - acquires;
         *                 // 如果剩余许可小于0了则直接返回
         *                 // 如果剩余许可不小于0,则尝试原子更新state的值,成功了返回剩余许可
         *                 if (remaining < 0 || compareAndSetState(available, remaining)){
         *                     return remaining;
         *                 }
         *             }
         *         }
         *         // 释放许可
         *         protected final boolean tryReleaseShared(int releases) {
         *             for (;;) {
         *                 // 看看还有几个许可
         *                 int current = getState();
         *                 // 加上这次释放的许可
         *                 int next = current + releases;
         *                 // 检测溢出
         *                 if (next < current){
         *                     throw new Error("Maximum permit count exceeded");
         *                 }
         *                 // 原子更新state的值，成功了返回true
         *                 if (compareAndSetState(current, next)){
         *                     return true;
         *                 }
         *             }
         *         }
         *         // 减少许可
         *         final void reducePermits(int reductions) {
         *             for (;;) {
         *                  // 看看还有几个许可
         *                 int current = getState();
         *                 // 减去将要减少的许可
         *                 int next = current - reductions;
         *                 if (next > current){// 检测溢出
         *                     throw new Error("Permit count underflow");
         *                  }
         *                  // 原子更新state的值,成功了返回true
         *                 if (compareAndSetState(current, next)){
         *                     return;
         *                 }
         *             }
         *         }
         *         // 销毁许可
         *         final int drainPermits() {
         *             for (;;) {
         *                 // 看看还有几个许可
         *                 int current = getState();
         *                 // 如果为0，直接返回
         *                 // 如果不为0，把state原子更新为0
         *                 if (current == 0 || compareAndSetState(current, 0)){
         *                     return current;
         *                 }
         *             }
         *         }
         *     }
         *
         */
        Semaphore semaphore = new Semaphore(1);

        System.out.println("默认是 isFair？:" + semaphore.isFair());

        System.out.println("当前可用的许可数目1：" + semaphore.availablePermits());

        boolean acquireResult = semaphore.tryAcquire(1);
        System.out.println("当前可用的许可数目2：" + semaphore.availablePermits());
        System.out.println("尝试获取许可的结果:" + acquireResult);
        boolean acquireResult2 = semaphore.tryAcquire(1);
        System.out.println("再次尝试获取许可：" + acquireResult2);
        if (acquireResult) {
            System.out.println("获取许可之后,释放许可：");
            Thread.sleep(1000000);
            semaphore.release();
        }
        System.out.println("当前可用的许可数目3：" + semaphore.availablePermits());
        System.out.println("================");


        /**
         * public Semaphore(int permits, boolean fair) {
         *         sync = fair ? new FairSync(permits) : new NonfairSync(permits);
         *     }
         *
         *
         * static final class FairSync extends Sync {
         *         private static final long serialVersionUID = 2014338818796000944L;
         *         // 构造方法,调用父类的构造方法
         *         FairSync(int permits) {
         *             super(permits);
         *         }
         *         // 尝试获取许可
         *         protected int tryAcquireShared(int acquires) {
         *             for (;;) {
         *                 // 公平模式需要检测是否前面排队的，如果有排队的直接返回失败
         *                 if (hasQueuedPredecessors()){
         *                     return -1;
         *                 }
         *                 // 没有排队的再尝试更新state的值
         *                 int available = getState();
         *                 int remaining = available - acquires;
         *                 if (remaining < 0 || compareAndSetState(available, remaining)){
         *                     return remaining;
         *                 }
         *             }
         *         }
         *     }
         */
        Semaphore semaphore1 = new Semaphore(1, true);
        System.out.println("指定为公平模式：" + semaphore1.isFair());
        System.out.println("公平锁当前可用的许可数目1：" + semaphore1.availablePermits());

        boolean acquireResult1 = semaphore1.tryAcquire(1);
        System.out.println("公平锁当前可用的许可数目2：" + semaphore1.availablePermits());

        semaphore1.release(2);
        System.out.println("释放多余的许可，semaphore释放许可的时候并不会检测当前线程有没有获取过许可，所以可以调用释放许可的方法动态增加一些许可");
        System.out.println("公平锁当前可用的许可数目3：" + semaphore1.availablePermits());
        boolean acquires = semaphore1.tryAcquire(3);
        System.out.println("获取多余的许可:" + acquires);
        System.out.println("公平锁当前可用的许可数目4：" + semaphore1.availablePermits());


    }
}
