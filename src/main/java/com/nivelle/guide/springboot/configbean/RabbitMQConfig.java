package com.nivelle.guide.springboot.configbean;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2019/07/01
 */
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitMQConfig {

    @Value("host")
    private String host;
    @Value("port")
    private int port;
    @Value("userName")
    private String userName;
    @Value("password")
    private String password;


    @Bean(name = "rabbitTemplate")
    public RabbitTemplate getRabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        return template;
    }

    public org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        connectionFactory.setHost(host);
        connectionFactory.setPort(port);

        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        //必须要设置
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
