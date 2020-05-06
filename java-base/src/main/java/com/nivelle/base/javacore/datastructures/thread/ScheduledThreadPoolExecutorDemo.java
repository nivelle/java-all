package com.nivelle.base.javacore.datastructures.thread;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * ScheduledThreadPoolExecutor(定时任务线程池)
 *
 * @author nivell
 * @date 2020/04/14
 */
public class ScheduledThreadPoolExecutorDemo {
    /**
     * （1）指定某个时刻执行任务，是通过延时队列的特性来解决的;
     * <p>
     * （2）重复执行，是通过在任务执行后再次把任务加入到队列中来解决的。
     */
    public static void main(String[] args) throws Exception {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5);
        System.out.println("start:" + System.currentTimeMillis());

        /**
         * 将来执行的任务无返回值,有延迟:执行一个无返回值任务，5秒后执行，只执行一次
         *
         **/
        scheduledThreadPoolExecutor.schedule(() -> {
            System.out.println("spring:" + System.currentTimeMillis());
        }, 5, TimeUnit.SECONDS);
        /**
         * 将来执行的任务有返回值,有延迟:执行一个有返回值任务，5秒后执行，只执行一次
         */
        ScheduledFuture<String> future = scheduledThreadPoolExecutor.schedule(() -> {
            System.out.print("inner summer" + System.currentTimeMillis());
            return "outer summer:";
        }, 5, TimeUnit.SECONDS);

        System.out.println(future.get() + System.currentTimeMillis());
        /**
         *
         * 按固定频率执行一个任务，每2秒执行一次，1秒后执行;任务开始时的2秒后
         * **/
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            System.out.print("autumn:" + System.currentTimeMillis());
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }, 1, 2, TimeUnit.SECONDS);

        /**
         * 按固定延时执行一个任务，每延时2秒执行一次，1秒后执行;
         */
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            System.out.println("winter:" + System.currentTimeMillis());
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }, 1, 2, TimeUnit.SECONDS);
    }

    /**
     * ## command: 任务
     * ## initialDelay:初始化多久后开始执行
     * ## period:周期
     * ## unit: 时间单位
     * public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,long initialDelay,long period,TimeUnit unit) {
     *         if (command == null || unit == null){
     *             throw new NullPointerException();
     *         }
     *         if (period <= 0){
     *             throw new IllegalArgumentException();
     *         }
     *         ## 将普通任务装饰成: ScheduledFutureTask 是 ScheduledThreadPoolExecutor的一个私有内部类
     *         ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command,null,triggerTime(initialDelay, unit),unit.toNanos(period));
     *         ## 钩子方法,给子类用来替换装饰 task,这里认为t==sft
     *         RunnableScheduledFuture<Void> t = decorateTask(command, sft);
     *         sft.outerTask = t;
     *         ## 延时执行
     *         delayedExecute(t);
     *         return t;
     *     }
     *
     * ## triggerTime:触发时间计算
     * long triggerTime(long delay) {
     *         return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
     *  }
     *
     * ## ScheduledThreadPoolExecutor.
     * private void delayedExecute(RunnableScheduledFuture<?> task) {
     *         ## 如果线程池关闭了,执行拒绝策略
     *         if (isShutdown()){
     *             reject(task);
     *         }
     *         else {
     *             ## 先把任务放到阻塞队列中去
     *             super.getQueue().add(task);
     *             ## 再次检查线程池状态
     *             if (isShutdown() && !canRunInCurrentRunState(task.isPeriodic()) && remove(task)){
     *                 task.cancel(false);
     *             }else{
     *                 ## 保证有足够有线程执行任务
     *                 ensurePrestart();
     *             }
     *         }
     *   }
     *
     *  void ensurePrestart() {
     *         int wc = workerCountOf(ctl.get());
     *         ## 创建工作线程,这里没有传入firstTask参数，因为上面先把任务扔到队列中去了,另外没用上maxPoolSize参数，所以最大线程数量在定时线程池中实际是没有用的
     *         if (wc < corePoolSize){
     *             addWorker(null, true);
     *         }else if (wc == 0){
     *             addWorker(null, false);
     *          }
     *     }
     *
     * ## ScheduledThreadPoolExecutor.ScheduledFutureTask
     * public void run() {
     *             ## 是否重复执行
     *             boolean periodic = isPeriodic();
     *             ## 线程池状态判断
     *             if (!canRunInCurrentRunState(periodic)){
     *                 cancel(false);
     *             }
     *             ## 一次性任务，直接调用父类的run()方法，这个父类实际上是FutureTask
     *             else if (!periodic){
     *                 ScheduledFutureTask.super.run();
     *             }else if (ScheduledFutureTask.super.runAndReset()) {## 重复性任务，先调用父类的runAndReset()方法，这个父类也是FutureTask
     *                 ## 设置下次执行的时间
     *                 setNextRunTime();
     *                 reExecutePeriodic(outerTask);
     *             }
     *         }
     *
     *  ## ScheduledFutureTask.reExecutePeriodic
     *  void reExecutePeriodic(RunnableScheduledFuture<?> task) {
     *         ## 线程池状态检查
     *         if (canRunInCurrentRunState(true)) {
     *             ## 再次把任务放到任务队列中
     *             super.getQueue().add(task);
     *             ## 再次检查线程池状态
     *             if (!canRunInCurrentRunState(true) && remove(task)){
     *                 task.cancel(false);
     *             }else{
     *                 ## 保证工作线程足够
     *                 ensurePrestart();
     *            }
     *         }
     *     }
     *
     *  ## DelayedWorkQueue 内部类
     *  public RunnableScheduledFuture<?> take() throws InterruptedException {
     *             final ReentrantLock lock = this.lock;
     *             ## 加锁
     *             lock.lockInterruptibly();
     *             try {
     *                 for (;;) {
     *                     ## 堆顶任务
     *                     RunnableScheduledFuture<?> first = queue[0];
     *                     ## 如果队列为空,则等待
     *                     if (first == null){
     *                         available.await();
     *                     }
     *                     else {
     *                         ## 还有多久到时间
     *                         long delay = first.getDelay(NANOSECONDS);
     *                         ## 如果小于等于0，说明这个任务到时间了，可以从队列中出队了
     *                         if (delay <= 0){
     *                             ## 出队，然后堆化
     *                             return finishPoll(first);
     *                         }
     *                         ## 还没有到时间
     *                         first = null; // don't retain ref while waiting
     *                         ## 如果前面有线程在等待，直接进入等待
     *                         if (leader != null){
     *                             available.await();
     *                         }
     *                         else {
     *                             ## 当前线程作为leader
     *                             Thread thisThread = Thread.currentThread();
     *                             leader = thisThread;
     *                             try {
     *                                 ##  等待上面计算的延时时间，再自动唤醒
     *                                 available.awaitNanos(delay);
     *                             } finally {
     *                                 ## 唤醒后再次获得锁后把leader再置空
     *                                 if (leader == thisThread){
     *                                     leader = null;
     *                                 }
     *                             }
     *                         }
     *                     }
     *                 }
     *             } finally {
     *                 if (leader == null && queue[0] != null){
     *                     ## 相当于唤醒下一个等待的任务
     *                     available.signal();
     *                 }
     *                 ## 解锁
     *                 lock.unlock();
     *             }
     *         }
     */

}
