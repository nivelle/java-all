## mysql 日志

### redo log(重做日志)

1. redo log 是 InnoDB 存储引擎层的日志,**记录的是这个页做了什么改动**;

2. 在一条更新语句进行执行的时候，InnoDB引擎会把更新记录写到`redo log日志`中，然后`更新内存`，此时算是语句执行完了, 然后在空闲的时候或者是按照设定的更新策略将redo log中的内容`更新到磁盘`中，这里涉及到WAL即 
   **Write Ahead Logging**技术，他的关键点是先写日志，再写磁盘。

3. redo log日志的大小是固定的，即记录满了以后就从头循环写;记录满时要停止数据库更新操作,转而将日志刷到磁盘;

4. 可以根据redo log日志进行恢复,也就达到了 `crash-safe【崩溃安全】`

5. 插入数据的过程中，生成的日志都得先保存起来，但又不能在还没 commit 的时候就直接写到 redo log 文件里;所以提供了一块内存：**redo log buffer 用来先存redo
   log日志**，真正执行commit语句的时候，才写入到日志文件。

### binlog(归档日志)。

1. binlog是MySQL Server层记录的日志,属于逻辑日志

2. 是以二进制的形式记录的是这个**语句的原始逻辑**，依靠binlog是没有`crash-safe`能力的

3. Binlog有两种模式，**statement【陈述】** 格式的话是记sql语句， **row【行】** 格式会记录行的内容，记两条，更新前和更新后都有

#### binlog事务完整性判断：

- statement 格式的 binlog,最后会有 COMMIT；

- row 格式的 binlog，最后会有一个 XID event。

### undoLog(回滚日志)

[![sBHSns.md.png](https://z3.ax1x.com/2021/01/16/sBHSns.md.png)](https://imgtu.com/i/sBHSns)

- 隐藏列回滚指针列将数据行的所有快照记录都通过链表的结构串联了起来,每个快照的记录都保存了当时的 **db_trx_id**，也是那个时间点操作这个数据的事务 ID。 这样如果我们想要找历史快照，就可以通过遍历回滚指针的方式进行查找

----

### 日志比较

#### redoLog 和 bingLog

redoLog | bingLog
---|---
属于innoDB层面,存储引擎 | 属于MySQL Server层面的，这样在数据库用别的存储引擎时可以达到一致性的要求
物理日志,记录该数据页更新的内容| 逻辑日志,记录的是这个更新语句的原始逻辑
循环写，日志空间大小固定| 追加写，是指一份写到一定大小的时候会更换下一个文件，不会覆盖
作为异常宕机或者介质故障后的数据恢复使用|可以作为恢复数据使用，主从复制搭建
****
---

##### redoLog 和 undoLog

redoLog | undoLog
---|---
用来恢复提交后的物理数据页(恢复数据页,且只能恢复到最后一次提交的位置) | 记录事务开始前的状态,用来回滚行记录到某个版本;`undo log`一般是逻辑日志，根据每行记录进行记录。
确保事务的持久性。防止在发生故障的时间点，尚有脏页未写入磁盘,在重启mysql服务的时候,根据redo log进行重做,从而达到事务的持久性这一特性。| 保存了事务发生之前的数据的一个版本，可以用于回滚，同时可以提供多版本并发控制下的读（MVCC），也即非锁定读

---

## 一条更新语句的执行过程(二阶段提交)

###  binlog（归档日志）和 redo log（重做日志）配合崩溃恢复的时候，使用两阶段提交，保证数据完整性。

[![yYr2fP.md.jpg](https://z3.ax1x.com/2021/02/06/yYr2fP.md.jpg)](https://imgtu.com/i/yYr2fP)

````
update T set c=c+1 where ID=2;
````

- 执行器先找引擎取 ID=2 这一行。ID 是主键，引擎直接用树搜索找到这一行。如果 ID=2 这一行所在的数据页本来就在内存中，就直接返回给执行器；否则，需要先从磁盘读入内存，然后再返回。

- 执行器拿到引擎给的行数据，把这个值加上 1，比如原来是 N，现在就是 N+1，得到新的一行数据，再调用引擎接口写入这行新数据

- 引擎将这行新数据更新到内存中,同时将这个更新操作记录到 redo log 里面，此时 redo log 处于 `prepare 状态`。然后告知执行器执行完成了，随时可以提交事务。

- **执行器生成这个操作的 binlog,并把 binlog 写入磁盘。**

- 执行器调用引擎的`提交事务接口`，引擎把刚刚写入的 redo log 改成提交`（commit）状态`，更新完成。

---

### 崩溃恢复判断：

#### 时刻 A

- 也就是写入 redo log 处于 prepare 阶段之后、写 binlog 之前，发生了崩溃（crash），由于此时binlog 还没写，redo log 也还没提交，所以崩溃恢复的时候，这个事务会回滚。这时候，binlog 还没写，所以也不会传到备库

#### 时刻B

- 如果 redo log 里面的事务是完整的，也就是已经有了 commit 标识，则直接提交；

- 如果 redo log 里面的事务只有完整的 prepare，则判断对应的事务 binlog 是否存在并完整：
  - 如果是，则提交事务
  - 否则，回滚事务

-----

### change buffer、redo log 和 buffer pool 的关系

实际上，redo log 并没有记录数据页的完整数据，所以它并没有能力自己去更新磁盘数据页，也就不存在“数据最终落盘，是由 redo log 更新过去”的情况。

- 如果是正常运行的实例的话，数据页被修改以后，跟磁盘的数据页不一致，称为脏页。最终数据落盘，就是把内存中的数据页写盘。这个过程，甚至与 redo log 毫无关系。

- 在崩溃恢复场景中，InnoDB 如果判断到一个数据页可能在崩溃恢复的时候丢失了更新，就会将它读到内存，然后让 redo log 更新内存内容。更新完成后，内存页变成脏页，就回到了第一种情况的状态。

- change buffer用的是**buffer pool**里的内存，change buffer的大小，可以通过参数**innodb_change_buffer_max_size**来动态设置。这个参数设置为50的时候，表示change
  buffer的大小最多只能占用buffer pool的50%。

- **redo log 主要节省的是随机写磁盘的 IO 消耗（转成顺序写），而 change buffer 主要节省的则是随机读磁盘的 IO 消耗**。


----
### 日志操作例子

- 插入数据

````
mysql> insert into t(id,k) values(id1,k1),(id2,k2);

````

[![ytpBPH.md.png](https://z3.ax1x.com/2021/02/06/ytpBPH.md.png)](https://imgtu.com/i/ytpBPH)

1. Page 1 在内存中，直接更新内存;

2. Page 2 没有在内存中，就在内存的 change buffer 区域，记录下“我要往 Page 2 插入一行”这个信息

3. 将上述两个动作记入 redo log 中

- 读取数据

````
select * from t where k in (k1, k2)。
````

1. 读 Page 1 的时候，直接从内存返回

2. 要读 Page 2 的时候，需要把 Page 2 从磁盘读入内存中，然后应用 change buffer 里面的操作日志，生成一个正确的版本并返回结果

[![ytPbLt.md.png](https://z3.ax1x.com/2021/02/06/ytPbLt.md.png)](https://imgtu.com/i/ytPbLt)


--------

### 慢日志

- SHOW VARIABLES LIKE 'slow_query%';

````
+---------------------+--------------------------------------+
| Variable_name       | Value                                |
+---------------------+--------------------------------------+
| slow_query_log      | ON                                   |
| slow_query_log_file | /var/lib/mysql/c27402a5c193-slow.log |
+---------------------+--------------------------------------+
````

- **set global slow_query_log = on**; //开启慢日志记录

- **set long_query_time=0**，将慢查询日志的时间阈值设置为 0 //设置慢日志阀值

----

### 数据库存储结构

[![sD9oRA.jpg](https://z3.ax1x.com/2021/01/16/sD9oRA.jpg)](https://imgtu.com/i/sD9oRA)

- 区（Extent）是比页大一级的存储结构，在 InnoDB 存储引擎中，**一个区会分配 64 个连续的页**。因为 InnoDB 中的页大小默认是 16KB，所以一个区的大小是 64*16KB=1MB

- 段（Segment）由一个或多个区组成，区在文件系统是一个连续分配的空间（在 InnoDB 中是连续的 64
  个页），不过在段中不要求区与区之间是相邻的。**段是数据库中的分配单位**，不同类型的数据库对象以不同的段形式存在。当我们创建数据表、索引的时候，就会相应创建对应的段，比如创建一张表时会创建一个表段，创建一个索引时会创建一个索引段

- 表空间（Tablespace）是一个逻辑容器，表空间存储的对象是段，在一个表空间中可以有一个或多个段，但是一个段只能属于一个表空间。数据库由一个或多个表空间组成，表空间从管理上可以划分为**系统表空间、用户表空间、撤销表空间、临时表空间**等

- 页（Page）如果按类型划分的话，常见的有**数据页（保存 B+ 树节点）**、**系统页**、**Undo 页**和**事务数据页**等。数据页是我们最常使用的页；`在数据库中，不论读一行，还是读多行，都是将这些行所在的页进行加载`。
  也就是说，`数据库管理存储空间的基本单位是页（Page），InnoDB默认是16K`

#### 数据页的内容结构

[![sDCKQ1.md.jpg](https://z3.ax1x.com/2021/01/16/sDCKQ1.md.jpg)](https://imgtu.com/i/sDCKQ1)

- 第一部分: **文件通用部分，也就是文件头和文件尾**。它们类似集装箱，将页的内容进行封装，通过文件头和文件尾校验的方式来确保页的传输是完整的；文件头中有两个字段，分别是 `FIL_PAGE_PREV` 和
  `FIL_PAGE_NEXT`，它们的作用相当于指针，分别指向上一个数据页和下一个数据页。连接起来的页相当于一个双向的链表

- 第二部分: **记录部分**，页的主要作用是存储记录，所以“最小和最大记录”和“用户记录”部分占了页结构的主要空间。另外空闲空间是个灵活的部分，当有新的记录插入时，会从空闲空间中进行分配用于存储新记录

- 第三部分：这部分重点指的是**页目录**，它起到了记录的索引作用，因为在页中，`记录是以单向链表的形式进行存储的`。单向链表的特点就是插入、删除非常方便，但是检索效率不高，最差的情况下需要遍历链表上的所有节点才能完成检索，
  因此在页目录中提供了**二分查找**的方式，用来提高记录的检索效率

----
## B+ 树的索引的记录检索

- 如果通过 B+ 树的索引查询行记录,首先是从 B+ 树的根开始,逐层检索,直到找到叶子节点,也就是找到对应的数据页为止,将数据页加载到内存中;
  
- 然后页目录中的槽（slot）采用二分查找的方式先找到一个粗略的记录分组，然后再在分组中通过链表遍历的方式查找记录

---- 
## 脏页(刷脏页导致数据库抖动)

- InnoDB 的策略是尽量使用内存，因此对于一个长时间运行的库来说，未被使用的页面很少。

- 当要读入的数据页没有在内存的时候，就必须到缓冲池中申请一个数据页。这时候只能把最久不使用的数据页从内存中淘汰掉：

  - 如果要淘汰的是一个干净页，就直接释放出来复用；

  - 如果是脏页呢，就必须将脏页先刷到磁盘，变成干净页后才能复用

#### 控制刷脏页的速度，会参考哪些因素呢？

1. 磁盘io能力

- **innodb_io_capacity** 这个参数了，它会告诉 InnoDB 你的磁盘能力

  innodb_io_capacity参数默认是200，单位是页。该参数设置的大小取决于硬盘的IOPS，即每秒的输入输出量（或读写次数）

2. 脏页比例

- **innodb_max_dirty_pages_pct**是脏页比例上限，默认值是 75%,也就是四分之三; 
  InnoDB 会根据当前的脏页比例（假设为 M）,算出一个范围在 0 到 100 之间的数字 InnoDB 每次写入的日志都有一个序号，当前写入的序号跟 checkpoint 对应的序号之间的差值，我们假设为 N。


3. redo log 写盘速度。

![redolog写盘速度.png](https://i.loli.net/2021/08/30/pjFXCdhgnw5J9DA.png)
----

## 日志写入机制

### binlog 写入机制

- 事务执行过程中，先把日志写到 binlog cache，事务提交的时候，再把 binlog cache 写到 binlog 文件中

- 一个事务的 binlog 是不能被拆开的，因此不论这个事务多大，也要确保一次性写入

系统给 binlog cache 分配了一片内存，每个线程一个，参数 `binlog_cache_size` 用于控制单个线程内 binlog cache 所占内存的大小。如果超过了这个参数规定的大小，就要暂存到磁盘。

[![6ib1c8.png](https://z3.ax1x.com/2021/03/01/6ib1c8.png)](https://imgtu.com/i/6ib1c8)

#### write和fsync的时机，是由sync_binlog参数控制的

1. `sync_binlog=0` 的时候，表示每次提交事务都只 write,

2. `sync_binlog=1` 的时候，表示每次提交事务都会执行fsync;

3. `sync_binlog=N(N>1)` 的时候，表示每次提交事务都write,但累积N个事务后才fsync

- 将 sync_binlog 设置为 N，对应的风险是：如果主机发生异常重启，会丢失最近 N 个事务的 binlog 日志。


-----

### redo log的写入机制

[![6ib1c8.md.png](https://z3.ax1x.com/2021/03/01/6ib1c8.md.png)](https://imgtu.com/i/6ib1c8)

#### 为了控制 redo log 的写入策略，InnoDB 提供了 innodb_flush_log_at_trx_commit 参数，它有三种可能取值：

-  `设置为0的时候`，表示每次事务提交时都只是把redo log 留在redo log buffer中

-  `设置为1的时候`，表示每次事务提交时都将redo log 直接持久化到磁盘

-  `设置为2的时候`，表示每次事务提交时都只是把redo log写到page cache

**InnoDB 有一个后台线程，每隔 1 秒，就会把 redo log buffer 中的日志，调用 write 写到文件系统的 page cache，然后调用 fsync 持久化到磁盘.**

#### 未提交的事务redo log 三种情况也会被持久化到磁盘

- 后台线程每一秒的轮询操作，会将一个没有提交的事务的redo log 写入到磁盘中

- redo log buffer占用的空间即将达到**innodb_log_buffer_size**一半的时候，后台线程会主动写盘

- 并行的事务提交的时候，顺带将这个事务的 redo log buffer 持久化到磁盘。

----
## 数据库的"双1"配置

- 每秒一次后台轮询刷盘，再加上崩溃恢复这个逻辑，InnoDB 就认为 redo log 在 commit 的时候就不需要 fsync 了，只会 write 到文件系统的 page cache 中就够了


### 为了确保 redo log 与 binlog 一致，MySQL 采用 2PC 来保证事务的完整性：

- 调用 binlog 和 innodb 的 prepare() 方法

  -  binlog 的 prepare() 方法什么也不做

  - innodb 的 prepare() 方法将事务状态设置为 `TRX_PREPARED`，并将 redo log 刷盘如果事务涉及到的所有存储引擎的 prepare() 方法都执行成功，则将 SQL 记录到 binlog，否则回滚

- 调用 commit() 方法完成事务的提交
  - binlog 的 commit() 方法什么也不会做，因为第二步已经将 binlog 刷盘
  - innodb 的 commit() 方法清理 undo 信息、刷 redo log、将事务状态设置为 `TRX_NOT_STARTED `innodb 在恢复时，会根据事务的状态，进行不同的处理:
    - 对于 `TRX_COMMITTED_IN_MEMORY` 状态的事务，会清理回滚段、然后将事务状态设置为 TRX_NOT_STARTED
    - 对于 `TRX_NOT_STARTED` 状态的事务，会跳过，因为事务已经提交
    - 对于 `TRX_ACTIVE` 状态的事务，会回滚
    - 对于 `TRX_PREPARED` 状态的事务，会根据 binlog 的状态决定如何处理

- flush 阶段: 支持 redo log 的组提交

  - 将 redo log 中处于 prepare 阶段的数据刷盘
  - 将 binlog 数据写入文件，但此时只是写入文件系统的缓冲，不能保证数据库崩溃时 binlog 不丢失

- sync 阶段: 支持 binlog 的组提交

  - 将 binlog 刷盘，若队列中有多个事务，那么仅一次 fsync 操作就可以完成二进制日志的刷盘操作，这在 MySQL 5.6 中称为 BLGC（binary log group commit）
  - 如果在这步完成后数据库崩溃，由于 binlog 中已经存在事务记录，MySQL 会通过 flush 阶段中已经刷盘的 redo log 继续进行事务的提交

- commit 阶段

   - 将 redo log 中处于 prepare 状态的事务在引擎层提交，commit 阶段不用刷盘
   - **sync_binlog 和 innodb_flush_log_at_trx_commit** 都设置成 1
   - 一个事务完整提交前，需要等待两次刷盘，一次是 redo log（prepare 阶段），一次是 binlog

#### 组提交

- 日志逻辑序列号（log sequence number，LSN）的概念：LSN 是单调递增的，用来对应 redo log 的一个个写入点。每次写入长度为 length 的 redo log， LSN 的值就会加上 length。

![mysql二阶段提交.png](https://i.loli.net/2021/08/30/hzjXHMuCxgiG6ry.png)

- 如果你想提升 binlog 组提交的效果，可以通过设置 binlog_group_commit_sync_delay 和 binlog_group_commit_sync_no_delay_count 来实现。

  - **binlog_group_commit_sync_delay** 参数，表示延迟多少微秒后才调用 fsync;

  - **binlog_group_commit_sync_no_delay_count** 参数，表示累积多少次以后才调用 fsync。

----

### WAL 机制主要得益于两个方面：redo log 和 binlog 都是顺序写，磁盘的顺序写比随机写速度要快；组提交机制，可以大幅度降低磁盘的 IOPS 消耗。

#### 如果你的 MySQL 现在出现了性能瓶颈，而且瓶颈在 IO 上，可以通过哪些方法来提升性能呢？针对这个问题，可以考虑以下三种方法：

- 设置 binlog_group_commit_sync_delay 和 binlog_group_commit_sync_no_delay_count 参数，减少 binlog
  的写盘次数。这个方法是基于“额外的故意等待”来实现的，因此可能会增加语句的响应时间，但没有丢失数据的风险。

- 将 **sync_binlog** 设置为大于 1 的值（比较常见是 100~1000）。这样做的风险是，主机掉电时会丢 binlog 日志。

- 将 **innodb_flush_log_at_trx_commit** 设置为 2。这样做的风险是,主机掉电的时候会丢数据。


