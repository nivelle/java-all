package com.nivelle.container.tomcat;

import com.nivelle.container.GsonUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/07/08
 */
@RestController
@RequestMapping(value = "/tomcat")
public class TomcatController {

    @RequestMapping(value = "mytomcat")
    public String myTomcat(ServletRequest servletRequest, ServletResponse servletResponse) {
        ServletContext servletContext = servletRequest.getServletContext();
        System.out.println("servlet:" + servletContext.getServerInfo());

        return servletContext.getServerInfo();
    }

    @RequestMapping(value = "mytomcat2")
    public String myTomcat(HttpServlet httpServlet) {
        ServletContext servletContext = httpServlet.getServletContext();
        System.out.println("servlet:" + servletContext.getServerInfo());
        return servletContext.getServerInfo();
    }

    @RequestMapping(value = "mytomcat3")
    public String myTomcat(HttpServletRequest httpServlet, HttpServletResponse httpServletResponse) {
        ServletContext servletContext = httpServlet.getServletContext();
        HashMap<String, Object> result = new HashMap();
        result.put("servername", httpServlet.getServerName());
        result.put("serverInfo", servletContext.getServerInfo());
        result.put("contextPath", servletContext.getContextPath());
        //result.put("classLoader", servletContext.getClassLoader());
        System.out.println("classLoader:"+servletContext.getClassLoader());
        return GsonUtils.toJson(result);
    }
}
