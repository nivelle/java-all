### 控制器

![kafkacontroller源码.jpg](https://i.loli.net/2021/05/16/BdHrIFjW7aKXQAS.jpg)
#### 集群元数据

- Controller承载了Zookeeper上的所有元数据

![集群元数据.jpg](https://i.loli.net/2021/05/16/YQEZsT3wdGqv6eu.jpg)

#### ControllerContext 是Controller的数据容器类

````

class ControllerContext {
  val stats = new ControllerStats // Controller统计信息类 
  var offlinePartitionCount = 0   // 离线分区计数器
  val shuttingDownBrokerIds = mutable.Set.empty[Int]  // 关闭中Broker的Id列表
  private val liveBrokers = mutable.Set.empty[Broker] // 当前运行中Broker对象列表
  private val liveBrokerEpochs = mutable.Map.empty[Int, Long]   // 运行中Broker Epoch列表
  var epoch: Int = KafkaController.InitialControllerEpoch   // Controller当前Epoch值
  var epochZkVersion: Int = KafkaController.InitialControllerEpochZkVersion  // Controller对应ZooKeeper节点的Epoch值
  val allTopics = mutable.Set.empty[String]  // 集群主题列表
  val partitionAssignments = mutable.Map.empty[String, mutable.Map[Int, ReplicaAssignment]]  // 主题分区的副本列表
  val partitionLeadershipInfo = mutable.Map.empty[TopicPartition, LeaderIsrAndControllerEpoch]  // 主题分区的Leader/ISR副本信息
  val partitionsBeingReassigned = mutable.Set.empty[TopicPartition]  // 正处于副本重分配过程的主题分区列表
  val partitionStates = mutable.Map.empty[TopicPartition, PartitionState] // 主题分区状态列表 
  val replicaStates = mutable.Map.empty[PartitionAndReplica, ReplicaState]  // 主题分区的副本状态列表
  val replicasOnOfflineDirs = mutable.Map.empty[Int, Set[TopicPartition]]  // 不可用磁盘路径上的副本列表
  val topicsToBeDeleted = mutable.Set.empty[String]  // 待删除主题列表
  val topicsWithDeletionStarted = mutable.Set.empty[String]  // 已开启删除的主题列表
  val topicsIneligibleForDeletion = mutable.Set.empty[String]  // 暂时无法执行删除的主题列表
  ......
}

````

- ControllerStats: UncleanLeaderElectionsPerSec 和所有 Controller 事件状态的执行速率与时间;

前者是计算 Controller 每秒执行的 Unclean Leader 选举数量，通常情况下，执行 Unclean Leader 选举可能造成数据丢失，一般不建议开启它;后者是统计所有 Controller
状态的速率和时间信息，单位是毫秒

- offlinePartitionCount: 该字段统计集群中所有离线活处于不可用状态的主题分区数量。也就是leader=-1;

- shuttingDownBrokerIds: 该字段保存所有真正关闭的Broker ID 列表

- liveBrokers: 该字段保存当前所有运行中的Broker对象。每个 Broker 对象就是一个 <Id，EndPoint，机架信息 > 的三元组

- liveBrokerEpochs：该字段保存所有运行中 Broker 的 Epoch 信息。Kafka 使用 Epoch 数据防止 Zombie Broker，即一个非常老的 Broker 被选举成为 Controller

- epoch & epochZkVersion： epoch 实际上就是 ZooKeeper 中 /controller_epoch 节点的值，你可以认为它就是 Controller 在整个 Kafka 集群的版本号，而
  epochZkVersion 实际上是 /controller_epoch 节点的 dataVersion 值

- allTopics：该字段保存集群上所有的主题名称。每当有主题的增减，Controller 就要更新该字段的值

- partitionAssignments： 该字段保存所有主题分区的副本分配情况

#### Controller发送请求类型

[![sYLAmT.md.jpg](https://s3.ax1x.com/2021/01/12/sYLAmT.md.jpg)](https://imgchr.com/i/sYLAmT)

controller会给集群中所有Broker(包括它自己所在的Broker)机器发送网络请求，让broker执行相应的指令

#### ControllerChannelManger

#### ControllerEventManger

![controllerEventManger.jpg](https://i.loli.net/2021/05/16/xYm6grFztvGohUj.jpg)
### Controller选举

- Controller 依赖 ZooKeeper 实现 Controller 选举，主要是借助于 /controller 临时节点和 ZooKeeper 的监听器机制。

- Controller 触发场景有 3 种：集群启动时；/controller 节点被删除时；/controller 节点数据变更时。

-----
## Controller的作用

### 一 选举Leader和ISR

- 控制器从ZK的/brokers/topics加载一个topic所有分区的所有副本，从分区副本列表中选出一个作为该分区的leader，并将该分区对应所有副本置于ISR列表，其他分区类似；其他topic的所有分区也类似。

### 二 同步元数据信息包括broker和分区的元数据信息

- 控制器架子ZK的/brokers/ids以及上一个步骤得到的topic下各分区leader和ISR将这些元数据信息同步到集群每个broker。

- 通过下面所阐述的监控机制当有broker或者分区发生变更时及时更新到集群保证集群每一台broker缓存的是最新元数据。

### 三 broker增删监听与处理

#### 3.1 broker加入的监听和处理

控制器启动时就起一个监视器监视ZK/brokers/ids/子节点。当存在broker启动加入集群后都会在ZK/brokers/ids/增加一个子节点brokerId，控制器的监视器发现这种变化后，控制器开始执行broker加入的相关流程并更新元数据信息到集群。

#### 3.2 broker崩溃的监听与处理

控制器启动时就起一个监视器监视ZK/brokers/ids/子节点。当一个broker崩溃时，该broker与ZK的会话失效导致ZK会删除该子节点，控制器的监视器发现这种变化后，控制器开始执行broker删除的相关流程并更新元数据信息到集群。

### 四 topic变化监听与处理

#### 4.1 topic创建的监听与处理

控制器启动时就起一个监视器监视ZK/brokers/topics/子节点。当通过脚本或者请求创建一个topic后，该topic对应的所有分区及其副本都会写入该目录下的一个子节点。控制器的监视器发现这种变化后，控制器开始执行topic创建的相关流程包括leader选举和ISR并同步元数据信息到集群；且新增一个监视器监视ZK/brokers/topics/<新增topic子节点内容>防止该topic内容变化。

#### 4.2 topic删除的监听与处理

控制器启动时就起一个监视器监视ZK/admin/delete_topics/子节点。当通过脚本或者请求删除一个topic后，该topic会写入该目录下的一个子节点。控制器的监视器发现这种变化后，控制器开始执行topic删除的相关流程包括通知该topic所有分区的所有副本停止运行；通知所有分区所有副本删除数据；删除ZK/admin/delete_topics/<待删除topic子节点>。

### 五 分区变化监听与变化处理

#### 5.1 分区重分配监听与处理

- 分区重分配通过KAFKA管理员脚本执行完成一个topic下分区的副本重新分配broker。

- 控制器启动时就起一个监视器监视ZK/admin/reassign_part/子节点。当通过脚本执行分区重分配后会在该目录增加一个子节点，子节点内容是按照一定格式构建的重分配方案，控制器的监视器发现这种变化后，控制器开始执行分区重分配相关流程如同步元数据信息。

#### 5.2 分区扩展监听与处理

- 如上面4.1 所述当创建一个topic后，控制器会增加一个监视器监视ZK/brokers/topics/<新增topic子节点内容>防止该topic内容变化。当通过脚本执行分扩展后会在该目录增加新的分区目录。控制器的监视器发现这种变化后，控制器开始执行分区扩展相应流程如选举leader和ISR并同步。

### 六 broker优雅退出

- 相比较broker机器直接宕机或强制kill，通过脚本或kill -9 关闭一个broker我们称为broker优雅退出。即将关闭的broker向控制器发送退出请求后一直阻塞。

- 控制器接收到请求后，执行leader重选举和ISR后响应broker。broker接收后退出。

- 这个比较特殊，不依赖ZK，直接通过broker和控制器RPC通信即可完成。

### 控制器fail-over

- 集群在开始时集群中第一个broker通过在ZK/controller注册子节点brokerId使得自己成为该集群的控制器，其他broker虽然没有争取到控制器资格，但是都会起一个监视器监视ZK/controller以及向/controller_EPOCH注册子节点。

- 如果控制器所在broker退出、崩溃或与ZK会话失效则ZK会删除/controller内该子节点，各个broker的监视器发现这种变化后，每个broker开始竞争直到有一个竞争成为新的控制器，并向/controller注册子节点，以及向/controller_EPOCH注册子节点。
