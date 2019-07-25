package com.nivelle.guide.kafka;

/**
 * consumer消费者
 *
 * @author fuxinzhong
 * @date 2019/07/25
 */

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Consumer {

    @KafkaListener(topics = {"myKafka"})
    public void listen(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            System.out.println("---->" + record);
            System.out.println("---->" + message);
        }

    }
}
