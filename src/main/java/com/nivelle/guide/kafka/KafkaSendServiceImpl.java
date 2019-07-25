package com.nivelle.guide.kafka;

import com.google.gson.GsonBuilder;
import com.nivelle.guide.springboot.entity.KafkaMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * kafka 消息发送
 *
 * @author fuxinzhong
 * @date 2019/07/25
 */
@Service
public class KafkaSendServiceImpl implements KafkaSendService {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Override
    public void send() {
        try {
            KafkaMessage message = new KafkaMessage();
            message.setId("KFK_" + System.currentTimeMillis());
            message.setMsg("nivelle love jessy");
            message.setSendTime(new Date());
            kafkaTemplate.send("myKafka", new GsonBuilder().create().toJson(message));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
