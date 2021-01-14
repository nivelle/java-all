### TopiDeletionManager  主题删除管理器

在主题删除过程中，Kafka 会调整集群中三个地方的数据：ZooKeeper、元数据缓存和磁盘日志文件。删除主题时，ZooKeeper 上与该主题相关的所有 ZNode 节点必须被清除；Controller 端元数据缓存中的相关项，也必须要被处理，并且要被同步到集群的其他 Broker 上；而磁盘日志文件，更是要清理的首要目标。这三个地方必须要统一处理，就好似我们常说的原子性操作一样。现在回想下开篇提到的那个“秘籍”，你就会发现它缺少了非常重要的一环，那就是：无法清除 Controller 端的元数据缓存项。因此，你要尽力避免使用这个“大招”。

### ReplicaStateMachine 副本状态机

副本状态机：ReplicaStateMachine 是 Kafka Broker 端源码中控制副本状态流转的实现类。每个 Broker 启动时都会创建 ReplicaStateMachine 实例，但只有 Controller 组件所在的 Broker 才会启动它。

#### 副本状态及状态管理流程

- NewReplica：副本被创建之后所处的状态。

- OnlineReplica：副本正常提供服务时所处的状态。 
  
-OfflineReplica：副本服务下线时所处的状态。
- ReplicaDeletionStarted：副本被删除时所处的状态。
- ReplicaDeletionSuccessful：副本被成功删除后所处的状态。
- ReplicaDeletionIneligible：开启副本删除，但副本暂时无法被删除时所处的状态。
- NonExistentReplica：副本从副本状态机被移除前所处的状态

[![sdhLUH.md.jpg](https://s3.ax1x.com/2021/01/14/sdhLUH.md.jpg)](https://imgchr.com/i/sdhLUH)
  
### PartitionStateMachine 分区状态转换

#### 分区状态

- NewPartition:分区被创建后被设置成这个状态，表明它是一个全新的分区对象。处于这个状态的分区，被kafka认为是"未初始化"，不能选举leader

- OnlinePartition:分区正式提供服务时所处的状态

- OffOnlinePartition:分区下线后所处的状态

- NonExistentPartition:分区被删除，并且从分区状态机移除后所处的状态。

[![sdcPhV.md.png](https://s3.ax1x.com/2021/01/14/sdcPhV.md.png)](https://imgchr.com/i/sdcPhV)

#### 分区leader选举的场景

#### PartitionLeaderElectionStrategy 分区选举策略

````

// 分区Leader选举策略接口
sealed trait PartitionLeaderElectionStrategy
// 离线分区Leader选举策略: 因为Leader副本下线而引发的分区Leader选举
final case class OfflinePartitionLeaderElectionStrategy(allowUnclean: Boolean) extends PartitionLeaderElectionStrategy
// 分区副本重分配Leader选举策略  ： 因为执行分区副本重新分配而引发的分区Leader选举
final case object ReassignPartitionLeaderElectionStrategy extends PartitionLeaderElectionStrategy
// 分区Preferred副本Leader选举策略： 因为执行 preferred【首选】副本Leader选举而引发的分区Leader选举
final case object PreferredReplicaPartitionLeaderElectionStrategy extends PartitionLeaderElectionStrategy
// Broker Controlled关闭时Leader选举策略： 因为正常关闭Broker而引发的分区Leader选举
final case object ControlledShutdownPartitionLeaderElectionStrategy  extends PartitionLeaderElectionStrategy

````

- OfflinePartitionLeaderElectionStrategy

````

def offlinePartitionLeaderElection(assignment: Seq[Int], 
  isr: Seq[Int], liveReplicas: Set[Int], 
  uncleanLeaderElectionEnabled: Boolean, controllerContext: ControllerContext): Option[Int] = {
  // 从当前分区副本列表中寻找首个处于存活状态的ISR副本
  assignment.find(id => liveReplicas.contains(id) && isr.contains(id)).orElse {
    // 如果找不到满足条件的副本，查看是否允许Unclean Leader选举
    // 即Broker端参数unclean.leader.election.enable是否等于true
    if (uncleanLeaderElectionEnabled) {
      // 选择当前副本列表中的第一个存活副本作为Leader
      val leaderOpt = assignment.find(liveReplicas.contains)
      if (leaderOpt.isDefined)
        controllerContext.stats.uncleanLeaderElectionRate.mark()
      leaderOpt
    } else {
      None // 如果不允许Unclean Leader选举，则返回None表示无法选举Leader
    }
  }
}

````

- assignments:

  分区的副本列表。 Assigned Replicas : AR 是有顺序的，而且不一定和ISR的顺序相同

- isr ： 保存了分区所有与Leader副本保存同步的副本列表。Leader自己也在ISR 中，ISR也是有顺序的

- liveReplicas: 保存了该分区下所有处于存活状态的副本。根据 Controller 元数据缓存中的数据来判定。简单来说，所有在运行中的 Broker 上的副本，都被认为是存活的

- uncleanLeaderElectionEnabled 
  
  所谓的 Unclean Leader 选举，是指在 ISR 列表为空的情况下，Kafka 选择一个非 ISR 副本作为新的 Leader。由于存在丢失数据的风险，目前，社区已经通过把 Broker 端参数 unclean.leader.election.enable 的默认值设置为 false 的方式，禁止 Unclean Leader 选举了

##### 选举过程

代码首先会顺序搜索 AR 列表，并把第一个同时满足以下两个条件的副本作为新的 Leader 返回：

- 该副本是存活状态，即副本所在的 Broker 依然在运行中
- 该副本在 ISR 列表中

#### 分区状态转换处理方法

``````

override def handleStateChanges(
    partitions: Seq[TopicPartition],
    targetState: PartitionState,
    partitionLeaderElectionStrategyOpt: Option[PartitionLeaderElectionStrategy]
  ): Map[TopicPartition, Either[Throwable, LeaderAndIsr]] = {
    if (partitions.nonEmpty) {
      try {
        // 清空Controller待发送请求集合，准备本次请求发送
        controllerBrokerRequestBatch.newBatch()
        // 调用doHandleStateChanges方法执行真正的状态变更逻辑
        val result = doHandleStateChanges(
          partitions,
          targetState,
          partitionLeaderElectionStrategyOpt
        )
        // Controller给相关Broker发送请求通知状态变化
        controllerBrokerRequestBatch.sendRequestsToBrokers(
          controllerContext.epoch)
        // 返回状态变更处理结果
        result
      } catch {
        // 如果Controller易主，则记录错误日志，然后重新抛出异常
        // 上层代码会捕获该异常并执行maybeResign方法执行卸任逻辑
        case e: ControllerMovedException =>
          error(s"Controller moved to another broker when moving some partitions to $targetState state", e)
          throw e
        // 如果是其他异常，记录错误日志，封装错误返回
        case e: Throwable =>
          error(s"Error while moving some partitions to $targetState state", e)
          partitions.iterator.map(_ -> Left(e)).toMap
      }
    } else { // 如果partitions为空，什么都不用做
      Map.empty
    }
  }

``````

- doHandleStateChanges

````

case OnlinePartition =>
  // 获取未初始化分区列表，也就是NewPartition状态下的所有分区
  val uninitializedPartitions = validPartitions.filter(
    partition => partitionState(partition) == NewPartition)
  // 获取具备Leader选举资格的分区列表
  // 只能为OnlinePartition和OfflinePartition状态的分区选举Leader 
  val partitionsToElectLeader = validPartitions.filter(
    partition => partitionState(partition) == OfflinePartition ||
     partitionState(partition) == OnlinePartition)
  // 初始化NewPartition状态分区，在ZooKeeper中写入Leader和ISR数据
  if (uninitializedPartitions.nonEmpty) {
    val successfulInitializations = initializeLeaderAndIsrForPartitions(uninitializedPartitions)
    successfulInitializations.foreach { partition =>
      stateChangeLog.info(s"Changed partition $partition from ${partitionState(partition)} to $targetState with state " +
        s"${controllerContext.partitionLeadershipInfo(partition).leaderAndIsr}")
      controllerContext.putPartitionState(partition, OnlinePartition)
    }
  }
  // 为具备Leader选举资格的分区推选Leader
  if (partitionsToElectLeader.nonEmpty) {
    val electionResults = electLeaderForPartitions(
      partitionsToElectLeader,
      partitionLeaderElectionStrategyOpt.getOrElse(
        throw new IllegalArgumentException("Election strategy is a required field when the target state is OnlinePartition")
      )
    )
    electionResults.foreach {
      case (partition, Right(leaderAndIsr)) =>
        stateChangeLog.info(
          s"Changed partition $partition from ${partitionState(partition)} to $targetState with state $leaderAndIsr"
        )
        // 将成功选举Leader后的分区设置成OnlinePartition状态
        controllerContext.putPartitionState(
          partition, OnlinePartition)
      case (_, Left(_)) => // 如果选举失败，忽略之
    }
    // 返回Leader选举结果
    electionResults
  } else {
    Map.empty
  }


````

- 第一步： 为 NewPartition 状态的分区做初始化操作，也就是在 ZooKeeper 中，创建并写入分区节点数据。
节点的位置是/brokers/topics/<topic>/partitions/<partition>，每个节点都要包含分区的 Leader 和 ISR 等数据。
而 Leader 和 ISR 的确定规则是：选择存活副本列表的第一个副本作为 Leader；选择存活副本列表作为 ISR。
  
````

private def initializeLeaderAndIsrForPartitions(partitions: Seq[TopicPartition]): Seq[TopicPartition] = {
  ......
    // 获取每个分区的副本列表
    val replicasPerPartition = partitions.map(partition => partition -> controllerContext.partitionReplicaAssignment(partition))
    // 获取每个分区的所有存活副本
    val liveReplicasPerPartition = replicasPerPartition.map { case (partition, replicas) =>
        val liveReplicasForPartition = replicas.filter(replica => controllerContext.isReplicaOnline(replica, partition))
        partition -> liveReplicasForPartition
    }
    // 按照有无存活副本对分区进行分组
    // 分为两组：有存活副本的分区；无任何存活副本的分区
    val (partitionsWithoutLiveReplicas, partitionsWithLiveReplicas) = liveReplicasPerPartition.partition { case (_, liveReplicas) => liveReplicas.isEmpty }
    ......
    // 为"有存活副本的分区"确定Leader和ISR
    // Leader确认依据：存活副本列表的首个副本被认定为Leader
    // ISR确认依据：存活副本列表被认定为ISR
    val leaderIsrAndControllerEpochs = partitionsWithLiveReplicas.map { case (partition, liveReplicas) =>
      val leaderAndIsr = LeaderAndIsr(liveReplicas.head, liveReplicas.toList)
      ......
    }.toMap
    ......
}

````

- 是为具备 Leader 选举资格的分区推选 Leader，代码调用 electLeaderForPartitions 方法实现。这个方法会不断尝试为多个分区选举 Leader，直到所有分区都成功选出 Leader

[![sdfCSP.md.jpg](https://s3.ax1x.com/2021/01/14/sdfCSP.md.jpg)](https://imgchr.com/i/sdfCSP)

- 选举第一步：

````

// doElectLeaderForPartitions方法的第1部分
val getDataResponses = try {
  // 批量获取ZooKeeper中给定分区的znode节点数据
  zkClient.getTopicPartitionStatesRaw(partitions)
} catch {
  case e: Exception =>
    return (partitions.iterator.map(_ -> Left(e)).toMap, Seq.empty)
}
// 构建两个容器，分别保存可选举Leader分区列表和选举失败分区列表
val failedElections = mutable.Map.empty[TopicPartition, Either[Exception, LeaderAndIsr]]
val validLeaderAndIsrs = mutable.Buffer.empty[(TopicPartition, LeaderAndIsr)]
// 遍历每个分区的znode节点数据
getDataResponses.foreach { getDataResponse =>
  val partition = getDataResponse.ctx.get.asInstanceOf[TopicPartition]
  val currState = partitionState(partition)
  // 如果成功拿到znode节点数据
  if (getDataResponse.resultCode == Code.OK) {
    TopicPartitionStateZNode.decode(getDataResponse.data, getDataResponse.stat) match {
      // 节点数据中含Leader和ISR信息
      case Some(leaderIsrAndControllerEpoch) =>
        // 如果节点数据的Controller Epoch值大于当前Controller Epoch值
        if (leaderIsrAndControllerEpoch.controllerEpoch > controllerContext.epoch) {
          val failMsg = s"Aborted leader election for partition $partition since the LeaderAndIsr path was " +
            s"already written by another controller. This probably means that the current controller $controllerId went through " +
            s"a soft failure and another controller was elected with epoch ${leaderIsrAndControllerEpoch.controllerEpoch}."
          // 将该分区加入到选举失败分区列表
          failedElections.put(partition, Left(new StateChangeFailedException(failMsg)))
        } else {
          // 将该分区加入到可选举Leader分区列表 
          validLeaderAndIsrs += partition -> leaderIsrAndControllerEpoch.leaderAndIsr
        }
      // 如果节点数据不含Leader和ISR信息
      case None =>
        val exception = new StateChangeFailedException(s"LeaderAndIsr information doesn't exist for partition $partition in $currState state")
        // 将该分区加入到选举失败分区列表
        failedElections.put(partition, Left(exception))
    }
  // 如果没有拿到znode节点数据，则将该分区加入到选举失败分区列表
  } else if (getDataResponse.resultCode == Code.NONODE) {
    val exception = new StateChangeFailedException(s"LeaderAndIsr information doesn't exist for partition $partition in $currState state")
    failedElections.put(partition, Left(exception))
  } else {
    failedElections.put(partition, Left(getDataResponse.resultException.get))
  }
}

if (validLeaderAndIsrs.isEmpty) {
  return (failedElections.toMap, Seq.empty)
}

````

- 选举第2步：

````

// doElectLeaderForPartitions方法的第2部分
// 开始选举Leader，并根据有无Leader将分区进行分区
val (partitionsWithoutLeaders, partitionsWithLeaders) = 
  partitionLeaderElectionStrategy match {
  case OfflinePartitionLeaderElectionStrategy(allowUnclean) =>
    val partitionsWithUncleanLeaderElectionState = collectUncleanLeaderElectionState(
      validLeaderAndIsrs,
      allowUnclean
    )
    // 为OffinePartition分区选举Leader
    leaderForOffline(controllerContext, partitionsWithUncleanLeaderElectionState).partition(_.leaderAndIsr.isEmpty)
  case ReassignPartitionLeaderElectionStrategy =>
    // 为副本重分配的分区选举Leader
    leaderForReassign(controllerContext, validLeaderAndIsrs).partition(_.leaderAndIsr.isEmpty)
  case PreferredReplicaPartitionLeaderElectionStrategy =>
    // 为分区执行Preferred副本Leader选举
    leaderForPreferredReplica(controllerContext, validLeaderAndIsrs).partition(_.leaderAndIsr.isEmpty)
  case ControlledShutdownPartitionLeaderElectionStrategy =>
    // 为因Broker正常关闭而受影响的分区选举Leader
    leaderForControlledShutdown(controllerContext, validLeaderAndIsrs).partition(_.leaderAndIsr.isEmpty)
}


// doElectLeaderForPartitions方法的第3部分
// 将所有选举失败的分区全部加入到Leader选举失败分区列表
partitionsWithoutLeaders.foreach { electionResult =>
  val partition = electionResult.topicPartition
  val failMsg = s"Failed to elect leader for partition $partition under strategy $partitionLeaderElectionStrategy"
  failedElections.put(partition, Left(new StateChangeFailedException(failMsg)))
}
val recipientsPerPartition = partitionsWithLeaders.map(result => result.topicPartition -> result.liveReplicas).toMap
val adjustedLeaderAndIsrs = partitionsWithLeaders.map(result => result.topicPartition -> result.leaderAndIsr.get).toMap
// 使用新选举的Leader和ISR信息更新ZooKeeper上分区的znode节点数据
val UpdateLeaderAndIsrResult(finishedUpdates, updatesToRetry) = zkClient.updateLeaderAndIsr(
  adjustedLeaderAndIsrs, controllerContext.epoch, controllerContext.epochZkVersion)
// 对于ZooKeeper znode节点数据更新成功的分区，封装对应的Leader和ISR信息
// 构建LeaderAndIsr请求，并将该请求加入到Controller待发送请求集合
// 等待后续统一发送
finishedUpdates.foreach { case (partition, result) =>
  result.foreach { leaderAndIsr =>
    val replicaAssignment = controllerContext.partitionFullReplicaAssignment(partition)
    val leaderIsrAndControllerEpoch = LeaderIsrAndControllerEpoch(leaderAndIsr, controllerContext.epoch)
    controllerContext.partitionLeadershipInfo.put(partition, leaderIsrAndControllerEpoch)
    controllerBrokerRequestBatch.addLeaderAndIsrRequestForBrokers(recipientsPerPartition(partition), partition,
      leaderIsrAndControllerEpoch, replicaAssignment, isNew = false)
  }
}
// 返回选举结果，包括成功选举并更新ZooKeeper节点的分区、选举失败分区以及
// ZooKeeper节点更新失败的分区
(finishedUpdates ++ failedElections, updatesToRetry)

````