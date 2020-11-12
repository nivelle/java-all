

### Sentinel

Sentinel是redis的高可用性解决方案:由一个或多个Sentinel实例组成的Sentinel系统可以监视任意多个主服务器,以及这些主服务器属下的所有从服务器,并在被监视的主服务器进入下线状态时,自动将下线主服务器属下的某个从服务器升级为新的主服务器,然后由新的主服务器代替已经下线的主服务器继续处理命令请求.

![image](http://7xpuj1.com1.z0.glb.clouddn.com/%E6%9C%8D%E5%8A%A1%E5%99%A8%E4%B8%8Esentinel%E7%B3%BB%E7%BB%9F.png  )

它的主要功能包括:

- 不时地监控redis是否按照预期进行良好地运行
- 如果发现某个redis节点运行出现状况,能够通知另外一个进程.
- 能够进行自动切换.当一个master节点不可用时,能够选举出master的多个slave中的一个作为新的master,其他的slave节点将它锁追随的master的地址改为被提升为master的slave的新地址.



### Sentinel支持集群

只使用单个sentinel进程来监控redis集群是不可靠的,当sentinel进程宕掉后(sentinel本身也有单点问题,single-point-of-failure)整个集群系统将无法按照预期的方式运行.所以有必要将sentinel集群,这样有几个好处:

- 即使有一些sentinel进程宕掉了,依然可以redis集群的主备切换
- 如果只有一个sentinel进程,如果这个进程运行出错,或者网络堵塞,那么将无法实现redis集群的主备切换
- 如果有多个sentinel,redis的客户端可以随意地连接任意一个sentinel来获得关于redis集群忠的信息.


### 启动并初始化Sentinel

启动一个sentinel可以使用命令;

```
$ redis-sentinel /path/to/your/sentinel.conf

```

或者

```
$ redis-sentinel /path/to/your/sentinel.conf --sentinel

```

Sentinel本质上是一个特殊模式下的Redis服务器.


![image](http://7xpuj1.com1.z0.glb.clouddn.com/sentinel%E6%A8%A1%E5%BC%8F%E4%B8%8B%E6%9C%8D%E5%8A%A1%E5%99%A8%E4%BD%BF%E7%94%A8%E6%83%85%E5%86%B5.png)


### Sentinel结构

```
struct sentinelState{
    
    //当前纪元,用于实现故障转移
    uint64_t current_epoch;
    
    //保存了所有被这个sentinel监视的主服务器
    // 字典的键是主服务器的名字
    //字段的值是一个指向sentinelRedisInstance结构的指针
    
    dict *masters;
    
    //是否进入了TILT模式
    int tilt;
    
    //目前正在执行的脚本的数量
    int running_scripts;
    
    //进入TILT模式的事件
    mstime_t tilt_start_time;
    
    //最后一次执行时间处理器的时间
    mstime_t previous_time;
    
    //一个FIFO队列,包含了所有需要执行的用户脚本
    list *scripts_queue;
}sentinel


```

### 初始化Sentinel状态的masters属性

Sentinel状态中的masters字典记录了所有被Sentinel监视的主服务器的相关信息,其中:

- 字典的键是被监视主服务器的名字
- 字典的值是被监视主服务器对应的sentinel.c/sentinelRedisInstance结构


sentinelRedisInstance结构代表一个被Sentinel监视的Redis服务器实例,这个实例可以是主服务器,从服务器,或者另外一个Sentinel监视的Redis服务器实例,这个实例可以是主服务器,从服务器,或者另外一个Sentinel.


### 部署哨兵之前需要了解的基本事情

- 一个健壮的部署至少需要三个哨兵实例
- 三个哨兵实例应该放置在客户使用独立方式确认故障的计算机或者虚拟机中.例如不同的物理机器或者不同可用区域的虚拟机
- sentinel+redis实例不保证在故障期间保留确认的写入,因为redis使用异步复制.然而有方式部署哨兵使丢失数据限制在特定时刻,虽然有更安全的方式部署它
- Sentinel,Docker或者其他形式的网络地址交换或者端口映射需要加倍小心:Docker执行端口重新映射,破坏Sentinel自动发现其他哨兵进程和master的slave列表.


```
typedef struct sentinelRedisInstance{
    //标识值,记录了实例的类型,以及该实例的当前状态
    int flags;
    //实例的名字
    //主服务器的名字由用户在配置文件中设置
    //从服务器以及Sentinel的名字由Sentinel自动设置
    //格式为ip:port,例如"127.0.0.1:26379""
    char *name;
    //实例的运行ID
    char *runid;
    
    //配置纪元,用于实现故障转移
    uint64_t config_epoch;
    
    //实例的地址
    sentinelAddr *addr;
    
    //SENTINEL down-after-milliseconds 选项设定的值
    // 实例无响应多少毫秒之后才会被判断为主观下线(subjectively down)
    mstime_t down_after_period;
    //SENTINEL monitor<master-name><IP><port><quorum>选项中的quorum参数
    //判断这个实例为客观下线(objectively down)所需的支持投票数量
    int quorum;
    //SENTINEL parallel-syncs<master-name><number>选项的值
    //在执行故障转移操作时,可以同时对新的主服务器进行同步的从服务器数量
    int parallel_syncs;
    //SENTINEL failover-timeout <master-name><ms>选项的值
    //刷新故障迁移状态的最大时限
    mstime_t failover_timeout;
}sentinelRedisInstance;




```


### Sentinel的配置

redis源码中包含了一个sentinel.conf文件作为sentinel的配置文件,配置文件自带了关于各个配置项的解释.典型的配置项如下所示:

```
sentinel monitor mymaster 127.0.0.1 6379 2
sentinel down-after-milliseconds mymaster 60000
sentinel failover-timeout mymaster 180000
sentinel parallel-syncs mymaster 1

sentinel monitor resque 192.168.1.3 6380 4
sentinel down-after-milliseconds resque 10000
sentinel failover-timeout resque 180000
sentinel parallel-syncs resque 5


```

上面的配置了两个名字分别为mymaster何request的master,配置文件只需要配置master的信息,不用配置slave的信息,因为slave能被自动检测到.需要注意的是,配置文件在sentinel运行期间是会被动态修改的,例如当发生主备切换时,配置文件中的master会被修改为另一个slave.这样,当sentinel如果重启时,就可以根据这个配置来 恢复其之前所监控的redis集群状态.

配置解析:

```
sentinel monitor mymaster 127.0.0.1 6379 2

```
这一行代表sentinel监控的master的名字叫做mymaster,地址为127.0.0.1:6379,行尾最后一个2表示:网络是不可靠的,有时候一个sentinel会因为网络堵塞而误以为一个master redis已经死掉,当sentinel集群模式,解决这个问题的方法就很简单了,只需要多个sentinel互相沟通来确认某个master是否真的死了,这个2代表,当集群忠有2个sentinel认为该master死了时,才能认为该master已经不可用了.(sentinel集群中各个sentinel也有互相通信,通过gossip协议)

除了第一行,我们发现剩下的配置都有一个统一的格式:

```
sentinel <option_name> <master_name> <option_value>

```

接下来我们根据上面格式中的option_name一个个解释这些配置项

- dowm-after-milliseconds:sentinel会向master发送心跳ping来确认master是否存活,如果在"一定时间范围内"不回应pong或者回复了一个错误的消息,那么这个sentinel会主观地认为这个master已经不可用了(subjectively down).而这个down-after-milliseconds就是用来指定这个"一定时间范围"的,单位是毫秒.


```

不过需要注意的是，这个时候sentinel并不会马上进行failover主备切换，这个sentinel还需要参考sentinel集群中其他sentinel的意见，如果超过某个数量的sentinel也主观地认为该master死了，那么这个master就会被客观地(注意哦，这次不是主观，是客观，与刚才的subjectively down相对，这次是objectively down，简称为ODOWN)认为已经死了。需要一起做出决定的sentinel数量在上一条配置中进行配置。

```

- parallel-syncs : 在发生failover主备切换时,这个选项指定了最多可以有多少个slave同时对新的master进行同步,这个数字越小,完成failover所需的时间就越长,但是如果这个数字太大,就意味着越多的slave因为replication而不可用.可以通过将这个值设为1来保证每次只有一个slave处于不能处理命令请求的状态.

所有的配置都可以在运行时用命令Sentinel set command动态修改.


### Sentinel的仲裁会

当一个master被sentinel集群监控时,需要为它指定一个参数,这个参数制定了当需要判决master为不可用,并且进行failover时,所需要的sentinel数量,我们暂时可以称之为票数.

不过当failover主备切换触发后,failover并不会马上进行,还需要sentinel中的大多数授权后才可以进行failover.当ODOWN时,failover被触发.一旦被触发,尝试去进行failover的sentinel会去获得大多数sentinel的授权.

### 配置版本号

当一个sentinel被授权后,它将会获得宕掉的master的一份最新配置版本号,当failover执行结束后,这个版本号将会被用于最新的配置.因为大多数sentinel都已经知道该版本号已经被要执行failover的sentinel拿走了,所以其他的sentinel都不能再去使用这个版本号.这意味着,每次failover都会附带一个独一无二逇版本号.

而且,sentinel集群都遵守一个规则:如果sentinel A推荐sentinel B去执行failover,A会等待一段时间后,自行再次去对同一个master执行failover,这个等待时间是通过failover-timeiut配置项去配置的.从这个配置看出,sentinel集群中的sentinel不会再同一时刻并发去failover同一个master,第一个进行failover的sentinel如果失败了,另外一个将会在一定时间内进行重新failover,依次类推.


```
** redis sentinel保证了活跃性：如果大多数sentinel能够互相通信，最终将会有一个被授权去进行failover. **
** redis sentinel也保证了安全性：每个试图去failover同一个master的sentinel都会得到一个独一无二的版本号。**

```

### 配置传播

一旦一个sentinel成功地对一个master进行了failover,它将会把关于master的最新配置通过广播形式通知其他sentinel,其他的sentinel则更新对应master的配置.

一个failover要想被成功实行,sentinel必须能够向选为master的slave发送slave of on one 命令,然后能够通过info命令看到新master的配置信息.

当将一个slave选举为master并发送slave of no one 后,即使其他的slave还没针对master重新配置自己,failover也被认为是成功了,然后所有sentinel将会发布新的配置信息.

新配置在集群中互相传播的方式,就是为什么我们需要当一个sentinel进行failover时必须被授权一个版本号的原因.

每个sentinel使用发布/订阅的方式持续地传播master配置版本信息,配置传播的发布/订阅管道是:__sentinel__:hello.

因为每一个配置都有一个版本号,所以版本号最大的那个为标准.

```
举个栗子：假设有一个名为mymaster的地址为192.168.1.50:6379。一开始，集群中所有的sentinel都知道这个地址，于是为mymaster的配置打上版本号1。一段时候后mymaster死了，有一个sentinel被授权用版本号2对其进行failover。如果failover成功了，假设地址改为了192.168.1.50:9000，此时配置的版本号为2，进行failover的sentinel会将新配置广播给其他的sentinel，由于其他sentinel维护的版本号为1，发现新配置的版本号为2时，版本号变大了，说明配置更新了，于是就会采用最新的版本号为2的配置。

这意味着sentinel集群保证了第二种活跃性：一个能够互相通信的sentinel集群最终会采用版本号最高且相同的配置。


```

### SDOWN和ODOWN的更多细节

sentinel对于不可用有两种不同的看法,一个叫主观不可用,一个叫客观不可用.sdown是sentinel自己主观上检测到的关于master的状态,odwon需要一定数量的sentinel达成一致意见才认为一个master客观上已经宕掉,各个sentinel之间通过命令SENTINEL is_master_down_by_addr来获得其他sentinel对master的检测结果.


从sentinel的角度来看,如果发生了ping心跳后,在一定时间内没有收到合法的回复,就达到了sdown的条件.这个时间在配置中通过is-master-down-after-milliseconds参数配置.

当sentinel发生ping后,以下回复之一被认为是合法的:

```

PING replied with +PONG.
PING replied with -LOADING error.
PING replied with -MASTERDOWN error.

```

其他任何回复都是不合法的


从sdown切换到odown不需要不需要任何一致性算法,只需要一个gossip协议:如果一个sentinel收到了足够多的sentinel发来消息告诉它某个master已经down掉了,sdown状态就会变成odown状态了.如果之后master可用了,这个状态就会响应地被清理掉.

ODOWN状态只适用于master，对于不是master的redis节点sentinel之间不需要任何协商，slaves和sentinel不会有ODOWN状态。


### Sentinel之间和Slaves之间的自动发现机制

虽然sentinel集群中各个sentinel都互相连接彼此来检查对方的可用性以及互相发送消息.但是你不用再任何一个sentinel配置任何其他的sentinel节点.因为sentinel利用了master的发布订阅机制去自动发现其他页监控了统一master的sentinel节点.

通过向名为__sentinel__:hello的管道中发送消息来实现.

同样,你也不需要在sentinel中配置某个master的所有slave的地址,sentinel会通过询问master来得到这些slave地址的.

每个sentinel通过向每个master和slavede 发布订阅频道__sentinel__:hello每秒发送一次消息,来宣布它的存在.

每个sentinel也订阅了每个master和slave的频道__sentinel__:hello的内容,来发现未知的sentinel,当检测到了新的sentinel,则将其加入到自身维护的master监控列表中.

每个sentinel发送的消息中包含了其当前维护的最新的master配置,如果某个sentinel发现自己的配置版本低于接收到的配置版本,则会用新的配置更新自己的master配置.

在为一个master添加一个新的sentinel之前,sentinel总是检查是否已经有sentinel与新的sentinel的进程号或者是地址是一样的.如果是那样,这个sentinel将会被删除,而把新的sentinel添加上去.

### 网络隔离时的一致性

redis sentinel集群的配置的一致性模型为最终一致性,集群中每个sentinel最终都会采用最高版本的配置.然而,在实际的应用环境中,有三个不同的角色会与sentinel打交道:

- Redis实例

- Sentinel实例

- 客户端

为了考察整个系统的行为我们必须同时考虑到这三个角色.

```

             +-------------+
             | Sentinel 1  | <--- Client A
             | Redis 1 (M) |
             +-------------+
                     |
                     |
 +-------------+     |                     +------------+
 | Sentinel 2  |-----+-- / partition / ----| Sentinel 3 | <--- Client B
 | Redis 2 (S) |                           | Redis 3 (M)|
 +-------------+                           +------------+



```

初始状态下redis3是master,redis1和redis2是slave.之后redis3所在主机网络不可用了,sentinel1和sentinel2启动了failover并把redis1选举为master.

sentinel集群的特性保证了sentinel1和sentinel2得到了关于master的最新配置.但是sentinel3依然保持着的就是旧的配置,因为它与外界隔离了.

当网络恢复以后,我们知道sentinel3将会更新它的配置.但是,如果客户端所连接的master被网络隔离,会发生什么呢?

```
客户端将依然可以向redis3写数据，但是当网络恢复后，redis3就会变成redis的一个slave，那么，在网络隔离期间，客户端向redis3写的数据将会丢失。

```


客户端将依然可以向redis3写数据，但是当网络恢复后，redis3就会变成redis的一个slave，那么，在网络隔离期间，客户端向redis3写的数据将会丢失。

也许你不会希望这个场景发生：


- 如果你把redis当做缓存来使用，那么你也许能容忍这部分数据的丢失。

- 但如果你把redis当做一个存储系统来使用，你也许就无法容忍这部分数据的丢失了。


因为redis采用的是异步复制，在这样的场景下，没有办法避免数据的丢失。然而，你可以通过以下配置来配置redis3和redis1，使得数据不会丢失。

```

min-slaves-to-write 1
min-slaves-max-lag 10

```

通过上面的配置，当一个redis是master时，如果它不能向至少一个slave写数据(上面的min-slaves-to-write指定了slave的数量)，它将会拒绝接受客户端的写请求。由于复制是异步的，master无法向slave写数据意味着slave要么断开连接了，要么不在指定时间内向master发送同步数据的请求了(上面的min-slaves-max-lag指定了这个时间)。


转载:[Redis Sentinel Documentation](https://redis.io/topics/sentinel)

