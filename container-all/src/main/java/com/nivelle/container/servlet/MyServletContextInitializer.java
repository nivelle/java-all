package com.nivelle.container.servlet;

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
@Component
public class MyServletContextInitializer implements ServletContextInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        ServletRegistration initServlet = servletContext
                .addServlet("initMyServlet", MyServlet.class);
        initServlet.addMapping("/initMyServlet");
        initServlet.setInitParameter("desc2", "nivelle");
    }
}
