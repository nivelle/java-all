package com.nivelle.base.datastructures;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPool
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class ThreadPoolDemo {

    public static void main(String[] args) {

        AtomicInteger atomicInteger = new AtomicInteger(1);
        ThreadLocal threadLocal = ThreadLocal.withInitial(() -> atomicInteger.get());
        /**
         * ThreadPoolExecutor 工作流程:
         *
         * 1. 线程池数目先增加至 corthreadLocal.remove();ePoolSize 大小限制数目
         * 2. 将任务添加至阻塞队列，至阻塞队列大小限制此时线程数目不再增加(若添加失败也会增加线程)
         * 3. 线程继续增加,最终达到 maximumPoolSize大小限制的数目
         * 4. 超出的任务数目采取拒绝策略
         */

        /**
         * 1. corePoolSize:默认等待有任务到来才创建线程去执行任务，调用preStartAllCoreThreads()或p
         * reStartCoreThread()在没有任务到来之前就创建corePoolSize个线程或者一个线程。//even if other worker threads are idle
         *
         * 2. keepAliveTime:如果线程池中的线程数量大于 corePoolSize时，如果某线程空闲时间超过keepAliveTime，线程将被终止，直至线程池中的线程数目不大于corePoolSize；
         * 如果设置 allowCoreThreadTimeOut，那么核心池中的线程空闲时间超过 keepAliveTime，线程也会被终止。
         *
         * 3. 默认的RejectedExecutionHandler 是AbortPolicy,拒绝任务同时抛出 RejectedExecutionException
         *
         * 总共有四种拒绝策略:
         * ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。
         * ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
         * ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
         * ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务
         *
         * 4. 队列
         * - SynchronousQueue:仅仅是投递作用。 处理能力不足//an attempt to queue a task will fail if no threads are immediately available to run it, so a new thread will be constructed
         * - LinkedBlockingQueue://This may be appropriate when each task is completely independent of others
         * - ArrayBlockingQueue
         *
         * */

        /**    ctl:高3位表示状态，低29位表示线程数目
         *
         *     RUNNING:  Accept new tasks and process queued tasks
         *     SHUTDOWN: Don't accept new tasks, but process queued tasks
         *     STOP:     Don't accept new tasks, don't process queued tasks,and interrupt in-progress tasks
         *     TIDYING:  All tasks have terminated, workerCount is zero,the thread transitioning to state TIDYING
         *               will run the terminated() hook method
         *     TERMINATED: terminated() has completed
         */
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5));
        // 提前启动所有的核心线程数目线程
        executor.prestartAllCoreThreads();
        //核心线程数目也可以设置过期时间
        executor.allowCoreThreadTimeOut(true);
        for (int i = 0; i < 1555; i++) {
            MyTask myTask = new MyTask(i, threadLocal);
            /** 在worker中通过 threadFactory 创建一个线程去执行这个task. **/
            executor.execute(myTask);
            if (i == 3) {
                //corePoolSize 和 maxMumPoolSize是可以调节的
                executor.setCorePoolSize(5);
                executor.setMaximumPoolSize(15);
                // keepAliveTime 可以动态设置
                executor.setKeepAliveTime(10, TimeUnit.MILLISECONDS);
            }
            System.out.println("线程池中线程数目：" + executor.getPoolSize() + "，队列中等待执行的任务数目：" +
                    executor.getQueue().size() + "，已执行完成的任务数目：" + executor.getCompletedTaskCount());
        }
        System.out.println("main 线程中的值:" + threadLocal.get());
        executor.shutdown();
        int priority = Thread.currentThread().getPriority();
        System.err.println("当前线程的优先级,priority=" + priority);

        executorsThreadFactory(threadLocal);
    }


    public static void executorsThreadFactory(ThreadLocal threadLocal) {

        /**
         * 线程池默认提供的线程工程实现,创建的线程优先级为Thread.NORMAL = 5;非守护线程；
         *
         * if a ThreadFactory ails to create a thread when asked by returning null from {@code newThread}, the executor will continue, but might not be able to execute any tasks.
         */
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadLocal.set(20);
        MyTask myTask = new MyTask(20, threadLocal);
        Thread thread = threadFactory.newThread(myTask);
        thread.start();
        threadLocal.remove();
    }
}


class MyTask implements Runnable {
    private int taskNum;
    private ThreadLocal threadLocal;

    public MyTask(int num, ThreadLocal threadLocal) {
        this.taskNum = num;
        this.threadLocal = threadLocal;
    }

    @Override
    public void run() {
        System.out.println("正在执行task " + taskNum + "修改自己的变量值:");
        try {
            if (taskNum == 20) {
                System.err.println("自定义线程:" + taskNum + "本地变量: " + threadLocal.get());
            } else {
                threadLocal.set(taskNum);

            }

            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        System.out.println(taskNum + ":当前线程中的数据:" + threadLocal.get());
        /**
         * threadName:
         * New threads have names accessible via  of pool-N-thread-M</em>, where <em>N</em> is the sequence
         * number of this factory, and <em>M</em> is the sequence number of the thread created by this factory.
         */
        System.out.println("task " + Thread.currentThread().getName() + taskNum + "执行完毕");
        threadLocal.remove();

    }

    //todo worker


}

