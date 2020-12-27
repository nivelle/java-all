### 重要参数配置

#### Broker级别参数

- 存储信息相关： log.dirs:指定了broker 需要使用的若干个文件目录路径。必须使用者指定(例如：/home/kafka1,/home/kafka2,/home/kafka3)

- 与ZooKeeper相关： zookeeper.connect：zookeeper存储了kafka的元数据信息,让多个 Kafka 集群使用同一套 ZooKeeper 集群（zk1:2181,zk2:2181,zk3:2181/kafka1和zk1:2181,zk2:2181,zk3:2181/kafka2）

- 与Broker相关： listeners: 监听器，告诉外部连接者通过什么协议和端口开放kafka服务,构成<协议名称，主机名，端口号>，协议名称可能是标志名称，也可以是自定义名称，如果使用自定义协议名称，需要指定安全协议，listener.security.protocol.map

- Top 管理相关： auto.create.topics.enable:是否允许自动创建topic，建议设置成false

- Top 管理相关： unclean.leader.election.enable:是否允许Unclean Leader选举,默认设置成false,这个参数表示是否允许非Leader副本参与竞选，成为Leader ,这样容易丢失数据

- Top 管理相关： auto.leader.rebalance.enable:是否允许定期进行Leader选举：表示是否允许Kafka定期对一些Topic分区进行Leader重选举，建议设置成为false

- 数据存留相关： log.retention.{hours|minutes|ms}:控制一条消息数据被保存多长时间。优先级：ms>minutes>hours

- 数据存留相关：log.retention.bytes:指定Broker为消息保存的总磁盘容量大小，默认是-1，表明不做限制；多租户下设置租户磁盘容量

- 数据存留相关：message.max.bytes:控制Broker能够接收的最大消息大小，默认值1000012。

#### Top级别参数

- retention.ms:规定了该Topic消息被保存的时长。默认是7天，即该Topic只保存最近7天的消息。一旦设置了这个值，它会覆盖掉Broker端的全局参数值

- retention.bytes:规定了要为该Topic预留多大的磁盘空间。
  
 ```` 
$> export KAFKA_HEAP_OPTS=--Xms6g  --Xmx6g
$> export KAFKA_JVM_PERFORMANCE_OPTS= -server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+ExplicitGCInvokesConcurrent -Djava.awt.headless=true
$> bin/kafka-server-start.sh config/server.properties
````

#### JVM参数

- KAFKA_HEAP_OPTS：指定堆大小

- KAFKA_JVM_PERFORMANCE_OPTS:指定GC参数

### 消息设计


#### 位移主题

- broker 端位移主题数目控制参数：offsets.topic.num.partitions，默认值为50

- broker 端 副本数或者备份因子： offsets.topic.replication.factor,默认值是3

- 位移主题如果是kafka自动创建的，那么该主题的分区数就是50，副本数是3

- 位移主题在第一个consumer程序启动时，kafka自动创建位移主题


##### 位移主题提交

- 配置参数： enable.auto.commit = false

- 手动提交: consumer.commitSync

- 自动提交: 如果是自动提交，则会不停的写位移信息，此时需要利用compact策略来删除位移主题中的过期消息。专门的后台线程定期地巡检待compact的主题，看看是否存在满足条件的可删除数据。 这个后台线程叫：log cleaner.

### coordinator

- 所有broker在启动时，都会创建和开启相应的coordinator 组件，也就是说，所有的broker都有各自的coordinator组件。

#### 为 consumer group 确定coordinator所在broker的方式：

- 确定由位移主题的那个分区保存该group数据：

````
partitionId = Math.abs(groupId.hashCode()%offsetsTopicParitionCount); 
````

- 找出该分区leader副本所在的broker,该broker即为对应的coordinator



 