package com.nivelle.spring.springcore.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * 上下文已经准备完毕的时候触发
 */

/**
 * Event published as late as conceivably【可以想象】 possible to indicate that the application is
 * ready to service requests. The source of the event is the {@link SpringApplication}
 * itself, but beware of modifying its internal state since all initialization steps will
 * have been completed by then.
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 * @see ApplicationFailedEvent
 */
public class MyApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 需要注意的是，在普通Spring环境中，基于ApplicationListener的监听器的 onApplicationEvent 方法可能会被执行多次
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.err.println("ApplicationReadyEvent 监听到容器初始化成功!可以注册bean");
        if (event.getApplicationContext().getParent() == null) {
            //ignore
        }

    }
}
