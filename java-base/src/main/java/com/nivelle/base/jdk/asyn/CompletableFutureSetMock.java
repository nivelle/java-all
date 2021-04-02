package com.nivelle.base.jdk.asyn;

import java.util.concurrent.*;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/04/02
 */
public class CompletableFutureSetMock {

    private final static int AVAILABLE_PROCESS = Runtime.getRuntime().availableProcessors();
    private final static ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            AVAILABLE_PROCESS,
            AVAILABLE_PROCESS * 2, 1,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(5),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<String>();
        //开启线程执行任务，并设置结果
        THREAD_POOL_EXECUTOR.execute(() -> {
            try {
                //模拟任务执行
                Thread.sleep(3000);
            } catch (Exception e) {

            }
            System.out.println("------" + Thread.currentThread().getName() + "set future result");
            //显示设置CompletableFuture 的结果
            future.complete("fuck jessy");
        });
        //等待计算结果
        System.out.println("main thread wait result----");
        //future 获取结果，如果任务没有执行完毕，则阻塞
        System.out.println(future.get());
        System.out.println("main thread wait result success");
    }
}
