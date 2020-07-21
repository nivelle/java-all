package com.nivelle.container.sci;

import javax.servlet.ServletContext;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2020/01/10
 */
public interface MyContainerInitalizer {
    void onStartup(ServletContext context);
}
