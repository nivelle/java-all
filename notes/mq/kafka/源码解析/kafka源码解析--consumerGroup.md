####   消费者组

- 消费者组元数据：这部分源码主要包括 GroupMetadata 和 MemberMetadata。这两个类共同定义了消费者组的元数据都由哪些内容构成。
  
- 组元数据管理器：由 GroupMetadataManager 类定义，可被视为消费者组的管理引擎，提供了消费者组的增删改查功能。
  
- **__consumer_offsets**：Kafka 的内部主题。除了我们熟知的消费者组提交位移记录功能之外，它还负责保存消费者组的注册记录消息。
  
- GroupCoordinator：组协调者组件，提供通用的组成员管理和位移管理。