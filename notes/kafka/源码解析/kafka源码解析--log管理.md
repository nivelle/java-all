### 高水位管理

````

@volatile private var highWatermarkMetadata: LogOffsetMetadata = LogOffsetMetadata(logStartOffset)

````

1. 高水位值是 volatile 的。 保证多个线程同时读取，保证了内存可见性。 为保证多个线程同时修改，使用Java Monitor 来保证并发修改线程的安全。 例如多个flower 同时拉取LEO 时，会同时更新分区的HW

2. 高水位值的初始化值是 Log start offset

- LogOffsetMetadata() 方法

````
case class LogOffsetMetadata(messageOffset: Long,segmentBaseOffset: Long = Log.UnknownOffset, relativePositionInSegment: Int = LogOffsetMetadata.UnknownFilePosition)
````
````
case class LogOffsetMetadata(messageOffset: Long,segmentBaseOffset: Long = Log.UnknownOffset,relativePositionInSegment: Int = LogOffsetMetadata.UnknownFilePosition)

````

- messageOffset:消息位移值，高水位的变量值

- segmentBaseOffset:保存该位移值所在日志段的起始位移。用于辅助计算在同一个segment 的两条消息在物理磁盘文件中的位移差值。

- relativePositionInSegment:保存该位移值所在日志段的物理磁盘位置。

#### 更新高水位值

````
private def updateHighWatermarkMetadata(newHighWatermark: LogOffsetMetadata): Unit = {
    if (newHighWatermark.messageOffset < 0) //高水位值不能是负数
      throw new IllegalArgumentException("High watermark offset should be non-negative")

    lock synchronized { //保护Log 对象修改的Monitor锁
      highWatermarkMetadata = newHighWatermark //赋值新的高水位值
      producerStateManager.onHighWatermarkUpdated(newHighWatermark.messageOffset)//处理事务状态管理器的高水位值更新逻辑
      maybeIncrementFirstUnstableOffset()//first unstable offset 是Kafka事务机制的一部分
    }
    trace(s"Setting high watermark $newHighWatermark")
  }

def updateHighWatermark(hw: Long): Long = {
    //新高水位值介于[Log Start Offset, Log End Offset]之间
    val newHighWatermark = if (hw < logStartOffset)
      logStartOffset
    else if (hw > logEndOffset)
      logEndOffset
    else
      hw
    //调用setter方法来更新高水位值
    updateHighWatermarkMetadata(LogOffsetMetadata(newHighWatermark))
    newHighWatermark //返回高水位值
  }
  
def maybeIncrementHighWatermark(newHighWatermark: LogOffsetMetadata): Option[LogOffsetMetadata] = {
    //新高水位值不能越过Log End Offset
    if (newHighWatermark.messageOffset > logEndOffset)
      throw new IllegalArgumentException(s"High watermark $newHighWatermark update exceeds current " +
        s"log end offset $logEndOffsetMetadata")

    lock.synchronized {
      //获取老的高水位值
      val oldHighWatermark = fetchHighWatermarkMetadata

      // Ensure that the high watermark increases monotonically. We also update the high watermark when the new
      // offset metadata is on a newer segment, which occurs whenever the log is rolled to a new segment.
      //新高水位值要比老高水位值大以维持单调增加特性，否则就不做更新！另外，如果新高水位值在新日志段上，也可执行更新高水位操作
      if (oldHighWatermark.messageOffset < newHighWatermark.messageOffset ||
        (oldHighWatermark.messageOffset == newHighWatermark.messageOffset && oldHighWatermark.onOlderSegment(newHighWatermark))) {
        updateHighWatermarkMetadata(newHighWatermark)
        Some(oldHighWatermark)
      } else {
        None
      }
    }
  }
````

- follower 副本拉取Leader副本的消息后必须更新高水位

- Producer 端向Leader副本写入消息时，分区的高水位值就可能不需要更新，需要等到其他Follower副本同步的进度

#### 读取高水位值

- 不仅获取高水位值，还要获取高水位的其他元数据信息，即日志段起始位移和物理位置信息

````

private def fetchHighWatermarkMetadata: LogOffsetMetadata = {
    checkIfMemoryMappedBufferClosed() // 读取时确保日志不能被关闭

    val offsetMetadata = highWatermarkMetadata // 保存当前高水位值到本地变量，避免多线程访问干扰
    if (offsetMetadata.messageOffsetOnly) { //没有获得到完整的高水位元数据
      lock.synchronized {
        val fullOffset = convertToOffsetMetadataOrThrow(highWatermark) // 通过读日志文件的方式把完整的高水位元数据信息拉出来
        updateHighWatermarkMetadata(fullOffset) // 然后再更新一下高水位对象
        fullOffset
      }
    } else { // 否则，直接返回即可
      offsetMetadata
    }
  }


````

### 日志段管理
````
private val segments: ConcurrentNavigableMap[java.lang.Long, LogSegment] = new ConcurrentSkipListMap[java.lang.Long, LogSegment]

````
- 使用java 的 concurrentSkipListMap 类来保存所有日志段对象。【1. 线程安全 2. 它是键值（key）可排序的Map】

#### 基于留存策略决定哪些日志可以删除

- deleteOldSegments : 使用传入的函数计算哪些日志段对象能够删除，使用deleteSegments方法删除日志段

- deletableSegments: 

````

private def deletableSegments(predicate: (LogSegment, Option[LogSegment]) => Boolean): Iterable[LogSegment] = {
    if (segments.isEmpty) { // 如果当前压根就没有任何日志段对象，直接返回
      Seq.empty
    } else {
      val deletable = ArrayBuffer.empty[LogSegment]
    var segmentEntry = segments.firstEntry
  
    // 从具有最小起始位移值的日志段对象开始遍历，直到满足以下条件之一便停止遍历：
    // 1. 测定条件函数predicate = false
    // 2. 扫描到包含Log对象高水位值所在的日志段对象
    // 3. 最新的日志段对象不包含任何消息
    // 最新日志段对象是segments中Key值最大对应的那个日志段，也就是我们常说的Active Segment。
    // 完全为空的Active Segment如果被允许删除，后面还要重建它，故代码这里不允许删除大小为空的Active Segment。
    // 在遍历过程中，同时不满足以上3个条件的所有日志段都是可以被删除的！
  
      while (segmentEntry != null) {
        val segment = segmentEntry.getValue
        val nextSegmentEntry = segments.higherEntry(segmentEntry.getKey)
        val (nextSegment, upperBoundOffset, isLastSegmentAndEmpty) = 
          if (nextSegmentEntry != null)
            (nextSegmentEntry.getValue, nextSegmentEntry.getValue.baseOffset, false)
          else
            (null, logEndOffset, segment.size == 0)

        if (highWatermark >= upperBoundOffset && predicate(segment, Option(nextSegment)) && !isLastSegmentAndEmpty) {
          deletable += segment
          segmentEntry = nextSegmentEntry
        } else {
          segmentEntry = null
        }
      }
      deletable
    }
  }
````
- deleteSegments: 真正的日志段删除操作

````

private def deleteSegments(deletable: Iterable[LogSegment]): Int = {
    maybeHandleIOException(s"Error while deleting segments for $topicPartition in dir ${dir.getParent}") {
      val numToDelete = deletable.size
      if (numToDelete > 0) {
        // 不允许删除所有日志段对象。如果一定要做，先创建出一个新的来，然后再把前面N个删掉
        if (segments.size == numToDelete)
          roll()
        lock synchronized {
          checkIfMemoryMappedBufferClosed() // 确保Log对象没有被关闭
          // 删除给定的日志段对象以及底层的物理文件
          removeAndDeleteSegments(deletable, asyncDelete = true)
          // 尝试更新日志的Log Start Offset值 : Log Start Offset 是整个Log 对象对外可见消息的最小位移，如果删除了日志段对象，很可能对外可见消息范围发生了变化。
          maybeIncrementLogStartOffset(segments.firstEntry.getValue.baseOffset)
        }
      }
      numToDelete
    }
  }

````

### 关键位移值管理

[![sQ2wqg.jpg](https://s3.ax1x.com/2021/01/09/sQ2wqg.jpg)](https://imgchr.com/i/sQ2wqg)

#### LEO : 下一条待插入消息，也就是 LEO是空位，还没有消息，对应的对象是-> nextOffsetMetadata

#####  LEO 更新时机

- Log对象初始化时
- 写入消息时
- Log对象发生日志切分（Log Roll）时，当前 日志段满了，log对象切换到了新的active segment
- 日志截断（log truncation）时

#### Log Start Offset

##### 更新时机

- Log 对象初始化时： 一般是将第一个日子段的起始位移值赋值给它

- 日志截断时

- Follower 副本同步时：一旦leader副本的Log Start Offset 发生变化，Follower 也需要更新该值

- 删除日志段对象时

- 删除消息时：删除消息就是通过抬高Log Start Offset值实现的，删除消息时必须更新该值


### 读写操作

#### 写操作

- appendAsLeader

- appendAsFollower

- append
  
[![sQ4uHU.jpg](https://s3.ax1x.com/2021/01/09/sQ4uHU.jpg)](https://imgchr.com/i/sQ4uHU)

````
private def append(records: MemoryRecords,
origin: AppendOrigin,
interBrokerProtocolVersion: ApiVersion,
assignOffsets: Boolean,
leaderEpoch: Int): LogAppendInfo = {
maybeHandleIOException(s"Error while appending records to $topicPartition in dir ${dir.getParent}") {
// 第1步：分析和验证待写入消息集合，并返回校验结果
val appendInfo = analyzeAndValidateRecords(records, origin)

      // 如果压根就不需要写入任何消息，直接返回即可
      if (appendInfo.shallowCount == 0)
        return appendInfo

      // 第2步：消息格式规整，即删除无效格式消息或无效字节
      var validRecords = trimInvalidBytes(records, appendInfo)

      lock synchronized {
        checkIfMemoryMappedBufferClosed() // 确保Log对象未关闭
        if (assignOffsets) { // 需要分配位移
          // 第3步：使用当前LEO值作为待写入消息集合中第一条消息的位移值
          val offset = new LongRef(nextOffsetMetadata.messageOffset)
          appendInfo.firstOffset = Some(offset.value)
          val now = time.milliseconds
          val validateAndOffsetAssignResult = try {
            LogValidator.validateMessagesAndAssignOffsets(validRecords,
              topicPartition,
              offset,
              time,
              now,
              appendInfo.sourceCodec,
              appendInfo.targetCodec,
              config.compact,
              config.messageFormatVersion.recordVersion.value,
              config.messageTimestampType,
              config.messageTimestampDifferenceMaxMs,
              leaderEpoch,
              origin,
              interBrokerProtocolVersion,
              brokerTopicStats)
          } catch {
            case e: IOException =>
              throw new KafkaException(s"Error validating messages while appending to log $name", e)
          }
          // 更新校验结果对象类LogAppendInfo
          validRecords = validateAndOffsetAssignResult.validatedRecords
          appendInfo.maxTimestamp = validateAndOffsetAssignResult.maxTimestamp
          appendInfo.offsetOfMaxTimestamp = validateAndOffsetAssignResult.shallowOffsetOfMaxTimestamp
          appendInfo.lastOffset = offset.value - 1
          appendInfo.recordConversionStats = validateAndOffsetAssignResult.recordConversionStats
          if (config.messageTimestampType == TimestampType.LOG_APPEND_TIME)
            appendInfo.logAppendTime = now

          // 第4步：验证消息，确保消息大小不超限
          if (validateAndOffsetAssignResult.messageSizeMaybeChanged) {
            for (batch <- validRecords.batches.asScala) {
              if (batch.sizeInBytes > config.maxMessageSize) {
                // we record the original message set size instead of the trimmed size
                // to be consistent with pre-compression bytesRejectedRate recording
                brokerTopicStats.topicStats(topicPartition.topic).bytesRejectedRate.mark(records.sizeInBytes)
                brokerTopicStats.allTopicsStats.bytesRejectedRate.mark(records.sizeInBytes)
                throw new RecordTooLargeException(s"Message batch size is ${batch.sizeInBytes} bytes in append to" +
                  s"partition $topicPartition which exceeds the maximum configured size of ${config.maxMessageSize}.")
              }
            }
          }
        } else {  // 直接使用给定的位移值，无需自己分配位移值
          if (!appendInfo.offsetsMonotonic) // 确保消息位移值的单调递增性
            throw new OffsetsOutOfOrderException(s"Out of order offsets found in append to $topicPartition: " +
                                                 records.records.asScala.map(_.offset))

          if (appendInfo.firstOrLastOffsetOfFirstBatch < nextOffsetMetadata.messageOffset) {
            val firstOffset = appendInfo.firstOffset match {
              case Some(offset) => offset
              case None => records.batches.asScala.head.baseOffset()
            }

            val firstOrLast = if (appendInfo.firstOffset.isDefined) "First offset" else "Last offset of the first batch"
            throw new UnexpectedAppendOffsetException(
              s"Unexpected offset in append to $topicPartition. $firstOrLast " +
              s"${appendInfo.firstOrLastOffsetOfFirstBatch} is less than the next offset ${nextOffsetMetadata.messageOffset}. " +
              s"First 10 offsets in append: ${records.records.asScala.take(10).map(_.offset)}, last offset in" +
              s" append: ${appendInfo.lastOffset}. Log start offset = $logStartOffset",
              firstOffset, appendInfo.lastOffset)
          }
        }

        // 第5步：更新Leader Epoch缓存
        validRecords.batches.asScala.foreach { batch =>
          if (batch.magic >= RecordBatch.MAGIC_VALUE_V2) {
            maybeAssignEpochStartOffset(batch.partitionLeaderEpoch, batch.baseOffset)
          } else {
            leaderEpochCache.filter(_.nonEmpty).foreach { cache =>
              warn(s"Clearing leader epoch cache after unexpected append with message format v${batch.magic}")
              cache.clearAndFlush()
            }
          }
        }

        // 第6步：确保消息大小不超限
        if (validRecords.sizeInBytes > config.segmentSize) {
          throw new RecordBatchTooLargeException(s"Message batch size is ${validRecords.sizeInBytes} bytes in append " +
            s"to partition $topicPartition, which exceeds the maximum configured segment size of ${config.segmentSize}.")
        }

        // 第7步：执行日志切分。当前日志段剩余容量可能无法容纳新消息集合，因此有必要创建一个新的日志段来保存待写入的所有消息
        val segment = maybeRoll(validRecords.sizeInBytes, appendInfo)

        val logOffsetMetadata = LogOffsetMetadata(
          messageOffset = appendInfo.firstOrLastOffsetOfFirstBatch,
          segmentBaseOffset = segment.baseOffset,
          relativePositionInSegment = segment.size)

        // 第8步：验证事务状态
        val (updatedProducers, completedTxns, maybeDuplicate) = analyzeAndValidateProducerState(
          logOffsetMetadata, validRecords, origin)

        maybeDuplicate.foreach { duplicate =>
          appendInfo.firstOffset = Some(duplicate.firstOffset)
          appendInfo.lastOffset = duplicate.lastOffset
          appendInfo.logAppendTime = duplicate.timestamp
          appendInfo.logStartOffset = logStartOffset
          return appendInfo
        }

        // 第9步：执行真正的消息写入操作，主要调用日志段对象的append方法实现
        segment.append(largestOffset = appendInfo.lastOffset,
          largestTimestamp = appendInfo.maxTimestamp,
          shallowOffsetOfMaxTimestamp = appendInfo.offsetOfMaxTimestamp,
          records = validRecords)

        // 第10步：更新LEO对象，其中，LEO值是消息集合中最后一条消息位移值+1
       // 前面说过，LEO值永远指向下一条不存在的消息
        updateLogEndOffset(appendInfo.lastOffset + 1)

        // 第11步：更新事务状态
        for (producerAppendInfo <- updatedProducers.values) {
          producerStateManager.update(producerAppendInfo)
        }

        for (completedTxn <- completedTxns) {
          val lastStableOffset = producerStateManager.lastStableOffset(completedTxn)
          segment.updateTxnIndex(completedTxn, lastStableOffset)
          producerStateManager.completeTxn(completedTxn)
        }

        producerStateManager.updateMapEndOffset(appendInfo.lastOffset + 1)
       maybeIncrementFirstUnstableOffset()

        trace(s"Appended message set with last offset: ${appendInfo.lastOffset}, " +
          s"first offset: ${appendInfo.firstOffset}, " +
          s"next offset: ${nextOffsetMetadata.messageOffset}, " +
          s"and messages: $validRecords")

        // 是否需要手动落盘。一般情况下我们不需要设置Broker端参数log.flush.interval.messages
       // 落盘操作交由操作系统来完成。但某些情况下，可以设置该参数来确保高可靠性
        if (unflushedMessages >= config.flushInterval)
          flush()

        // 第12步：返回写入结果
        appendInfo
      }
    }
}
````

#### 构建 LogAppendInfo对象

````

private def analyzeAndValidateRecords(records: MemoryRecords, origin: AppendOrigin): LogAppendInfo = {
    var shallowMessageCount = 0
    var validBytesCount = 0
    var firstOffset: Option[Long] = None
    var lastOffset = -1L
    var sourceCodec: CompressionCodec = NoCompressionCodec
    var monotonic = true
    var maxTimestamp = RecordBatch.NO_TIMESTAMP
    var offsetOfMaxTimestamp = -1L
    var readFirstMessage = false
    var lastOffsetOfFirstBatch = -1L

    for (batch <- records.batches.asScala) {
      // 消息格式Version 2的消息批次，起始位移值必须从0开始
      if (batch.magic >= RecordBatch.MAGIC_VALUE_V2 && origin == AppendOrigin.Client && batch.baseOffset != 0)
        throw new InvalidRecordException(s"The baseOffset of the record batch in the append to $topicPartition should " +
          s"be 0, but it is ${batch.baseOffset}")

      if (!readFirstMessage) {
        if (batch.magic >= RecordBatch.MAGIC_VALUE_V2)
          firstOffset = Some(batch.baseOffset)  // 更新firstOffset字段
        lastOffsetOfFirstBatch = batch.lastOffset // 更新lastOffsetOfFirstBatch字段
        readFirstMessage = true
      }

      // 一旦出现当前lastOffset不小于下一个batch的lastOffset，说明上一个batch中有消息的位移值大于后面batch的消息
      // 这违反了位移值单调递增性
      if (lastOffset >= batch.lastOffset)
        monotonic = false

      // 使用当前batch最后一条消息的位移值去更新lastOffset
      lastOffset = batch.lastOffset

      // 检查消息批次总字节数大小是否超限，即是否大于Broker端参数**max.message.bytes**值
      val batchSize = batch.sizeInBytes
      if (batchSize > config.maxMessageSize) {
        brokerTopicStats.topicStats(topicPartition.topic).bytesRejectedRate.mark(records.sizeInBytes)
        brokerTopicStats.allTopicsStats.bytesRejectedRate.mark(records.sizeInBytes)
        throw new RecordTooLargeException(s"The record batch size in the append to $topicPartition is $batchSize bytes " +
          s"which exceeds the maximum configured value of ${config.maxMessageSize}.")
      }

      // 执行消息批次校验，包括格式是否正确以及CRC校验
      if (!batch.isValid) {
        brokerTopicStats.allTopicsStats.invalidMessageCrcRecordsPerSec.mark()
        throw new CorruptRecordException(s"Record is corrupt (stored crc = ${batch.checksum()}) in topic partition $topicPartition.")
      }

      // 更新maxTimestamp字段和offsetOfMaxTimestamp
      if (batch.maxTimestamp > maxTimestamp) {
        maxTimestamp = batch.maxTimestamp
        offsetOfMaxTimestamp = lastOffset
      }

      // 累加消息批次计数器以及有效字节数，更新shallowMessageCount字段
      shallowMessageCount += 1
      validBytesCount += batchSize

      // 从消息批次中获取压缩器类型
      val messageCodec = CompressionCodec.getCompressionCodec(batch.compressionType.id)
      if (messageCodec != NoCompressionCodec)
        sourceCodec = messageCodec
    }

    // 获取Broker端设置的压缩器类型，即Broker端参数compression.type值。
    // 该参数默认值是producer，表示sourceCodec用的什么压缩器，targetCodec就用什么
    val targetCodec = BrokerCompressionCodec.getTargetCompressionCodec(config.compressionType, sourceCodec)
    // 最后生成LogAppendInfo对象并返回
    LogAppendInfo(firstOffset, lastOffset, maxTimestamp, offsetOfMaxTimestamp, RecordBatch.NO_TIMESTAMP, logStartOffset,
      RecordConversionStats.EMPTY, sourceCodec, targetCodec, shallowMessageCount, validBytesCount, monotonic, lastOffsetOfFirstBatch)
  }


````

#### 读取操作

````

def read(startOffset: Long,maxLength: Int,isolation: FetchIsolation,minOneMessage: Boolean): FetchDataInfo = {
         
}

````

- startOffset: 从Log对象的那个位移开始读消息

- maxLength:最多能读取多少自己

- isolation:设置读取隔离级别，控制能读取的最大位移

- minOneMessage: 是否允许至少读一条消息。如果消息很大，超过了maxLength，正常情况下read方法永远不会返回任何消息。若设置了该参数为true,read方法就保证至少能返回一条消息。

#### 读取方法

```

def read(startOffset: Long,
           maxLength: Int,
           isolation: FetchIsolation,
           minOneMessage: Boolean): FetchDataInfo = {
    maybeHandleIOException(s"Exception while reading from $topicPartition in dir ${dir.getParent}") {
      trace(s"Reading $maxLength bytes from offset $startOffset of length $size bytes")

      val includeAbortedTxns = isolation == FetchTxnCommitted

      // 读取消息时没有使用Monitor锁同步机制，因此这里取巧了，用本地变量的方式把LEO对象保存起来，避免争用（race condition）
      val endOffsetMetadata = nextOffsetMetadata
      val endOffset = nextOffsetMetadata.messageOffset
      if (startOffset == endOffset) // 如果从LEO处开始读取，那么自然不会返回任何数据，直接返回空消息集合即可
        return emptyFetchDataInfo(endOffsetMetadata, includeAbortedTxns)

      // 找到startOffset值所在的日志段对象。注意要使用floorEntry方法
      var segmentEntry = segments.floorEntry(startOffset)

      // return error on attempt to read beyond the log end offset or read below log start offset
      // 满足以下条件之一将被视为消息越界，即你要读取的消息不在该Log对象中：
      // 1. 要读取的消息位移超过了LEO值
      // 2. 没找到对应的日志段对象
      // 3. 要读取的消息在Log Start Offset之下，同样是对外不可见的消息
      if (startOffset > endOffset || segmentEntry == null || startOffset < logStartOffset)
        throw new OffsetOutOfRangeException(s"Received request for offset $startOffset for partition $topicPartition, " +
          s"but we only have log segments in the range $logStartOffset to $endOffset.")

      // 查看一下读取隔离级别设置。
      // 普通消费者能够看到[Log Start Offset, 高水位值)之间的消息
      // 事务型消费者只能看到[Log Start Offset, Log Stable Offset]之间的消息。Log Stable Offset(LSO)是比LEO值小的位移值，为Kafka事务使用
      // Follower副本消费者能够看到[Log Start Offset，LEO)之间的消息
      val maxOffsetMetadata = isolation match {
        case FetchLogEnd => nextOffsetMetadata
        case FetchHighWatermark => fetchHighWatermarkMetadata
        case FetchTxnCommitted => fetchLastStableOffsetMetadata
      }

      // 如果要读取的起始位置超过了能读取的最大位置，返回空的消息集合，因为没法读取任何消息
      if (startOffset > maxOffsetMetadata.messageOffset) {
        val startOffsetMetadata = convertToOffsetMetadataOrThrow(startOffset)
        return emptyFetchDataInfo(startOffsetMetadata, includeAbortedTxns)
      }

      // 开始遍历日志段对象，直到读出东西来或者读到日志末尾
      while (segmentEntry != null) {
        val segment = segmentEntry.getValue

        val maxPosition = {
          if (maxOffsetMetadata.segmentBaseOffset == segment.baseOffset) {
            maxOffsetMetadata.relativePositionInSegment
          } else {
            segment.size
          }
        }

        // 调用日志段对象的read方法执行真正的读取消息操作
        val fetchInfo = segment.read(startOffset, maxLength, maxPosition, minOneMessage)
        if (fetchInfo == null) { // 如果没有返回任何消息，去下一个日志段对象试试
          segmentEntry = segments.higherEntry(segmentEntry.getKey)
        } else { // 否则返回
          return if (includeAbortedTxns)
            addAbortedTransactions(startOffset, segmentEntry, fetchInfo)
          else
            fetchInfo
        }
      }

      // 已经读到日志末尾还是没有数据返回，只能返回空消息集合
      FetchDataInfo(nextOffsetMetadata, MemoryRecords.EMPTY)
    }
  }

```