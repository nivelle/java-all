## 创建数据库
CREATE DATABASE javaguideslave;

### 指定编码
drop database if EXISTS teambuild;
create database teambuild CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;



## 查看数据库版本

select version();

+-----------+
| version() |
+-----------+
| 8.0.16    |
+-----------+

## 查看当前默认的数据库引擎

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


## 初始化表
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


## 创建账号
create user 'root'@'%' identified with mysql_native_password by 'Nivelle123';

## 给当前账号授权
GRANT ALL PRIVILEGES ON javaguides.* TO 'root'@'localhost' IDENTIFIED BY 'Nivelle123' WITH GRANT OPTION;

## 查看数据库
show databases;

## 使用某个数据库
use databases;

## 查看所有表
show tables;

## 创建唯一索引
create unique index uniq_device_no on activity_pv(`device_no`);

## 创建联合索引
CREATE INDEX device ON activity_pv (`device_type`,`device_no`);

## 查看索引
show index from activity_pv;

+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| Table       | Non_unique | Key_name       | Seq_in_index | Column_name | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment | Visible | Expression |
+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| activity_pv |          0 | PRIMARY        |            1 | id          | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity_pv |          0 | uniq_device_no |            1 | device_no   | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity_pv |          1 | device         |            1 | device_type | A         |           2 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| activity_pv |          1 | device         |            2 | device_no   | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
+-------------+------------+----------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+


## explain 的用法

1. mysql> explain select * from activity_pv where id = 6 union select * from activity_pv where id =9;
### 当通过union来连接多个查询结果时,第二个之后的select其select_type为UNION

+----+--------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+--------------------------------+
| id | select_type  | table       | partitions | type  | possible_keys | key     | key_len | ref   | rows | filtered | Extra                          |
+----+--------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+--------------------------------+
|  1 | PRIMARY     | activity_pv | NULL       | const | PRIMARY       | PRIMARY | 8       | const |    1 |   100.00 | NULL                           |
|  2 | UNION        | NULL        | NULL       | NULL  | NULL          | NULL    | NULL    | NULL  | NULL |     NULL | no matching row in const table |
| NULL | UNION RESULT | <union1,2>  | NULL       | ALL   | NULL          | NULL    | NULL    | NULL  | NULL |     NULL | Using temporary                |
+----+--------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+--------------------------------+

2. mysql> explain select * from activity_pv where id in (select id from activity_pv where id = 6 union select id from activity_pv where id =7);
### 当union作为子查询时,其中第二个union的select_type就是DEPENDENT UNION.第一个子查询的select_type则是DEPENDENT SUBQUERY.

+----+--------------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+-----------------+
| id | select_type        | table       | partitions | type  | possible_keys | key     | key_len | ref   | rows | filtered | Extra           |
+----+--------------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+-----------------+
|  1 | PRIMARY            | activity_pv | NULL       | ALL   | NULL          | NULL    | NULL    | NULL  |    3 |   100.00 | Using where     |
|  2 | DEPENDENT SUBQUERY | activity_pv | NULL       | const | PRIMARY       | PRIMARY | 8       | const |    1 |   100.00 | Using index     |
|  3 | DEPENDENT UNION    | activity_pv | NULL       | const | PRIMARY       | PRIMARY | 8       | const |    1 |   100.00 | Using index     |
| NULL | UNION RESULT       | <union2,3>  | NULL       | ALL   | NULL          | NULL    | NULL    | NULL  | NULL |     NULL | Using temporary |
+----+--------------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+-----------------+

3. mysql> explain select * from activity_pv where id =  (select id from activity_pv where id =9);

mysql> explain select * from activity_pv where id =  (select id from activity_pv where id =7);
## 子查询的第一个select 其select type 为SUBQUERY
+----+-------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+-------------+
| id | select_type | table       | partitions | type  | possible_keys | key     | key_len | ref   | rows | filtered | Extra       |
+----+-------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+-------------+
|  1 | PRIMARY     | activity_pv | NULL       | const | PRIMARY       | PRIMARY | 8       | const |    1 |   100.00 | NULL        |
|  2 | SUBQUERY    | activity_pv | NULL       | const | PRIMARY       | PRIMARY | 8       | const |    1 |   100.00 | Using index |
+----+-------------+-------------+------------+-------+---------------+---------+---------+-------+------+----------+-------------+

4. mysql> explain select * from user_info u,sys_user_role ur where u.uid = ur.uid;

## 对于每个来自于前面的表的行组合,从该表中读取一行.这可能是最好的联接类型,除了const类型.
+----+-------------+-------+------------+--------+---------------+---------+---------+-------------------+------+----------+-------+
| id | select_type | table | partitions | type   | possible_keys | key     | key_len | ref               | rows | filtered | Extra |
+----+-------------+-------+------------+--------+---------------+---------+---------+-------------------+------+----------+-------+
|  1 | SIMPLE      | ur    | NULL       | ALL    | NULL          | NULL    | NULL    | NULL              |    1 |   100.00 | NULL  |
|  1 | SIMPLE      | u     | NULL       | eq_ref | PRIMARY       | PRIMARY | 8       | javaguides.ur.uid |    1 |   100.00 | NULL  |
+----+-------------+-------+------------+--------+---------------+---------+---------+-------------------+------+----------+-------+
