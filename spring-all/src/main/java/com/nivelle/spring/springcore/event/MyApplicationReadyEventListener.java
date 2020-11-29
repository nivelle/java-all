package com.nivelle.spring.springcore.event;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * 上下文已经准备完毕的时候触发
 */
public class MyApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 需要注意的是，在普通Spring环境中，基于ApplicationListener的监听器的onApplicationEvent方法可能会被执行多次
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.err.println("ApplicationReadyEvent 监听到容器初始化成功!可以注册bean");
        if (event.getApplicationContext().getParent() == null) {
            //ignore
        }

    }
}
