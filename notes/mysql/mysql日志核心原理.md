### mysql 逻辑架构

![mysql逻辑架构图.png](https://i.loli.net/2020/04/07/1NdFLzB9hnlPKkc.png)

### 性能配置

#### –skip-grant-tables :跳过权限验证

#### 增删改数据（DML),修改表结构的操作（DDL)

#### set global slow_query_log = on;  //开启慢查询日志

#### 如果你的 MySQL 现在出现了性能瓶颈，而且瓶颈在 IO 上，可以通过哪些方法来提升性能呢？

1. 设置 binlog_group_commit_sync_delay 和 binlog_group_commit_sync_no_delay_count 参数,减少 binlog 的写盘次数。这个方法是基于“额外的故意等待”来实现的，因此可能会增加语句的响应时间，但没有丢失数据的风险。

2. 将 sync_binlog 设置为大于 1 的值（比较常见是 100~1000）。这样做的风险是，主机掉电时会丢 binlog 日志。

3. 将 innodb_flush_log_at_trx_commit 设置为 2。这样做的风险是，主机掉电的时候会丢数据。

---

### mysql 日志

- redo log(重做日志)

1. redo log 是 InnoDB 存储引擎层的日志,**记录的是修改之后的值**;

2. 在一条更新语句进行执行的时候，InnoDB引擎会把更新记录写到redo log日志中，然后更新内存，此时算是语句执行完了，
然后在空闲的时候或者是按照设定的更新策略将redo log中的内容更新到磁盘中，这里涉及到WAL即Write Ahead logging技术，他的关键点是先写日志，再写磁盘。

3. redo log日志的大小是固定的，即记录满了以后就从头循环写;记录满时要停止数据库更新操作,转而将日志刷到磁盘;
   
4. 可以根据redo log日志进行恢复，也就达到了 crash-safe

- binlog(归档日志)。

1. binlog是MySQL Server层记录的日志,属于逻辑日志

2. 是以二进制的形式记录的是这个**语句的原始逻辑**，依靠binlog是没有crash-safe能力的
   

redoLog | bingLog
---|---
属于innoDB层面 | 属于MySQL Server层面的，这样在数据库用别的存储引擎时可以达到一致性的要求
物理日志，记录该数据页更新的内容| 逻辑日志，记录的是这个更新语句的原始逻辑
循环写，日志空间大小固定| 追加写，是指一份写到一定大小的时候会更换下一个文件，不会覆盖
作为异常宕机或者介质故障后的数据恢复使用|可以作为恢复数据使用，主从复制搭建


redoLog | undoLog
---|---
用来恢复提交后的物理数据页(恢复数据页,且只能恢复到最后一次提交的位置) | 记录事务开始前的状态,用来回滚行记录到某个版本;undo log一般是逻辑日志，根据每行记录进行记录。
确保事务的持久性。防止在发生故障的时间点，尚有脏页未写入磁盘,在重启mysql服务的时候,根据redo log进行重做,从而达到事务的持久性这一特性。| 保存了事务发生之前的数据的一个版本，可以用于回滚，同时可以提供多版本并发控制下的读（MVCC），也即非锁定读

### 一条更新语句的执行过程

update T set c=c+1 where ID=2;

- 执行器先找引擎取 ID=2 这一行。ID 是主键，引擎直接用树搜索找到这一行。如果 ID=2 这一行所在的数据页本来就在内存中，就直接返回给执行器；否则，需要先从磁盘读入内存，然后再返回。

- 执行器拿到引擎给的行数据，把这个值加上 1，比如原来是 N，现在就是 N+1，得到新的一行数据，再调用引擎接口写入这行新数据

- 引擎将这行新数据更新到内存中，同时将这个更新操作记录到 redo log 里面，此时 redo log 处于 prepare 状态。然后告知执行器执行完成了，随时可以提交事务。

- 执行器生成这个操作的 binlog，并把 binlog 写入磁盘。

- 执行器调用引擎的提交事务接口，引擎把刚刚写入的 redo log 改成提交（commit）状态，更新完成。


### 库

#### 创建数据库

```
CREATE DATABASE javaguideslave;

```

#### 指定编码

```
drop database if EXISTS teambuild;
create database teambuild CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 查看数据库版本


```
select version();

+-----------+
| version() |
+-----------+
| 8.0.16    |
+-----------+
```

#### 查看数据库隔离级别(默认RR重复读)

```
mysql> select @@global.tx_isolation;
+-----------------------+
| @@global.tx_isolation |
+-----------------------+
| REPEATABLE-READ       |
+-----------------------+
```

#### 查看当前默认的数据库引擎


```
show engines;  

+--------------------+---------+----------------------------------------------------------------+--------------+------+------------+
| Engine             | Support | Comment                                                        | Transactions | XA   | Savepoints |
+--------------------+---------+----------------------------------------------------------------+--------------+------+------------+
| ARCHIVE            | YES     | Archive storage engine                                         | NO           | NO   | NO         |
| BLACKHOLE          | YES     | /dev/null storage engine (anything you write to it disappears) | NO           | NO   | NO         |
| MRG_MYISAM         | YES     | Collection of identical MyISAM tables                          | NO           | NO   | NO         |
| FEDERATED          | NO      | Federated MySQL storage engine                                 | NULL         | NULL | NULL       |
| MyISAM             | YES     | MyISAM storage engine                                          | NO           | NO   | NO         |
| PERFORMANCE_SCHEMA | YES     | Performance Schema                                             | NO           | NO   | NO         |
| InnoDB             | DEFAULT | Supports transactions, row-level locking, and foreign keys     | YES          | YES  | YES        |
| MEMORY             | YES     | Hash based, stored in memory, useful for temporary tables      | NO           | NO   | NO         |
| CSV                | YES     | CSV storage engine                                             | NO           | NO   | NO         |
+--------------------+---------+----------------------------------------------------------------+--------------+------+------------+
```
#### 查看当前的查询状态

```

show processlist;

```

#### 创建账号

```
create user 'root'@'%' identified with mysql_native_password by 'Nivelle123';
```

#### 给当前账号授权

```
GRANT ALL PRIVILEGES ON javaguides.* TO 'root'@'localhost' IDENTIFIED BY 'Nivelle123' WITH GRANT OPTION;

```

#### 查看数据库

```
show databases;

```

#### 使用某个数据库

```
use databases;

```
#### 删除某个数据库

```
drop database databasesName;

```

---

### 表

#### 初始化表

```
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` varchar(32) NOT NULL default '' COMMENT '活动id',
  `position_type` tinyint(10) NOT NULL default 0 COMMENT '广告位置 1.活动页 2.首页',
  `ip` varchar (15) NOT NULL DEFAULT '' COMMENT 'ip地址',
  `type` varchar(32) NOT NULL default '' COMMENT '设备类型',
  `device_no` varchar (32) NOT NULL default '' COMMENT '设备号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

```

#### 查看所有表

```
show tables;
```
#### 删除表

```
drop table tableName;
```

#### 修改表名

```
alter table oldTableName rename to newTableName;
```
#### 修改列

```
alter table tableName change oldName newName;

```

#### 添加列

```
alter table tableName add column 列名 类型；

```
#### 修改列属性

```
alter table 表名 modify name varchar(22);

```

#### 删除整个表数据后整理表

```
alter table A engine=InnoDB;

```

---

