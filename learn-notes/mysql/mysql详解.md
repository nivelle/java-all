
## 性能配置

### –skip-grant-tables :跳过权限验证

### 增删改数据（DML),修改表结构的操作（DDL)

### set global slow_query_log = on;  //开启慢查询日志


### 如果你的 MySQL 现在出现了性能瓶颈，而且瓶颈在 IO 上，可以通过哪些方法来提升性能呢？

1. 设置 binlog_group_commit_sync_delay 和 binlog_group_commit_sync_no_delay_count 参数，减少 binlog 的写盘次数。这个方法是基于“额外的故意等待”来实现的，因此可能会增加语句的响应时间，但没有丢失数据的风险。

2. 将 sync_binlog 设置为大于 1 的值（比较常见是 100~1000）。这样做的风险是，主机掉电时会丢 binlog 日志。

3. 将 innodb_flush_log_at_trx_commit 设置为 2。这样做的风险是，主机掉电的时候会丢数据。

---

## 库

## 创建数据库

```
CREATE DATABASE javaguideslave;

```

### 指定编码

```
drop database if EXISTS teambuild;
create database teambuild CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 查看数据库版本


```
select version();

+-----------+
| version() |
+-----------+
| 8.0.16    |
+-----------+
```

### 查看数据库隔离级别(默认RR重复读)

```
mysql> select @@global.tx_isolation;
+-----------------------+
| @@global.tx_isolation |
+-----------------------+
| REPEATABLE-READ       |
+-----------------------+
```

### 查看当前默认的数据库引擎


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
### 查看当前的查询状态

```

show processlist;

```

### 创建账号

```
create user 'root'@'%' identified with mysql_native_password by 'Nivelle123';
```

### 给当前账号授权

```
GRANT ALL PRIVILEGES ON javaguides.* TO 'root'@'localhost' IDENTIFIED BY 'Nivelle123' WITH GRANT OPTION;

```

### 查看数据库

```
show databases;

```

### 使用某个数据库

```
use databases;

```
### 删除某个数据库

```
drop database databasesName;

```

---

## 表

### 初始化表

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

### 查看所有表

```
show tables;
```
### 删除表
```
drop table tableName;
```

### 修改表名

```
alter table oldTableName rename to newTableName;
```
### 修改列

```
alter table tableName change oldName newName;

```

### 添加列

```
alter table tableName add column 列名 类型；

```
### 修改列属性

```
alter table 表名 modify name varchar(22);

```

### 删除整个表数据后整理表

```
alter table A engine=InnoDB;

```

---

## 索引

### InnoDB的索引有两类索引，聚集索引(Clustered Index)与普通索引(Secondary Index)。

- InnoDB的每一个表都会有聚集索引(叶子节点存储行记录(row)；):

 (1)如果表定义了PK，则PK就是聚集索引;
 
 (2)如果表没有定义PK，则第一个非空unique列是聚集索引;
 
 (3)否则，InnoDB会创建一个隐藏的row-id作为聚集索引；

- 普通索引，叶子节点存储了PK的值；



### 创建唯一索引

```
create unique index uniq_device_no on activity(`device_no`);

```

### 创建联合索引

```

CREATE INDEX device ON activity (`type`,`device_no`);

```

### 查看索引

```
show index from activity;

+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| Table       | Non_unique | Key_name       | Seq_in_index | Column_name | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment | Visible | Expression |
+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| activity |          0 | PRIMARY        |            1 | id          | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity |          0 | uniq_device_no |            1 | device_no   | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity |          1 | device         |            1 | type | A         |           2 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity |          1 | device         |            2 | device_no   | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+

```

---

## 锁

### 锁类型
 
(1) 共享/排它锁(Shared and Exclusive Locks)
    
    - for update //排他当前读,加的是意向排它记录锁,读到所有已经提交的记录值
   
    - lock in share mode;//共享当前读,意向共读记录锁

(2)意向锁(Intention Locks)

```
意向锁之间的互斥关系:

        IS        IX
IS      兼容      兼容
IX      兼容      兼容

--------------------------------

         S       X
IS      兼容      互斥
IX      互斥      互斥

```

(3)记录锁(Record Locks)

(4)间隙锁(Gap Locks)

   - 间隙锁的主要目的，就是为了防止其他事务在间隔中插入数据，以导致“不可重复读”。
   
   - 如果把事务的隔离级别降级为读提交(Read Committed, RC)，间隙锁则会自动失效。
   
   - between,>,<等for update,lock in share mode 造成间隙锁则可能会阻塞update事务

(5)临键锁(Next-key Locks)

(6)插入意向锁(Insert Intention Locks)

(7)自增锁(Auto-inc Locks)

```
自增锁是一种特殊的表级别锁（table-level lock),专门针对事务插入AUTO_INCREMENT类型的列。

最简单的情况，如果一个事务正在往表中插入记录，所有其他事务的插入必须等待，以便第一个事务插入的行，是连续的主键值。

```

#### MySQL 里面表级别的锁有两种：

(1) 表锁 

````
表锁的语法是 lock tables … read/write

如果在某个线程 A 中执行 lock tables t1 read, t2 write; 这个语句，则其他线程写 t1、读写 t2 的语句都会被阻塞。同时，线程 A 在执行 unlock tables 之前，也只能执行读 t1、读写 t2 的操作。连写 t1 都不允许，自然也不能访问其他表

````

(2)元数据锁（meta data lock，MDL)

```
MDL 不需要显式使用，在访问一个表的时候会被自动加上。MDL 的作用是，保证读写的正确性。

```

当对一个表做增删改查操作的时候，加 MDL 读锁；当要对表做结构变更操作的时候，加 MDL 写锁。

#### 可重复读情况下才有间隙锁,你如果把隔离级别设置为读提交的话，就没有间隙锁了。但同时，你要解决可能出现的数据和日志不一致问题，需要把 binlog 格式设置为 row。

- 原则 1：加锁的基本单位是 next-key lock。希望你还记得，next-key lock 是前开后闭区间。

**更新或查询一个范围内主键，如果不存在则会加存在的主键的间隙锁，此时另外一个事务插入则会阻塞；如果存在则会退化为行锁,此时另外一个事务插入则不会被阻塞了。**
  
- 原则 2：查找过程中访问到的对象才会加锁。

- 优化 1：索引上的等值查询，给唯一索引加锁的时候，next-key lock 退化为行锁。

- 优化 2：索引上的等值查询，向右遍历时且最后一个值不满足等值条件的时候，next-key lock 退化为间隙锁。

- 一个 bug：唯一索引上的范围查询会访问到不满足条件的第一个值为止。


### 死锁问题解决

- 直接进入等待，直到超时。这个超时时间可以通过参数 **innodb_lock_wait_timeout** 来设置

- 发起死锁检测，发现死锁后，主动回滚死锁链条中的某一个事务，让其他事务得以继续执行。将参数 **innodb_deadlock_detect** 设置为 on，表示开启这个逻辑。(加锁访问的行上有锁，他才要检测)

### 事务

```
- start transaction;

- 添加事务的SQL语句

- commit or rollback;

```

#### 隔离级别

- RR下,事务在第一个read操作时,建立Read view.注意不是 start transaction

- RC下,事务在每次read操作时，都会建立Read view

#### 幻读

产生幻读的原因是:行锁只能锁住行，但是新插入记录这个动作，要更新的是记录之间的“间隙”。因此，为了解决幻读问题，InnoDB 只好引入新的锁，也就是间隙锁 (Gap Lock)。

跟间隙锁存在冲突关系的，是“往这个间隙中插入一个记录”这个操作。间隙锁之间都不存在冲突关系

- 在可重复读隔离级别下，普通的查询是快照读，是不会看到别的事务插入的数据的。因此，幻读在“当前读”下才会出现。

- 幻读仅仅指的是插入行，修改行不算幻读;


