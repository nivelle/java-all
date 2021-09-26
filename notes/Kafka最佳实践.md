OS调优
1 页缓存：尽量分配与所有日志的激活日志段大小相同的页缓存大小
2 文件描述符限制： 10万以上
3 禁掉swap
4 使用Java 8和G1，分配6~8GB的堆大小
磁盘调优
1 使用多块磁盘，专属分配给kafka
2 一般环境使用JBOD即可，但JBOD有一些固有的缺陷，比如磁盘失败将导致Kafka异常关闭，造成数据不一致，社区已经着手解决
3 使用EXT4或XFS
4 尽量使用SSD
基本监控
1 CPU负载
2 网络带宽
3 文件句柄数
4 磁盘空间
5 磁盘IO性能
6 垃圾回收
7 zookeeper监控
如何监控备份不足情况发生？
JMX指标：kafka.server:type=ReplicaManager,name=UnderReplicatedPartitions

可能原因
broker挂了
controller问题
zk问题
网络问题
解决办法
调整ISR参数，比如 min.insync.replica和replica.lag.time.max.ms, num.replica.fetchers
增加broker数
controller问题
1 避免zk会话超时
ISR抖动
zk性能问题
Long GC
网络问题
2 监控controller
kafka.controller:type=KafkaController,name=ActiveControllerCount应该=1
监控LeaderElectionRate
unclean leader选举
1 允许非ISR中的副本成为leader
2 监控JMX指标： kafka.controller:type=ControllerStats,name=UncleanLeaderElectionsPerSec
集群评估(sizing)
1 broker评估
单broker上的分区数<2000
控制分区大小，不要超过25GB
2 broker数评估：根据retention和流量进行评估
3 集群扩展
磁盘使用率<60%
网络使用率<75%
4 集群监控
确保topic分区分布尽量均匀
确保broker节点不会磁盘、带宽耗尽
broker监控
1 分区数： kafka.server:type=ReplicaManager,name=PartitionCount
2 leader副本数： kafka.server:type=ReplicaManager,name=LeaderCount
3 ISR扩容率/缩容率：kafka.server:type=ReplicaManager,name=IsrExpandsPerSec
4 入站消息/出站消息：Message in rate/Byte in rate/Byte out rate
5 broker网络请求处理平均空闲率： NetworkProcessorAvgIdlePercent
6 请求平均处理空闲率： RequestHandlerAvgIdlePercent
topic评估
1 分区数
至少和最大的消费者组中consumer的数量一致
分区不要太大，小于25GB
要考虑未来业务的扩容
2 使用keyed消息，即指定key
3 为扩展分区确立阈值，即确定当分区大小达到阈值时增加topic分区数
选择分区
1 基于TPS需求大致确定分区数, 即目标TPS/min(Producer TPS, Consumer TPS)
2 更多分区意味着更多的文件句柄、消息处理延时和更多的内存使用
份额控制
1 避免恶意客户端并维护SLA
2 设定字节率阈值限制
3 监控throttle-rate，byte-rate
4 replica.fecth.response.max.bytes： 设置follower副本FETCH请求response大小
5 限制带宽： kafka-reassign-partitions.sh --throttle options...
Kafka producer
1 使用Java版本producer
2 使用kafka-producer-perf-test.sh测试
3 设置好内存、cpu、batch、压缩等参数
batch.size: 越大，TPS越大，延时也越大
linger.ms: 越大，TPS越大，延时也越大
max.in.flight.requests.per.connection: 增加TPS，关乎消息接收顺序
compression.type: 设置压缩类型，提升TPS
acks: 设置消息持久性级别
4 避免发送大消息(会使用更多内存，降低broker处理)
性能调优
1 如果TPS<网络带宽
增加用户线程
增加batch size
使用多个producer实例
添加分区
2 acks=-1时如何降低延时：增加num.replica.fetchers
3 跨数据中心的传输：增加Socket缓冲区设置，以及TCP缓存设置
监控指标
batch-size-avg
compression-rate-avg
waiting-threads
buffer-available-bytes
record-queue-time-max
record-send-rate
records-per-request-avg
Kafka Consumer
1 使用kafka-consumer-perf.test.sh测试
2 TPS问题
分区数不够
OS缓存命中太低，分配更多页缓存
处理逻辑过重
3 位移管理： 异步提交+手动提交
4 重要参数
fetch.min.bytes、fetch.max.wait.ms
max.poll.interval.ms
max.poll.records
session.timeout.ms
监控
1 consumer lag
2 JMX指标： records-lag-max
3 bin/kafka-consumer-groups.sh
4 如何减少lag
分析consumer，是GC问题还是consumer hang住了
增加consumer instances
增加分区数
无数据丢失配置
1 producer端
retries = MAX
acks=all
max.in.flight.requests.per.connection = 1
关闭producer
2 broker端
replication factor >= 3
min.insync.replicas = 2
关闭unclean leader选举
3 consumer端
关闭auto.offset.commit
消息被处理后提交位移