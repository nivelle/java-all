package com.nivelle.spring.springboot.listener.context;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;


public class MyContextStartedEventListener implements ApplicationListener<ContextStartedEvent> {

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        System.err.println("ContextStartedEvent 当使用ConfigurableApplicationContext接口的start()方法启动ApplicationContext容器时触发该事件");
    }
}
