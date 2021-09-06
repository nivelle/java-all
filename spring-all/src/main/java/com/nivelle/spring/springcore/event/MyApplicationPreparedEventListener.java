package com.nivelle.spring.springcore.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

/**
 * 正在启动的时候候触发,支持两种添加方式（application.properties配置或者java启动配置）
 * 不能通过在类上添加@Component来实现注册,因为此时容器还没有初始化成功。
 */


/**
 * Event published as when a {@link SpringApplication} is starting up and the
 * {@link ApplicationContext} is fully prepared but not refreshed. The bean definitions
 * will be loaded and the {@link Environment} is ready for use at this stage.
 *
 * @author Dave Syer
 * @since 1.0.0
 */
public class MyApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent> {


    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        System.err.println("ApplicationPreparedEvent 上下文context创建完成,但此时spring中的bean是没有完全加载完成的；！！！！！");

    }
}
