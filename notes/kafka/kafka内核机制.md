
### Kafka副本机制详解

副本机制(replication),备份机制，指的是分布式系统在多台网络互联的机器上保存有相同的数据拷贝。

- 提供数据冗余（kafka实现）

- 提供搞伸缩性： 横向扩展，能够通过增加机器的方式来提升读性能，进而提高读操作的吞吐量

- 改善数据局部性： 允许将数据放入与用户地理位置相近的地方，从而降低系统延时

#### 副本定义

本质是一个只能追加写消息的提交日志。同一个分区下的所有副本保存相同的消息序列，这些副本分散保存在不同的broker上，从而能对抗部分broker宕机带来的数据不可用。


#### 副本角色

- 副本分成两类： 领导者副本（leader replica）和 追随者副本（follower replica）. 每个分区在创建时都要选举一个副本，成为领导者副本，其余的副本成为追随者副本

- kafka 副本对外不提供服务。 任何一个追随者副本都不能相应消费者和生产者的读写请求。所有的读写请求都必须发往领导者副本所在broker，由该broker负责处理。追随者不处理客户端请求，它唯一的任务就是从领导者副本异步拉取消息，并写入到自己的提交日志中，从而实现与领导者的同步。

- 领导者副本挂掉或者领导者副本所在broker宕机时，kafka依托于zk提供的监控功能实时感知到，并立即开始新的一轮领导者选举，从追随者中选一个作为新的领导者。老leader副本重启后，只能作为追随者副本假如到集群中。


#### 副本机制的优点

- 方便实现"read-your-writes": 使用生产者API 写入成功一条消息后，马上使用消费者API去读取刚才产生的消息

- 方便实现单调读（monotonic reads）:

  对一个消费者用户而言，在多次消费消息时，不会看到某条消息一会儿存在一会儿不存在。

- In-sync Replicas(ISR)

    1. ISR副本集合，ISR中的副本都是与Leader同步的副本，相反不在ISR中的追随者就被认为是与Leader不同步的。Leader副本天然在ISR,ISR不只是追随者副本集合，它必然包括Leader副本。甚至某些情况下，ISR只有Leader一个副本。

    2. broker参数->同步的标准是：**replica.lag.time.max.ms** 参数。 参数含义是Follower副本能够落后Leader副本的最长事件间隔，默认是10秒。

    3. 如果ISR在规定时间内追不上Leader 则踢出ISR集合，若追上了则重新加回去
    
    4. 0.9x版本之前，kafka由另外一个参数 replica.lag.max.messages,通过消息数来判定是否失效

#### Unclean 领导者选举（unclean Leader Election）

- kafka把所有不在ISR中的存放副本都称为非同步副本。通常非同步副本落后leader太多，因此，如果选择这些副本作为新的leader ,就肯能出现数据丢失。

- broker参数-> unclean.leader.election.enable 控制是否允许unclean领导者选举

#### 内部实现

1. kafka在启动的时候会开启两个任务，一个任务用来定期检测是否需要缩减或者扩大ISR集合，这个周期是replica.lag.time.max.ms的一半，默认是5000ms.当检测到ISR集合中有失效副本时，就会收缩ISR集合，当检测到有Follower的HighWatermark 追赶上leader 时，就会扩充ISR

2. 当ISR集合发生变更的时候会将变更后的记录缓存isrChangeSet中，另外一个任务会周期性地检查这个set,如果发现这个Set中有ISR集合的变更记录，那么它会在zk中持久化一个节点。然后因为 controller 在这个节点的路径上注册了一个Watcher,所以能够感知到ISR的变化
   并向它所管理的broker 发送更新元数据的请求。最后删除该路径下已经处理过的节点。
   

### kafka请求处理过程

kafka定义了一组请求协议，用于实现各种交互操作;所有的请求都是通过TCP网络以Socket的方式进行通讯的。

- PRODUCE ：请求是用于生产消息的

- FETCH：请求是用于消费消息的

- METADATA：是用于请求Kafka集群元数据信息的。

#### 线程模型

![kafka网络模型.jpg](https://i.loli.net/2020/12/30/Ti4waZFAUGVOWDo.jpg)

1. kafka的broker端有个SocketServer组件，类似于 reactor模式中的Dispatcher,它也有对应的Acceptor线程和一个工作线程池（网络线程池）

2. broker参数：**num.network.threads**,用于调整该网络线程池的线程数，默认值是3，表示每台Broker启动时会创建3个网络线程，专门处理客户端发送的请求

![kafka网络线程池模型.jpg](https://i.loli.net/2020/12/30/NWHsQKM8iUmDw16.jpg)

3. 当网络线程池拿到请求后，将请求放入到一个共享请求队列中。 broker端的IO线程池，负责从该队列中取出请求，执行真正的处理。如果是PRODUCE生产请求，则将消息写入到
底层的磁盘日志中；如果是FETCH请求，则从磁盘或者缓存页中读取消息
   
4. io线程池中的线程才是执行请求逻辑的线程。broker端参数**num.io.threads**控制了这个线程池的线程数。目前该参数默认值是8，表示每台broker启动后自动创建8个IO线程处理请求。

5. 请求队列是所有网络线程共享的，而相应队列则是每个线程网络专属的。因为Dispatcher只是用于请求分发而不负责相应回传，因此只能让每个网络线程发送Response给客户端，所以
Response也就没有放在一个公共地方
   
6. purgatory[炼狱]组件：用来缓存延时请求（delayed request）,所谓延时请求，就是哪些一时未满足条件不能立刻处理的请求。

````
比如设置了 acks=all 的 PRODUCE 请求，一旦设置了 acks=all，那么该请求就必须等待 ISR 中所有副本都接收了消息后才能返回，此时处理该请求的 IO 线程就必须等待其他 Broker 的写入结果。
当请求不能立刻处理时，它就会暂存在 Purgatory 中。稍后一旦满足了完成条件，IO 线程会继续处理该请求，并将 Response 放入对应网络线程的响应队列中。

````

#### 请求类型

##### 数据请求

- produce

- fetch

##### 控制请求

- LeaderAndIsr：更新leader副本、follower副本、ISR 集合

- StopReplica:副本下线

### 消费者组重平衡

#### 通知机制

- 重平衡机制通过心跳线程来完成。当协调者决定开启新一轮平衡后，会将"REBALANCE_IN_PROGRESS"封装进心跳请求的响应中，发还给消费者实例。

- 当消费者实例发现心跳响应中包含了"REBALANCE_IN_PROGRESS",就能立马直到重平衡开始

- **heartbeat.interval.ms** : 控制重平衡通知的频率

#### 消费者组状态机


状态 | 含义
---|---
Empty | 组内没有任何成员，但消费者组可能存在已提交的位移数据，而且这些位移尚未过期
Eead | 同样是组内没有任何成员，但组的元数据信息已经再协调者端被移除。协调组组件保存着当前向它注册过的所有组信息，所谓的元数据信息就类似于这个注册信息。
PreparingRebalance | 消费者组准备开启重平衡，此时所有的成员都要重新加入消费者组
CompletingReabalance| 消费者组下所有成员已经加入，各个成员正再等待分配方案。该状态再老一点的版本中被称为AwatitingSync,它和CompletingReblance是等价的
Stable| 消费者组的稳定状态。该状态表明重平衡已经完成，组内成员能够正常消费数据


[![s9Yi5R.jpg](https://s3.ax1x.com/2021/01/03/s9Yi5R.jpg)](https://imgchr.com/i/s9Yi5R)


#### 重平衡过程

- joinGroup
  
1. 当组内成员加入时，会向协调者发送JoinGroup请求。在该请求中，每个成员都要将自己订阅的主题上报，这样的协调者就能收集到所有成员的订阅信息。一旦收集了全部成员的joinGroup请求后，协调者会从这些成员中选择一个担任这个消费者组的领导者
  
2. 第一个发送JoinGroup请求的成员自动成为领导者

3.领导者消费者的任务是收集所有成员的订阅信息，然后根据这些信息，制定具体的分区消费分配方案

4.选出领导者之后，协调者会把消费者组订阅信息封装进joinGroup请求的响应中，然后发送给领导者，由领导者统一做出分配方案，进入到下一步:发送SyncGroup请求


- SyncGroup

1. 领导者向协调者发送SyncGroup请求，将刚刚做出的分配方案发给协调者。

2. 其他成员也会向协调者发送SyncGroup请求，只不过请求体中并没有实际的内容。

3. 分配方案，统一以SyncGroup的响应方式发给所有成员，这样组内成员就知道自己该消费哪些分区了。

### 控制器

#### 概念

在zk协助下管理和协调整个Kafka集群。集群中任意一台broker都能充当控制器角色，但是运行中只能有一个broker成为控制器，行驶其管理和协调的职责。

#### 控制器选举

broker在启动时，会尝试zookeeper中创建/controller节点。 kafka当前选举控制器的规则：第一个成功创建/controller节点的broker会被制定为控制器

#### 控制器作用

1. 主题管理

完成对kafka主题的创建、删除以及分区增加的操作。对kafka-topics脚本的支持

2. 分区重分配

kafka-reassign-partitions 脚本提供的对已有主题分区进行细粒度的分配功能。

3. preferred 领导者选举

避免部分broker负载过重而提供的一种换leader的方案。

4. 集群成员管理（新增broker、broker主动关闭、broker宕机）

依赖于zk的watch功能和zk临时节点组合实现。

5. 数据服务

向其他broker提供数据服务。控制器保存了最全的集群元数据信息，其他所有broker会定期接收控制器发来的元数据更新请求，从而更新其内存中的缓存数据。

#### 控制器保存的数据

- 所有主题信息：包括具体的分区信息，领导者副本是谁，ISR集合中有哪些副本

- 所有broker信息：包括当前都有哪些运行中的broker，哪些正在关闭中的broker等

- 所有涉及运维任务的分区：包括当前正在进行preferred领导者选举以及分区重分配的分区列表

[![s92xb9.jpg](https://s3.ax1x.com/2021/01/03/s92xb9.jpg)](https://imgchr.com/i/s92xb9)

#### 控制器故障转移

当broker0 宕机后，zk通过watch机制感知到并删除了/controller临时节点。之后所存活的broker开始竞选新的控制器身份。broker3赢得选举，成功在zk 上重建/controller 节点。之后
broker 3 会从zk中读取集群元数据信息，并初始化到自己的缓存中。

#### 控制器设计原理

- 单线程加事件队列的实现

- 事件处理线程，统一处理各种控制器事件，然后控制器将原来执行的操作全部建模成一个个独立的事件，发送到专属的事件队列中，供此线程消费

[![s97UIg.jpg](https://s3.ax1x.com/2021/01/03/s97UIg.jpg)](https://imgchr.com/i/s97UIg)


### 高水位和leader epoch

#### 水位

在时刻T，任意创建时间（event time）为 T'，且T'<=T 的所有时间已经到达或被观测到,那么T就被定义为水位。

#### 高水位

1. 定义消息可见性，即用来标识分区下的哪些消息是可以被消费者消费的

2. 帮助kafka完成副本同步

3. 位移值等于高水位的消息也属于未提交消息。高水位上的消息是不能被消费者消费的

#### 日志末端位移

1. 表示副本写入下一条消息的位移值

2. 介于高水位和LEO之间的消息属于未提交消息，同一个副本对象，其高水位值不会大于LEO值

3. kafka所有副本都有对应的高水位和LEO值，而不仅仅是Leader副本。只不过Leader副本比较特殊，kafka使用Leader副本高水位来定义所在分区的高水位。 
   也就是，分区的高水位就是其leader副本的高水位
   

[![s9jKmR.jpg](https://s3.ax1x.com/2021/01/03/s9jKmR.jpg)](https://imgchr.com/i/s9jKmR)

#### 高水位更新机制

![高水位更新机制.jpg](https://i.loli.net/2021/01/03/XqRxDlOnJmpMdK1.jpg)

- 每个副本对象都保存了一组高水位值和LEO值，但实际上，在Leader副本所在的Broker上，还保存了其他Follower副本的LEO值，其他的Follower副本又成为远程副本，这些远程副本的作用是帮助Leader副本确定其高水位，也就是区分高水位。

- kafka副本机制在运行过程中，会更新broker1上的Follower副本的高水位和LEO值，同时也会更新Broker0上Leader副本的高水位和LEO 以及所有远程副本的LEO，
但它不会更新远程副本的高水位

更新对象 | 更新时机
---|---
broker1 上Follower副本LEO | Follower副本从Leader副本拉取消息，写入到本地磁盘后，会更新其LEO值
Broker 0 上Leader 副本LEO  | Leader副本接收生产者发送的消息，写入到本地磁盘后，会更新其LEO值
Broker 0 上远程副本LEO | Follower 副本从Leader副本拉取消息时，会告诉Leader副本从那个位移处开始拉取。Leader副本会使用这个位移来更新远程副本的LEO
Broker 1 上Follower副本高水位 | Follower副本成功更新完LEO之后，会比较其LEO 值与Leader副本发来的高水位值，并用两者的较小值更新自己的高水位
Broker 0 上Leader副本高水位 | 主要有两个更新时机：一个是Leader 副本更新其LEO之后；另一个是更新完远程副本LEO之后。 具体的算法是：取Leader 副本和所有与Leader同步的远程副本LEO中的最小值。

##### leader副本

##### 处理生产者请求逻辑

1. 写入消息到本地磁盘
2. 更新分区高水位值
- 获取Leader副本所在broker端保存的所有远程副本LEO
- 获取Leader副本高水位值：currentHW
- 更新currentHW = max{currentHW,min(远程副本LEO)}

##### 处理Follower副本拉取逻辑
1. 读取磁盘中的消息数据
2. 使用Follower副本发送请求中的位移值更新远程副本LEO值
3. 更新分区高水位值
- 获取Leader副本所在broker端保存的所有远程副本LEO
- 获取Leader副本高水位值：currentHW
- 更新currentHW = max{currentHW,min(远程副本LEO)}

##### Follower副本

##### 从Leader拉去消息的处理逻辑

1. 写入消息到本地磁盘
2. 更新LEO值
3. 更新高水位值
- 获取Leader发送的高水位值：currentHW
- 获取步骤2中更新过的LEO值：currentLEO
- 更新高水位为min(currentHW,currentLEO)

### Leader Epoch 

- Epoch :单调递增的版本号，每当副本领导权发生变更的时候，增加版本好。小版本号的Leader被认为是过期的Leader，不再行驶Leader权利

- 起始位移：Leader副本在该Epoch值上写入的首条消息的位移

kafka broker会在内存中为每个分区都缓存 Leader epoch 数据，同时还会定期地将这些信息持久化到一个checkpoint文件中。当leader副本写入消息到磁盘时，broker会尝试更新这部分缓存，
如果该leader是首次写入消息，那么broker会向缓存中增加一个leader epoch条目，否则就不做更新。每次有leader 变更时，新的leader 副本会查询这部分缓存，取出对应的leader epoch的起始位移，以避免
数据丢失和不一致情况。

- 单纯依赖高水位 在broker参数**min.insync.replicas设置为1**时，一旦消息被写入到Leader副本磁盘，但因为时间错配问题，导致Follower端的高水位更新有滞后。如果
在这个时间范围内，接连发生broker宕机，因为重启后日志截断，会导致数据的丢失。