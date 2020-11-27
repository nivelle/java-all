package com.nivelle.container;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.nivelle.container.dubbospi.MySpi;
import com.sun.tools.javac.util.ServiceLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.Iterator;

import com.sun.tools.attach.*;

import java.io.IOException;
import java.util.List;

/**
 * war包方式启动
 *
 * @author nivelle
 * @date 2020/01/06
 */

/**
 * WebApplicationInitializer 让我们可以使用传统的WAR包的方式部署运行 SpringApplication，
 * 可以将 servlet、filter 和 ServletContextInitializer 从应用程序上下文绑定到服务器。
 * <p>
 * 如果要配置应用程序：
 * 1. 要么覆盖 configure(SpringApplicationBuilder) 方法(调用 SpringApplicationBuilder#Sources(Class.))，
 * 2. 要么使初始化式本身成为 @configuration。
 * <p>
 * 如果将 SpringBootServletInitializer与其他 WebApplicationInitializer 结合使用，则可能还需要添加@Ordered注解来配置特定的启动顺序。
 */
@SpringBootApplication
public class ContainerBootstrapApplication extends SpringBootServletInitializer {

    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {

        //获取当前系统中所有 运行中的 虚拟机
        System.out.println("running JVM start ");
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : list) {
            //如果虚拟机的名称为 xxx 则 该虚拟机为目标虚拟机，获取该虚拟机的 pid
            //然后加载 agent.jar 发送给该虚拟机
            System.out.println("vm displayName:" + vmd.displayName());
            if (vmd.displayName().endsWith("com.nivelle.container.ContainerBootstrapApplication")) {
                System.out.println("vmd is:" + vmd.id());
                VirtualMachine virtualMachine = VirtualMachine.attach(vmd.id());
                virtualMachine.loadAgent("/Users/nivellefu/IdeaProjects/java-guides/java-agent/target/java-agent-1.0-SNAPSHOT.jar");
                virtualMachine.detach();
            }
        }

        SpringApplication.run(ContainerBootstrapApplication.class, args);
        //JDK SPI机制
        ServiceLoader<MySpi> serviceLoader = ServiceLoader.load(MySpi.class);
        Iterator iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            System.err.println(iterator.next());
        }
        /**
         * dubbo的spi机制
         */
        ExtensionLoader<MySpi> extensionLoader = ExtensionLoader.getExtensionLoader(MySpi.class);

        MySpi mySpi1 = extensionLoader.getExtension("MySpiService1");
        mySpi1.sayHelloSpi();
        MySpi mySpi2 = extensionLoader.getExtension("MySpiService2");
        mySpi2.sayHelloSpi();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ContainerBootstrapApplication.class);
    }

    /**
     * 部署到阿里云后的启动日志
     *
     * tail -f catalina.out
     *
     * of web application directory [/home/tomcat/apache-tomcat-8.5.50/webapps/examples] has finished in [445] ms
     * 14-Jan-2020 00:17:08.649 信息 [main] org.apache.coyote.AbstractProtocol.start 开始协议处理句柄["http-nio-80"]
     * 14-Jan-2020 00:17:08.682 信息 [main] org.apache.coyote.AbstractProtocol.start 开始协议处理句柄["ajp-nio-8009"]
     * 14-Jan-2020 00:17:08.685 信息 [main] org.apache.catalina.startup.Catalina.start Server startup in 1263 ms
     * 14-Jan-2020 00:18:48.686 信息 [ContainerBackgroundProcessor[StandardEngine[Catalina]]] org.apache.catalina.startup.HostConfig.undeploy Undeploying context []
     * 14-Jan-2020 00:18:48.714 信息 [localhost-startStop-2] org.apache.catalina.startup.HostConfig.deployWAR Deploying web application archive [/home/tomcat/apache-tomcat-8.5.50/webapps/ROOT.war]
     * class com.nivelle.container.sci.MyMapContainerInitalizer
     * MyMapContainerInitalizer Init ...
     * class com.nivelle.container.sci.MyListContainerInitalizer
     * MyListContainerInitalizer Init ...
     * >>>>>>
     *
     *   .   ____          _            __ _ _
     *  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
     * ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
     *  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
     *   '  |____| .__|_| |_|_| |_\__, | / / / /
     *  =========|_|==============|___/=/_/_/_/
     *  :: Spring Boot ::        (v2.1.4.RELEASE)
     */
}
