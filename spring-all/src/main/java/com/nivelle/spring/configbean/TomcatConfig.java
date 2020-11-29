package com.nivelle.spring.configbean;

import com.nivelle.spring.springcore.event.GracefulShutdown;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * tomcat配置类
 *
 * @author nivelle
 * @date 2019/07/25
 */
@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }


    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(final GracefulShutdown gracefulShutdown) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(gracefulShutdown);
        return factory;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {
        server.setPort(8080);
        server.setContextPath("/springAll");
        ((TomcatServletWebServerFactory) server).addConnectorCustomizers((Connector connector) -> {
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
            protocol.setMaxConnections(200);
            protocol.setMaxThreads(200);
            protocol.setSelectorTimeout(3000);
            protocol.setSessionTimeout(3000);
            protocol.setConnectionTimeout(3000);
        });
    }

}
