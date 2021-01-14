### 控制器

[![sYTTKO.md.jpg](https://s3.ax1x.com/2021/01/12/sYTTKO.md.jpg)](https://imgchr.com/i/sYTTKO)

#### 集群元数据

- Controller承载了Zookeeper上的所有元数据

[![sY7KLF.jpg](https://s3.ax1x.com/2021/01/12/sY7KLF.jpg)](https://imgchr.com/i/sY7KLF)

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

前者是计算 Controller 每秒执行的 Unclean Leader 选举数量，通常情况下，执行 Unclean Leader 选举可能造成数据丢失，一般不建议开启它;后者是统计所有 Controller 状态的速率和时间信息，单位是毫秒

- offlinePartitionCount: 该字段统计集群中所有离线活处于不可用状态的主题分区数量。也就是leader=-1;

- shuttingDownBrokerIds: 该字段保存所有真正关闭的Broker ID 列表

- liveBrokers: 该字段保存当前所有运行中的Broker对象。每个 Broker 对象就是一个 <Id，EndPoint，机架信息 > 的三元组

- liveBrokerEpochs：该字段保存所有运行中 Broker 的 Epoch 信息。Kafka 使用 Epoch 数据防止 Zombie Broker，即一个非常老的 Broker 被选举成为 Controller

- epoch & epochZkVersion： epoch 实际上就是 ZooKeeper 中 /controller_epoch 节点的值，你可以认为它就是 Controller 在整个 Kafka 集群的版本号，而 epochZkVersion 实际上是 /controller_epoch 节点的 dataVersion 值

- allTopics：该字段保存集群上所有的主题名称。每当有主题的增减，Controller 就要更新该字段的值

- partitionAssignments： 该字段保存所有主题分区的副本分配情况

#### Controller发送请求类型

[![sYLAmT.md.jpg](https://s3.ax1x.com/2021/01/12/sYLAmT.md.jpg)](https://imgchr.com/i/sYLAmT)

controller会给集群中所有Broker(包括它自己所在的Broker)机器发送网络请求，让broker执行相应的指令


#### ControllerChannelManger

#### ControllerEventManger

[![sdISHS.md.jpg](https://s3.ax1x.com/2021/01/14/sdISHS.md.jpg)](https://imgchr.com/i/sdISHS)

### Controller选举

- Controller 依赖 ZooKeeper 实现 Controller 选举，主要是借助于 /controller 临时节点和 ZooKeeper 的监听器机制。
  
- Controller 触发场景有 3 种：集群启动时；/controller 节点被删除时；/controller 节点数据变更时。

### Controller的作用

#### 成员管理

- 成员数量管理，主要体现在新增成员和移除现有成员

- 单个成员管理，单个broker的数据变更

#### 主题管理

- 主题创建/变更/删除


#### 操作元数据