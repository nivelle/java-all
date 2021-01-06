### 日志

[![sV7blT.jpg](https://s3.ax1x.com/2021/01/06/sV7blT.jpg)](https://imgchr.com/i/sV7blT)

1. 一个分区对于一个Log对象，在物理磁盘上对于一个子目录，每个子目录下存在多组日志段，也就是多组.log、.index、timeindex文件组合，文件名因为日志段起始位移不同而不同


### 日志段

````
class LogSegment private[log] (val log: FileRecords,
                               val lazyOffsetIndex: LazyIndex[OffsetIndex],
                               val lazyTimeIndex: LazyIndex[TimeIndex],
                               val txnIndex: TransactionIndex,
                               val baseOffset: Long,
                               val indexIntervalBytes: Int,
                               val rollJitterMs: Long,
                               val time: Time) extends Logging 

````

1. baseOffSet : 日志段起始位移，也就是文件名，当前日志段最小，大于前一个日志段最大位移

2. indexIntervalBytes: 也就是log.index.interval.bytes ,控制日志段对象新增索引项的频率

````
默认日志段写入4KB的消息数据才会增加一条索引项， 是日志段对象新增倒计时的"扰动值"。避免在某个时刻大量创建日志段对象，增加磁盘I/O压力。


kafka一个非空日志段segment在超时一段时间后，即使还没有写满，也强制新建日志段。

(1). segment.jitter.ms :日志段超时滚动最大扰动值

(2). segment.ms :日志段文件超过这段时间后，即使没写满，kafka也会强制滚动新建日志段,确保可以删除或者压缩旧数据

(3). cleanup.policy = compant 开启旧数据压缩，cleanup.policy = delete 就数据直接删除 

(4). max.compaction.lag.ms 消息在log中保持原样不被压缩的最大时间，仅适用与压缩中的log 

````

#### append 方法

- largestOffset : 最大位移
- largestTimestamp：最大时间戳
- shallowOffsetOfMaxTimestamp：最大时间戳对应消息的位移
- 要写入的消息集合

````
def append(largestOffset: Long,largestTimestamp: Long,shallowOffsetOfMaxTimestamp: Long,records: MemoryRecords): Unit = {
    if (records.sizeInBytes > 0) {
      trace(s"Inserting ${records.sizeInBytes} bytes at end offset $largestOffset at position ${log.sizeInBytes} with largest timestamp $largestTimestamp at shallow offset $shallowOffsetOfMaxTimestamp")
      //1. 判断该日志段是否为空
      val physicalPosition = log.sizeInBytes()
      //若为空
      if (physicalPosition == 0)
        //2. 记录要写入消息集合的最大时间戳，作为后面新增日志段倒计时的依据
        rollingBasedTimestamp = Some(largestTimestamp)
      //3. 确保输入参数最大位移值合法：也就是 largestOffset - baseOffset 的值是否介于[0,Int.MAXVALUE]之间
      ensureOffsetInRange(largestOffset)

      // append the messages
      // 4.将内存中的消息对象写入到操作系统的页缓存
      val appendedBytes = log.append(records)
      trace(s"Appended $appendedBytes to ${log.file} at end offset $largestOffset")
      // Update the in memory max timestamp and corresponding offset.
      // 5. 更新日志段的最大时间戳以及最大时间戳所属消息的位移值属性。
      // 最大时间戳用于实现定期删除日志功能
      if (largestTimestamp > maxTimestampSoFar) {
        maxTimestampSoFar = largestTimestamp
        offsetOfMaxTimestampSoFar = shallowOffsetOfMaxTimestamp
      }
      // append an entry to the index (if needed)
      //6. 更新索引项和写入的字节数
      if (bytesSinceLastIndexEntry > indexIntervalBytes) {
        offsetIndex.append(largestOffset, physicalPosition)
        timeIndex.maybeAppend(maxTimestampSoFar, offsetOfMaxTimestampSoFar)
        //晴空字节数，以备下次重新累计计算
        bytesSinceLastIndexEntry = 0
      }
      bytesSinceLastIndexEntry += records.sizeInBytes
    }
  }

````

#### read 方法

- startOffSet: 要读取的第一条消息的位移

- maxSize:能读取的最大字节数

- maxPosition: 能读到的最大文件位置

- minOneMessage: 是否允许在消息体过大是至少返回第一条消息，如果为true,即使出现消息字节数超过了MaxSize情形，read方法依然能至少返回一条消息

````
def read(startOffset: Long,
           maxSize: Int,
           maxPosition: Long = size,
           minOneMessage: Boolean = false): FetchDataInfo = {
    if (maxSize < 0)
      throw new IllegalArgumentException(s"Invalid max size $maxSize for log read from segment $log")
    // 定位要读取的起始文件位置(startPosition),startOffset 是位移值，根据索引信息找到对应的物理文件位置才能开始读取消息
    val startOffsetAndSize = translateOffset(startOffset)

    // if the start position is already off the end of the log, return null
    if (startOffsetAndSize == null)
      return null

    val startPosition = startOffsetAndSize.position
    val offsetMetadata = LogOffsetMetadata(startOffset, this.baseOffset, startPosition)

    val adjustedMaxSize =
      if (minOneMessage) math.max(maxSize, startOffsetAndSize.size)
      else maxSize

    // return a log segment but with zero size in the case below
    if (adjustedMaxSize == 0)
      return FetchDataInfo(offsetMetadata, MemoryRecords.EMPTY)
 
    // calculate the length of the message set to read based on whether or not they gave us a maxOffset
    // min(maxPosition - startPostion ,maxPosition) 就是能读取的消息数
    val fetchSize: Int = min((maxPosition - startPosition).toInt, adjustedMaxSize)
    // 从指定位置读取指定大小的消息集合
    FetchDataInfo(offsetMetadata, log.slice(startPosition, fetchSize),
      firstEntryIncomplete = adjustedMaxSize < startOffsetAndSize.size)
  }


````

#### recover 

- 恢复日志段，broker启动时从磁盘上加载所有日志段消息到内存中，并创建相应的LogSegment对象实例

````
def recover(producerStateManager: ProducerStateManager, leaderEpochCache: Option[LeaderEpochFileCache] = None): Int = {
    //1. 清空索引文件
    offsetIndex.reset()
    timeIndex.reset()
    txnIndex.reset()
    var validBytes = 0
    var lastIndexEntry = 0
    maxTimestampSoFar = RecordBatch.NO_TIMESTAMP
    try {
      //2. 遍历日志段中所有消息集合
      for (batch <- log.batches.asScala) {
        //2.1 校验消息集合
        batch.ensureValid()
        ensureOffsetInRange(batch.lastOffset)

        // The max timestamp is exposed at the batch level, so no need to iterate the records
        //2.2 保存最大时间戳和所属消息位移
        if (batch.maxTimestamp > maxTimestampSoFar) {
          maxTimestampSoFar = batch.maxTimestamp
          offsetOfMaxTimestampSoFar = batch.lastOffset
        }

        // Build offset index
        //2.3 更新索引项
        if (validBytes - lastIndexEntry > indexIntervalBytes) {
          offsetIndex.append(batch.lastOffset, validBytes)
          timeIndex.maybeAppend(maxTimestampSoFar, offsetOfMaxTimestampSoFar)
          lastIndexEntry = validBytes
        }
        //2.4 更新总消息字节数
        validBytes += batch.sizeInBytes()
       
        if (batch.magic >= RecordBatch.MAGIC_VALUE_V2) {
          leaderEpochCache.foreach { cache =>
            if (batch.partitionLeaderEpoch >= 0 && cache.latestEpoch.forall(batch.partitionLeaderEpoch > _))
              cache.assign(batch.partitionLeaderEpoch, batch.baseOffset)
          }
           //2.5 更新事物producer 状态和Leader Epoch缓存
          updateProducerState(producerStateManager, batch)
        }
      }
    } catch {
      case e@ (_: CorruptRecordException | _: InvalidRecordException) =>
        warn("Found invalid messages in log segment %s at byte offset %d: %s. %s"
          .format(log.file.getAbsolutePath, validBytes, e.getMessage, e.getCause))
    }
    //3. 将日志段当前总字节数和刚刚累加的已读取字节数进行比较，如果发现前者比后者大，说明日志段写入了一些非法消息，需要执行截断操作，将日志段大小调整回合法的数值。同时， Kafka 还必须相应地调整索引文件的大小
    val truncated = log.sizeInBytes - validBytes
    if (truncated > 0)
      debug(s"Truncated $truncated invalid bytes at the end of segment ${log.file.getAbsoluteFile} during recovery")

    log.truncateTo(validBytes)
    offsetIndex.trimToValidSize()
    // A normally closed segment always appends the biggest timestamp ever seen into log segment, we do this as well.
    timeIndex.maybeAppend(maxTimestampSoFar, offsetOfMaxTimestampSoFar, skipFullCheck = true)
    timeIndex.trimToValidSize()
    truncated
  }

````
