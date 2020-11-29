package com.nivelle.spring.springcore.event;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 正在启动的时候候触发,支持两种添加方式（application.properties配置或者java启动配置）
 * 不能通过在类上添加@Component来实现注册,因为此时容器还没有初始化成功。
 */
public class MyApplicationStartedEventListener implements ApplicationListener<ApplicationStartedEvent> {


    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        System.err.println("ApplicationStartedEvent 监听器监听到容器启动完毕！！！！！");

    }
}
