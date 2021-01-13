### kafkaApis

[![sYoekn.jpg](https://s3.ax1x.com/2021/01/12/sYoekn.jpg)](https://imgchr.com/i/sYoekn)

#### 定义类

````

class KafkaApis(
  val requestChannel: RequestChannel, // 请求通道
  val replicaManager: ReplicaManager, // 副本管理器
  val adminManager: AdminManager,   // 主题、分区、配置等方面的管理器
    val groupCoordinator: GroupCoordinator,  // 消费者组协调器组件
  val txnCoordinator: TransactionCoordinator,  // 事务管理器组件
  val controller: KafkaController,  // 控制器组件
  val zkClient: KafkaZkClient,    // ZooKeeper客户端程序，Kafka依赖于该类实现与ZooKeeper交互
  val brokerId: Int,          // broker.id参数值
    val config: KafkaConfig,      // Kafka配置类
    val metadataCache: MetadataCache,  // 元数据缓存类
    val metrics: Metrics,      
  val authorizer: Option[Authorizer],
  val quotas: QuotaManagers,          // 配额管理器组件
  val fetchManager: FetchManager,
  brokerTopicStats: BrokerTopicStats,
  val clusterId: String,
  time: Time,
  val tokenManager: DelegationTokenManager) extends Logging {
  type FetchResponseStats = Map[TopicPartition, RecordConversionStats]
  this.logIdent = "[KafkaApi-%d] ".format(brokerId)
  val adminZkClient = new AdminZkClient(zkClient)
  private val alterAclsPurgatory = new DelayedFuturePurgatory(purgatoryName = "AlterAcls", brokerId = config.brokerId)
  ......
}

````

#### 入口类

````

def handle(request: RequestChannel.Request): Unit = {
  try {
    trace(s"Handling request:${request.requestDesc(true)} from connection ${request.context.connectionId};" +
 s"securityProtocol:${request.context.securityProtocol},principal:${request.context.principal}")
    // 根据请求头部信息中的apiKey字段判断属于哪类请求
    // 然后调用响应的handle***方法
    // 如果新增RPC协议类型，则：
    // 1. 添加新的apiKey标识新请求类型
    // 2. 添加新的case分支
    // 3. 添加对应的handle***方法   
    request.header.apiKey match {
      case ApiKeys.PRODUCE => handleProduceRequest(request)
      case ApiKeys.FETCH => handleFetchRequest(request)
      case ApiKeys.LIST_OFFSETS => handleListOffsetRequest(request)
      case ApiKeys.METADATA => handleTopicMetadataRequest(request)
      case ApiKeys.LEADER_AND_ISR => handleLeaderAndIsrRequest(request)
      case ApiKeys.STOP_REPLICA => handleStopReplicaRequest(request)
      case ApiKeys.UPDATE_METADATA => handleUpdateMetadataRequest(request)
      case ApiKeys.CONTROLLED_SHUTDOWN => handleControlledShutdownRequest(request)
      case ApiKeys.OFFSET_COMMIT => handleOffsetCommitRequest(request)
      case ApiKeys.OFFSET_FETCH => handleOffsetFetchRequest(request)
      case ApiKeys.FIND_COORDINATOR => handleFindCoordinatorRequest(request)
      case ApiKeys.JOIN_GROUP => handleJoinGroupRequest(request)
      case ApiKeys.HEARTBEAT => handleHeartbeatRequest(request)
      case ApiKeys.LEAVE_GROUP => handleLeaveGroupRequest(request)
      case ApiKeys.SYNC_GROUP => handleSyncGroupRequest(request)
      case ApiKeys.DESCRIBE_GROUPS => handleDescribeGroupRequest(request)
      case ApiKeys.LIST_GROUPS => handleListGroupsRequest(request)
      case ApiKeys.SASL_HANDSHAKE => handleSaslHandshakeRequest(request)
      case ApiKeys.API_VERSIONS => handleApiVersionsRequest(request)
      case ApiKeys.CREATE_TOPICS => handleCreateTopicsRequest(request)
      case ApiKeys.DELETE_TOPICS => handleDeleteTopicsRequest(request)
      case ApiKeys.DELETE_RECORDS => handleDeleteRecordsRequest(request)
      case ApiKeys.INIT_PRODUCER_ID => handleInitProducerIdRequest(request)
      case ApiKeys.OFFSET_FOR_LEADER_EPOCH => handleOffsetForLeaderEpochRequest(request)
      case ApiKeys.ADD_PARTITIONS_TO_TXN => handleAddPartitionToTxnRequest(request)
      case ApiKeys.ADD_OFFSETS_TO_TXN => handleAddOffsetsToTxnRequest(request)
      case ApiKeys.END_TXN => handleEndTxnRequest(request)
      case ApiKeys.WRITE_TXN_MARKERS => handleWriteTxnMarkersRequest(request)
      case ApiKeys.TXN_OFFSET_COMMIT => handleTxnOffsetCommitRequest(request)
      case ApiKeys.DESCRIBE_ACLS => handleDescribeAcls(request)
      case ApiKeys.CREATE_ACLS => handleCreateAcls(request)
      case ApiKeys.DELETE_ACLS => handleDeleteAcls(request)
      case ApiKeys.ALTER_CONFIGS => handleAlterConfigsRequest(request)
      case ApiKeys.DESCRIBE_CONFIGS => handleDescribeConfigsRequest(request)
      case ApiKeys.ALTER_REPLICA_LOG_DIRS => handleAlterReplicaLogDirsRequest(request)
      case ApiKeys.DESCRIBE_LOG_DIRS => handleDescribeLogDirsRequest(request)
      case ApiKeys.SASL_AUTHENTICATE => handleSaslAuthenticateRequest(request)
      case ApiKeys.CREATE_PARTITIONS => handleCreatePartitionsRequest(request)
      case ApiKeys.CREATE_DELEGATION_TOKEN => handleCreateTokenRequest(request)
      case ApiKeys.RENEW_DELEGATION_TOKEN => handleRenewTokenRequest(request)
      case ApiKeys.EXPIRE_DELEGATION_TOKEN => handleExpireTokenRequest(request)
      case ApiKeys.DESCRIBE_DELEGATION_TOKEN => handleDescribeTokensRequest(request)
      case ApiKeys.DELETE_GROUPS => handleDeleteGroupsRequest(request)
      case ApiKeys.ELECT_LEADERS => handleElectReplicaLeader(request)
      case ApiKeys.INCREMENTAL_ALTER_CONFIGS => handleIncrementalAlterConfigsRequest(request)
      case ApiKeys.ALTER_PARTITION_REASSIGNMENTS => handleAlterPartitionReassignmentsRequest(request)
      case ApiKeys.LIST_PARTITION_REASSIGNMENTS => handleListPartitionReassignmentsRequest(request)
      case ApiKeys.OFFSET_DELETE => handleOffsetDeleteRequest(request)
      case ApiKeys.DESCRIBE_CLIENT_QUOTAS => handleDescribeClientQuotasRequest(request)
      case ApiKeys.ALTER_CLIENT_QUOTAS => handleAlterClientQuotasRequest(request)
    }
  } catch {
    // 如果是严重错误，则抛出异常
    case e: FatalExitError => throw e
    // 普通异常的话，记录下错误日志
    case e: Throwable => handleError(request, e)
  } finally {
    // 记录一下请求本地完成时间，即Broker处理完该请求的时间
    if (request.apiLocalCompleteTimeNanos < 0)
      request.apiLocalCompleteTimeNanos = time.nanoseconds
  }
}



````

