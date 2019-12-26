package com.nivelle.spring.kafka;

/**
 * consumer消费者
 *
 * @author fuxinzhong
 * @date 2019/07/25
 */

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@EnableKafka
public class Consumer {

    /**
     * 消费者接受数据类型:
     * <p>
     * data:对于data值的类型其实并没有限定，根据KafkaTemplate所定义的类型来决定。data为List集合的则是用作批量消费。
     * ConsumerRecord:具体消费数据类，包含Headers信息、分区信息、时间戳等
     * Acknowledgment:用作Ack机制的接口
     * Consumer:消费者类，使用该类我们可以手动提交偏移量、控制消费速率等功能
     */

    @KafkaListener(topics = "kafkaLearn")
    public void listen(ConsumerRecord<?, ?> record, Acknowledgment ack) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            System.err.println("top is:" + record.topic() + ";record:" + record);
        }
        ack.acknowledge();

    }

    @KafkaListener(id = "defaultId", topics = "defautTopic", containerFactory = "concurrentListenContainerFactory")
    public void listen2(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            System.err.println("top is:" + record.topic() + ";record:" + record);
        }
    }

    @KafkaListener(id = "filterTopicId", topics = "filterTopic", containerFactory = "concurrentListenContainerFactory",
            errorHandler = "consumerAwareErrorHandler")
    public void listen3(String data, Acknowledgment ack) {
        /**
         * 被过滤掉的空消息不能转为ConsumerRecord
         */
        System.err.println("record is:" + data);
        ack.acknowledge();

    }

    @KafkaListener(id = "ackId", topics = "ackTopic", containerFactory = "ackListenContainerFactory",
            errorHandler = "consumerAwareErrorHandler")
    public void listen4(String data, Acknowledgment ack) {
        System.err.println("topic.quick.ack receive : " + data);
        ack.acknowledge();

    }


    @KafkaListener(id = "batch", clientIdPrefix = "batch",
            topicPattern = "0", topics = {"topic.quick.batch"}, containerFactory =
            "batchListenContainerFactory")
    public void listen5(List<String> data) {
        for (int i = 0; i < data.size(); i++) {
            System.err.println("topic.quick.ack receive : " + data.get(i));
        }
    }

    /**
     * 获取指定分区消息
     * @param data
     */
    @KafkaListener(id = "batchWithPartition", clientIdPrefix = "bwp",
            containerFactory = "batchListenContainerFactory",
            topicPartitions = {
                    @TopicPartition(topic = "topic.quick.batch.partition", partitions = {"1", "3"}),
                    @TopicPartition(topic = "topic.quick.batch.partition", partitions = {"0", "4"},partitionOffsets = @PartitionOffset(partition = "2", initialOffset = "100"))
            }
    )
    public void batchListenerWithPartition(List<String> data) {
        for (String s : data) {
            System.err.println("topic.quick.batch.partition  receive : " + s);
        }
    }

    /**
     * 注解获取消息头以及消息体
     * @param data
     * @param key
     * @param partition
     * @param topic
     * @param ts
     */
    @KafkaListener(id = "anno", topics = "topic.quick.anno")
    public void annoListener(@Payload String data,
                             @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) Integer key,
                             @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                             @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) {
        System.err.println("topic.quick.anno receive : \n" +
                "data : " + data + "\n" +
                "key : " + key + "\n" +
                "partitionId : " + partition + "\n" +
                "topic : " + topic + "\n" +
                "timestamp : " + ts + "\n"
        );

    }

    /**
     * @KafkaListener
     * id：消费者的id，当GroupId没有被配置的时候，默认id为GroupId
     * containerFactory：上面提到了@KafkaListener区分单数据还是多数据消费只需要配置一下注解的containerFactory属性就可以了，这里面配置的是监听容器工厂，也就是ConcurrentKafkaListenerContainerFactory，配置BeanName
     * topics：需要监听的Topic，可监听多个
     * topicPartitions：可配置更加详细的监听信息，必须监听某个Topic中的指定分区，或者从offset为200的偏移量开始监听
     * errorHandler：监听异常处理器，配置BeanName
     * groupId：消费组ID
     * idIsGroup：id是否为GroupId
     * clientIdPrefix：消费者Id前缀
     * beanRef：真实监听容器的BeanName，需要在 BeanName前加 "__"
     */
}
