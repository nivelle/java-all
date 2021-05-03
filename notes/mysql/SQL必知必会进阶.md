### 建表

````
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
CREATE TABLE `tb_stu` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键自增id',
                          `name` varchar(32) DEFAULT NULL,
                          `subject` varchar(32) DEFAULT NULL,
                          `score` int(11) DEFAULT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB;

````

### 初始化数据

````
insert into tb_stu (`name`,`subject`,`score`) values ("zhangsan","shuxue",90),("zhangsan","yuwen",50),("zhangsan","dili",40),("lisi","dili",40),("zhangsan","yuwen",55),("wangwu","zhengzhi",30),("wangwu","yuwen",70);

````

#### 查询出两门及两门以上不及格者的平均成绩(注意是所有科目的平均成绩)

- 用count(score<60)查不到: count(a),无论a是什么，都只是数一行；count时，每遇到一行，就数一个a，跟条件无关！

````
select name,avg(score),sum(score<60) as gk from tb_stu group by name having gk>=2;
+----------+------------+------+
| name     | avg(score) | gk   |
+----------+------------+------+
| lisi     |    47.5000 |    2 |
| zhangsan |    60.0000 |    2 |
+----------+------------+------+


````