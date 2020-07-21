package com.nivelle.container.tomcat;

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
    public TomcatGracefulShutdownListener gracefulShutdown() {
        return new TomcatGracefulShutdownListener();
    }


    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(final TomcatGracefulShutdownListener gracefulShutdown) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(gracefulShutdown);
        factory.addAdditionalTomcatConnectors(createConnector());
        return factory;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {
        server.setPort(8080);
        ((TomcatServletWebServerFactory) server).addConnectorCustomizers((Connector connector) -> {
            connector.setParseBodyMethods("delete");
            Http11NioProtocol protocolHandler = (Http11NioProtocol) connector.getProtocolHandler();
            protocolHandler.setMaxConnections(200);
            protocolHandler.setMaxThreads(200);
            protocolHandler.setSelectorTimeout(3000);
            protocolHandler.setSessionTimeout(3000);
            protocolHandler.setConnectionTimeout(3000);
        });
    }

    private Connector createConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocolHandler = (Http11NioProtocol) connector.getProtocolHandler();
        connector.setPort(8090);
        // 最大线程数
        protocolHandler.setMaxThreads(2);
        // 最大连接数
        protocolHandler.setMaxConnections(10);
        connector.setAllowTrace(true);
        connector.setRedirectPort(8080);
        return connector;
    }
}
