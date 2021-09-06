package com.nivelle.spring.springcore.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Event raised when an {@code ApplicationContext} gets closed.
 *
 * @author Juergen Hoeller
 * @since 12.08.2003
 * @see ContextRefreshedEvent
 */
public class MyContextClosedEventListenerListener implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        System.err.println("ContextClosedEvent 当使用 ConfigurableApplicationContext接口的close()方法关闭ApplicationContext容器时触发该事件");
    }
}
