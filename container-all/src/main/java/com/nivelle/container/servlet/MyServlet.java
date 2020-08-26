package com.nivelle.container.servlet;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义Servlet
 *
 * @author fuxinzhong
 * @date 2020/08/25
 */
public class MyServlet implements Servlet {

    private ServletConfig servletConfig;
    private ServletRequest servletRequest;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        this.servletConfig=servletConfig;
        System.out.println("Servlet的初始化方法...");
    }


    @Override
    public ServletConfig getServletConfig() {

        return servletConfig;
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        this.servletRequest=servletRequest;
        System.out.println("service....运行时方法");
        System.out.println("获得Servlet的配置对象:"+this.getServletConfig());
        System.out.println("变量tom的值："+servletConfig.getInitParameter("tom"));

        // 解决页面乱码，同时指定页面类型为HTML
        servletRequest.setCharacterEncoding("utf-8");
        servletResponse.setContentType("text/html;charset=utf-8");
        servletResponse.setCharacterEncoding("utf-8");
        PrintWriter writer = servletResponse.getWriter();
        writer.println("===============================");
        writer.println("<a href='http://nivelle.me'>去nivelle</a>");
    }

    @Override
    public String getServletInfo() {
        return servletRequest.getServletContext().getServerInfo();
    }

    @Override
    public void destroy() {
        System.out.println("Servlet的销毁方法...");
    }

}
