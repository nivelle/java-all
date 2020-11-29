package com.nivelle.spring.springcore.event;

import org.springframework.context.ApplicationEvent;

public class MyEvent extends ApplicationEvent {

    public MyEvent(Object source) {
        super(source);
        System.err.println("自定义事件触发了！！！"+source);
    }

    public void printEventContent(String message) {
        System.out.println("监听到事件:" + MyEvent.class + message);
    }
}
