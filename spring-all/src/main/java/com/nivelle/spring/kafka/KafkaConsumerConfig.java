package com.nivelle.spring.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;


/**
 * 消费者配置
 *
 * @author fuxinzhong
 * @date 2019/12/04
 */
@Configuration
@PropertySource("classpath:config/application.properties")
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.servers}")
    private String servers;
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;
    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;
    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.consumer.session.timeout}")
    private int sessionTimeOut;
    @Value("${spring.kafka.consumer.auto.commit.interval}")
    private int autoCommitInterval;
    @Value("${spring.kafka.consumer.auto.offset.reset}")
    private String autoOffSetReset;
    @Value("${spring.kafka.consumer.concurrency}")
    private int concurrency;


    public Map<String, Object> ConsumerConfigs() {
        Map<String, Object> props = new HashMap<>(12);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeOut);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffSetReset);
        //一次拉取的数量
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, concurrency);
        return props;
    }


    @Bean(value = "listenContainerFactory")
    public ConsumerFactory listenContainerFactory() {
        ConsumerFactory factory = new DefaultKafkaConsumerFactory<>(ConsumerConfigs());
        return factory;
    }

    /**
     * 8 个分区副本数为1
     *
     * @return
     */
    @Bean
    public NewTopic batchTopic() {
        return new NewTopic("topic.quick.batch", 8, (short) 1);
    }


    @Bean(value = "concurrentListenContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, String>> concurrentListenContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new
                ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(listenContainerFactory());
        //设置可以丢弃消息  配合RecordFilterStrategy使用
        //factory.setAckDiscarded(true);
        //设置并发量，小于或等于Topic的分区数
        //factory.setConcurrency(2);
        //设置为批量监听
        //factory.setBatchListener(true);
        factory.setRecordFilterStrategy(new MyKafkaRecordFilterStrategy());
        return factory;
    }


    @Bean(value = "batchListenContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, String>> batchListenContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new
                ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(listenContainerFactory());
        //设置并发量，小于或等于Topic的分区数
        factory.setConcurrency(5);
        //设置为批量监听
        factory.setBatchListener(true);
        return factory;
    }

    /**
     * 监听Topic中指定的分区
     */
    @Bean
    public NewTopic batchWithPartitionTopic() {
        return new NewTopic("topic.quick.batch.partition", 8, (short) 1);
    }

    /**
     * 手动确认
     *
     * @return
     */
    @Bean(value = "ackListenContainerFactory")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, String>> ackListenContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new
                ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(listenContainerFactory());
        //设置可以丢弃消息  配合RecordFilterStrategy使用
        factory.setAckDiscarded(true);
        //设置为批量监听
        factory.setBatchListener(true);
        factory.setRecordFilterStrategy(new MyKafkaRecordFilterStrategy());
        /**
         * RECORD  每处理一条commit一次
         * BATCH(默认) 每次poll的时候批量提交一次，频率取决于每次poll的调用频率
         * MANUAL_IMMEDIATE listner负责ack，每调用一次，就立即commit
         */
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
