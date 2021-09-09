### 消费者组

- 消费者组元数据：这部分源码主要包括 GroupMetadata 和 MemberMetadata。这两个类共同定义了消费者组的元数据都由哪些内容构成。
  
- 组元数据管理器：由 GroupMetadataManager 类定义，可被视为消费者组的管理引擎，提供了消费者组的增删改查功能。
  
- **__consumer_offsets**：Kafka 的内部主题。除了我们熟知的消费者组提交位移记录功能之外，它还负责保存消费者组的注册记录消息。
  
- GroupCoordinator：组协调者组件，提供通用的组成员管理和位移管理。

#### Rebalance 的流程大致分为两大步：加入组（JoinGroup）和组同步（SyncGroup)

##### 加入组：JoinGroup
加入组，是指消费者组下的各个成员向 Coordinator 发送 JoinGroupRequest 请求加入进组的过程。

[![gJcbQK.md.jpg](https://z3.ax1x.com/2021/05/09/gJcbQK.md.jpg)](https://imgtu.com/i/gJcbQK)

- handleJoinGroup 方法
````

// 验证消费者组状态的合法性
validateGroupStatus(groupId, ApiKeys.JOIN_GROUP).foreach { error =>
  responseCallback(JoinGroupResult(memberId, error))
  return
}
// 确保sessionTimeoutMs介于
// [group.min.session.timeout.ms值，group.max.session.timeout.ms值]之间
// 否则抛出异常，表示超时时间设置无效
if (sessionTimeoutMs < groupConfig.groupMinSessionTimeoutMs ||
  sessionTimeoutMs > groupConfig.groupMaxSessionTimeoutMs) {
  responseCallback(JoinGroupResult(memberId, Errors.INVALID_SESSION_TIMEOUT))
} else {
  // 消费者组成员ID是否为空
  val isUnknownMember = memberId == JoinGroupRequest.UNKNOWN_MEMBER_ID
  // 获取消费者组信息，如果组不存在，就创建一个新的消费者组
  groupManager.getOrMaybeCreateGroup(groupId, isUnknownMember) match {
    case None =>
      responseCallback(JoinGroupResult(memberId, Errors.UNKNOWN_MEMBER_ID))
    case Some(group) =>
      group.inLock {
        // 如果该消费者组已满员
        if (!acceptJoiningMember(group, memberId)) {
          // 移除该消费者组成员
          group.remove(memberId)
          group.removeStaticMember(groupInstanceId)
          // 封装异常表明组已满员
          responseCallback(JoinGroupResult(
            JoinGroupRequest.UNKNOWN_MEMBER_ID, 
            Errors.GROUP_MAX_SIZE_REACHED))
        // 如果消费者组成员ID为空
        } else if (isUnknownMember) {
          // 为空ID成员执行加入组操作
          doUnknownJoinGroup(group, groupInstanceId, requireKnownMemberId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs, protocolType, protocols, responseCallback)
        } else {
          // 为非空ID成员执行加入组操作
          doJoinGroup(group, memberId, groupInstanceId, clientId, clientHost, rebalanceTimeoutMs, sessionTimeoutMs, protocolType, protocols, responseCallback)
        }
        // 如果消费者组正处于PreparingRebalance状态
        if (group.is(PreparingRebalance)) {
          // 放入Purgatory，等待后面统一延时处理
          joinPurgatory.checkAndComplete(GroupKey(group.groupId))
        }
      }
  }
}
````

###### 第 4 步，检查当前消费者组是否已满员。

该逻辑是通过 acceptJoiningMember 方法实现的。这个方法根据消费者组状态确定是否满员。这里的消费者组状态有三种:

- 状态一：如果是 Empty 或 Dead 状态，肯定不会是满员，直接返回 True，表示可以接纳申请入组的成员；
  
- 状态二：如果是 PreparingRebalance 状态，那么，批准成员入组的条件是必须满足一下两个条件之一。
  (1). 该成员是之前已有的成员，且当前正在等待加入组；
  (2). 当前等待加入组的成员数小于 Broker 端参数 **group.max.size** 值。只要满足这两个条件中的任意一个，当前消费者组成员都会被批准入组。
  
- 状态三：如果是其他状态，那么，入组的条件是该成员是已有成员，或者是当前组总成员数

##### 组同步: SyncGroup

- 当所有成员都成功加入组之后，Coordinator 指定其中一个成员为 Leader，然后将订阅分区信息发给 Leader 成员。

- 接着，所有成员（包括 Leader 成员）向 Coordinator 发送 SyncGroupRequest 请求。 
  **需要注意的是，只有 Leader 成员发送的请求中包含了订阅分区消费分配方案，在其他成员发送的请求中，这部分的内容为空。**

- 当 Coordinator 接收到分配方案后，会通过向成员发送响应的方式，通知各个成员要消费哪些分区。

###### handleSyncGroup 方法

````

// 验证消费者状态及合法性 
validateGroupStatus(groupId, ApiKeys.SYNC_GROUP) match {
  // 如果未通过合法性检查，且错误原因是Coordinator正在加载
  // 那么，封装REBALANCE_IN_PROGRESS异常，并调用回调函数返回
  case Some(error) if error == Errors.COORDINATOR_LOAD_IN_PROGRESS =>
    responseCallback(SyncGroupResult(Errors.REBALANCE_IN_PROGRESS))
  // 如果是其它错误，则封装对应错误，并调用回调函数返回
  case Some(error) => responseCallback(SyncGroupResult(error))
  case None =>
    // 获取消费者组元数据
    groupManager.getGroup(groupId) match {
      // 如果未找到，则封装UNKNOWN_MEMBER_ID异常，并调用回调函数返回
      case None => 
        responseCallback(SyncGroupResult(Errors.UNKNOWN_MEMBER_ID))
      // 如果找到的话，则调用doSyncGroup方法执行组同步任务
      case Some(group) => doSyncGroup(
        group, generation, memberId, protocolType, protocolName,
        groupInstanceId, groupAssignment, responseCallback)
    }
}

````
[![gJWo7D.md.jpg](https://z3.ax1x.com/2021/05/09/gJWo7D.md.jpg)](https://imgtu.com/i/gJWo7D)