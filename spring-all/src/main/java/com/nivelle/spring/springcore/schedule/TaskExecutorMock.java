package com.nivelle.spring.springcore.schedule;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor;

/**
 * Spring 框架内置的TaskExecutor实现
 *
 * @author fuxinzhong
 * @date 2021/05/04
 */
public class TaskExecutorMock {
    /**
     * 不会复用线程，对应每个请求都会新创建一个对应的线程来执行
     */
    SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
    /**
     * 不会异步执行任务，同步使用调用线程来执行
     */
    SyncTaskExecutor syncTaskExecutor = new SyncTaskExecutor();
    /**
     * 对 Executor的包装，通过setConcurrentExecutor 接口可以设置JUC中的线程池到其内部来做适配
     * ThreadPoolTaskExecutor 通过bean属性的方式配置executor线程池的属性
     */
    ConcurrentTaskExecutor concurrentTaskExecutor = new ConcurrentTaskExecutor();
    /**
     * 实际上是Quartz 的 SimpleThreadPool 的子类，它监听spring 的生命周期。当你有一个可能需要Quartz和非Quartz组件共享的线程池时，通常回使用该实现
     *
     */
    SimpleThreadPoolTaskExecutor simpleThreadPoolTaskExecutor = new SimpleThreadPoolTaskExecutor();




}
