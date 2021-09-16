package com.nivelle.core.javacore.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * FutureTask
 *
 * @author nivelle
 * @date 2020/05/06
 */
public class FutureTaskMock {
    /**
     * FutureTask实现了RunnableFuture接口，而RunnableFuture接口组合了Runnable接口和Future接口的能力，Future接口提供了get任务返回值的能力。
     * <p>
     * 问题:submit()方法返回的为什么是Future接口而不是RunnableFuture接口或者FutureTask类呢？
     * <p>
     * 答:这是因为submit()返回的结果，对外部调用者只想暴露其get()的能力（Future接口,而不想暴露其run()的能力（RunAble接口）.
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        List<Future<Integer>> futureList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int num = i;
            Future<Integer> future = threadPool.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
                System.out.println("return:" + num);
                return num;
            });
            futureList.add(future);
        }
        int sum = 0;
        for (Future<Integer> future1 : futureList) {
            sum += future1.get();
        }
        System.out.println("sum:=" + sum);
    }

    /**
     * // AbstractExecutorService.submit
     * public Future<?> submit(Runnable task) {
     *         if (task == null) {
     *            throw new NullPointerException();
     *         }
     *         // 包装成FutureTask
     *         RunnableFuture<Void> ftask = newTaskFor(task, null);
     *         //交给execute()方法去执行
     *         execute(ftask);
     *         //返回futureTask
     *         return ftask;
     * }
     *
     * // AbstractExecutorService.newTaskFor
     *
     * // 返回值: public interface RunnableFuture<V> extends Runnable, Future<V>
     *
     * protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
     *         return new FutureTask<T>(runnable, value);
     * }
     *
     * // execute()方法最后调用的是task的run()方法; execute方法所在接口是Executor,Executes the given command at some time in the future.
     * // The command may execute in a new thread, in a pooled thread, or in the calling thread, at the discretion of the {@code Executor} implementation
     *
     * // FutureTask.run()
     * public void run() {
     *         // 状态不为NEW，或者修改为当前线程来运行这个任务失败，则直接返回
     *         if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset,null, Thread.currentThread())){
     *             return;
     *         }
     *         try {
     *             // 真正的任务
     *             Callable<V> c = callable;
     *             //state必须为NEW时才运行
     *             if (c != null && state == NEW) {
     *                 //运行的结果
     *                 V result;
     *                 boolean ran;
     *                 try {
     *                     //任务执行的地方
     *                     result = c.call();
     *                     //已执行完毕
     *                     ran = true;
     *                 } catch (Throwable ex) {
     *                     result = null;
     *                     ran = false;
     *                     setException(ex);
     *                 }
     *                 if (ran){
     *                     //设置处理结果
     *                     set(result);
     *                 }
     *             }
     *         } finally {
     *             //置空runner(The thread running the callable; CASed during run())
     *             runner = null;
     *             //处理中断
     *             int s = state;
     *             if (s >= INTERRUPTING){
     *                 handlePossibleCancellationInterrupt(s);
     *             }
     *         }
     *     }
     *
     * // FutureTask.set()设置处理结果
     * protected void set(V v) {
     *         //设置处理任务处理状态从New 为: COMPLETING(state = 1) 完成
     *         if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
     *             //返回值置为传进来的结果（Object outcome 为调用get()方法时返回的）;/** The result to return or exception to throw from get()
     *             outcome = v;
     *             //设置完返回值后,设置最终的状态设置为 NORMAL
     *             UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
     *             //调用完成方法
     *             finishCompletion();
     *         }
     *     }
     *
     * // FutureTask.setException()处理异常情况
     * protected void setException(Throwable t) {
     *         //将状态从NEW置为COMPLETING,异常也是COMPLETING(state=1状态)
     *         if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
     *             // 返回值置为传进来的异常（outcome为调用get()方法时返回的）
     *             outcome = t;
     *             //最终的状态设置为EXCEPTIONAL,设置终态为(EXCEPTIONAL =3 )
     *             UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // final state
     *             //调用完成方法
     *             finishCompletion();
     *         }
     *     }
     *
     * // Removes and signals all waiting threads【移除和唤醒所有等待的线程】 invokes done(), and nulls out callable.
     * // FutureTask.finishCompletion(),处理正常情况和异常情况
     * private void finishCompletion() {
     *         //如果队列不为空（这个队列实际上为调用者线程）
     *         for (WaitNode q; (q = waiters) != null;) {
     *             //设置等待队列为空
     *             if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
     *                 for (;;) {
     *                     //调用者线程
     *                     Thread t = q.thread;
     *                     if (t != null) {
     *                         q.thread = null;
     *                         // 如果调用者线程不为空，则唤醒它
     *                         LockSupport.unpark(t);
     *                     }
     *
     *                     WaitNode next = q.next;
     *                     //如果都唤醒了，则退出
     *                     if (next == null){
     *                         break;
     *                     }
     *                     q.next = null; // unlink to help gc
     *                     q = next;
     *                 }
     *                 break;
     *             }
     *         }
     *         //钩子方法，子类重写
     *         done();
     *         // 置空任务
     *         callable = null;        // to reduce footprint
     *     }
     *
     * 整个run()方法总结下来：
     *
     * （1）FutureTask 有一个状态state控制任务的运行过程,正常运行结束state从NEW->COMPLETING->NORMAL,异常运行结束state从NEW->COMPLETING->EXCEPTIONAL;
     *
     * （2）FutureTask 保存了运行任务的线程 runner,它是线程池中的某个线程;
     *
     * （3）调用者线程是保存在 waiters 队列中的,它是get()方法调用时,如果状态不为 COMPLETING时设置为等待
     *
     * （4)任务执行完毕,除了设置状态state变化之外,还要唤醒调用者线程。
     *
     *
     * // FutureTask.get() 获取执行结果
     * public V get() throws InterruptedException, ExecutionException {
     *         int s = state;
     *         // 如果状态小于等于 COMPLETING，则进入队列等待
     *         if (s <= COMPLETING){
     *             s = awaitDone(false, 0L);
     *         }
     *         //返回结果（异常）
     *         return report(s);
     * }
     *
     * // FutureTask.awaitDone(boolean timed,long nanos) 调用者线程阻塞等待线程池线程的执行结果
     * // 如果任务状态小于等于COMPLETING，则进入队列等待
     * private int awaitDone(boolean timed, long nanos) throws InterruptedException {
     *         final long deadline = timed ? System.nanoTime() + nanos : 0L;
     *         WaitNode q = null;
     *         boolean queued = false;
     *         for (;;) {
     *             //处理中断(会清除中断状态)
     *             if (Thread.interrupted()) {
     *                 removeWaiter(q);
     *                 throw new InterruptedException();
     *             }
     *             // 如果调用等待方法时返回状态大于COMPLETING了,则跳出循环并返回;这是自旋的出口
     *             int s = state;
     *             if (s > COMPLETING) {
     *                 if (q != null){
     *                     q.thread = null;
     *                 }
     *                 return s;
     *             }
     *             //如果状态等于 COMPLETING，说明任务快完成了，就差设置状态到NORMAL或EXCEPTIONAL和设置结果了;这时候就让出CPU，优先完成任务
     *             else if (s == COMPLETING){
     *                 Thread.yield();
     *             }
     *             //如果队列为空
     *             else if (q == null){
     *                 //初始化队列节点（WaitNode中记录了调用者线程）
     *                 q = new WaitNode();
     *             }
     *             // 等待节点进入队列（第二次循环queued是false）,后续线程还会入队
     *             else if (!queued){
     *                 // 尝试入队,设置q.next 为 waiters
     *                 // waiter队列首位
     *                 queued = UNSAFE.compareAndSwapObject(this, waitersOffset,q.next = waiters, q);
     *             }
     *             // 超时处理
     *             else if (timed) {
     *                 nanos = deadline - System.nanoTime();
     *                 // 如果超时了还没获取结果，则从等待队列移除，返回当前线程任务的状态
     *                 if (nanos <= 0L) {
     *                     removeWaiter(q);
     *                     return state;
     *                 }
     *                 //阻塞线程
     *                 LockSupport.parkNanos(this, nanos);
     *             }
     *             // 阻塞当前线程（调用者线程）
     *             else{
     *                 LockSupport.park(this);
     *             }
     *         }
     *     }
     *
     *  // awaitDone 流程：
     *
     * （1）第一次循环，状态为NEW，直接到1处，初始化队列并把调用者线程封装在WaitNode中；
     *
     * （2）第二次循环，状态为NEW，队列不为空，到2处，让包含调用者线程的WaitNode入队；
     *
     * （3）第三次循环，状态为NEW，队列不为空，且已入队，到3处，阻塞调用者线程；
     *
     * （4）假设过了一会任务执行完毕了,根据run()方法的分析最后会 unpark 调用者线程,也就是3处会被唤醒；
     *
     * （5）第四次循环，状态肯定大于COMPLETING了,退出循环并返回；
     *
     *
     *
     * // FutureTask.report(int s)
     * private V report(int s) throws ExecutionException {
     *         Object x = outcome;
     *         // 任务正常结束
     *         if (s == NORMAL){
     *             return (V)x;
     *         }
     *         if (s >= CANCELLED){
     *             throw new CancellationException();
     *         }
     *         throw new ExecutionException((Throwable)x);
     *     }
     */
}
