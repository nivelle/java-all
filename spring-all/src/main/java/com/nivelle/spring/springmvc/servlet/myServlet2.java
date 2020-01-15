package com.nivelle.spring.springmvc.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义Servlet
 *
 * @author fuxinzhong
 * @date 2020/01/15
 */
public class myServlet2 extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String desc = getServletConfig().getInitParameter("desc2");
        resp.getOutputStream().println("desc2 is " + desc);
    }

}
