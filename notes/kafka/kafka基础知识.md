### kafka

- 消息引擎系统

- 分布式流处理平台

### 版本演进

- 0.7 版本：只有基础消息队列功能，无副本；不能线上使用

- 0.8 版本：增加了副本机制，新的producer API；建议使用0.8.2.2 版本；不建议使用0.8.2.0之后的producer API,bug多

- 0.9 版本：增加权限认证，新的consumer API ,Kafka Connect 功能；不建议使用Consumer API,bug 多

- 0.10 版本：引入kafka Streams 功能，bug修复；建议版本0.10.2.2 ；建议使用新版本consumer API

- 0.11 版本：producer API 幂等，事物API ，消息格式重构；建议版本 0.11.0.3 ;谨慎对待消息格式变化

- 1.0 和 2.0：kafka Streams 改进；建议版本2.0

### 消息格式

- kafka有两个消息格式，v1 和 v2(0.11.0.0版本引入)

#### 消息集合（message set）

一个消息集合包含若干条日志项（record item）,日志项是封装消息的地方。kafka底层消息日志由一系列消息集合日志项组成。

#### 消息


### 消息压缩

**消息压缩可能发生的地方：生产者和Broker端**

#### producer 开启消息压缩

````
Properties props = new Properties();
props.put("compression.type","gzip");

````

#### broker 开启压缩/解压

````
compression.type
````

- broker 端和producer 端指定了不一样的压缩算法，会导致CPU飙升

- broker 端发生消息转换，为了兼容旧版本消息。会丧失 ZERO_COPY 能力

- broker 端为了对消息进行校验，每个压缩过的消息集合在broker端写入的时都要发生解压缩操作 

#### consumer 开启解压

1. 消息集合标注了消息采用何种压缩算法，按照指定的算法解压。
2. kafka 2.1.0 之前，支持3种算法 GZIP、Snappy和LZ4 ; 从 2.1.0 开始，开始支持 Zstandard算法
3. 吞吐量： LZ4> Snappy>zstd和GZIP;压缩比：zstd>LZ4>GZIP>Snappy ;带宽：zstd最少，Snappy最多

### 无消息丢失配置

#### kafka只对"已提交"的消息做有限度的持久化保证。

- 已提交的消息： 当Kafka的若干个Broker成功地接收到一条消息并写入到日志文件后，broker 才会认为生产者这条消息已成功提交

- 有限度的持久化保证：Kafka不丢消息有一定的前提条件，若N个broker 则消息至少保存在一个broker上。

#### 生产者丢失数据：
- Producer 永远要使用带有回调通知的发送API,也就是说不要使用producer.send(msg),而使用producer.send(msg,callback). 从callback(回调)里获取消息提交结果，或者失败原因

#### 消费者丢失数据：

- 维持先消费消息，再更新位移的顺序。

- 如果是多线程异步处理消费消息，Consumer 程序不要开启自动提交位移，而是要应用程序手动提交位移。

### 无消息丢失配置最佳实践

1. 不要使用 producer.send(msg),而要使用 producer.send(msg,callback). 一定要使用带有回调通知的send方法

2. 设置 acks = all : acks 是Producer 的一个参数，代表了你对"已提交"消息的定义。如果设置成 all,则表明所有副本broker 都要接收到消息，该消息才算是"已提交" 。这是最高等级的已提交

3. 设置 reties 为一个较大的只:这里的reties同样是producer的参数，对应前面提到的producr自动重试。当出现网络的瞬时抖动时，消息发送可能会失败，此时配置了retiescl>0 的producer能够自动重试发送，避免消息丢失。

4. 设置 unclean.leader.election.enable=false: 这是broker端的参数，它控制的是哪些Broker有资格竞选分区的Leader。 如果一个broker落后原先的Leader太多，那么一旦成为新的leader，必然会造成消息的丢失。故一般都将该参数设置成false,即不允许这种情况发送。

5. 设置 replication.factor>=3 :broker 端的参数，这里的意思是将消息多保存几份，毕竟目前防止消息丢失的主要机制就是冗余。

6. 设置 min.insync.replicas>1 : 依然是broker端参数，控制的是消息至少要被写入到多少个副本才算是"已提交"。 设置成大于1可以提升消息持久性。在实际环境不要使用默认值1

7. 确保 replication.factor > min.insync.replication 如果两者相等，那么只要有一个副本挂机，整个分区就无法工作了，我们不仅要改善消息的持久性，防止消息丢失，还要在不降低可用性的基础上完成。推荐设置replication.factor=min.sync.replicas+1.

8. 确保消息消费完成再提交. consumer 端 有个参数 enable.auto.commit ,最好把它设置成false,并采用手动提交位移的方式。这对于单Consumer 多线程处理的场景而言是至关重要的。

### 拦截器

拦截器可以用于包括客户端监控、端到端系统性能检测、消息审计等多种功能在内的场景。


#### kafka 生产者拦截器

生产者拦截器允许你在发送消息前以及消息提交成功后植入你的拦截器逻辑

#### 拦截器配置

- 拦截器接口: org.apache.kafka.clients.producer.ProducerInterceptor

````

Properties props = new Properties();
List<String> interceptors = new ArrayList<>();
interceptors.add("com.yourcompany.kafkaproject.interceptors.AddTimestampInterceptor"); // 拦截器1
interceptors.add("com.yourcompany.kafkaproject.interceptors.UpdateCounterInterceptor"); // 拦截器2
props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, interceptors);

````

- onSend() : 该方法会在消息发送之前被调用，如果你想在发送之前对消息做出修改，这个方法是唯一机会

- onAcknowledgement():该方法会在消息成功提交或者发送失败后被调用。onAcknowledgement 的调用要早于 callback 的调用。 这个方法和onSend 不是在同一个线程中被调用的，因此如果你在这两个方法中调用了某个共享可变对象，一定要保证线程安全。这个方法处在Producer 发送的
  主路径中，所以最好不要放一些太重的逻辑，否则会造成Producer TPS直线下降。

#### kafka 消费者拦截器

消费者拦截器支持在消息消费前以及在提交位移后编写特定逻辑

- 拦截器接口: org.apache.kafka.clients.consumer.consumerInterceptor

- onConsumer: 该方法在消息返回给Consumer程序之前调用。也就是说在正式开始处理消息之前，拦截器会先拦截一道，之后再返回。

- onCommit: Consumer 在提交位移之后调用该方法。通常你可以在该方法中做一些记账类的动作，比如日志
