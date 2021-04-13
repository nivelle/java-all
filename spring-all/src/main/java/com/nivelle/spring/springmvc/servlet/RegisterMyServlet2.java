package com.nivelle.spring.springmvc.servlet;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

/**
 * 自定义Servlet
 *
 * @author nivelle
 * @date 2020/01/15
 */
//容器启动时自动注入
public class RegisterMyServlet2 implements ServletContextInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        ServletRegistration initServlet = servletContext
                .addServlet("initServlet2", MyServlet2.class);
        initServlet.addMapping("/initServlet2");
        initServlet.setInitParameter("desc2", "nivelle");
    }
}
