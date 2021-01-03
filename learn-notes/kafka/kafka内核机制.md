
### Kafka副本机制详解

副本机制(replication),备份机制，指的是分布式系统在多台网络互联的机器上保存有相同的数据拷贝。

- 提供数据冗余（kafka实现）

- 提供搞伸缩性： 横向扩展，能够通过增加机器的方式来提升读性能，进而提高读操作的吞吐量

- 改善数据局部性： 允许将数据放入与用户地理位置相近的地方，从而降低系统延时

#### 副本定义

本质是一个只能追加写消息的提交日志。同一个分区下的所有副本保存相同的消息序列，这些副本分散保存在不同的broker上，从而能对抗部分broker宕机带来的数据不可用。


#### 副本角色

- 副本分成两类： 领导者副本（leader replica）和 追随者副本（follower replica）. 每个分区在创建时都要选举一个副本，成为领导者副本，其余的副本成为追随者副本

- kafka 副本对外不提供服务。 任何一个追随者副本都不能相应消费者和生产者的读写请求。所有的读写请求都必须发往领导者副本所在broker，由该broker负责处理。追随者不处理客户端请求，它唯一的任务就是从领导者副本异步拉取消息，并写入到自己的提交日志中，从而实现与领导者的同步。

- 领导者副本挂掉或者领导者副本所在broker宕机时，kafka依托于zk提供的监控功能实时感知到，并立即开始新的一轮领导者选举，从追随者中选一个作为新的领导者。老leader副本重启后，只能作为追随者副本假如到集群中。


#### 副本机制的优点

- 方便实现"read-your-writes": 使用生产者API 写入成功一条消息后，马上使用消费者API去读取刚才产生的消息

- 方便实现单调读（monotonic reads）:

  对一个消费者用户而言，在多次消费消息时，不会看到某条消息一会儿存在一会儿不存在。

- In-sync Replicas(ISR)

    1. ISR副本集合，ISR中的副本都是与Leader同步的副本，相反不在ISR中的追随者就被认为是与Leader不同步的。Leader副本天然在ISR,ISR不只是追随者副本集合，它必然包括Leader副本。甚至某些情况下，ISR只有Leader一个副本。

    2. broker参数->同步的标准是：**replica.lag.time.max.ms** 参数。 参数含义是Follower副本能够落后Leader副本的最长事件间隔，默认是10秒。

    3. 如果ISR在规定时间内追不上Leader 则踢出ISR集合，若追上了则重新加回去
    
    4. 0.9x版本之前，kafka由另外一个参数 replica.lag.max.messages,通过消息数来判定是否失效

#### Unclean 领导者选举（unclean Leader Election）

- kafka把所有不在ISR中的存放副本都称为非同步副本。通常非同步副本落后leader太多，因此，如果选择这些副本作为新的leader ,就肯能出现数据丢失。

- broker参数-> unclean.leader.election.enable 控制是否允许unclean领导者选举

#### 内部实现

1. kafka在启动的时候会开启两个任务，一个任务用来定期检测是否需要缩减或者扩大ISR集合，这个周期是replica.lag.time.max.ms的一半，默认是5000ms.当检测到ISR集合中有失效副本时，就会收缩ISR集合，当检测到有Follower的HighWatermark 追赶上leader 时，就会扩充ISR

2. 当ISR集合发生变更的时候会将变更后的记录缓存isrChangeSet中，另外一个任务会周期性地检查这个set,如果发现这个Set中有ISR集合的变更记录，那么它会在zk中持久化一个节点。然后因为 controller 在这个节点的路径上注册了一个Watcher,所以能够感知到ISR的变化
   并向它所管理的broker 发送更新元数据的请求。最后删除该路径下已经处理过的节点。
   

### kafka请求处理过程

kafka定义了一组请求协议，用于实现各种交互操作;所有的请求都是通过TCP网络以Socket的方式进行通讯的。

- PRODUCE 请求是用于生产消息的

- FETCH 请求是用于消费消息的

- METADATA 是用于请求Kafka集群元数据信息的。

#### 线程模型

![kafka网络模型.jpg](https://i.loli.net/2020/12/30/Ti4waZFAUGVOWDo.jpg)

1. kafka的broker端有个SocketServer组件，类似于 reactor模式中的Dispatcher,它也有对应的Acceptor线程和一个工作线程池（网络线程池）

2. broker参数：**num.network.threads**,用于调整该网络线程池的线程数，默认值是3，表示每台Broker启动时会创建3个网络线程，专门处理客户端发送的请求

![kafka网络线程池模型.jpg](https://i.loli.net/2020/12/30/NWHsQKM8iUmDw16.jpg)

3. 当网络线程池拿到请求后，将请求放入到一个共享请求队列中。 broker端的IO线程池，负责从该队列中取出请求，执行真正的处理。如果是PRODUCE生产请求，则将消息写入到
底层的磁盘日志中；如果是FETCH请求，则从磁盘或者缓存页中读取消息
   
4. io线程池中的线程才是执行请求逻辑的线程。broker端参数**num.io.threads**控制了这个线程池的线程数。目前该参数默认值是8，表示每台broker启动后自动创建8个IO线程处理请求。

5. 请求队列是所有网络线程共享的，而相应队列则是每个线程网络专属的。因为Dispatcher只是用于请求分发而不负责相应回传，因此只能让每个网络线程发送Response给客户端，所以
Response也就没有放在一个公共地方
   
6. purgatory[炼狱]组件：用来缓存延时请求（delayed request）,所谓延时请求，就是哪些一时未满足条件不能立刻处理的请求。

````
比如设置了 acks=all 的 PRODUCE 请求，一旦设置了 acks=all，那么该请求就必须等待 ISR 中所有副本都接收了消息后才能返回，此时处理该请求的 IO 线程就必须等待其他 Broker 的写入结果。
当请求不能立刻处理时，它就会暂存在 Purgatory 中。稍后一旦满足了完成条件，IO 线程会继续处理该请求，并将 Response 放入对应网络线程的响应队列中。

````

7. 