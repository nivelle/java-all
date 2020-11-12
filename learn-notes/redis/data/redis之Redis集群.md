

### 定义

Redis集群是Redis提供的分布式数据库方案,集群通过分片来进行数据共享,并提供复制和故障转移功能.

#### 节点

一个Redis集群通常由多个节点(node)组成,在刚开始的时候,每个节点都是相互独立的,它们都处在一个只包含自己的集群中,要组建一个真正的可工作的集群,我们必须将各个独立的节点连接起来,构成一个包含多个节点的集群.

连接各个节点的工作可以使用CLUSTER_MEET命令来完成,该命令的格式如下:

```

CLUSTER MEET<IP><PORT>

```

向一个节点node 发送cluster meet命令,可以让node节点与ip和port所指定的节点进行握手,握手成功,node节点就会将ip和port指定的节点添加到node节点节点当前所在的集群中.

#### 启动节点

一个节点就是一个运行在集群模式下的Redis服务器,Redis服务器在启动时会根据cluster-enabled配置选项是否为yes来决定是否开启服务器的集群模式.

![image](http://7xpuj1.com1.z0.glb.clouddn.com/%E6%9C%8D%E5%8A%A1%E5%99%A8%E5%88%A4%E6%96%AD%E6%98%AF%E5%90%A6%E5%BC%80%E5%90%AF%E9%9B%86%E7%BE%A4%E6%A8%A1%E5%BC%8F%E7%9A%84%E8%BF%87%E7%A8%8B.png)

节点会继续使用所在单机模式中使用的服务器组件:

- 节点会继续使用文件事件处理器来处理命令请求和返回命令回复
- 节点会继续使用时间事件处理器来执行serverCron函数,而serverCron函数又会调用集群模式特有的clusterCron函数.clusterCron函数负责执行在集群模式下要执行的常规操作,例如向集群中的其他节点发送Gossip消息,检查节点是否断线,或者检查是否需要对下线节点进行自动故障转移.
- 节点会继续使用数据库来保存键值对数据,键值对依然会是各种不同类型的对象
- 节点会继续使用RDB和AOF持久化模块来实现持久化工作
- 节点会使用发布订阅模式来执行PUBLISH,SUBSCRIBE等命令
- 节点会继续使用复制模块来继续节点的赋值工作
- 节点会继续使用lua脚本环境来执行向客户端输入的lua脚本.

节点会继续使用redisServer结构来保存服务器的状态,使用redisClient结构来保存客户端的状态,至于那些只有在集群模式下才会用到的数据,节点将他们保存到了cluster.h/clusterNode结构,cluster.h/clusterLink结构,以及cluster.h/clusterState结构里面.