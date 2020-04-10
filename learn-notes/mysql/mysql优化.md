
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

- innodb_buffer_pool_size : 默认8M

- innodb_buffer_pool_instances

- innodb_read_io_threads / innodb_write_io_threads

- innodb_log_file_size

- innodb_log_buffer_size

- innodb_flush_log_at_trx_commit

- max_connections

- back_log

- thread_cache_size