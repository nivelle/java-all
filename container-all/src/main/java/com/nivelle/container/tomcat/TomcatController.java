package com.nivelle.container.tomcat;

import com.nivelle.container.GsonUtils;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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


    ApplicationContext applicationContext;

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

        result.put("接口处的收到的host",httpServlet.getRemoteHost());
        return GsonUtils.toJson(result);
    }

    /**
     * 默认加载类
     *
     * @para
     */
    @RequestMapping("/defaultBeans")
    @ResponseBody
    public Object defaultBeans() {
        String[] defaultBeans = applicationContext.getBeanDefinitionNames();
        System.out.println(defaultBeans.length);
        return defaultBeans;
    }

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();

        Server server = tomcat.getServer();
        System.out.println("0 tomcat server:");
        System.out.println("server address is:" + server.getAddress());
        System.out.println("server port is:" + server.getPort());

        Service service = tomcat.getService();
        System.out.println("3 tomcat service:");
        System.out.println("service name:" + service.getName());
        System.out.println("service mapper:" + service.getMapper());
        System.out.println("service LifecycleState state:" + service.getState());
        System.out.println("service container " + service.getContainer());

        System.out.println("2 tomcat engine:");
        Engine engine = tomcat.getEngine();
        System.out.println("engine logger:" + engine.getLogger());
        System.out.println("engine jvmRoute:" + engine.getJvmRoute());
        System.out.println("engine domain:" + engine.getDomain());
        System.out.println("engine parent:" + engine.getParent());

        System.out.println("1 tomcat host:");
        Host host = tomcat.getHost();
        System.out.println("host appBase:" + host.getAppBase());
        System.out.println("host appBaseFile:" + host.getAppBaseFile());
        System.out.println("host autoDeploy:" + host.getAutoDeploy());
        System.out.println("host configClass:" + host.getConfigClass());
        System.out.println("host deployOnStartup:" + host.getDeployOnStartup());
        System.out.println("host pipleline:" + host.getPipeline());
        System.out.println("host xmbBase:" + host.getXmlBase());

        Connector connector = tomcat.getConnector();
        System.out.println("4 tomcat connector:");
        System.out.println("connector protocol:" + connector.getProtocol());
        System.out.println("connector domain:" + connector.getDomain());
        System.out.println("connector scheme:" + connector.getScheme());
        System.out.println("connector executor name:" + connector.getExecutorName());
        System.out.println();
        org.apache.catalina.connector.Request request = connector.createRequest();
        System.out.println("connector state:"+connector.getState());


        ProtocolHandler protocolHandler = connector.getProtocolHandler();
        java.util.concurrent.Executor executor = protocolHandler.getExecutor();
        System.out.println("protocolHandler executor:" + executor);
        System.out.println("protocolHandler adapter:" + protocolHandler.getAdapter());
//        protocolHandler.getExecutor().execute(()->{
//            System.out.println("protocolHandler executor execute");
//        });
        Adapter adapter = protocolHandler.getAdapter();

    }

}
