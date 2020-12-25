### 消息分区

#### 结构：

- kafka 消息组织方式是三级结构： 主题-分区-消息

#### 作用：

- 提供负载能力，提高系统的伸缩性。

- 不同的分区放置在不同节点的机器上，而数据读操作也都是针对分区这个粒度进行的，这样每个节点的机器都能独立执各自分区的读写请求处理

- 可以通过增加新的节点来增加整个系统的吞吐量

#### 分区策略

- 自定义分区策略：partitioner.calss = full Qualified Name

- 轮询策略（round-robin）: kafka 默认提供的分区策略，能保证消息最大限度第被平均分配到所有分区上，默认情况的最合理

- 随机策略（randomness）：随机策略是老版本生产者使用的分区策略，在新版本中已经改为轮询

**kafka默认分区策略：默认策略为：如果指定了partition就直接发送到该分区；如果没有指定分区但是指定了key，就按照key的hash值选择分区；如果partition和key都没有指定就使用轮询策略。而且如果key不为null，那么计算得到的分区号会是所有分区中的任意一个；如果key为null并且有可用分区时，那么计算得到的分区号仅为可用分区中的任意一个**

````

List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
return ThreadLocalRandom.current().nextInt(partitions.size());
````

- 按消息键保序策略：kafka允许为每条消息定义消息键，简称为key. kafka同一个key的所有消息都进入到相同的分区里面，由于每个分区下的消息处理都是有顺序的，故这个策略被成为按消息键保序策略


