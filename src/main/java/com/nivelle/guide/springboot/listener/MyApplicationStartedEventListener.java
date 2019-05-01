package com.nivelle.guide.springboot.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * spring boot启动监听类
 */
@Component

public class MyApplicationStartedEventListener implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * 需要注意的是，在普通Spring环境中，基于ApplicationListener的监听器的onApplicationEvent方法可能会被执行多次
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("MyApplicationStartedEventListener 监听到容器初始化成功！也可以注册bean");
        if (event.getApplicationContext().getParent() == null) {
            //ignore
        }

    }
}
