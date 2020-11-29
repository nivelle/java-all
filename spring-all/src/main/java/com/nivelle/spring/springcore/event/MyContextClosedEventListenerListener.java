package com.nivelle.spring.springcore.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;


public class MyContextClosedEventListenerListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.err.println("ContextClosedEvent 当使用ConfigurableApplicationContext接口的close()方法关闭ApplicationContext容器时触发该事件");
    }
}
