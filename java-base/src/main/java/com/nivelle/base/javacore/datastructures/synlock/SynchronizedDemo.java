package com.nivelle.base.javacore.datastructures.synlock;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Synchronized 方法
 *
 * @author fuxinzhong
 * @date 2019/11/02
 */
public class SynchronizedDemo {

    public static ThreadPoolTaskExecutor threadPoolTaskExecutor = null;

    static {
        threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setKeepAliveSeconds(200);
        threadPoolTaskExecutor.setThreadNamePrefix("taskExecutor--");
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setAwaitTerminationSeconds(60);
        threadPoolTaskExecutor.setQueueCapacity(5);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static void main(String[] args) {
        SynchronizedDemo synchronizedDemo = new SynchronizedDemo();
        Long count = new Long(1);
        for (int i = 0; i < 1; i++) {
            SynchronizedDemo.threadPoolTaskExecutor.execute(new SynchronizedTask(count));
        }
    }
}


/**
 * 1. synchronized
 */
class SynchronizedTask implements Runnable {

    Long count;

    public SynchronizedTask(Long count) {
        this.count = count;
    }

    @Override
    public synchronized void run() {
        Long result = count++;
        System.out.println("threadName:" + Thread.currentThread().getName() + ";current int:" + result);
        return;
    }
}
