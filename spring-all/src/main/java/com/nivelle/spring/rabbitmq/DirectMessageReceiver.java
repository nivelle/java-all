package com.nivelle.spring.rabbitmq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Service;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2019/07/03
 */
@Service
@Slf4j
public class DirectMessageReceiver implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {

        try {
            byte[] body = message.getBody();
            System.err.println("simple directQueue2,message:" + new String(body));
            //成功消费
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //没有成功消费
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
