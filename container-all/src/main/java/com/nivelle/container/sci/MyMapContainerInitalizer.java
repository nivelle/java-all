package com.nivelle.container.sci;

import javax.servlet.ServletContext;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2020/01/10
 */
public class MyMapContainerInitalizer implements MyContainerInitalizer {
    @Override
    public void onStartup(ServletContext context) {
        context.setAttribute("MyMapContainerInitalizer",this);
        System.out.println("MyMapContainerInitalizer Init ...");
    }
}
