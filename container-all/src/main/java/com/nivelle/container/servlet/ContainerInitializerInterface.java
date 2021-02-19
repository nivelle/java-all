package com.nivelle.container.servlet;

import javax.servlet.ServletContext;

/**
 * @author nivelle
 * @HandlesTypes 标注的 ServletContainerInitializer 要注入到Servlet容器中的组件
 * @date 2020/01/10
 */
public interface ContainerInitializerInterface {
    void onStartup(ServletContext context);
}
