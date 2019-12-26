package com.nivelle.container.sci;

import javax.servlet.ServletContext;

/**
 * 自定义 SpringServletContainerInitializer
 *
 * @author fuxinzhong
 * @date 2019/12/25
 */
public interface MyContainerInitalizer {

    void onStartup(ServletContext servletContext);
}
