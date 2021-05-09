### 1.kafka是什么

Apach Kafka 是一款分布式流处理框架，用于实时构建流处理应用。它有一个核心的功能广为人知，即作为企业级的消息引擎被广泛使用。


### 2.消费者组

关于它的定义，官网上的介绍言简意赅，即消费者组是 Kafka 提供的可扩展且具有容错性的消费者机制

在 Kafka 中，消费者组是一个由多个消费者实例构成的组。多个实例共同订阅若干个主题，实现共同消费。同一个组下的每个实例都配置有相同的组 ID，被分配不同的订阅分区。当某个实例挂掉的时候，其他实例会自动地承担起它负责消费的分区。

### 3.zookeeper的作用

目前，Kafka 使用 ZooKeeper 存放集群元数据、成员管理、Controller 选举，以及其他一些管理类任务。之后，等 KIP-500 提案完成后，Kafka 将完全不再依赖于 ZooKeeper。

- “存放元数据”是指主题分区的所有数据都保存在 ZooKeeper 中，且以它保存的数据为权威，其他“人”都要与它保持对齐。
  
- “成员管理”是指 Broker 节点的注册、注销以及属性变更，等等。
  
- “Controller 选举”是指选举集群 Controller，而
  
- 其他管理类任务包括但不限于主题删除、参数配置等。

### 4. kafka位移的作用

在 Kafka 中，每个主题分区下的每条消息都被赋予了一个唯一的 ID 数值，用于标识它在分区中的位置。

这个 ID 数值，就被称为位移，或者叫偏移量。一旦消息被写入到分区日志，它的位移值将不能被修改。

### 5.阐述下 Kafka 中的领导者副本（Leader Replica）和追随者副本（Follower Replica）的区别

Kafka 副本当前分为领导者副本和追随者副本。只有 Leader 副本才能对外提供读写服务，响应 Clients 端的请求。Follower 副本只是采用拉（PULL）的方式，被动地同步 Leader 副本中的数据，并且在 Leader 副本所在的 Broker 宕机后，随时准备应聘 Leader 副本。

- 强调 Follower 副本也能对外提供读服务。自 Kafka 2.4 版本开始，社区通过引入新的 Broker 端参数，允许 Follower 副本有限度地提供读服务

- 强调 Leader 和 Follower 的消息序列在实际场景中不一致。

### 6.如何设置 Kafka 能接收的最大消息的大小？

- Broker 端参数：**message.max.bytes**、**max.message.bytes（主题级别** 和 **replica.fetch.max.bytes**。

- Consumer 端参数：**fetch.message.max.bytes**

### 7. Broker 的 Heap Size 如何设置？

任何 Java 进程 JVM 堆大小的设置都需要仔细地进行考量和测试。

一个常见的做法是，以默认的初始 JVM 堆大小运行程序，当系统达到稳定状态后，手动触发一次 Full GC，然后通过 JVM 工具查看 GC 后的存活对象大小。之后，将堆大小设置成存活对象总大小的 1.5~2 倍。

对于 Kafka 而言，这个方法也是适用的。不过，业界有个最佳实践，那就是将 Broker 的 Heap Size 固定为 6GB。经过很多公司的验证，这个大小是足够且良好的。

### 8. 如何估算 Kafka 集群的机器数量？

- 所谓资源，也就是 CPU、内存、磁盘和带宽。

- 通常来说，CPU 和内存资源的充足是比较容易保证的，因此，你需要从磁盘空间和带宽占用两个维度去评估机器数量;在预估磁盘的占用时，你一定不要忘记计算副本同步的开销。如果一条消息占用 1KB 的磁盘空间，那么，在有 3 个副本的主题中，你就需要 3KB 的总空间来保存这条消息

- 常见的带宽有 1Gbps 和 10Gbps，但你要切记，这两个数字仅仅是最大值。因此，你最好和面试官确认一下给定的带宽是多少。然后，明确阐述出当带宽占用接近总带宽的 90% 时，丢包情形就会发生

### 9. Leader 总是 -1，怎么破

删除 ZooKeeper 节点 /controller，触发 Controller 重选举。Controller 重选举能够为所有主题分区重刷分区状态，可以有效解决因不一致导致的 Leader 不可用问题。

### 10. LEO、LSO、AR、ISR、HW 都表示什么含义？

- LEO：Log End Offset。日志末端位移值或末端偏移量，表示日志下一条待插入消息的位移值。举个例子，如果日志有 10 条消息，位移值从 0 开始，那么，第 10 条消息的位移值就是 9。此时，LEO = 10

- LSO：Log Stable Offset。这是 Kafka 事务的概念。如果你没有使用到事务，那么这个值不存在（其实也不是不存在，只是设置成一个无意义的值）。该值控制了事务型消费者能够看到的消息范围。
  它经常与 Log Start Offset，即日志起始位移值相混淆，因为有些人将后者缩写成 LSO，这是不对的。在 Kafka 中，LSO 就是指代 Log Stable Offset。
  
- AR：Assigned[分配] Replicas。AR 是主题被创建后，分区创建时被分配的副本集合，副本个数由副本因子决定

- SR：In-Sync Replicas。Kafka 中特别重要的概念，指代的是 AR 中那些与 Leader 保持同步的副本集合。
  在 AR 中的副本可能不在 ISR 中，但 Leader 副本天然就包含在 ISR 中。
  
- 关于 ISR，还有一个常见的面试题目是如何判断副本是否应该属于 ISR。目前的判断依据是：Follower 副本的 LEO 落后 Leader LEO 的时间，是否超过了 Broker 端参数 **replica.lag.time.max.ms** 值。如果超过了，副本就会被从 ISR 中移除

- HW：高水位值（High watermark）。这是控制消费者可读取消息范围的重要字段。一个普通消费者只能“看到”Leader 副本上介于 Log Start Offset 和 HW（不含）之间的所有消息。水位以上的消息是对消费者不可见的

### 11. Kafka 能手动删除消息吗？

其实，Kafka 不需要用户手动删除消息。它本身提供了留存策略，能够自动删除过期消息。当然，它是支持手动删除消息的。因此，你最好从这两个维度去回答

- 对于设置了 Key 且参数 **cleanup.policy=compact**的主题而言，我们可以构造一条 <Key，null> 的消息发送给 Broker，依靠 Log Cleaner 组件提供的功能删除掉该 Key 的消息。

- 对于普通主题而言，我们可以使用 kafka-delete-records 命令，或编写程序调用 Admin.deleteRecords 方法来删除消息。这两种方法殊途同归，底层都是调用 Admin 的 deleteRecords 方法，通过将分区 Log Start Offset 值抬高的方式间接删除消息

### 12. __consumer_offsets 是做什么用的

- 它是一个内部主题，无需手动干预，由 Kafka 自行管理。当然，我们可以创建该主题

- 它的主要作用是**负责注册消费者**以及**保存位移值**。可能你对保存位移值的功能很熟悉，但其实该主题也是保存消费者元数据的地方。千万记得把这一点也回答上。另外，这里的消费者泛指消费者组和独立消费者，而不仅仅是消费者组。

- Kafka 的 GroupCoordinator 组件提供对该主题完整的管理功能，包括该主题的创建、写入、读取和 Leader 维护等

### 13. 分区 Leader 选举策略有几种？

- **OfflinePartition Leader** 选举：每当有分区上线时，就需要执行 Leader 选举。所谓的分区上线，可能是创建了新分区，也可能是之前的下线分区重新上线。这是最常见的分区 Leader 选举场景

- **ReassignPartition Leader**选举:当你手动运行 kafka-reassign[重新分配]-partitions 命令，或者是调用 Admin 的 alterPartitionReassignments 方法执行分区副本重分配时，可能触发此类选举。假设原来的 AR 是[1，2，3]，Leader 是 1，当执行副本重分配后，副本集合 AR 被设置成[4，5，6]，显然，Leader 必须要变更，此时会发生 Reassign Partition Leader 选举。

- **PreferredReplicaPartition Leader** 选举：当你手动运行 kafka-preferred-replica-election 命令，或自动触发了 Preferred Leader 选举时，该类策略被激活。所谓的 Preferred Leader，指的是 AR 中的第一个副本。比如 AR 是[3，2，1]，那么，Preferred Leader 就是 3。

- **ControlledShutdownPartition Leader** 选举：当 Broker 正常关闭时，该 Broker 上的所有 Leader 副本都会下线，因此，需要为受影响的分区执行相应的 Leader 选举


### 14. Kafka 的哪些场景中使用了零拷贝（Zero Copy）？

在 Kafka 中，体现 Zero Copy 使用场景的地方有两处：基于 mmap 的索引和日志文件读写所用的 TransportLayer

- 基于 mmap 的索引

索引都是基于 MappedByteBuffer 的，也就是让用户态和内核态共享内核态的数据缓冲区，此时，数据不需要复制到用户态空间。不过，mmap 虽然避免了不必要的拷贝，但不一定就能保证很高的性能。在不同的操作系统下，mmap 的创建和销毁成本可能是不一样的。很高的创建和销毁开销会抵消 Zero Copy 带来的性能优势。由于这种不确定性，在 Kafka 中，只有索引应用了 mmap，最核心的日志并未使用 mmap 机制

- 日志文件读写所用的 TransportLayer

TransportLayer 是 Kafka 传输层的接口。它的某个实现类使用了 FileChannel 的 transferTo 方法。该方法底层使用 sendfile 实现了 Zero Copy。对 Kafka 而言，如果 I/O 通道使用普通的 PLAINTEXT，那么，Kafka 就可以利用 Zero Copy 特性，直接将页缓存中的数据发送到网卡的 Buffer 中，避免中间的多次拷贝。相反，如果 I/O 通道启用了 SSL，那么，Kafka 便无法利用 Zero Copy 特性了。

### 15.Kafka 为什么不支持读写分离？

Leader/Follower 模型并没有规定 Follower 副本不可以对外提供读服务。很多框架都是允许这么做的，只是 Kafka 最初为了避免不一致性的问题，而采用了让 Leader 统一提供服务的方式。

自 Kafka 2.4 之后，Kafka 提供了有限度的读写分离，也就是说，Follower 副本能够对外提供读服务

- 场景不适用。读写分离适用于那种读负载很大，而写操作相对不频繁的场景，可 Kafka 不属于这样的场景。

- 同步机制。Kafka 采用 PULL 方式实现 Follower 的同步，因此，Follower 与 Leader 存在不一致性窗口。如果允许读 Follower 副本，就势必要处理消息滞后（Lagging）的问题。

### 16.如何调优 Kafka？

回答任何调优问题的第一步，就是确定优化目标，并且定量给出目标;确定了目标之后，还要明确优化的维度。有些调优属于通用的优化思路，比如对操作系统、JVM 等的优化；


#### 有些则是有针对性的，比如要优化 Kafka 的 TPS

- Producer 端：增加 **batch.size、linger.ms**，启用压缩，关闭重试等

- Broker 端：增加 **num.replica.fetchers**，提升 Follower 同步 TPS，避免 Broker Full GC 等。

- Consumer：增加 fetch.min.bytes 等

### 17.Controller 发生网络分区（Network Partitioning）时，Kafka 会怎么样？

一旦发生 Controller 网络分区，那么，第一要务就是查看集群是否出现“脑裂”，即同时出现两个甚至是多个 Controller 组件。这可以根据 Broker 端监控指标 ActiveControllerCount 来判断。

由于 Controller 会给 Broker 发送 3 类请求，即 LeaderAndIsrRequest、StopReplicaRequest 和 UpdateMetadataRequest，因此，一旦出现网络分区，这些请求将不能顺利到达 Broker 端。这将影响主题的创建、修改、删除操作的信息同步，表现为集群仿佛僵住了一样，无法感知到后面的所有操作。因此，网络分区通常都是非常严重的问题，要赶快修复。

### 18. Java Consumer 为什么采用单线程来获取消息？

Java Consumer 是双线程的设计。一个线程是用户主线程，负责获取消息；另一个线程是心跳线程，负责向 Kafka 汇报消费者存活情况。将心跳单独放入专属的线程，能够有效地规避因消息处理速度慢而被视为下线的“假死”情况。

单线程获取消息的设计能够避免阻塞式的消息获取方式。单线程轮询方式容易实现异步非阻塞式，这样便于将消费者扩展成支持实时流处理的操作算子。因为很多实时流处理操作算子都不能是阻塞式的。另外一个可能的好处是，可以简化代码的开发。多线程交互的代码是非常容易出错的。

### 19. 简述 Follower 副本消息同步的完整流程

首先，Follower 发送 FETCH 请求给 Leader。接着，Leader 会读取底层日志文件中的消息数据，再更新它内存中的 Follower 副本的 LEO 值，更新为 FETCH 请求中的 fetchOffset 值。最后，尝试更新分区高水位值。Follower 接收到 FETCH 响应之后，会把消息写入到底层日志，接着更新 LEO 和 HW 值。

Leader 和 Follower 的 HW 值更新时机是不同的，Follower 的 HW 更新永远落后于 Leader 的 HW。这种时间上的错配是造成各种不一致的原因。