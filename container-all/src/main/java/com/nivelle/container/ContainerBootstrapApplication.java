package com.nivelle.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * war包方式启动
 *
 * @author nivelle
 * @date 2020/01/06
 */
@SpringBootApplication
public class ContainerBootstrapApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ContainerBootstrapApplication.class, args);
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
