package com.nivelle.spring.springcore.event;

import org.springframework.context.ApplicationEvent;

/**
 * webApplicationContext.publishEvent(new MyEvent("你好")); 通过事件发布器 实现自定义事件的发布， 同时需要有自定义事件监听器
 * 完成 事件发生时的处理动作
 */
public class MyEvent extends ApplicationEvent {

    public MyEvent(Object source) {
        super(source);
        System.out.println("自定义事件触发了！！！" + source);
    }

    public void printEventContent(String message) {
        System.out.println("监听到事件:" + MyEvent.class + message);
    }
}
