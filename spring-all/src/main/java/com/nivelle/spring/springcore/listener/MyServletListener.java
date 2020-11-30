package com.nivelle.spring.springcore.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * 依赖于 servlet 容器
 * 1. 基于ServletContextListener的监听器要比基于ApplicationListener的监听器先执行。因为前者是Tomcat/Jetty容器启动后就执行,后者需要Spring应用初始化完成后才执行。
 * <p>
 * 2. 依赖于 servlet 容器,需要配置 web.xml（Spring Boot只需要配置@WebListener即可,并且使用@WebListener后,可以注入bean)
 */
@WebListener//配合启动类上的@ServletComponentScan
public class MyServletListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("ServletContextListener监听到容器销毁操作！");

    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        System.out.println("ServletContentListener:监听到容器初始化成功,先于ApplicationListener执行，可注入bean");
    }


}
