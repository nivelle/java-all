package com.nivelle.base.jdk.asyn;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/02
 */
public class CompleteAbleFutureRunAsyncMock {
    private final static int AVAILABLE_PROCESS = Runtime.getRuntime().availableProcessors();

    private final static ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            AVAILABLE_PROCESS,
            AVAILABLE_PROCESS * 2, 1,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(5),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) throws Exception {
        CompletableFuture completableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {

                }
                System.out.println("run over.....");
            }
        });
        //默认是ForkJoinPool.commonPool()线程池来执行任务
        System.out.println("主线程执行");
        //同步等待异步任务执行结束
        System.out.println(completableFuture.get());

        System.out.println("=============自定义线程池================");

        CompletableFuture completableFutureAsync = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {

                }
                System.out.println("async run over.....");
            }
        }, THREAD_POOL_EXECUTOR);
        System.out.println("主线程等待执行结果");
        System.out.println(completableFutureAsync.get());

    }
}
