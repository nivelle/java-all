package com.nivelle.spring.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * 异常处理器
 *
 * @author fuxinzhong
 * @date 2019/12/02
 */
@Component
public class ConsumerAwareErrorHandler implements ConsumerAwareListenerErrorHandler {

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        //异常处理类,此类可以做重试策略，当消费者出现异常的时候发送给其它topic下的分区
        System.err.println("message header is:" + message.getHeaders());
        System.err.println("message payLoad is:" + message.getPayload());
        System.err.println("exception is:" + exception);
        System.err.println("consumer is:" + consumer);
        return null;
    }

}
