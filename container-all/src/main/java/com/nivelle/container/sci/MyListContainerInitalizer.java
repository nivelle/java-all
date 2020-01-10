package com.nivelle.container.sci;

import javax.servlet.ServletContext;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/01/10
 */
public class MyListContainerInitalizer implements MyContainerInitalizer {
    @Override
    public void onStartup(ServletContext context) {
        context.setAttribute("MyListContainerInitalizer", this);
        System.out.println("MyListContainerInitalizer Init ...");
    }
}
