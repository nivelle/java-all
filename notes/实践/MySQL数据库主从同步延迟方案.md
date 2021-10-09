### 一、MySQL的数据库主从复制原理

MySQL主从复制实际上基于二进制日志，原理可以用一张图来表示：

![](http://p9.pstatp.com/large/53e300012f4ea58c1165)

### 分为四步走：

1. 主库对所有DDL和DML产生的日志写进binlog；

2. 主库生成一个 __log dump 线程__，用来给从库 __I/O线程__ 读取binlog；

3. 从库的 __I/O Thread__ 去请求主库的binlog，并将得到的binlog日志写到 __relay log__ 文件中；

4. 从库的 __SQL Thread__ 会读取relay log文件中的日志解析成具体操作，将主库的DDL和DML操作事件重放。

### 关于DDL和DML

SQL语言共分为四大类：查询语言DQL，控制语言DCL，操纵语言DML，定义语言DDL。

DQL：可以简单理解为SELECT语句；

DCL：GRANT、ROLLBACK和COMMIT一类语句；

DML：可以理解为CREATE一类的语句；

DDL：INSERT、UPDATE和DELETE语句都是；

### 主从复制存在的问题

- 主库宕机后，数据可能丢失；

- 主从同步延迟。

### MySQL数据库主从同步延迟产生原因
#### 原因分析
- MySQL的主从复制都是单线程的操作，主库对所有DDL和DML产生的日志写进binlog，由于binlog是顺序写，所以效率很高。Slave的SQL Thread线程将主库的DDL和DML操作事件在slave中重放。DML和DDL的IO操作是随即的，不是顺序的，成本高很多。

- 由于SQL Thread也是单线程的，当主库的并发较高时，产生的DML数量超过slave的SQL Thread所能处理的速度，或者当slave中有大型query语句产生了锁等待那么延时就产生了。

#### 常见原因

- Master负载过高、Slave负载过高、网络延迟、机器性能太低、MySQL配置不合理。

### 主从延时排查方法

通过监控 show slave status 命令输出的Seconds_Behind_Master参数的值来判断：

- NULL，表示io_thread或是sql_thread有任何一个发生故障；

- 0，该值为零，表示主从复制良好；

- 正值，表示主从已经出现延时，数字越大表示从库延迟越严重。

![](https://images2018.cnblogs.com/blog/1032486/201804/1032486-20180420163800563-1378244978.png)

### 解决方案

### 解决数据丢失问题
#### 半同步复制：

从MySQL5.5开始，MySQL已经支持半同步复制了，半同步复制介于异步复制和同步复制之间，主库在执行完事务后不立刻返回结果给客户端，需要等待至少一个从库接收到并写到relay log中才返回结果给客户端。相对于异步复制，半同步复制提高了数据的安全性，同时它也造成了一个TCP/IP往返耗时的延迟。

#### 主库配置sync_binlog=1，innodb_flush_log_at_trx_commit=1

- sync_binlog的默认值是0，MySQL不会将binlog同步到磁盘，其值表示每写多少binlog同步一次磁盘。


- innodb_flush_log_at_trx_commit为1表示每一次事务提交或事务外的指令都需要把日志flush到磁盘。

注意:将以上两个值同时设置为1时，写入性能会受到一定限制，只有对数据安全性要求很高的场景才建议使用，比如涉及到钱的订单支付业务，而且系统I/O能力必须可以支撑！

### 解决从库复制延迟的问题：

1. 优化网络

2. 升级Slave硬件配置

3. Slave调整参数，关闭binlog，修改innodb_flush_log_at_trx_commit参数值

4. 升级MySQL版本到5.7，使用并行复制

