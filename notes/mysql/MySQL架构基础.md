### MySQL 全表扫描对server层的影响

#### 服务端并不保存完整的结果集,取数据和发数据的流程如下:

1. 获取一行，写到net_buffer中。这块内存的大小是由参数，net_buffer_length 定义的，默认是16K

2. 重复获取行，直到net_buffer写满，调用网络接口发出去

3. 如果发送成功，就清空net_buffer,然后继续取下一行，并写入net_buffer

4. 如果发送函数返回EAGAIN 或 WSAEWOULDBLOCK ,就表示本地网络栈(socket send buffer)写满了，进入等待。直到网络栈重新可写，再继续发送。

[![cQ7jhD.jpg](https://z3.ax1x.com/2021/04/05/cQ7jhD.jpg)](https://imgtu.com/i/cQ7jhD)

- 一个查询在发送过程中，占用的MySQL 内部的内存最大就是net_buffer_length 

- socket send buffer 也不可能达到200G(默认定义 /proc/sys/net/core/vmem_default),如果socket send buffer被写满，就会暂停读数据流

### 全表扫描对InnoDB的影响

- InnoDB内存的作用：保存更新结果，再配合redo log ,避免了随机写盘

- 内存的数据页是在buffer pool 中管理的，在WAL里Buffer Pool 起到了加速更新作用，实际上，buffer pool还有一个更重要的作用，就是加速查询

- 由于有WAL机制，当事务提交的时候，磁盘上的数据页是旧的，这个时候有个查询来读取这个数据页，不需要马上把redo log 应用到数据页，因为这个时候内存数据页的结果是最新的，直接读取内存页就可以了。

- 对buffer pool 采取分区策略，避免LRU算法对yong区的影响，保证了buffer pool响应正常业务的查询命中率