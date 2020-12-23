package com.nivelle.rpc.dubbo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 开启异步执行功能,配置两个线程池
 *
 * @author nivelle
 * @date 2019/08/23
 */
@Configuration
@EnableAsync
public class AsyncConfig {


    @Bean(name = "littleExecutor")
    public Executor littleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(100);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("littleExecutor-");
        executor.setAllowCoreThreadTimeOut(true);
        //executor.setThreadFactory();
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean(name = "largeExecutor")
    public Executor largeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("largeExecutor-");
        executor.initialize();
        return executor;
    }

}
