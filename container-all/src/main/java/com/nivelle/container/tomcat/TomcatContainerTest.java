package com.nivelle.container.tomcat;

import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;

/**
 * tomcat基础
 *
 * @author fuxinzhong
 * @date 2020/07/08
 */
public class TomcatContainerTest {

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        Host host = tomcat.getHost();
        System.out.println(host.getAppBase());
        System.out.println(host.getAppBaseFile());
        System.out.println(host.getAutoDeploy());


        Engine engine = tomcat.getEngine();
        Service service = tomcat.getService();

        Connector connector = tomcat.getConnector();
        System.out.println("connector protocol:" + connector.getProtocol());
        System.out.println("connector domain:" + connector.getDomain());
        System.out.println("connector scheme:" + connector.getScheme());

        ProtocolHandler protocolHandler = connector.getProtocolHandler();
        java.util.concurrent.Executor executor = protocolHandler.getExecutor();
        Adapter adapter = protocolHandler.getAdapter();
        //protocolHandler.init();

    }

}
