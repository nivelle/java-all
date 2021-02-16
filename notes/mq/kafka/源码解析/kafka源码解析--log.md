### 日志

[![sV7blT.jpg](https://s3.ax1x.com/2021/01/06/sV7blT.jpg)](https://imgchr.com/i/sV7blT)

1. 一个分区对于一个Log对象，在物理磁盘上对于一个子目录，每个子目录下存在多组日志段，也就是多组.log、.index、timeindex文件组合，文件名因为日志段起始位移不同而不同

#### 日志涉及到的类和对象

1. LogAppendInfo

````
public class LogAppendInfo {
    // 保存了一组待写入消息的各种元数据信息
    //这组消息的第一条消息的位移
    public final long firstOffset;
    //最后一条消息的位移
    public final long lastOffset;

    public LogAppendInfo(long firstOffset, long lastOffset) {
        this.firstOffset = firstOffset;
        this.lastOffset = lastOffset;
    }
}

````

2. RollParams:定义用于控制日志段是否切分(roll)的数据结构


### Log Object

````
 /** a log file */
  val LogFileSuffix = ".log"

  /** an index file */
  val IndexFileSuffix = ".index"

  /** a time index file */
  val TimeIndexFileSuffix = ".timeindex"
  //kafka为幂等行或者事物性producer所做的快照文件
  val ProducerSnapshotFileSuffix = ".snapshot"

  /** an (aborted) txn index */
  val TxnIndexFileSuffix = ".txnindex"

  /** a file that is scheduled to be deleted */
  //删除日志端操作创建的文件，删除日志段文件是异步操作，broker端把.log后缀修改为.delete后缀
  val DeletedFileSuffix = ".deleted"

  /** A temporary file that is being used for log cleaning */
  val CleanedFileSuffix = ".cleaned"

  /** A temporary file used when swapping files into the log */
  val SwapFileSuffix = ".swap"

  /** Clean shutdown file that indicates the broker was cleanly shutdown in 0.8 and higher.
   * This is used to avoid unnecessary recovery after a clean shutdown. In theory this could be
   * avoided by passing in the recovery point, however finding the correct position to do this
   * requires accessing the offset index which may not be safe in an unclean shutdown.
   * For more information see the discussion in PR#2104
   */
  val CleanShutdownFile = ".kafka_cleanshutdown"

  /** a directory that is scheduled to be deleted */
  //应用于文件夹，当删除一个主题，主题的分区文件夹会被加上这个后缀
  val DeleteDirSuffix = "-delete"

  /** a directory that is used for future partition */
  //用于主题变更分区文件夹地址
  val FutureDirSuffix = "-future"

````

#### filenamePrefixFromOffset

通过给定的位移值计算出对于的日志段文件名。日志段文件名固定20位，通过前面补0的方式，把给定位移值扩充成一个固定20位长度的字符串。

#### leaderEpochFileCache

- Leader Epoch = epoch assigned to each leader by the controller.
- Offset = offset of the first message in each epoch.

````
 def assign(epoch: Int, startOffset: Long): Unit = {
    val entry = EpochEntry(epoch, startOffset)
    if (assign(entry)) {
      debug(s"Appended new epoch entry $entry. Cache now contains ${epochs.size} entries.")
      flush()
    }
  }
````

### log 类初始化动作

````
 locally {
    // create the log directory if it doesn't exist
    //创建分区日志路径
    Files.createDirectories(dir.toPath)
    // 初始化Leader Epoch Cache 
    initializeLeaderEpochCache()
    initializePartitionMetadata()
    //重要: 加载所有日志段对象，并返回该Log对象下一条消息的位移值
    val nextOffset = loadSegments()
    /* Calculate the offset of the next message */
    // 初始化LEO元数据对象，LEO值为上一步获取的位移值，起始位移值是Active Segment的起始位移
    nextOffsetMetadata = LogOffsetMetadata(nextOffset, activeSegment.baseOffset, activeSegment.size)

    leaderEpochCache.foreach(_.truncateFromEnd(nextOffsetMetadata.messageOffset))

    updateLogStartOffset(math.max(logStartOffset, segments.firstEntry.getValue.baseOffset))

    // The earliest leader epoch may not be flushed during a hard failure. Recover it here.
    leaderEpochCache.foreach(_.truncateFromStart(logStartOffset))

    // Any segment loading or recovery code must not use producerStateManager, so that we can build the full state here
    // from scratch.
    if (!producerStateManager.isEmpty)
      throw new IllegalStateException("Producer state must be empty during log initialization")

    // Reload all snapshots into the ProducerStateManager cache, the intermediate ProducerStateManager used
    // during log recovery may have deleted some files without the Log.producerStateManager instance witnessing the
    // deletion.
    producerStateManager.removeStraySnapshots(segments.values().asScala.map(_.baseOffset).toSeq)
    loadProducerState(logEndOffset, reloadFromCleanShutdown = hadCleanShutdown)

    // Recover topic ID if present
    partitionMetadataFile.foreach { file =>
      if (!file.isEmpty())
        topicId = file.read().topicId
    }
  }


````

#### loadSegments 日志启动时加载日志段逻辑

````
private def loadSegments(): Long = {
    // first do a pass through the files in the log directory and remove any temporary files
    // and find any interrupted swap operations
    // 1. 移除上次failer遗留下来的各种临时文件（.cleaned、.swap、.deleted）
    val swapFiles = removeTempFilesAndCollectSwapFiles()

    // Now do a second pass and load all the log and index files.
    // We might encounter legacy log segments with offset overflow (KAFKA-6264). We need to split such segments. When
    // this happens, restart loading segment files from scratch.
    retryOnOffsetOverflow {
      // In case we encounter a segment with offset overflow, the retry logic will split it after which we need to retry
      // loading of segments. In that case, we also need to close all segments that could have been left open in previous
      // call to loadSegmentFiles().
      logSegments.foreach(_.close())
      segments.clear()
      //2. 重新加载日志段文件
      loadSegmentFiles()
    }

    // Finally, complete any interrupted swap operations. To be crash-safe,
    // log files that are replaced by the swap segment should be renamed to .deleted
    // before the swap file is restored as the new segment file.
    //3. 处理返回的有效.swap 文件集合
    completeSwapOperations(swapFiles)

    if (!dir.getAbsolutePath.endsWith(Log.DeleteDirSuffix)) {
      val nextOffset = retryOnOffsetOverflow {
        //4. 恢复日志
        recoverLog()
      }

      // reset the index size of the currently active log segment to allow more entries
      activeSegment.resizeIndexes(config.maxIndexSize)
      nextOffset
    } else {
       if (logSegments.isEmpty) {
          addSegment(LogSegment.open(dir = dir,
            baseOffset = 0,
            config,
            time = time,
            initFileSize = this.initFileSize))
       }
      0
    }
  }

````

1. removeTempFilesAndCollectSwapFiles()

````

private def removeTempFilesAndCollectSwapFiles(): Set[File] = {
    //内部方法，用于删除日志文件对应的索引文件
    def deleteIndicesIfExist(baseFile: File, suffix: String = ""): Unit = {
      info(s"Deleting index files with suffix $suffix for baseFile $baseFile")
      val offset = offsetFromFile(baseFile)
      Files.deleteIfExists(Log.offsetIndexFile(dir, offset, suffix).toPath)
      Files.deleteIfExists(Log.timeIndexFile(dir, offset, suffix).toPath)
      Files.deleteIfExists(Log.transactionIndexFile(dir, offset, suffix).toPath)
    }
    val swapFiles = mutable.Set[File]()
    val cleanFiles = mutable.Set[File]()
    var minCleanedFileOffset = Long.MaxValue
    //遍历分区日志下的所有文件
    for (file <- dir.listFiles if file.isFile) {
      //不可读抛出异常
      if (!file.canRead)
        throw new IOException(s"Could not read file $file")
      val filename = file.getName
      //如果以.delete 结尾，说明是上次failure 遗留下来的，直接删除
      if (filename.endsWith(DeletedFileSuffix)) {
        debug(s"Deleting stray temporary file ${file.getAbsolutePath}")
        Files.deleteIfExists(file.toPath)
        //如果以.cleaned结尾,选取文件名中位移最小的.cleaned文件获取其位移值,并将该文件加入待删除文件的集合中
      } else if (filename.endsWith(CleanedFileSuffix)) {
        minCleanedFileOffset = Math.min(offsetFromFileName(filename), minCleanedFileOffset)
        cleanFiles += file
      } else if (filename.endsWith(SwapFileSuffix)) {//如果以.swap 结尾
        // we crashed in the middle of a swap operation, to recover:
        // if a log, delete the index files, complete the swap operation later
        // if an index just delete the index files, they will be rebuilt
        val baseFile = new File(CoreUtils.replaceSuffix(file.getPath, SwapFileSuffix, ""))
        info(s"Found file ${file.getAbsolutePath} from interrupted swap operation.")
        if (isIndexFile(baseFile)) {//如果该.swap 文件原来是索引文件
          deleteIndicesIfExist(baseFile) //删除掉原来的索引文件
        } else if (isLogFile(baseFile)) {
          deleteIndicesIfExist(baseFile)//删除掉原来的索引文件
          swapFiles += file //加入待恢复的.swap文件集合中
        }
      }
    }

    // KAFKA-6264: Delete all .swap files whose base offset is greater than the minimum .cleaned segment offset. Such .swap
    // files could be part of an incomplete split operation that could not complete. See Log#splitOverflowedSegment
    // for more details about the split operation.
    // 从待恢复swap集合中找出那些起始位移值大于minCleanedFileOffset值的文件，直接删掉这些无效的.swap文件
    val (invalidSwapFiles, validSwapFiles) = swapFiles.partition(file => offsetFromFile(file) >= minCleanedFileOffset)
    invalidSwapFiles.foreach { file =>
      debug(s"Deleting invalid swap file ${file.getAbsoluteFile} minCleanedFileOffset: $minCleanedFileOffset")
      val baseFile = new File(CoreUtils.replaceSuffix(file.getPath, SwapFileSuffix, ""))
      deleteIndicesIfExist(baseFile, SwapFileSuffix)
      Files.deleteIfExists(file.toPath)
    }

    // Now that we have deleted all .swap files that constitute an incomplete split operation, let's delete all .clean files
    //清除所有待删除文件集合中的文件
    cleanFiles.foreach { file =>
      debug(s"Deleting stray .clean file ${file.getAbsolutePath}")
      Files.deleteIfExists(file.toPath)
    }
    //最后返回当前有效的.swap文件集合
    validSwapFiles
  }

````

2. loadSegmentFiles 

````

 private def loadSegmentFiles(): Unit = {
    // load segments in ascending order because transactional data from one segment may depend on the
    // segments that come before it
    // 按照日志段文件名中的位移值正序排列，然后遍历每个文件
    for (file <- dir.listFiles.sortBy(_.getName) if file.isFile) {
      //如果是索引文件
      if (isIndexFile(file)) {
        // if it is an index file, make sure it has a corresponding .log file
        val offset = offsetFromFile(file)
        val logFile = Log.logFile(dir, offset)
        //确保存在对应的日志文件，否则记录一个警告，并删除该索引文件
        if (!logFile.exists) {
          warn(s"Found an orphaned index file ${file.getAbsolutePath}, with no corresponding log file.")
          Files.deleteIfExists(file.toPath)
        }
        //如果是日志文件
      } else if (isLogFile(file)) {
        // if it's a log file, load the corresponding log segment
        val baseOffset = offsetFromFile(file)
        val timeIndexFileNewlyCreated = !Log.timeIndexFile(dir, baseOffset).exists()
        //创建对应的LogSegment对象实例，并加入segements中
        val segment = LogSegment.open(dir = dir,
          baseOffset = baseOffset,
          config,
          time = time,
          fileAlreadyExists = true)

        try segment.sanityCheck(timeIndexFileNewlyCreated)
        catch {
          case _: NoSuchFileException =>
            error(s"Could not find offset index file corresponding to log file ${segment.log.file.getAbsolutePath}, " +
              "recovering segment and rebuilding index files...")
            recoverSegment(segment)
          case e: CorruptIndexException =>
            warn(s"Found a corrupted index file corresponding to log file ${segment.log.file.getAbsolutePath} due " +
              s"to ${e.getMessage}}, recovering segment and rebuilding index files...")
            recoverSegment(segment)
        }
        addSegment(segment)
      }
    }
  }


````

3. completeSwapOperations

````
 private def completeSwapOperations(swapFiles: Set[File]): Unit = {
    //遍历所有有效 .swap文件
    for (swapFile <- swapFiles) {
      val logFile = new File(CoreUtils.replaceSuffix(swapFile.getPath, SwapFileSuffix, ""))
      //日志文件的起始位移值
      val baseOffset = offsetFromFile(logFile)
      //创建对应的LogSegment实例
      val swapSegment = LogSegment.open(swapFile.getParentFile,
        baseOffset = baseOffset,
        config,
        time = time,
        fileSuffix = SwapFileSuffix)
      info(s"Found log file ${swapFile.getPath} from interrupted swap operation, repairing.")】
      //执行日志段恢复操作
      recoverSegment(swapSegment)

      // We create swap files for two cases:
      // (1) Log cleaning where multiple segments are merged into one, and
      // (2) Log splitting where one segment is split into multiple.
      //
      // Both of these mean that the resultant swap segments be composed of the original set, i.e. the swap segment
      // must fall within the range of existing segment(s). If we cannot find such a segment, it means the deletion
      // of that segment was successful. In such an event, we should simply rename the .swap to .log without having to
      // do a replace with an existing segment.
      //确认之前删除日志段是否成功，是否存在老的日志段文件
      val oldSegments = logSegments(swapSegment.baseOffset, swapSegment.readNextOffset).filter { segment =>
        segment.readNextOffset > swapSegment.baseOffset
      }
      //将生成的.swap 文件加入到日志中，删除调swap 之前的日志段
      replaceSegments(Seq(swapSegment), oldSegments.toSeq, isRecoveredSwapFile = true)
    }
  }

````

4. recoverLog

````
private[log] def recoverLog(): Long = {
    // if we have the clean shutdown marker, skip recovery
    // 如果不存在以 .kafka_cleanshutdown 结尾的文件
    if (!hadCleanShutdown) {
      // okay we need to actually recover this log
      // 获取到上次恢复点意外以外的所有 unflushed 日志段对象
      val unflushed = logSegments(this.recoveryPoint, Long.MaxValue).iterator
      var truncated = false
      //遍历这些unflushed 日志段
      while (unflushed.hasNext && !truncated) {
        val segment = unflushed.next()
        info(s"Recovering unflushed segment ${segment.baseOffset}")
        val truncatedBytes =
          try {
            //执行恢复日子段操作
            recoverSegment(segment, leaderEpochCache)
          } catch {
            case _: InvalidOffsetException =>
              val startOffset = segment.baseOffset
              warn("Found invalid offset during recovery. Deleting the corrupt segment and " +
                s"creating an empty one with starting offset $startOffset")
              segment.truncateTo(startOffset)
          }
          //如果有无效的消息导致被截断的字节数不为0，直接删除剩余的日志段对象
        if (truncatedBytes > 0) {
          // we had an invalid message, delete all remaining log
          warn(s"Corruption found in segment ${segment.baseOffset}, truncating to offset ${segment.readNextOffset}")
          removeAndDeleteSegments(unflushed.toList,
            asyncDelete = true,
            reason = LogRecovery)
          truncated = true
        }
      }
    }
    //如果日志段集合不为空
    if (logSegments.nonEmpty) {
      val logEndOffset = activeSegment.readNextOffset
      if (logEndOffset < logStartOffset) { //验证分区日志的LEO值不能小于 Log Start Offset 值，否则删除这些日志段对象
        warn(s"Deleting all segments because logEndOffset ($logEndOffset) is smaller than logStartOffset ($logStartOffset). " +
          "This could happen if segment files were deleted from the file system.")
        removeAndDeleteSegments(logSegments,
          asyncDelete = true,
          reason = LogRecovery)
      }
    }
    //如果日志段集合为空了
    if (logSegments.isEmpty) {
      // no existing segments, create a new mutable segment beginning at logStartOffset
      //至少创建一个新的日志段，以logStartOffset为日志段的起始位移，并加入日志段集合中
      addSegment(LogSegment.open(dir = dir,
        baseOffset = logStartOffset,
        config,
        time = time,
        initFileSize = this.initFileSize,
        preallocate = config.preallocate))
    }
    //更新上次恢复点属性，返回
    recoveryPoint = activeSegment.readNextOffset
    recoveryPoint
  }

````

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
