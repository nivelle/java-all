package com.nivelle.spring.configbean;


import com.nivelle.spring.rabbitmq.DirectMessageReceiver;
import com.nivelle.spring.rabbitmq.DirectMessageReceiver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ配置
 *
 * @author fuxinzhong
 * @date 2019/07/01
 */
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Setter
@Getter
@Slf4j
public class RabbitMQConfig {

    @Value("host")
    private String host;
    @Value("port")
    private String port;
    @Value("userName")
    private String userName;
    @Value("password")
    private String password;

    //fanout
    @Value("fanout.exchange")
    private String fanoutExchange;
    @Value("fanout.exchange.routingKey")
    private String fanoutExchangeRoutingKey;

    @Value("fanout.queue1")
    private String fanoutQueue1;
    @Value("fanout.queue2")
    private String fanoutQueue2;
    @Value("fanout.queue3")
    private String fanoutQueue3;

    //direct
    @Value("direct.exchange")
    private String directExchange;
    @Value("direct.exchange.routingKey1")
    private String directExchangeRoutingKey1;
    @Value("direct.exchange.routingKey2")
    private String directExchangeRoutingKey2;

    @Value("direct.queue1")
    private String directQueue1;
    @Value("direct.queue2")
    private String directQueue2;

    @Autowired
    DirectMessageReceiver directMessageReceiver;


    //fanout
    @Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange(fanoutExchange);
    }

    @Bean
    public Queue fanoutMessage1() {
        return new Queue(fanoutQueue1);
    }

    @Bean
    public Queue fanoutMessage2() {
        return new Queue(fanoutQueue2);
    }

    @Bean
    public Queue fanoutMessage3() {
        return new Queue(fanoutQueue3);
    }

    @Bean
    Binding bindingFanoutExchangeA(Queue fanoutMessage1, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutMessage1).
                to(fanoutExchange);
    }

    @Bean
    Binding bindingFanoutExchangeB(Queue fanoutMessage2, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutMessage2).to(fanoutExchange);
    }

    @Bean
    Binding bindingFanoutExchangeC(Queue fanoutMessage3, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutMessage3).to(fanoutExchange);
    }


    //direct
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(directExchange);
    }

    @Bean
    public Queue directMessage1() {
        return new Queue(directQueue1);
    }

    @Bean
    public Queue directMessage2() {
        return new Queue(directQueue2);
    }

    @Bean
    Binding bindingDirectExchangeA(Queue directMessage1, DirectExchange directExchange) {
        return BindingBuilder.bind(directMessage1).to(directExchange).with(directExchangeRoutingKey1);
    }

    @Bean
    Binding bindingDirectExchangeB(Queue directMessage2, DirectExchange directExchange) {
        return BindingBuilder.bind(directMessage2).to(directExchange).with(directExchangeRoutingKey2);
    }

    /**
     * 初始化阅读详情 rabbitMqTemplate
     *
     * @return
     */
    @Bean(name = "rabbitTemplate")
    public RabbitTemplate getRabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        //未找到投递队列时，则将消息返回给生成者
        template.setMandatory(true);

        //确认消息是否到达broker服务器，也就是只确认是否正确到达exchange中即可，只要正确的到达exchange中，broker即可确认该消息返回给客户端ack。
        template.setConfirmCallback((correlationData, ack, cause) -> {
            log.info("消息确认结果:correlationData={},ack={},cause={}", correlationData, ack, cause);
        });
        //mandatory这个参数为true表示如果发送消息到了RabbitMq，没有对应该消息的队列。那么会将消息返回给生产者，此时仍然会发送ack确认消息
        template.setReturnCallback((message, replyCode, replyText, exchange, routingKey)
                -> log.info("消息返回结果：return callback message：{},code:{},text:{}", message, replyCode, replyText));
        return template;
    }

    /**
     * 连接配置
     *
     * @return
     */
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(Integer.parseInt(port));
        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        //发送者确认
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    @Bean
    SimpleMessageListenerContainer fanoutContainerA(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageListener(message -> log.info("simple fanoutQueue1,message:{}", message));
        container.setQueueNames(fanoutQueue1);
        //消费者手动确认,其实都是手动确认，只不过 Auto确认是spring包装了一层,对于执行异常会放到待确认队列，重新投递
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return container;
    }

    @Bean
    SimpleMessageListenerContainer fanoutContainerB(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageListener(message -> log.info("simple fanoutQueue2,message:{}", message));
        container.setQueueNames(fanoutQueue2);
        return container;
    }

    @Bean
    SimpleMessageListenerContainer fanoutContainerC(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageListener(message -> log.info("simple fanoutQueue3,message:{}", message));
        container.setQueueNames(fanoutQueue3);
        return container;
    }

    @Bean
    SimpleMessageListenerContainer directContainerA(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageListener(message -> log.info("simple directQueue1,message:{}", message));
        container.setQueueNames(directQueue1);
        return container;
    }

    @Bean
    SimpleMessageListenerContainer directContainerB(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageListener(directMessageReceiver);
        container.setQueueNames(directQueue2);
        //消费者手动确认,其实都是手动确认，只不过 Auto确认是spring包装了一层,对于执行异常会放到待确认队列，重新投递
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return container;
    }
}
