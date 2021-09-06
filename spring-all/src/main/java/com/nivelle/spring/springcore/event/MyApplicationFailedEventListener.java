package com.nivelle.spring.springcore.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * 上下文 容器启动失败的时候触发
 */

/**
 * Event published by a {@link SpringApplication} when it fails to start.
 *
 * @author Dave Syer
 * @since 1.0.0
 * @see ApplicationReadyEvent
 */
public class MyApplicationFailedEventListener implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        System.err.println("ApplicationFailedEvent 监听到容器启动失败");
        System.err.println("spring启动失败原因:" + event.getException().getMessage());

    }
}
