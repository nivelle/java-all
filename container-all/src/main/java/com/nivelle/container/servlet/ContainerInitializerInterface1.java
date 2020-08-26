package com.nivelle.container.servlet;

import javax.servlet.ServletContext;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2020/01/10
 */
public class ContainerInitializerInterface1 implements ContainerInitializerInterface {
    @Override
    public void onStartup(ServletContext context) {
        context.setAttribute("ContainerInitializerInterface1", this);
        System.out.println("ContainerInitializerInterface1 Init ...");
    }
}
