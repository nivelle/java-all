package com.nivelle.middleware.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Produce 生产者配置
 *
 * @author nivell
 * @date 2019/12/02
 */
@Configuration
@PropertySource("classpath:config/application.properties")
public class KafkaProducerConfig {
    @Value("${spring.kafka.producer.servers}")
    private String servers;
    @Value("${spring.kafka.producer.retries}")
    private int retries;
    @Value("${spring.kafka.producer.batch.size}")
    private int batchSize;
    @Value("${spring.kafka.producer.linger}")
    private int linger;
    @Value("${spring.kafka.producer.buffer-memory}")
    private int bufferMemory;

    /**
     *
     * 1. acks: 该参数用于控制produce生产消息的持久性（durability）
     *
     *   (1). acks=0:设置成0表示produce完全不理睬leader broker端的处理结果。此时，produce发送消息后立即开启下一条消息的发送，根本不等待leader broker端返回结果。
     *
     *   (2). acks=all或者-1:表示当发送消息时，leader broker不仅会将消息写入本地日志，同时还等ISR中所有其他副本都成功写入它们格子的本地日志后，才发送响应结果给produce.
     *
     *   (3). acks=1:是0和all折中方法，也是默认的参数值。produce发送消息后leader broker仅将该消息写入本地日志，然后便发送响应结果给producer，而无须等待ISR中其他副本写入该消息。
     *
     * 2. buffer.memory: 该参数指定了produce端用于缓存消息的缓冲区大小，单位是字节，默认值是 33554432，即32MB。produce启动时首先会创建一块内存缓冲去用于保存待发送的消息，然后由另一个专属线程从缓冲区读取消息执行真正的发送。
     *
     * 3. retries:该参数表示进行重试的次数，默认值是0，表示不进行重试。
     *
     * 4. batch.size: produce会发送batch中所有消息，不过，produce并不总是等待batch满了才发送消息，很有可能当batch还有很多空闲空间时produce就发送该batch
     *
     * 5. linger.ms: 吞吐量与时延之间的权衡。该参数就是控制消息发送延时行为的。该参数默认是0，表示消息需要被立即发送，无须关心batch是否已经被填满，大多数情况这是合理，毕竟我们总是希望消息被尽快可能地发送。
     *
     * 6. request.timeout.ms:当produce发送请求给broker后，broker需要在规定的时间范围内将处理结果返还给produce。默认30s,如果broker在30秒内都没有给produce发送响应，那么producer就会认为该请求超时了，并在回调函数中显示地抛出TimeoutException异常交由用户处理。
     *
     * 7. log.index.interval.bytes:默认值4kb，kafka分区至少写入4KB 日志才添加一个索引项。
     */
    public Map<String, Object> producerConfigs() {

        Map<String, Object> props = new HashMap<>(12);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        //设置重试次数
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        //达到batchSize大小的时候会发送消息
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        //延时时间，延时时间到达之后计算批量发送的大小没达到也发送消息
        //props.put(ProducerConfig.LINGER_MS_CONFIG, linger);
        //缓冲区的值
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        //序列化手段
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //producer端的消息确认机制,-1和all都表示消息不仅要写入本地的leader中还要写入对应的副本中
        props.put(ProducerConfig.ACKS_CONFIG, "-1");
        /**
         * 单条消息的最大值以字节为单位, 默认值为1048576
         * props.put(ProducerConfig.LINGER_MS_CONFIG, 10485760);
         */
        /**
         * 设置broker响应时间，如果broker在60秒之内还是没有返回给producer确认消息，则认为发送失败
         * props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
         */
        /**
         * 指定拦截器(value为对应的class全限定名)
         * props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, "");
         */
        /**
         * 设置压缩算法
         * props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "LZ4");
         */
        /**
         * 自定义分区器
         */
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, MyPartitioner.class);

        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate(producerFactory());
    }

}
