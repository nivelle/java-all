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