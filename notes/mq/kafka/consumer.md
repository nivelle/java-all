### 消费者组

consumer group 是Kafka 提供的可扩展且具有容错性的消费者机制。

#### 消费者组特性

- consumer group 下可以有一个或者多个Consumer实例。 这里的实例可以是一个单独的进程，也可以是同一进程下的线程。

- group id 是一个字符串，在一个kafka集群中，它标识唯一的一个Consumer group

- consumer group 下所有实例订阅的主题的单个分区，只能分配给组内的某个consumer实例消费。这个分区也可以被其他的group消费

- 当consumer group 订阅了多个主题后，组内的每个实例不要求一定要订阅主题的所有分区，它只会消费部分分区中的消息

- consumer group 之间彼此独立，互不影响，它们能够订阅相同的一组主题而互不干涉

- 如果所有实例都属于同一个group，那么它实现的就是消息队列模型；如果所有实例分别属于不同的group,那么它实现的就是发布/订阅模型

- 在Rebalance过程中，所有consumer实例都会停止消费，等待Rebalacne完成

### 位移主题

- 新版本 consumer group 奖位移保存在broker端的内部主题中，内部主题__consumer_offsets 

#### 消费者组的 rebalance 的触发条件有3个

- 组成员数发生变化。 新的consumer实例加入组或者离开组，或者有consumer实例崩溃奔溃被踢出组

- 订阅主题数发生变化。

- 订阅主题的分区数发生变更。kafka当前只能允许增加一个主题的分区数，当分区数增加时，就会触发该订阅主题的所有group 开启 rebalance

##### rebalance 的弊端

-  rebalance影响consumer 端的tps。

-  rebalance 很慢

-  rebalance 效率不高

#### rebalance 的避免

- session.timeout.ms : 默认值是10秒，如果coordinator在10秒内没有收到group 下某个consumer实例的心跳，它就会认为这个实例已经挂掉，需要rebalance

- heartbeat.interval.ms:consumer实例控制发送心跳请求的频率，值越小，频率越快。 coordinator 通知各个consumer开启rebalance的方法就是将REBALANCE_NEEDED 标志封装进心跳请求的响应体中

- max.poll.interval.ms: 限定了consumer端两次调用poll方法的最大时间间隔。默认值：5分钟 表示你的consumer程序如果在5分钟内无法消费完poll方法返回的消息。那么consumer会主动发起"离开组”的请求，coordinator也会开启新的一轮rebalance

### 位移提交

- consumer端应用程序在提交位移时，其实是向coordinator所在的broker提交位移。

- consumer应用启动时，也是向coordinator所在的broker发生各种请求，然后由coordinator负责执行消费者组的注册、成员管理记录等元数据管理操作

- consumer消费位移，它记录了consumer要消费的下一个消息的位移

### 消费位移

- consumer需要向kafka汇报自己的位移数据，这个汇报过程被称为提交位移(committing offsets).
  
- 因为consumer能够同时消费多个分区的数据，所以位移的提交实际上是在分区粒度上进行的，即consumer需要为分配给它的每个分区提交各自的位移数据。

#### 提交方式

##### 自动提交

- kafka consumer 在后台自动为你提交位移,开启自动提交位移 enable.auto.commit = true;

- auto.commit.interval.ms 默认是5，表明kafka每5秒就会自动提交一次位移,至少5秒可能多余5秒

- 可能会出现重复消费，提交后积累一定消息后发生rebalance,之后从上次提交的位移处开始消费，导致这段时间积累的数据重新提交。可通过auto.commit.interval.ms 来提高提交频率，单仅仅缩小了重复消费的事件窗口，不可能完全消除。

- consumer poll是从commit的位置开始的，但是持续消费的时候，就会利用consumer内部指针探测到下一次poll的位置，可能这个位置还没有commit 


##### 手动提交

- enable.auto.commit =false

- kafkaConsumer.commitSync() 该方法会提交 KafkaConsumer#poll()返回的最新位移。

- commitSync()提交时，consumer程序处于阻塞状态，直到远端的broker返回提交结果，这个状态才会结束。

- commitAsync()提交时，会立即返回，不会阻塞，因此不影响consumer的TPS

### commitFailedException

#### 原因

1. 消费者连续两次调用poll 方法的时间间隔超过了期望的max.poll.interval.ms参数，表示处理消费逻辑花费了太多时间

#### 解决方案

1. 增加期望的事件间隔： max.poll.interval.ms 参数值

2. 减少poll方法一次性返回的消息数量，即减少max.poll.records参数值

3. 缩短单挑消息处理的时间

4. 下游系统使用多线程来加速消费


### kafka java consumer 设计原理

v0.10.1.0版本开始，kafkaConsumer就变成了双线程设计：**用户主线程和心跳线程**

#### 用户主线程

1. 启动Consumer应用程序main方法的那个线程，而新引入的心跳线程(heartbeat thread)支负责定期给对应的broker机器发送心跳请求，以标识消费者应用的存活性(liveness)

2.单线程+轮询机制

3. 消费者程序启动多个线程，每个线程维护专属的KafkaConsumer实例，负责完整的消息获取、消息处理流程

4. 消费者程序使用单或者多线程获取消息，同时创建多个消费线程执行消息处理逻辑。获取消息的线程可以是一个，也可以是多个，每个线程维护专属的kafkaConsumer实例，处理消息则交由特定的线程池来做，从而实现消息
   获取与消息处理的真正解耦
   
````
1. 消息放在数据库或者redis这种不会丢失的队列中，多线程消费队列

2. 使用闭锁工具，多个线程处理完毕之后再提交位移

````

[![rIu3Mq.jpg](https://s3.ax1x.com/2020/12/27/rIu3Mq.jpg)](https://imgchr.com/i/rIu3Mq)


### 消费者创建TCP连接

#### tcp连接是在调用kafkaConsumer.poll方法的时候被创建的

- 发起findCoordinator请求时

当消费者程序首次启动调用poll方法时,它需要向kafka集群负载最小的那台broker发送发送一个名为FindCoordinator的请求,kafka集群返回是协调者的broker

- 连接协调者时

broker处理完上一步发送的FindCoordinator请求之后，会返还对应的响应结果(response),显示地告诉消费者那个broker是真正的协调者，
之后会创建连向该broker的socket连接。只有成功连入协调者，协调者才能开启正常的组协调操作，比如加入组,等待组分配方案,心跳请求处理,位移获取,位移提交。

- 消费数据时

消费者会为每个要消费的分区创建与该分区领导者副本所在Broker连接的TCP.假设消费者要消费 5 个分区的数据，这 5 个分区各自的领导者副本分布在 4 台 Broker 上，那么该消费者在消费时会创建与这 4 台 Broker 的 Socket 连接

#### 三类TCP连接

- 确定协调者和获取集群元数据

- 连接协调者，令其执行组成员管理操作

- 执行实际的消息获取，当该类连接创建成功后，消费者程序就会废弃第一类TCP连接，之后在定期请求元数据时，它会改为使用该类TCP连接；最终第一类连接会在后台逐渐关闭，后面只会有后面两类TCP连接存在



### 消费者关闭TCP连接

- 主动关闭:KafkaConsumer.close()方法，或者执行 kill 命令

- 自动关闭：connection.max.idle.ms  默认9分钟，如果某个连接上连续9分钟没有任何请求，那么消费者强行"杀掉”这个socket连接


### 消费者组消费进度监控方法

1. 使用kafka自带的命令行工具 kafka-consumer-groups

````
$ bin/kafka-consumer-groups.sh --bootstrap-server <Kafka broker连接信息> --describe --group <group名称>
//Kafka 连接信息就是 < 主机名：端口 > 对，而 group 名称就是你的消费者程序中设置的 group.id 值
````

2. 使用 Kafka java consumer api 编程

3. 使用 Kafka 自带的JMX监控指标