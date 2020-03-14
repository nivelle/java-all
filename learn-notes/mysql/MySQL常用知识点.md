## 创建数据库

```
CREATE DATABASE javaguideslave;
```

### 指定编码

```
drop database if EXISTS teambuild;
create database teambuild CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```




## 查看数据库版本


```
select version();

+-----------+
| version() |
+-----------+
| 8.0.16    |
+-----------+
```


## 查看当前默认的数据库引擎


```
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

## 初始化表

```
DROP TABLE IF EXISTS `activity_pv`;
CREATE TABLE `activity_pv` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` varchar(32) NOT NULL default '' COMMENT '活动id',
  `position_type` tinyint(10) NOT NULL default 0 COMMENT '广告位置 1.活动页 2.首页',
  `ip` varchar (15) NOT NULL DEFAULT '' COMMENT 'ip地址',
  `device_type` varchar(32) NOT NULL default '' COMMENT '设备类型',
  `device_no` varchar (32) NOT NULL default '' COMMENT '设备号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```



## 创建账号

```
create user 'root'@'%' identified with mysql_native_password by 'Nivelle123';
```

## 给当前账号授权

```
GRANT ALL PRIVILEGES ON javaguides.* TO 'root'@'localhost' IDENTIFIED BY 'Nivelle123' WITH GRANT OPTION;
```

## 查看数据库

```
show databases;
```

## 使用某个数据库

```
use databases;
```

## 查看所有表
show tables;

## 创建唯一索引

```
create unique index uniq_device_no on activity_pv(`device_no`);
```

## 创建联合索引

```
CREATE INDEX device ON activity_pv (`device_type`,`device_no`);
```

## 查看索引

```
show index from activity_pv;

+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| Table       | Non_unique | Key_name       | Seq_in_index | Column_name | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment | Visible | Expression |
+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| activity_pv |          0 | PRIMARY        |            1 | id          | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity_pv |          0 | uniq_device_no |            1 | device_no   | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity_pv |          1 | device         |            1 | device_type | A         |           2 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity_pv |          1 | device         |            2 | device_no   | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
```



## 删除整个表数据后整理表

```
alter table A engine=InnoDB;

```

## 查看当前的查询状态

```

show processlist;

```

## 可重复读情况下才有间隙锁,你如果把隔离级别设置为读提交的话，就没有间隙锁了。但同时，你要解决可能出现的数据和日志不一致问题，需要把 binlog 格式设置为 row。

- 原则 1：加锁的基本单位是 next-key lock。希望你还记得，next-key lock 是前开后闭区间。

- 原则 2：查找过程中访问到的对象才会加锁。

- 优化 1：索引上的等值查询，给唯一索引加锁的时候，next-key lock 退化为行锁。

- 优化 2：索引上的等值查询，向右遍历时且最后一个值不满足等值条件的时候，next-key lock 退化为间隙锁。

- 一个 bug：唯一索引上的范围查询会访问到不满足条件的第一个值为止。