package com.nivelle.middleware.kafka;

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
 * @author nivelle
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

    /**
     * 1. session.time.out : coordinator 检测失败时间，设置一个较小的值让coordinator能快速检测consumer组的崩溃情况，从而更快地 rebalance,避免造成更大的消息滞后。目前默认值是10s
     *
     * 2. max.poll.interval.ms:用于设置消息处理逻辑的最大时间
     *
     * 3. auto.offset.reset: 指定了无位移信息或位移越界时的应对策略：
     *
     * （1）earliest:指定从最早的位移开始消费。这里的最早的位移不一定就是0
     *
     * （2）latest:指定从最新出位移开始消费
     *
     * （3）none:指定如果未发现位移信息或位移越界，则抛出异常。
     *
     * 4. enable.auto.commit:该参数指定consumer是否自动提交位移。 若未true则consumer在后台自动提交位移；否则，用户需要手动提交位移。对于有较强精确处理一次语义需求，最好设置未false，由用户自行处理位移提交问题。
     *
     * 5. fetch.max.bytes:指定了consumer端单次获取数据的最大字节数。
     *
     * 6. max.poll.records: 该参数控制单次poll调用返回的最大消息数目。 默认500条
     *
     * 7. heartbeat.interval.ms:心跳间隔时间，当coordinator决定开启新一轮rebalace时，它会将这个决定以REBALANCE_IN_PROGRESS异常的形式塞进 consumer心跳请求的response中，这样其他成员拿到respose后才知道它需要重新加入group. 此参数就是做这个事情的。
     *
     * 8. connections.max.idle.ms: kafka会定期关闭空闲socket导致下次consumer处理请求时需要重新创建连向broker的socket连接。当前默认值是9分钟，如果用户实际环境中不在乎这些socket资源开销，比较推荐该参数值为-1，即不要关闭这些空连接。
     *
     * 9. replica.lag.time.max.ms: 默认值10秒，副本滞后的最大时间。
     *
     * 10. min.insync.replicas:min.insync.replicas这个参数设定ISR中的最小副本数是多少，默认值为1，当且仅当request.required.acks参数设置为-1时，此参数才生效
     *
     *
     */
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
