package com.nivelle.spring.springcore.event;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 上下文已经准备完毕的时候触发
 */
@Component
public class MyApplicationEnvironmentPreparedEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        System.err.println("ApplicationEnvironmentPreparedEvent:对应 Enviroment 已经准备完毕，但此时上下文context还没有创建；！");
    }
}
