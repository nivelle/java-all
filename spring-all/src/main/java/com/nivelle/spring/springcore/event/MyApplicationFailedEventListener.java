package com.nivelle.spring.springcore.event;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 上下文已经准备完毕的时候触发
 */
public class MyApplicationFailedEventListener implements ApplicationListener<ApplicationFailedEvent> {

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        System.err.println("ApplicationFailedEvent 监听到容器启动失败");
        System.err.println("spring启动失败原因:" + event.getException().getMessage());

    }
}
