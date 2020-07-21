package com.nivelle.container;

import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.Adapter;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.Request;

/**
 * tomcat基础
 *
 * @author fuxinzhong
 * @date 2020/07/08
 */
public class TomcatContainerTest {

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
