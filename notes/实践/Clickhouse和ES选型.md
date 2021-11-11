- Elasticsearch 是一个实时的分布式搜索分析引擎，它的底层是构建在Lucene之上的。简单来说是通过扩展Lucene的搜索能力，使其具有分布式的功能。ES通常会和其它两个开源组件logstash（日志采集）和Kibana（仪表盘）一起提供端到端的日志/搜索分析的功能，常常被简称为ELK。

- Clickhouse是俄罗斯搜索巨头Yandex开发的面向列式存储的关系型数据库。ClickHouse是过去两年中OLAP领域中最热门的，并于2016年开源。

ES是最为流行的大数据日志和搜索解决方案，但是近几年来，它的江湖地位受到了一些挑战，许多公司已经开始把自己的日志解决方案从ES迁移到了Clickhouse，这里就包括：携程，快手等公司。

### 架构和设计的对比

### ES
- ES的底层是Lucenc，主要是要解决搜索的问题。搜索是大数据领域要解决的一个常见的问题，就是在海量的数据量要如何按照条件找到需要的数据。搜索的核心技术是**倒排索引和布隆过滤器**。ES通过分布式技术，利用分片与副本机制，直接解决了集群下搜索性能与高可用的问题。

![](https://img-blog.csdnimg.cn/img_convert/b6eb9b155a77ee7fe2164cb1c642f170.png)

ElasticSearch是为分布式设计的，有很好的扩展性，在一个典型的分布式配置中，每一个节点（node）可以配制成不同的角色，如下图所示：

![](https://img-blog.csdnimg.cn/img_convert/f46b1988a23de810cd7ffbc026c1512e.png)

- Client Node，负责API和数据的访问的节点，不存储／处理数据

- Data Node，负责数据的存储和索引

- Master Node， 管理节点，负责Cluster中的节点的协调，不存储数据。

### Clickhouse

ClickHouse是基于MPP架构的分布式ROLAP（关系OLAP）分析引擎。每个节点都有同等的责任，并负责部分数据处理（不共享任何内容）。ClickHouse 是一个真正的列式数据库管理系统（DBMS)。在 ClickHouse 中，数据始终是按列存储的，包括矢量（向量或列块）执行的过程。让查询变得更快，最简单且有效的方法是减少数据扫描范围和数据传输时的大小，而列式存储和数据压缩就可以帮助实现上述两点。Clickhouse同时使用了日志合并树，稀疏索引和CPU功能（如SIMD单指令多数据）充分发挥了硬件优势，可实现高效的计算。Clickhouse 使用Zookeeper进行分布式节点之间的协调。

![](https://img-blog.csdnimg.cn/img_convert/f26a3823e92442f2907aaf1a0b013b13.png)

### 对比

- 通过测试数据我们可以看出Clickhouse在大部分的查询的性能上都明显要优于Elastic。在正则查询（Regex query）和单词查询（Term query）等搜索常见的场景下，也并不逊色。

- 在聚合场景下，Clickhouse表现异常优秀，充分发挥了列村引擎的优势。