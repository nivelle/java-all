package com.nivelle.spring.springcore.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;

public class MyContextStopedEventEventListener implements ApplicationListener<ContextStoppedEvent> {

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {

        System.err.println("ContextStoppedEvent 当使用ConfigurableApplicationContext接口的stop()方法停止ApplicationContext容器时触发该事件");
    }
}
