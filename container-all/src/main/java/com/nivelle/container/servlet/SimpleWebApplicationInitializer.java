package com.nivelle.container.servlet;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

/**
 * 等效web.xml
 *
 * @author fuxinzhong
 * @date 2020/08/25
 */
public class SimpleWebApplicationInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        servletContext.addListener(new ContextLoaderListener(webContext));
        webContext.register(WebConfig.class);
        ServletRegistration.Dynamic registration = servletContext.addServlet("myServlet", new DispatcherServlet(webContext));
        registration.setLoadOnStartup(1);
        registration.addMapping("/myServlet");
    }

}

