package com.nivelle.container.servlet;

import javax.servlet.ServletContext;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2020/01/10
 */
public class ContainerInitializerInterface2 implements ContainerInitializerInterface {
    @Override
    public void onStartup(ServletContext context) {
        context.setAttribute("ContainerInitializerInterface2",this);
        System.out.println("ContainerInitializerInterface2 Init ...");
    }
}
