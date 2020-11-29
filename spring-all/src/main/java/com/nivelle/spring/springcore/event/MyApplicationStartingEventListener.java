package com.nivelle.spring.springcore.event;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

/**
 * 正在启动的时候候触发,支持两种添加方式（application.properties配置或者java启动配置）
 * 不能通过在类上添加@Component来实现注册,因为此时容器还没有初始化成功。
 */
public class MyApplicationStartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {

        System.err.println("==============================");
        System.err.println("ApplicationStartingEvent 监听器监听到容器启动中");
        System.err.println("==============================");

    }
}
