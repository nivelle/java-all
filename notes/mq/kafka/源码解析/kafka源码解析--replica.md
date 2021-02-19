### 副本管理机制

#### 同步副本线程的定义

````

abstract class AbstractFetcherThread(
  name: String,  // 线程名称
  clientId: String,  // Client Id，用于日志输出
  val sourceBroker: BrokerEndPoint,  // 数据源Broker地址
  failedPartitions: FailedPartitions,  // 处理过程中出现失败的分区
  fetchBackOffMs: Int = 0,  // 获取操作重试间隔, 参数值：replica.fetch.backoff.ms 
  isInterruptible: Boolean = true,  // 线程是否允许被中断
  val brokerTopicStats: BrokerTopicStats) // Broker端主题监控指标
  extends ShutdownableThread(name, isInterruptible) {
  // 定义FetchData类型表示获取的消息数据
  type FetchData = FetchResponse.PartitionData[Records]
  // 定义EpochData类型表示Leader Epoch数据
  type EpochData = OffsetsForLeaderEpochRequest.PartitionData
  private val partitionStates = new PartitionStates[PartitionFetchState]
  ......
}

````

#### 同步获取的返回值

````

public static final class PartitionData<T extends BaseRecords> {
    public final Errors error;           // 错误码
    public final long highWatermark;     // 高水位值
    public final long lastStableOffset;  // 最新LSO值 
    public final long logStartOffset;    // 最新Log Start Offset值
    // 期望的Read Replica
    // KAFKA 2.4之后支持部分Follower副本可以对外提供读服务 
    public final Optional<Integer> preferredReadReplica;
    // 该分区对应的已终止事务列表
    public final List<AbortedTransaction> abortedTransactions;
    // 消息集合，最重要的字段！
    public final T records;
    // 构造函数......
}
````

### 分区读取状态

- 可获取：表明副本获取线程当前能够读取数据

- 截断中：表明分区副本正在执行截断操作

- 被推迟：表明副本获取线程获取数据时出现错误，需要等待重试

#### doWork

````
override def doWork(): Unit = {
  maybeTruncate()   // 执行副本截断操作
  maybeFetch()      // 执行消息获取操作
}
````

- maybeTruncate

````

private def maybeTruncate(): Unit = {
  // 将所有处于截断中状态的分区依据有无Leader Epoch值进行分组
  val (partitionsWithEpochs, partitionsWithoutEpochs) = fetchTruncatingPartitions()
  // 对于有Leader Epoch值的分区，将日志截断到Leader Epoch值对应的位移值处
  if (partitionsWithEpochs.nonEmpty) {
    truncateToEpochEndOffsets(partitionsWithEpochs)
  }
  // 对于没有Leader Epoch值的分区，将日志截断到高水位值处
  if (partitionsWithoutEpochs.nonEmpty) {
     //1. 遍历给定的所有分区  2. 依次为每个分区获取当前的高水位值，并将其保存在分区读取状态类中
     //3. 调用doTruncate 方法执行真正的日志截断操作 4. 代码更新这种分区的分区读取状态
    truncateToHighWatermark(partitionsWithoutEpochs)
  }
}
````

- maybeFetch

````

private def maybeFetch(): Unit = {
  val fetchRequestOpt = inLock(partitionMapLock) {
    // 为partitionStates中的分区构造FetchRequest
    // partitionStates中保存的是要去获取消息的分区以及对应的状态
    val ResultWithPartitions(fetchRequestOpt, partitionsWithError) = 
      buildFetch(partitionStates.partitionStateMap.asScala)
    // 处理出错的分区，处理方式主要是将这个分区加入到有序Map末尾
    // 等待后续重试
    handlePartitionsWithErrors(partitionsWithError, "maybeFetch")
    // 如果当前没有可读取的分区，则等待fetchBackOffMs时间等候后续重试
    if (fetchRequestOpt.isEmpty) {
      trace(s"There are no active partitions. Back off for $fetchBackOffMs ms before sending a fetch request")
      partitionMapCond.await(fetchBackOffMs, TimeUnit.MILLISECONDS)
    }
    fetchRequestOpt
  }
  // 发送FETCH请求给Leader副本，并处理Response
  fetchRequestOpt.foreach { case ReplicaFetch(sessionPartitions, fetchRequest) =>
    processFetchRequest(sessionPartitions, fetchRequest)
  }
}

````

[![sDIW24.png](https://s3.ax1x.com/2021/01/16/sDIW24.png)](https://imgchr.com/i/sDIW24)

#### 子类： ReplicaFetcherThread

````


class ReplicaFetcherThread(name: String,
                           fetcherId: Int,
                           sourceBroker: BrokerEndPoint,
                           brokerConfig: KafkaConfig,
                           failedPartitions: FailedPartitions,
                           replicaMgr: ReplicaManager,
                           metrics: Metrics,
                           time: Time,
                           quota: ReplicaQuota,
                           leaderEndpointBlockingSend: Option[BlockingSend] = None)
  extends AbstractFetcherThread(name = name,
                                clientId = name,
                                sourceBroker = sourceBroker,
                                failedPartitions,
                                fetchBackOffMs = brokerConfig.replicaFetchBackoffMs,
                                isInterruptible = false,
                                replicaMgr.brokerTopicStats) {
  // 副本Id就是副本所在Broker的Id
  private val replicaId = brokerConfig.brokerId
  ......
  // 用于执行请求发送的类
  private val leaderEndpoint = leaderEndpointBlockingSend.getOrElse(
    new ReplicaFetcherBlockingSend(sourceBroker, brokerConfig, metrics, time, fetcherId,
      s"broker-$replicaId-fetcher-$fetcherId", logContext))
  // Follower发送的FETCH请求被处理返回前的最长等待时间
  private val maxWait = brokerConfig.replicaFetchWaitMaxMs
  // 每个FETCH Response返回前必须要累积的最少字节数
  private val minBytes = brokerConfig.replicaFetchMinBytes
  // 每个合法FETCH Response的最大字节数
  private val maxBytes = brokerConfig.replicaFetchResponseMaxBytes
  // 单个分区能够获取到的最大字节数
  private val fetchSize = brokerConfig.replicaFetchMaxBytes
  // 维持某个Broker连接上获取会话状态的类
  val fetchSessionHandler = new FetchSessionHandler(
    logContext, sourceBroker.id)
  ......
}

````

- fetcherId: Follower拉取线程ID，线程的编号。单台broker上允许存在多个ReplicaFetcherThread 线程。 broker端参数 **num.replica.fetchers**
  决定了kafka到底创建多少个Follower拉取线程

- brokerConfig:它封装了 Broker 端所有的参数信息。同样地，ReplicaFetcherThread 类也是通过它来获取 Broker 端指定参数的值

- replicaMgr:副本管理器。该线程类通过副本管理器来获取分区对象、副本对象以及它们下面的日志对象

- quota:用做限流

- leaderEndpointBlockingSend:这是用于实现同步发送请求的类。所谓的同步发送，是指该线程使用它给指定 Broker 发送请求，然后线程处于阻塞状态，直到接收到 Broker 返回的 Response。

#### 核心参数

- maxWait: Follower 发送的 FETCH 请求被处理返回前的最长等待时间。它是 Broker 端参数 **replica.fetch.wait.max.ms**的值。

- minBytes: 每个 FETCH Response 返回前必须要累积的最少字节数。它是 Broker 端参数 **replica.fetch.min.bytes**的值

- maxBytes：每个合法 FETCH Response 的最大字节数。它是 Broker 端参数 **replica.fetch.response.max.bytes**的值。

- fetchSize: 单个分区能够获取到的最大字节数。它是 Broker 端参数 **replica.fetch.max.bytes**的值

##### processPartitionData 处理返回的同步消息

````

override def processPartitionData(
  topicPartition: TopicPartition,
  fetchOffset: Long,
  partitionData: FetchData): Option[LogAppendInfo] = {
  val logTrace = isTraceEnabled
  // 从副本管理器获取指定主题分区对象
  val partition = replicaMgr.nonOfflinePartition(topicPartition).get
  // 获取日志对象
  val log = partition.localLogOrException
  // 将获取到的数据转换成符合格式要求的消息集合
  val records = toMemoryRecords(partitionData.records)
  maybeWarnIfOversizedRecords(records, topicPartition)
  // 要读取的起始位移值如果不是本地日志LEO值则视为异常情况
  if (fetchOffset != log.logEndOffset)
    throw new IllegalStateException("Offset mismatch for partition %s: fetched offset = %d, log end offset = %d.".format(
      topicPartition, fetchOffset, log.logEndOffset))
  if (logTrace)
    trace("Follower has replica log end offset %d for partition %s. Received %d messages and leader hw %d"
      .format(log.logEndOffset, topicPartition, records.sizeInBytes, partitionData.highWatermark))
  // 写入Follower副本本地日志
  val logAppendInfo = partition.appendRecordsToFollowerOrFutureReplica(records, isFuture = false)
  if (logTrace)
    trace("Follower has replica log end offset %d after appending %d bytes of messages for partition %s"
      .format(log.logEndOffset, records.sizeInBytes, topicPartition))
  val leaderLogStartOffset = partitionData.logStartOffset
  // 更新Follower副本的高水位值
  val followerHighWatermark = 
    log.updateHighWatermark(partitionData.highWatermark)
  // 尝试更新Follower副本的Log Start Offset值
  log.maybeIncrementLogStartOffset(leaderLogStartOffset, LeaderOffsetIncremented)
  if (logTrace)
    trace(s"Follower set replica high watermark for partition $topicPartition to $followerHighWatermark")
  // 副本消息拉取限流
  if (quota.isThrottled(topicPartition))
    quota.record(records.sizeInBytes)
  // 更新统计指标值
  if (partition.isReassigning && partition.isAddingLocalReplica)
    brokerTopicStats.updateReassignmentBytesIn(records.sizeInBytes)
  brokerTopicStats.updateReplicationBytesIn(records.sizeInBytes)
  // 返回日志写入结果
  logAppendInfo
}

`````

[![sDOrNR.md.png](https://s3.ax1x.com/2021/01/16/sDOrNR.md.png)](https://imgchr.com/i/sDOrNR)

##### buildFetch 构建发送给 Leader 副本所在 Broker 的 FETCH 请求

``````

override def buildFetch(
  partitionMap: Map[TopicPartition, PartitionFetchState]): ResultWithPartitions[Option[ReplicaFetch]] = {
  val partitionsWithError = mutable.Set[TopicPartition]()
  val builder = fetchSessionHandler.newBuilder(partitionMap.size, false)
  // 遍历每个分区，将处于可获取状态的分区添加到builder后续统一处理
  // 对于有错误的分区加入到出错分区列表
  partitionMap.foreach { case (topicPartition, fetchState) =>
    if (fetchState.isReadyForFetch && !shouldFollowerThrottle(quota, fetchState, topicPartition)) {
      try {
        val logStartOffset = this.logStartOffset(topicPartition)
        builder.add(topicPartition, new FetchRequest.PartitionData(
          fetchState.fetchOffset, logStartOffset, fetchSize, Optional.of(fetchState.currentLeaderEpoch)))
      } catch {
        case _: KafkaStorageException =>
          partitionsWithError += topicPartition
      }
    }
  }
  val fetchData = builder.build()
  val fetchRequestOpt = if (fetchData.sessionPartitions.isEmpty && fetchData.toForget.isEmpty) {
    None
  } else {
    // 构造FETCH请求的Builder对象
    val requestBuilder = FetchRequest.Builder
      .forReplica(fetchRequestVersion, replicaId, maxWait, minBytes, fetchData.toSend)
      .setMaxBytes(maxBytes)
      .toForget(fetchData.toForget)
      .metadata(fetchData.metadata)
    Some(ReplicaFetch(fetchData.sessionPartitions(), requestBuilder))
  }
  // 返回Builder对象以及出错分区列表
  ResultWithPartitions(fetchRequestOpt, partitionsWithError)
}


``````

[![sDOH8P.png](https://s3.ax1x.com/2021/01/16/sDOH8P.png)](https://imgchr.com/i/sDOH8P)

#### truncate 截断方法

``````

override def truncate(
  tp: TopicPartition, 
  offsetTruncationState: OffsetTruncationState): Unit = {
  // 拿到分区对象
  val partition = replicaMgr.nonOfflinePartition(tp).get
  //拿到分区本地日志 
  val log = partition.localLogOrException
  // 执行截断操作，截断到的位置由offsetTruncationState的offset指定
  partition.truncateTo(offsetTruncationState.offset, isFuture = false)
  if (offsetTruncationState.offset < log.highWatermark)
    warn(s"Truncating $tp to offset ${offsetTruncationState.offset} below high watermark " +
      s"${log.highWatermark}")
  if (offsetTruncationState.truncationCompleted)
    replicaMgr.replicaAlterLogDirsManager
      .markPartitionsForTruncation(brokerConfig.brokerId, tp,
      offsetTruncationState.offset)
}

``````

### replicaManager 副本管理器

#### 需要副本写入的场景

- 生产者向Leader副本写入消息

- Follower副本拉取消息后写入副本

- 消费者组写入消息

- 事务管理器写入事务信息

#### 副本写入方法 appendRecords

所谓的副本写入，是指向副本底层日志写入消息

[![srtejs.md.jpg](https://s3.ax1x.com/2021/01/16/srtejs.md.jpg)](https://imgchr.com/i/srtejs)

````

// requiredAcks合法取值是-1，0，1，否则视为非法
if (isValidRequiredAcks(requiredAcks)) {
  val sTime = time.milliseconds
  // 调用appendToLocalLog方法写入消息集合到本地日志
  val localProduceResults = appendToLocalLog(
    internalTopicsAllowed = internalTopicsAllowed,
    origin, entriesPerPartition, requiredAcks)
  debug("Produce to local log in %d ms".format(time.milliseconds - sTime))
  val produceStatus = localProduceResults.map { case (topicPartition, result) =>
    topicPartition ->
            ProducePartitionStatus(
              result.info.lastOffset + 1, // 设置下一条待写入消息的位移值
              // 构建PartitionResponse封装写入结果
              new PartitionResponse(result.error, result.info.firstOffset.getOrElse(-1), result.info.logAppendTime,
                result.info.logStartOffset, result.info.recordErrors.asJava, result.info.errorMessage))
  }
  // 尝试更新消息格式转换的指标数据
  recordConversionStatsCallback(localProduceResults.map { case (k, v) => k -> v.info.recordConversionStats })
  // 需要等待其他副本完成写入
  if (delayedProduceRequestRequired(
    requiredAcks, entriesPerPartition, localProduceResults)) {
    val produceMetadata = ProduceMetadata(requiredAcks, produceStatus)
    // 创建DelayedProduce延时请求对象
    val delayedProduce = new DelayedProduce(timeout, produceMetadata, this, responseCallback, delayedProduceLock)
    val producerRequestKeys = entriesPerPartition.keys.map(TopicPartitionOperationKey(_)).toSeq
    // 再一次尝试完成该延时请求
    // 如果暂时无法完成，则将对象放入到相应的Purgatory中等待后续处理
    delayedProducePurgatory.tryCompleteElseWatch(delayedProduce, producerRequestKeys)
  } else { // 无需等待其他副本写入完成，可以立即发送Response 
    val produceResponseStatus = produceStatus.map { case (k, status) => k -> status.responseStatus }
    // 调用回调逻辑然后返回即可
    responseCallback(produceResponseStatus)
  }
} else { // 如果requiredAcks值不合法
  val responseStatus = entriesPerPartition.map { case (topicPartition, _) =>
    topicPartition -> new PartitionResponse(Errors.INVALID_REQUIRED_ACKS,
      LogAppendInfo.UnknownLogAppendInfo.firstOffset.getOrElse(-1), RecordBatch.NO_TIMESTAMP, LogAppendInfo.UnknownLogAppendInfo.logStartOffset)
  }
  // 构造INVALID_REQUIRED_ACKS异常并封装进回调函数调用中
  responseCallback(responseStatus)
}

````

#### 副本读取： fetchMessages

在 ReplicaManager 类中，负责读取副本数据的方法是 fetchMessages。不论是 Java 消费者 API，还是 Follower 副本，它们拉取消息的主要途径都是向 Broker 发送 FETCH 请求，Broker
端接收到该请求后，调用 fetchMessages 方法从底层的 Leader 副本取出消息。

````
def fetchMessages(timeout: Long,
replicaId: Int,
fetchMinBytes: Int,
fetchMaxBytes: Int,
hardMaxBytesLimit: Boolean,
fetchInfos: Seq[(TopicPartition, PartitionData)],
quota: ReplicaQuota,
responseCallback: Seq[(TopicPartition, FetchPartitionData)] => Unit,
isolationLevel: IsolationLevel,
clientMetadata: Option[ClientMetadata]): Unit = {
......
}
``````

- timeout：请求处理超时时间。对于消费者而言，该值就是 **request.timeout.ms** 参数值；对于 Follower 副本而言，该值是 Broker 端参数 **replica.fetch.wait.max.ms**
  的值

- replicaId: 副本 ID。对于消费者而言，该参数值是 -1；对于 Follower 副本而言，该值就是 Follower 副本所在的 Broker ID

- fetchMinBytes & fetchMaxBytes:能够获取的最小字节数和最大字节数。对于消费者而言，它们分别对应于 Consumer 端参数 **fetch.min.bytes 和 fetch.max.bytes** 值；对于
  Follower 副本而言，它们分别对应于 Broker 端参数 **replica.fetch.min.bytes 和 replica.fetch.max.bytes**值。

- hardMaxBytesLimit:对能否超过最大字节数做硬限制。如果 hardMaxBytesLimit=True，就表示，读取请求返回的数据字节数绝不允许超过最大字节数。

- fetchInfos: 规定了读取分区的信息，比如要读取哪些分区、从这些分区的哪个位移值开始读、最多可以读多少字节，等等

- quota:这是一个配额控制类，主要是为了判断是否需要在读取的过程中做限速控制

- responseCallback:Response 回调逻辑函数。当请求被处理完成后，调用该方法执行收尾逻辑。

[![srUTns.md.jpg](https://s3.ax1x.com/2021/01/16/srUTns.md.jpg)](https://imgchr.com/i/srUTns)

#### 读取本地日志

````
第一部分：

// 判断该读取请求是否来自于Follower副本或Consumer
val isFromFollower = Request.isValidBrokerId(replicaId)
val isFromConsumer = !(isFromFollower || replicaId == Request.FutureLocalReplicaId)
// 根据请求发送方判断可读取范围
// 如果请求来自于普通消费者，那么可以读到高水位值
// 如果请求来自于配置了READ_COMMITTED的消费者，那么可以读到Log Stable Offset值
// 如果请求来自于Follower副本，那么可以读到LEO值
val fetchIsolation = if (!isFromConsumer)
  FetchLogEnd
else if (isolationLevel == IsolationLevel.READ_COMMITTED)
  FetchTxnCommitted
else
  FetchHighWatermark
val fetchOnlyFromLeader = isFromFollower || (isFromConsumer && clientMetadata.isEmpty)
// 定义readFromLog方法读取底层日志中的消息
def readFromLog(): Seq[(TopicPartition, LogReadResult)] = {
  val result = readFromLocalLog(
    replicaId = replicaId,
    fetchOnlyFromLeader = fetchOnlyFromLeader,
    fetchIsolation = fetchIsolation,
    fetchMaxBytes = fetchMaxBytes,
    hardMaxBytesLimit = hardMaxBytesLimit,
    readPartitionInfo = fetchInfos,
    quota = quota,
    clientMetadata = clientMetadata)
  if (isFromFollower) updateFollowerFetchState(replicaId, result)
  else result
}
// 读取消息并返回日志读取结果
val logReadResults = readFromLog()

第二部分：

var bytesReadable: Long = 0
var errorReadingData = false
val logReadResultMap = new mutable.HashMap[TopicPartition, LogReadResult]
// 统计总共可读取的字节数
logReadResults.foreach { case (topicPartition, logReadResult) =>
 brokerTopicStats.topicStats(topicPartition.topic).totalFetchRequestRate.mark()
  brokerTopicStats.allTopicsStats.totalFetchRequestRate.mark()
  if (logReadResult.error != Errors.NONE)
    errorReadingData = true
  bytesReadable = bytesReadable + logReadResult.info.records.sizeInBytes
  logReadResultMap.put(topicPartition, logReadResult)
}
// 判断是否能够立即返回Reponse，满足以下4个条件中的任意一个即可：
// 1. 请求没有设置超时时间，说明请求方想让请求被处理后立即返回
// 2. 未获取到任何数据
// 3. 已累积到足够多的数据
// 4. 读取过程中出错
if (timeout <= 0 || fetchInfos.isEmpty || bytesReadable >= fetchMinBytes || errorReadingData) {
  // 构建返回结果
  val fetchPartitionData = logReadResults.map { case (tp, result) =>
    tp -> FetchPartitionData(result.error, result.highWatermark, result.leaderLogStartOffset, result.info.records,
      result.lastStableOffset, result.info.abortedTransactions, result.preferredReadReplica, isFromFollower && isAddingReplica(tp, replicaId))
  }
  // 调用回调函数
  responseCallback(fetchPartitionData)
} else { // 如果无法立即完成请求
  val fetchPartitionStatus = new mutable.ArrayBuffer[(TopicPartition, FetchPartitionStatus)]
  fetchInfos.foreach { case (topicPartition, partitionData) =>
    logReadResultMap.get(topicPartition).foreach(logReadResult => {
      val logOffsetMetadata = logReadResult.info.fetchOffsetMetadata
      fetchPartitionStatus += (topicPartition -> FetchPartitionStatus(logOffsetMetadata, partitionData))
    })
  }
  val fetchMetadata: SFetchMetadata = SFetchMetadata(fetchMinBytes, fetchMaxBytes, hardMaxBytesLimit,
    fetchOnlyFromLeader, fetchIsolation, isFromFollower, replicaId, fetchPartitionStatus)
  // 构建DelayedFetch延时请求对象  
  val delayedFetch = new DelayedFetch(timeout, fetchMetadata, this, quota, clientMetadata,
    responseCallback)
  val delayedFetchKeys = fetchPartitionStatus.map { case (tp, _) => TopicPartitionOperationKey(tp) }
  // 再一次尝试完成请求，如果依然不能完成，则交由Purgatory等待后续处理
  delayedFetchPurgatory.tryCompleteElseWatch(delayedFetch, delayedFetchKeys)
}

第三部分：


// 启动高水位检查点专属线程
// 定期将Broker上所有非Offline分区的高水位值写入到检查点文件，默认5秒执行一次
startHighWatermarkCheckPointThread()
// 添加日志路径数据迁移线程
maybeAddLogDirFetchers(partitionStates.keySet, highWatermarkCheckpoints)
// 关闭空闲副本拉取线程
replicaFetcherManager.shutdownIdleFetcherThreads()
// 关闭空闲日志路径数据迁移线程
replicaAlterLogDirsManager.shutdownIdleFetcherThreads()
// 执行Leader变更之后的回调逻辑
onLeadershipChange(partitionsBecomeLeader, partitionsBecomeFollower)
// 构造LeaderAndIsrRequest请求的Response并返回
val responsePartitions = responseMap.iterator.map { case (tp, error) =>
  new LeaderAndIsrPartitionError()
    .setTopicName(tp.topic)
    .setPartitionIndex(tp.partition)
    .setErrorCode(error.code)
}.toBuffer
new LeaderAndIsrResponse(new LeaderAndIsrResponseData()
  .setErrorCode(Errors.NONE.code)
  .setPartitionErrors(responsePartitions.asJava))
````

### becomeLeaderOrFollower->具体处理 LeaderAndIsrRequest 请求的地方，同时也是副本管理器添加分区的地方

[![srrG80.jpg](https://s3.ax1x.com/2021/01/17/srrG80.jpg)](https://imgchr.com/i/srrG80)

````

// 如果LeaderAndIsrRequest携带的Controller Epoch
// 小于当前Controller的Epoch值
if (leaderAndIsrRequest.controllerEpoch < controllerEpoch) {
  stateChangeLogger.warn(s"Ignoring LeaderAndIsr request from controller $controllerId with " +
    s"correlation id $correlationId since its controller epoch ${leaderAndIsrRequest.controllerEpoch} is old. " +
    s"Latest known controller epoch is $controllerEpoch")
  // 说明Controller已经易主，抛出相应异常
  leaderAndIsrRequest.getErrorResponse(0, Errors.STALE_CONTROLLER_EPOCH.exception)
} else {
  val responseMap = new mutable.HashMap[TopicPartition, Errors]
  // 更新当前Controller Epoch值
  controllerEpoch = leaderAndIsrRequest.controllerEpoch
  val partitionStates = new mutable.HashMap[Partition, LeaderAndIsrPartitionState]()
  // 遍历LeaderAndIsrRequest请求中的所有分区
  requestPartitionStates.foreach { partitionState =>
    val topicPartition = new TopicPartition(partitionState.topicName, partitionState.partitionIndex)
    // 从allPartitions中获取对应分区对象
    val partitionOpt = getPartition(topicPartition) match {
      // 如果是Offline状态
      case HostedPartition.Offline =>
        stateChangeLogger.warn(s"Ignoring LeaderAndIsr request from " +
          s"controller $controllerId with correlation id $correlationId " +
          s"epoch $controllerEpoch for partition $topicPartition as the local replica for the " +
          "partition is in an offline log directory")
        // 添加对象异常到Response，并设置分区对象变量partitionOpt=None
        responseMap.put(topicPartition, Errors.KAFKA_STORAGE_ERROR)
        None
      // 如果是Online状态，直接赋值partitionOpt即可
      case HostedPartition.Online(partition) =>
        Some(partition)
      // 如果是None状态，则表示没有找到分区对象
      // 那么创建新的分区对象将，新创建的分区对象加入到allPartitions统一管理
      // 然后赋值partitionOpt字段
      case HostedPartition.None =>
        val partition = Partition(topicPartition, time, this)
        allPartitions.putIfNotExists(topicPartition, HostedPartition.Online(partition))
        Some(partition)
    }
    // 检查分区的Leader Epoch值
    ......
  }



// 确定Broker上副本是哪些分区的Leader副本
val partitionsToBeLeader = partitionStates.filter { case (_, partitionState) =>
  partitionState.leader == localBrokerId
}
// 确定Broker上副本是哪些分区的Follower副本
val partitionsToBeFollower = partitionStates.filter { case (k, _) => !partitionsToBeLeader.contains(k) }

val highWatermarkCheckpoints = new LazyOffsetCheckpoints(this.highWatermarkCheckpoints)
val partitionsBecomeLeader = if (partitionsToBeLeader.nonEmpty)
  // 调用makeLeaders方法为partitionsToBeLeader所有分区
  // 执行"成为Leader副本"的逻辑
  makeLeaders(controllerId, controllerEpoch, partitionsToBeLeader, correlationId, responseMap,
    highWatermarkCheckpoints)
else
  Set.empty[Partition]
val partitionsBecomeFollower = if (partitionsToBeFollower.nonEmpty)
  // 调用makeFollowers方法为令partitionsToBeFollower所有分区
  // 执行"成为Follower副本"的逻辑
  makeFollowers(controllerId, controllerEpoch, partitionsToBeFollower, correlationId, responseMap,
    highWatermarkCheckpoints)
else
  Set.empty[Partition]
val leaderTopicSet = leaderPartitionsIterator.map(_.topic).toSet
val followerTopicSet = partitionsBecomeFollower.map(_.topic).toSet
// 对于当前Broker成为Follower副本的主题
// 移除它们之前的Leader副本监控指标
followerTopicSet.diff(leaderTopicSet).foreach(brokerTopicStats.removeOldLeaderMetrics)
// 对于当前Broker成为Leader副本的主题
// 移除它们之前的Follower副本监控指
leaderTopicSet.diff(followerTopicSet).foreach(brokerTopicStats.removeOldFollowerMetrics)
// 如果有分区的本地日志为空，说明底层的日志路径不可用
// 标记该分区为Offline状态
leaderAndIsrRequest.partitionStates.forEach { partitionState =>
  val topicPartition = new TopicPartition(partitionState.topicName, partitionState.partitionIndex)
  if (localLog(topicPartition).isEmpty)
    markPartitionOffline(topicPartition)
}
````

#### makeLeaders 方法

makeLeaders 方法的作用是，让当前 Broker 成为给定一组分区的 Leader，也就是让当前 Broker 下该分区的副本成为 Leader 副本

- 停掉这些分区对应的获取线程
- 更新broker缓存中的分区元数据信息
- 将指定分区添加到Leader分区集合

[![sr5Z24.md.png](https://s3.ax1x.com/2021/01/17/sr5Z24.md.png)](https://imgchr.com/i/sr5Z24)

1. 将给定的一组分区的状态全部初始化成 Errors.None
2. 停止为这些分区服务的所有拉取线程。毕竟该 Broker 现在是这些分区的 Leader 副本了，不再是 Follower 副本了，所以没有必要再使用拉取线程了
3. makeLeaders 方法调用 Partition 的 makeLeader 方法，去更新给定一组分区的 Leader 分区信息，而这些是由 Partition 类中的 makeLeader 方法完成的。该方法保存分区的
   Leader 和 ISR 信息，同时创建必要的日志对象、重设远端 Follower 副本的 LEO 值。

#### 远端副本

是指保存在 Leader 副本本地内存中的一组 Follower 副本集合，在代码中用字段 remoteReplicas 来表征；ReplicaManager 在处理 FETCH 请求时，会更新 remoteReplicas 中副本对象的
LEO 值。同时，Leader 副本会将自己更新后的 LEO 值与 remoteReplicas 中副本的 LEO 值进行比较，来决定是否“抬高”高水位值

#### makeFollowers

- 更新Controller Epoch值
- 保存副本列表（Assigned Replicas,AR）和清空ISR
- 创建日志对象
- 重设Leader副本的Broker ID

````

// 第一部分：遍历partitionStates所有分区
......
partitionStates.foreach { case (partition, partitionState) =>
  ......
  // 将所有分区的处理结果的状态初始化为Errors.NONE
  responseMap.put(partition.topicPartition, Errors.NONE)
}
val partitionsToMakeFollower: mutable.Set[Partition] = mutable.Set()
try {
  // 遍历partitionStates所有分区
  partitionStates.foreach { case (partition, partitionState) =>
    // 拿到分区的Leader Broker ID
    val newLeaderBrokerId = partitionState.leader
    try {
      // 在元数据缓存中找到Leader Broke对象
      metadataCache.getAliveBrokers.find(_.id == newLeaderBrokerId) match {
        // 如果Leader确实存在
        case Some(_) =>
          // 执行makeFollower方法，将当前Broker配置成该分区的Follower副本
          if (partition.makeFollower(partitionState, highWatermarkCheckpoints))
            // 如果配置成功，将该分区加入到结果返回集中
            partitionsToMakeFollower += partition
          else // 如果失败，打印错误日志
            ......
        // 如果Leader不存在
        case None =>
          ......
          // 依然创建出分区Follower副本的日志对象
          partition.createLogIfNotExists(isNew = partitionState.isNew, isFutureReplica = false,
            highWatermarkCheckpoints)
      }
    } catch {
      case e: KafkaStorageException =>
        ......
    }
  }

第二部分：

// 第二部分：执行其他动作
// 移除现有Fetcher线程
replicaFetcherManager.removeFetcherForPartitions(
  partitionsToMakeFollower.map(_.topicPartition))
......
// 尝试完成延迟请求
partitionsToMakeFollower.foreach { partition =>
  completeDelayedFetchOrProduceRequests(partition.topicPartition)
}
if (isShuttingDown.get()) {
  .....
} else {
  // 为需要将当前Broker设置为Follower副本的分区
  // 确定Leader Broker和起始读取位移值fetchOffset
  val partitionsToMakeFollowerWithLeaderAndOffset = partitionsToMakeFollower.map { partition =>
  val leader = metadataCache.getAliveBrokers
    .find(_.id == partition.leaderReplicaIdOpt.get).get
    .brokerEndPoint(config.interBrokerListenerName)
  val fetchOffset = partition.localLogOrException.highWatermark
    partition.topicPartition -> InitialFetchState(leader, 
      partition.getLeaderEpoch, fetchOffset)
  }.toMap
  // 使用上一步确定的Leader Broker和fetchOffset添加新的Fetcher线程
  replicaFetcherManager.addFetcherForPartitions(
    partitionsToMakeFollowerWithLeaderAndOffset)
  }
} catch {
  case e: Throwable =>
    ......
    throw e
}
......
// 返回需要将当前Broker设置为Follower副本的分区列表
partitionsToMakeFollower
````

#### ISR管理

- 一个是 maybeShrinkIsr 方法，作用是阶段性地查看 ISR 中的副本集合是否需要收缩

- 另一个是 maybePropagateIsrChanges 方法，作用是定期向集群 Broker 传播 ISR 的变更。

##### maybeShrinkIsr 方法

收缩是指，把 ISR 副本集合中那些与 Leader 差距过大的副本移除的过程。所谓的差距过大，就是 ISR 中 Follower 副本滞后 Leader 副本的时间，超过了 Broker 端参数 **
replica.lag.time.max.ms**值的 1.5 倍

[![srINfU.md.jpg](https://s3.ax1x.com/2021/01/17/srINfU.md.jpg)](https://imgchr.com/i/srINfU)

````

def maybeShrinkIsr(): Unit = {
  // 第一步：判断是否需要执行ISR收缩，调用 needShrinkIsr 方法来获取与 Leader 不同步的副本。如果存在这样的副本，说明需要执行 ISR 收缩
  val needsIsrUpdate = inReadLock(leaderIsrUpdateLock) {
    needsShrinkIsr()
  }
  val leaderHWIncremented = needsIsrUpdate && inWriteLock(leaderIsrUpdateLock) {
    leaderLogIfLocal match {
      // 如果是Leader副本
      case Some(leaderLog) =>
        // 第二步：获取不同步的副本Id列表，再次获取与 Leader 不同步的副本列表，并把它们从当前 ISR 中剔除出去，然后计算得出最新的 ISR 列表
        val outOfSyncReplicaIds = getOutOfSyncReplicas(replicaLagTimeMaxMs)
        // 如果存在不同步的副本Id列表
        if (outOfSyncReplicaIds.nonEmpty) {
          // 计算收缩之后的ISR列表
          val newInSyncReplicaIds = inSyncReplicaIds -- outOfSyncReplicaIds
          assert(newInSyncReplicaIds.nonEmpty)
          info("Shrinking ISR from %s to %s. Leader: (highWatermark: %d, endOffset: %d). Out of sync replicas: %s."
            .format(inSyncReplicaIds.mkString(","),
              newInSyncReplicaIds.mkString(","),
              leaderLog.highWatermark,
              leaderLog.logEndOffset,
              outOfSyncReplicaIds.map { replicaId =>
                s"(brokerId: $replicaId, endOffset: ${getReplicaOrException(replicaId).logEndOffset})"
              }.mkString(" ")
            )
          )
          // 第三步:更新ZooKeeper中分区的ISR数据以及Broker的元数据缓存中的数据
          shrinkIsr(newInSyncReplicaIds)
          // 第四步: 尝试更新Leader副本的高水位值
          maybeIncrementLeaderHW(leaderLog)
        } else {
          false
        }
      // 如果不是Leader副本，什么都不做
      case None => false
    }
  }
  // 如果Leader副本的高水位值抬升了
  if (leaderHWIncremented)
    // 第五步: 尝试解锁一下延迟请求
    tryCompleteDelayedRequests()
}

````

##### maybePropagateIsrChanges 方法

确定 ISR 变更传播的条件。这里需要同时满足两点

1. 存在尚未被传播的 ISR 变更

2. 最近 5 秒没有任何 ISR 变更，或者自上次 ISR 变更已经有超过 1 分钟的时间

一旦满足了这两个条件，代码会创建 ZooKeeper 相应的 Znode 节点；然后，清空 isrChangeSet 集合；最后，更新最近 ISR 变更时间戳

````

def maybePropagateIsrChanges(): Unit = {
  val now = System.currentTimeMillis()
  isrChangeSet synchronized {
    // ISR变更传播的条件，需要同时满足：
    // 1. 存在尚未被传播的ISR变更
    // 2. 最近5秒没有任何ISR变更，或者自上次ISR变更已经有超过1分钟的时间
    if (isrChangeSet.nonEmpty &&
      (lastIsrChangeMs.get() + ReplicaManager.IsrChangePropagationBlackOut < now ||
        lastIsrPropagationMs.get() + ReplicaManager.IsrChangePropagationInterval < now)) {
      // 创建ZooKeeper相应的Znode节点
      zkClient.propagateIsrChanges(isrChangeSet)
      // 清空isrChangeSet集合
      isrChangeSet.clear()
      // 更新最近ISR变更时间戳
      lastIsrPropagationMs.set(now)
    }
  }
}

````
