### 消息分区

#### 结构：

- kafka 消息组织方式是三级结构： 主题-分区-消息

#### 作用：

- 提供负载能力，提高系统的伸缩性。

- 不同的分区放置在不同节点的机器上，而数据读操作也都是针对分区这个粒度进行的，这样每个节点的机器都能独立执各自分区的读写请求处理

- 可以通过增加新的节点来增加整个系统的吞吐量

#### 分区策略

- 自定义分区策略：partitioner.calss = full Qualified Name

- 轮询策略（round-robin）: kafka 默认提供的分区策略，能保证消息最大限度第被平均分配到所有分区上，默认情况的最合理

- 随机策略（randomness）：随机策略是老版本生产者使用的分区策略，在新版本中已经改为轮询

##### kafka默认分区策略：
- 如果指定了partition就直接发送到该分区；
  
- 如果没有指定分区但是指定了key，就按照key的hash值选择分区；
  
- 如果partition和key都没有指定就使用轮询策略：
  - 而且如果key不为null，那么计算得到的分区号会是所有分区中的任意一个；
  - 如果key为null并且有可用分区时，那么计算得到的分区号仅为可用分区中的任意一个

````

List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
return ThreadLocalRandom.current().nextInt(partitions.size());
````

- 按消息键保序策略：kafka允许为每条消息定义消息键，简称为key. kafka同一个key的所有消息都进入到相同的分区里面，由于每个分区下的消息处理都是有顺序的，故这个策略被成为按消息键保序策略

### java生产者选择TCP连接

- 开发客户端时，人们能利用TCP本身提供的一些高级功能，比如多路复用请求以及同时轮询多个连接的能力；多路复用的前提是上层的应用协议允许发送多条消息。

- 目前已知的http库存在很多编程语言中不够丰富

#### 创建TCP连接的时机

- 创建KafkaProducer 实例时，生产者应用会在后台创建并启动一个名为Sender 线程，该Sender线程开始运行时首先会创建与Broker的连接；
  bootstrap.servers 仅需配置部分broker机器即刻，producer 会自动向某一台Broker发送METADATA 请求，获取集群所有信息

- 更新元数据时

````
1. 当producer 尝试给一个不存在的主题送消息时，Broker会告诉Producer说这个主题不存在。此时Producer会发送METADATA请求给kafka集群，去尝试获取最新的元数据信息

2. producer 通过 metadata.max.age.ms 参数定期地去更新元数据信息。该参数默认值是5分钟，也就是不管集群是否有变化，producer每5分钟都会轻质刷新一次元数据以保证它是最及时的数据

````

- 发送消息时

#### 断开TCP连接的时机

- 主动关闭：

````
1. kill -9 

2. producer.clouse()

````
- 自动关闭

````
1. connections.max.idle.ms :默认值是9分钟，如果在9分钟内没有任何请求流过某个TCP 连接，那么kafka会主动关闭TCP; 如果connections.man.idle.ms =-1 则该TCP成为永久连接，但还是会遵守keepalive探活机制；被动关闭会产生大量的CLOSE_WAIT 连接

````

### 幂等性producer(0.11.0.0版本引入)

#### producer 使用：

````
props.put("enable.idempotence",true)

或者 props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,true)

````

#### 幂等性特征：

1. 以空间换时间，在broker 多保存一些字段，当Producer发送了相同字段值的消息后，broker 能够自动知晓这些消息已经重复了， 于是自动 "丢弃”；

2. 仅仅能保证某个主题的一个分区上不出现重复性消息，无法实现多个分区的幂等性

3. 只能实现单会话上的幂等性，不能实现跨会话的幂等性，当重启了producer 的进程之后，这种幂等性就丧失了

#### 实现逻辑

- 区分produce 会话: produce 每次启动后，首先向broker申请一个全局唯一的 PID ,用来标识本次会话。

- 消息检测：message v2 版本增加了 sequence number 字段，producer 每发一批消息，seq 就加1.
  broker 在内存维护(pid,seq)映射，收到消息后检测seq.如果：

````
new_seq = old_seq + 1 ;正常消息
new_seq <= old_seq;//消息重复
new_seq > old_seq + 1 ;//消息丢失

````

- producer 重试： producer 在收到明确的消息的丢失或者ack,或者超时后未收到ack,要进行重试


### 事务(0.11版本引入)

#### producer事务使用：

````
props.put("enable.idempotence",true)

设置producer端参数 transactional.id 最好为其设置一个有意义的名字

````

#### producer事务特征：

1. 主要是在 read committed 隔离级别上做事情

2. 保证多条消息原子性的写入到目标分区，同时也能保证Consumer 只能看到事务成功提交的消息

3. 事务型producer 的显著特征是调用了一些事务型API ,如 initTransaction 、beginTransaction、committedTransaction和 abortTransaction 分别对应事务初始化、事务开始、事务提交、事务终止

4. producer 事务即使写入失败，kafka 也会把它们写入到底层日志中，consumer还是会看到这些消息，因此consumer端，读取事务型producer发送的消息需要设置 isolation.level 参数即可：

````
1. read_uncommitted:默认值,表明consumer能够读取到kafka写入的任何消息，不论事务型producer 提交事务还是终止事务，其写入的消息都可以读取。

2. read_committed:表明Consumer 只会读取事务型producer 成功提交事务写入的消息。也能看到非事务型producer写入的所有消息

````

#### 实现原理 [简书：架构禅话](https://www.jianshu.com/p/f77ade3f41fd)

##### producer

1. 为producer指定固定的 transactionalId，可以穿越producer的多次会话(producer 重启/断线重连)，持续标识 producer的身份；
   
2. 使用epoch标识producer的每一次"重生”，防止同一producer存在多个；
   
3. producer遵从幂等消息的行为，并在发送的recordBatch中增加事务 transactionalId 和epoch

##### 事务协调器(transaction coordinator)

1. 引入事务协调器，以两阶段提交的方式，实现消息的事务提交

2. 事务协调器使用一个特殊的topic: transaction,来做事务提交日志

3. 事务控制通过RPC调用，协调 broker 和 consumer coordinator 实现事务的两阶段提交

4. 每个broker 都会启动一个事务协调器，使用hash(transactionalId)确定producer对应的事务协调器，使得整个集群的负载均衡

##### broker

1. broker处理在事务协调器的commit/abort控制消息。把控制消息像正常消息一样写入topic(和正常消息交织在一起，用来确认事务提交的日志偏移)，并向前推进消息提交偏移(hw)

##### 组协调器

1. 如果在事务过程中，提交了消费偏移，组协调器在offset log 中写入事务消费偏移

2. 当事务提交时，在off set 中写入事务offset确认消息

##### consumer

consumer过滤未提交消息和事务控制消息，使这些消息对用户不可见

- consumer 缓存方式：

1. 设置 isolation.level = read_uncommitted ,此时topic的所有消息对consumer都可见

2. consumer 缓存这些消息，直到收到事务控制消息。若事务commit ,则对外发布这些消息，若事务abort,则丢弃这些消息。

- broker 过滤方式

1. 设置isolation.level = read_committed ,此时topic 中未提交的消息对consumer不可见，只有在事务结束后，消息才对consumer可见

2. broker 给consumer的BatchRecord消息中，会包含以列表，指明那些是“abort”事务consumer丢弃abort事务的消息即可

#### 事务消息处理流程

[![r4H2xf.png](https://s3.ax1x.com/2020/12/27/r4H2xf.png)](https://imgchr.com/i/r4H2xf)

1. 查找事务协调器 ：findCoordinatorRequest

事务协调器是分配pid和管理事务的核心，producer首先对任何一个broker发送FindCoordinatorRequest,发现自己的事务协调器。

2. 申请pid:initPidRequest

producer向事务协调器发送initPidRequest,申请pid;

- 2.1 当指定了transactional.id时，事务协调器为producer分区pid，并更新epoch，把(tid,pid)的映射关系写入事务日志。 同时清理tid任何未完成的事务，丢弃未提交的消息

3. 启动事务

启动事务是producer的本地操作，促使producer更新内部状态，不会和事务协调器发生关系。事务协调器自动启动事务，始终处在一个接一个的事务处理状态机中。

4. consumer-transform-produce 事务循环

- 4.1 注册partition: addPartitionsToTxnRequest
  
    对于每一个要在事务中写消息的topic分区，produce 应当在第一次发消息前，向事务处理器注册分区

  - 4.1.1 事务处理器把事务关联的分区写入事务日志：在提交或者终止事务时，事务协调器需要这些信息，控制事务涉及的所有分区leader完成事务提交或者终止

- 4.2 写消息 produceRequest

  - 4.2.1 producer 向分区leader写消息，消息中包含tid,pid,epoch,seq

- 4.3.1 提交消息偏移： addOffsetCommitsToTxnRequest

- 4.3.2 produce 向事务协调器发送消费偏移，事务协调器在事务日志中记录偏移信息，并把组协调器返回给producer

- 4.4 提交消费偏移： txnOffsetCommitRequest
  
  - 4.4.1 producer 向组协调器发送TxnOffsetCommitRequest,组协调器把偏移信息写入偏移日志。但是，要一直等到事务提交后，这个偏移才生效，对外部可见。

5. 提交或者终止事务

- 5.1 endTxnRequest

收到提交或者终止事务请求时，事务处理器将执行下面操作

(1). 在事务日志中写入PREPARE_COMMIT 或者 PREPARE_ABORT消息(5.1a)

(2). 通过WriteTxnMarkerRequest 向事务中的所有broker发事务控制消息(5.2)

(3). 在事务之日志中写入COMMITTED 或者 ABORTED消息(5.3)

- 5.2 writeTxnMarkerRequest

这个消息由事务处理器发给事务中所涉及分区的leader

当收到这个消息后，broker会在分区log中写入一个COMMIT 或者 ABORT控制消息。同时，也会更新该分区的事务提交偏移hw

如果事务中有未提交消息偏移，broker也会把控制消息写入 __consumer-offsets log ,并通知组协调器使事务中提交的消费偏移生效

- 5.3 写最终的commit或abort消息

当所有的commit或abort消息写入数据日志，事务协调器在事务日志中写入事务日志，标志这事务结束。至此，本事务的所有状态信息都可以被删除，可以开始一个新的事务。


#### 我们要认识到，虽然kafka事务消息提供了多个消息原子写的保证，但它不保证原子读。

````
1）事务向topic_a和topic_b两个分区写入消息，在事务提交后的某个时刻，topic_a的全部副本失效。这时topic_b中的消息可以正常消费，但topic_a中的消息就丢失了。

2）假如consumer只消费了topic_a，没有消费topic_b，这样也不能读到完整的事务消息。

3）典型的kafka stream应用从多个topic消费，然后向一个或多个topic写。在一次故障后，kafka stream应用重新开始处理流数据，由于从多个topic读到的数据之间不存在稳定的顺序(即便只有一个topic，从多个分区读到的数据之间也没有稳定的顺序)，那么两次处理输出的结果就可能会不一样。
````
