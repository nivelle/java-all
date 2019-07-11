package com.nivelle.guide.datastructures;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadPool
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class ThreadPoolData {

    public static void main(String[] args) {

        AtomicInteger atomicInteger = new AtomicInteger(1);
        ThreadLocal threadLocal = ThreadLocal.withInitial(() -> atomicInteger.get());
        /**
         * ThreadPoolExecutor 工作流程:
         *
         * 1. 线程池数目先增加至 corePoolSize 大小限制数目
         * 2. 将任务添加至阻塞队列，至阻塞队列大小限制此时线程数目不再增加(若添加失败也会增加线程)
         * 3. 线程继续增加,最终达到 maximumPoolSize大小限制的数目
         * 4. 超出的任务数目采取拒绝策略
         */

        /**
         * corePoolSize:默认等待有任务到来才创建线程去执行任务，调用preStartAllCoreThreads()或preStartCoreThread()在没有任务到来之前就创建corePoolSize个线程或者一个线程。
         *
         * keepAliveTime:如果线程池中的线程数量大于 corePoolSize时，如果某线程空闲时间超过keepAliveTime，线程将被终止，直至线程池中的线程数目不大于corePoolSize；
         * 如果设置allowCoreThreadTimeOut，那么核心池中的线程空闲时间超过 keepAliveTime，线程也会被终止。
         */

        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue(5));

        for (int i = 0; i < 15; i++) {
            MyTask myTask = new MyTask(i, threadLocal);
            executor.execute(myTask);
            System.out.println("线程池中线程数目：" + executor.getPoolSize() + "，队列中等待执行的任务数目：" +
                    executor.getQueue().size() + "，已执行完成的任务数目：" + executor.getCompletedTaskCount());
        }
        System.out.println("main 线程中的值:"+threadLocal.get());
        executor.shutdown();
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
        System.out.println("正在执行task " + taskNum+"修改自己的变量值:");
        threadLocal.set(taskNum);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        System.out.println(taskNum+":当前线程中的数据:" + threadLocal.get());
        System.out.println("task " + taskNum + "执行完毕");
    }
}

