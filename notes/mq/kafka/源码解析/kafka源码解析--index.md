## 索引

[![slFPyt.jpg](https://s3.ax1x.com/2021/01/10/slFPyt.jpg)](https://imgchr.com/i/slFPyt)

- 索引的底层实现原理：内存映射文件，Java中的MappedByteBuffer,在linux 中，这段映射的内存区域实际上就是内核的页缓存（page Cache）

- 每当consumer需要从主题分区的某个位置开始读取消息时，kafka就会用到OffsetIndex直接定未物理文件位置，避免从头读取消息而引入昂贵的I/O操作

#### AbstractIndex 属性

````
abstract class AbstractIndex(@volatile var file: File, val baseOffset: Long, val maxIndexSize: Int = -1, val writable: Boolean) extends Closeable {
}        
````

1. 索引文件(file)：每个索引对象在磁盘上都对应一个索引文件。

2. 起始位移值(baseOffset):索引对象对应日志段对象的起始位移值。

3. 索引文件最大字节数(maxIndexSize): 控制索引文件的最大长度。**segment.index.bytes** 默认位10MB。

4. 索引文件打开方式(writeable): "true"表示以"读写"方式打开，"false"表示以"只读"方式打开。

- OffSetIndex 索引项是<位移值,物理磁盘位置>；位移值用4个字节，物理磁盘用4个字节，共8个字节；其中位移值是是真实位移值相对于baseOffSet的相对位移值；日志段 最大字节数**log.segment.bytes** 是整型，所以相对位移肯定也是整型

- TimeIndex 索引项是<时间戳，位移值>；时间戳用8个字节，位移值用4个字节，共12个字节。


#### MappedByteBuffer

````

 @volatile
      protected var mmap: MappedByteBuffer = {
        // 第1步：创建索引文件
        val newlyCreated = file.createNewFile()
        // 第2步：以writable指定的方式（读写方式或只读方式）打开索引文件
        val raf = if (writable) new RandomAccessFile(file, "rw") else new RandomAccessFile(file, "r")
        try {
          if(newlyCreated) {
            if(maxIndexSize < entrySize) // 预设的索引文件大小不能太小，如果连一个索引项都保存不了，直接抛出异常
              throw new IllegalArgumentException("Invalid max index size: " + maxIndexSize)
            // 第3步：设置索引文件长度，roundDownToExactMultiple计算的是不超过maxIndexSize的最大整数倍entrySize
            // 比如maxIndexSize=1234567，entrySize=8，那么调整后的文件长度为1234560
            raf.setLength(roundDownToExactMultiple(maxIndexSize, entrySize))
          }
          // 第4步：更新索引长度字段_length
          _length = raf.length()
          // 第5步：创建MappedByteBuffer对象
          val idx = {
            if (writable)
              raf.getChannel.map(FileChannel.MapMode.READ_WRITE, 0, _length)
            else
              raf.getChannel.map(FileChannel.MapMode.READ_ONLY, 0, _length)
          }
          /* set the position in the index for the next entry */
          // 第6步：如果是新创建的索引文件，将MappedByteBuffer对象的当前位置置成0
          // 如果索引文件已存在，将MappedByteBuffer对象的当前位置设置成最后一个索引项所在的位置
          if(newlyCreated)
            idx.position(0)
          else
            idx.position(roundDownToExactMultiple(idx.limit(), entrySize))
          // 第7步：返回创建的MappedByteBuffer对象
          idx
        } finally {
          CoreUtils.swallow(raf.close(), AbstractIndex) // 关闭打开索引文件句柄
        }
      }

````

#### 位移索引->写入索引项 (offsetIndex)

````
def append(offset: Long, position: Int): Unit = {
    inLock(lock) {
      //1. 判断索引文件未写满
      require(!isFull, "Attempt to append to a full index (size = " + _entries + ").")
      //2. 必须满足一下条件之一才允许写入索引项 条件1：当前索引文件为空 条件2：要写入的位移大于当前所有已写入的索引项的位移（Kafka规定索引项中的位移值必须是单调增加的）
      if (_entries == 0 || offset > _lastOffset) {
        trace(s"Adding index entry $offset => $position to ${file.getAbsolutePath}")
        mmap.putInt(relativeOffset(offset))//第3步A：向mmap中写入相对位移值
        mmap.putInt(position)//第3步B：向mmap中写入物理位置信息
        _entries += 1 // 第4步：更新其他元数据统计信息，如当前索引项计数器_entries和当前索引项最新位移值_lastOffset
        _lastOffset = offset
        //第5步：执行校验。写入的索引项格式必须符合要求，即索引项个数*单个索引项占用字节数匹配当前文件物理大小，否则说明文件已损坏
        require(_entries * entrySize == mmap.position(), s"$entries entries but file position in index is ${mmap.position()}.")
      } else {
        // 如果第2步中两个条件都不满足，不能执行写入索引项操作，抛出异常
        throw new InvalidOffsetException(s"Attempt to append an offset ($offset) to position $entries no larger than" +
          s" the last offset appended (${_lastOffset}) to ${file.getAbsolutePath}.")
      }
    }
  }

````

[![slcADe.jpg](https://s3.ax1x.com/2021/01/10/slcADe.jpg)](https://imgchr.com/i/slcADe)

#### 查找索引项

````

def lookup(targetOffset: Long): OffsetPosition = {
  maybeLock(lock) {
    val idx = mmap.duplicate // 使用私有变量复制出整个索引映射区
    // largestLowerBoundSlotFor方法底层使用了改进版的二分查找算法寻找对应的槽
    val slot = largestLowerBoundSlotFor(idx, targetOffset, IndexSearchType.KEY)
    // 如果没找到，返回一个空的位置，即物理文件位置从0开始，表示从头读日志文件
  // 否则返回slot槽对应的索引项
    if(slot == -1)
      OffsetPosition(baseOffset, 0)
    else
      parseEntry(idx, slot)
  }
}
````

### 时间索引

TimeIndex 保存的是<时间戳，相对位移值>，时间戳需要一个长整型来保存，相对位移使用integer来保存。在保存通达数量的索引项基础上，TimeIndex比OffsetIndex占用更多的磁盘空间

````

def maybeAppend(timestamp: Long, offset: Long, skipFullCheck: Boolean = false): Unit = {
  inLock(lock) {
    if (!skipFullCheck)
      // 如果索引文件已写满，抛出异常
      require(!isFull, "Attempt to append to a full time index (size = " + _entries + ").")
    // 确保索引单调增加性
    if (_entries != 0 && offset < lastEntry.offset)
      throw new InvalidOffsetException(s"Attempt to append an offset ($offset) to slot ${_entries} no larger than" +
        s" the last offset appended (${lastEntry.offset}) to ${file.getAbsolutePath}.")
    // 确保时间戳的单调增加性
    if (_entries != 0 && timestamp < lastEntry.timestamp)
      throw new IllegalStateException(s"Attempt to append a timestamp ($timestamp) to slot ${_entries} no larger" +
        s" than the last timestamp appended (${lastEntry.timestamp}) to ${file.getAbsolutePath}.")

    if (timestamp > lastEntry.timestamp) {
      trace(s"Adding index entry $timestamp => $offset to ${file.getAbsolutePath}.")
      mmap.putLong(timestamp) // 向mmap写入时间戳
      mmap.putInt(relativeOffset(offset)) // 向mmap写入相对位移值
      _entries += 1 // 更新索引项个数
      _lastEntry = TimestampOffset(timestamp, offset) // 更新当前最新的索引项
      require(_entries * entrySize == mmap.position(), s"${_entries} entries but file position in index is ${mmap.position()}.")
    }
  }
}

````

[![s1BO1S.jpg](https://s3.ax1x.com/2021/01/10/s1BO1S.jpg)](https://imgchr.com/i/s1BO1S)






























