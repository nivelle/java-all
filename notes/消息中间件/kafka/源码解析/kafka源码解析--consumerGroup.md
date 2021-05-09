### 消费者组

- 消费者组元数据：这部分源码主要包括 GroupMetadata 和 MemberMetadata。这两个类共同定义了消费者组的元数据都由哪些内容构成。
  
- 组元数据管理器：由 GroupMetadataManager 类定义，可被视为消费者组的管理引擎，提供了消费者组的增删改查功能。
  
- **__consumer_offsets**：Kafka 的内部主题。除了我们熟知的消费者组提交位移记录功能之外，它还负责保存消费者组的注册记录消息。
  
- GroupCoordinator：组协调者组件，提供通用的组成员管理和位移管理。

#### Rebalance 的流程大致分为两大步：加入组（JoinGroup）和组同步（SyncGroup)

##### JoinGroup
加入组，是指消费者组下的各个成员向 Coordinator 发送 JoinGroupRequest 请求加入进组的过程。

##### SyncGroup

- 当所有成员都成功加入组之后，Coordinator 指定其中一个成员为 Leader，然后将订阅分区信息发给 Leader 成员。

- 接着，所有成员（包括 Leader 成员）向 Coordinator 发送 SyncGroupRequest 请求。 
  **需要注意的是，只有 Leader 成员发送的请求中包含了订阅分区消费分配方案，在其他成员发送的请求中，这部分的内容为空。**

- 当 Coordinator 接收到分配方案后，会通过向成员发送响应的方式，通知各个成员要消费哪些分区。