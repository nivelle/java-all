
### 优化分页查询

通常我们是使用 <LIMIT M,N> + 合适的 order by 来实现分页查询，这种实现方式在没有任何索引条件支持的情况下，需要做大量的文件排序操作（file sort），性能将会非常得糟糕。如果有对应的索引，通常刚开始的分页查询效率会比较理想，但越往后，分页查询的性能就越差。

直接查询:

```

select * from `demo`.`order` order by order_no limit 10000, 20;//会返回10020行数据，返回的数据太多了,导致执行的效率比较低。

```
- 利用子查询优化分页查询

子查询遍历索引的范围跟上一个查询差不多，而主查询扫描了更多的行数，但执行时间却减少了，只有 0.004s。这就是因为返回行数只有 20 行了，执行效率得到了明显的提升。

```

select * from `demo`.`order` where id> (select id from `demo`.`order` order by order_no limit 10000, 1)  limit 20;

```

### 优化 SELECT COUNT(*)

- count()是一个聚合函数，主要用来统计行数，有时也用来统计某一列的行数（不统计NULL值的行）。

- COUNT() 函数在 MyISAM 和 InnoDB 存储引擎所执行的原理是不一样的，通常在没有任何查询条件下的 COUNT(*)，MyISAM 的查询速度要明显快于 InnoDB;因为 MyISAM 存储引擎记录的是整个表的行数，在 COUNT(*) 查询操作时无需遍历表计算，直接获取该值即可。而在 InnoDB 存储引擎中就需要扫描表来统计具体的行数。而当带上 where 条件语句之后，MyISAM 跟 InnoDB 就没有区别了，它们都需要扫描表来进行行数的统计

优化建议:

- 使用近似值：可以使用 EXPLAIN 对表进行估算，要知道，执行 EXPLAIN 并不会真正去执行查询，而是返回一个估算的近似值

- 增加汇总统计

### InnoDB 调优参数

#### innodb_buffer_pool_size : 默认 128M

 ```
  mysql> SELECT @@innodb_buffer_pool_size; //134217728/1024*1024 = 128 M
  +---------------------------+
  | @@innodb_buffer_pool_size |
  +---------------------------+
  |                 134217728 |
  +---------------------------+
  
  ```

  1. IBP 默认的内存大小是 128M，我们可以通过参数 innodb_buffer_pool_size 来设置 IBP 的大小，IBP 设置得越大，InnoDB 表性能就越好。
 
  2. 将 IBP 大小设置得过大也不好，可能会导致系统发生 SWAP 页交换。所以我们需要在 IBP 大小和其它系统服务所需内存大小之间取得平衡。
 
  3. MySQL 推荐配置 IBP 的大小为服务器物理内存的 80%。
  
  4. InnoDB_buffer_pool_size参数同时提供为数据块和索引块做缓存.这个值设置的越高,访问表中数据需要的磁盘IO就越少

#### innodb_buffer_pool_instances:默认1

  1. InnoDB 中的 IBP 缓冲池被划分为了多个实例，对于具有数千兆字节的缓冲池的系统来说，将缓冲池划分为单独的实例可以减少不同线程读取和写入缓存页面时的争用，从而提高系统的并发性。
  
  2. 该参数项仅在将 innodb_buffer_pool_size 设置为 1GB 或更大时才会生效。
  
  3. 建议 innodb_buffer_pool_instances 的大小不超过 innodb_read_io_threads + innodb_write_io_threads 之和，建议实例和线程数量比例为 1:1。

#### innodb_read_io_threads （默认4）/ innodb_write_io_threads（默认4):

  1. 在默认情况下，MySQL 后台线程包括了主线程、IO 线程、锁线程以及监控线程等，其中读写线程属于 IO 线程，主要负责数据库的读取和写入操作，这些线程分别读取和写入 innodb_buffer_pool_instances 创建的各个内存页面
  
  ```
  
   SHOW GLOBAL STATUS LIKE 'Com_select';//读取数量

   SHOW GLOBAL STATUS WHERE Variable_name IN ('Com_insert', 'Com_update', 'Com_replace', 'Com_delete');//写入数量
   
  ```
  
  2. 决定这两个参数数值的因素也有两个：cpu核数、应用场景中读写事务比例。
  

#### innodb_log_file_size: 默认48M

  当日志文件大小已经超过我们参数设置的日志文件大小时，InnoDB 会自动切换到另外一个日志文件，由于重做日志是一个循环使用的环，在切换时，就需要将新的日志文件脏页的缓存数据刷新到磁盘中（触发检查点）。

#### innodb_log_buffer_size: 默认16

  ```
  mysql> SELECT @@innodb_log_buffer_size;
  +--------------------------+
  | @@innodb_log_buffer_size |
  +--------------------------+
  |                 16777216 |
  +--------------------------+

  ```

  这个参数决定了 InnoDB 重做日志缓冲池的大小,如果高并发中存在大量的事务，该值设置得太小，就会增加写入磁盘的 I/O 操作。我们可以通过增大该参数来减少写入磁盘操作，从而提高并发时的事务性能。

#### innodb_flush_log_at_trx_commit:默认1 


 0: log buffer中的数据将以每秒一次的频率写入到log file中,且同时会进行文件系统到磁盘的同步操作，但是每个事务的commit并不会触发任何log buffer 到log file的刷新或者文件系统到磁盘的刷新操作;

 1: 在每次事务提交的时候将logbuffer 中的数据都会写入到log file,同时也会触发文件系统到磁盘的同步;

 2: 事务提交会触发log buffer 到log file的刷新，但并不会触发磁盘文件系统到磁盘的同步。此外，每秒会有一次文件系统到磁盘同步操作。


#### max_connections: 默认151

  控制允许连接到MySQL数据库的最大的连接数。

#### back_log:默认 80 

  MySQL能暂存的连接数量。当主要MySQL线程在一个很短时间内得到非常多的连接请求，这就起作用。如果MySQL的连接数据达到 max_connections时，新来的请求将会被存在堆栈中，以等待某一连接释放资源，该堆栈的数量即back_log，如果等待连接的数量超过 back_log，将不被授予连接资源。

#### thread_cache_size:缓存线程数目大小，默认9

  MySQL连接收到客户端连接时，需要生成线程处理连接。档连接断开的时候，线程并不会立刻销毁，而是对线程进行缓存，便于下一个连接使用，减少线程的创建和销毁，

#### innodb_log_files_in_group:默认2

#### innodb_file_per_table

1. 关键参数，默认情况下配置为off。

2. 控制innodb每一个表使用独立的表空间，默认情况下，所有的表都会建立在共享表空间当中。
