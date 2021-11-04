## consul架构图

![](http://ljchen.net/uploads/consul-arch.png)

首先，从架构上，图片被两个datacenter分成了上下两部分；但这两部分又并不是完全隔离的，他们之间通过`WAN GOSSIP`在Internet上交互报文。

因此，我们了解到consul是可以支持多个数据中心之间基于WAN来做同步的。

再看单个datacenter内部，节点被划分为两种颜色，其中红色为server，紫色为client。它们之间通过GRPC通信（主要用于业务数据）。除此之外，server和client之间，还有一条LAN GOSSIP通信，这是用于当LAN内部发生了拓扑变化时，存活的节点们能够及时感知，比如server节点down掉后，client就会触发将对应server节点从可用列表中剥离出去。


当然，server与server之间，client与client之间，client与server之间，在同一个datacenter中的所有consul agent会组成一个LAN网络（当然它们之间也可以按照区域划分segment），当LAN网中有任何角色变动，或者有用户自定义的event产生的时候，其他节点就会感知到，并触发对应的预置操作。


所有的server节点共同组成了一个集群，他们之间运行raft协议，通过共识仲裁选举出leader。所有的业务数据都通过leader写入到集群中做持久化，当有半数以上的节点存储了该数据后，server集群才会返回ACK，从而保障了数据的强一致性。当然，server数量大了之后，也会影响写数据的效率。所有的follower会跟随leader的脚步，保障其有最新的数据副本。

同一个consul agent程序，通过启动的时候指定不同的参数来运行server或client模式。这两种模式下，各自所负责的事务具体如下。

### Server

- 参与共识仲裁(raft)
- 存储群集状态(日志存储)
- 处理查询
- 维护与周边(LAN/WAN)各节点关系

### Agent节点
- 负责通过该节点注册到consul的微服务的健康检查
- 将客户端注册请求以及查询转化为对server的RPC请求
- 维护与周边(LAN/WAN)各节点关系

### 服务端口

端口 | 作用
---|---
8300|RPC exchanges
8301|	LAN GOSSIP
8302|	WAN GOSSIP
8400|	RPC exchanges by the CLI
8500|	Used for HTTP API and web interface
8600|	Used for DNS server

### 实现原理
纵观consul的实现，其核心在于两点：

- 集群内节点间信息的高效同步机制，其保障了拓扑变动以及控制信号的及时传递；
- server集群内日志存储的强一致性。

#### 它们主要基于以下两个协议来实现：

- 使用gossip协议在集群内传播信息
- 使用raft协议来保障日志的一致性

### Serf
serf是hashicorp基于GOSSIP协议来实现的一个用于分布式集群成员管理，失败检测以及编排的工具，当前最新版本为v0.8.1。有兴趣的朋友可以到这个链接具体了解hashicorp serf，下面我来简单介绍一下其功能。

### 集群管理

这台机器上有两个IP地址，一个是172.20.20.10，另一个为172.20.20.10。我准备启动两个serf agent进程，分别绑定到不同的两个IP地址上，各自叫做agent-one和agent-two。

由于它们启动之后，相互之间是不知道彼此的，我通过执行serf join来把它们组成一个LAN serf。这样它们就可以彼此检测到彼此，通过查看serf members可以看到所有的节点以及其健康状况。

### 事件响应

在前面的步骤中，我们将两个serf进程加入到了同一个LAN中，接下来我们将进行一些更加激动人心的实践。接下来，我们创建了一个脚本(handler.sh)，大致内容为:当脚本被调用的时候，会打印出一些具体的信息。然后，我们在启动serf agent的时候，通过参数将该脚本传递给serf agent。这样当收该serf节点收到event时，就会调用用户指定的handler（即执行脚本）。

````shell
$ cat handler.sh

#!/bin/bash
echo
echo "New event: ${SERF_EVENT}. Data follows..."
while read line; do
    printf "${line}\n"
done

$ serf agent -log-level=debug -event-handler=handler.sh


````

发送自定义event

### Event类型

serf指定了下面这些类型的event，各自的作用如下所示：

````shell
member-join    One or more members have joined the cluster.
member-leave   One or more members have gracefully left the cluster.
member-failed  One or more members have failed, meaning that they did not properly respond to ping requests.
member-update  One or more members have updated, likely to update the associated tags
member-reap    Serf has removed one or more members from its list of members. This means a failed node exceeded the reconnect_timeout, or a left node reached the tombstone_timeout.
user           A custom user event, covered later in this guide.
query          A query event, covered later in this guide

````

### Raft

由于介绍raft协议的文章已经比较多，我这里就不在详述。这里重点分析一下在consul中，raft协议运作的一些实践和日志。

#### 节点状态变更

- 在节点数达到bootstrap-expect的数时，开始启用raft选举
- 在节点数超过bootstrap-expect数时，其他节点为follower
- 在leader被干掉后，raft如果判断到节点数依然大于等于bootstrap-expect时，重新选举
- 逐一干掉节点，当节点数少于bootstrap-expect时，raft协议不再选举，将维持先前的状态。

### Raft选举日志分析

````shell
# 选举日志信息 （bootstrap）

==> Starting Consul agent...
bootstrap_expect > 0: expecting 3 servers
==> Consul agent running!
           Version: 'v1.4.0'
           Node ID: 'f217ca95-e83c-9a1f-9e87-3b5c1f5a82a3'
         Node name: '42ddc7aa3bb6'
        Datacenter: 'dc1' (Segment: '<all>')
            Server: true (Bootstrap: false)
       Client Addr: [127.0.0.1] (HTTP: 8500, HTTPS: -1, gRPC: -1, DNS: 8600)
      Cluster Addr: 172.17.0.2 (LAN: 8301, WAN: 8302)
           Encrypt: Gossip: false, TLS-Outgoing: false, TLS-Incoming: false
==> Log data will now stream in as it occurs:
    2018/12/03 07:21:34 [INFO] raft: Initial configuration (index=0): []
    2018/12/03 07:21:34 [INFO] serf: EventMemberJoin: 42ddc7aa3bb6.dc1 172.17.0.2
    2018/12/03 07:21:34 [INFO] serf: EventMemberJoin: 42ddc7aa3bb6 172.17.0.2
    2018/12/03 07:21:34 [INFO] raft: Node at 172.17.0.2:8300 [Follower] entering Follower state (Leader: "")
    2018/12/03 07:21:34 [INFO] consul: Adding LAN server 42ddc7aa3bb6 (Addr: tcp/172.17.0.2:8300) (DC: dc1)
    2018/12/03 07:21:34 [INFO] consul: Handled member-join event for server "42ddc7aa3bb6.dc1" in area "wan"
    2018/12/03 07:21:34 [INFO] agent: Started DNS server 127.0.0.1:8600 (tcp)
    2018/12/03 07:21:34 [INFO] agent: Started DNS server 127.0.0.1:8600 (udp)
    2018/12/03 07:21:34 [INFO] agent: Started HTTP server on 127.0.0.1:8500 (tcp)
    2018/12/03 07:21:34 [INFO] agent: started state syncer
    2018/12/03 07:21:34 [INFO] agent: Retry join LAN is supported for: aliyun aws azure digitalocean gce k8s os packet scaleway softlayer triton vsphere
    2018/12/03 07:21:34 [INFO] agent: Joining LAN cluster...
    2018/12/03 07:21:34 [INFO] agent: (LAN) joining: [172.17.0.2]
    2018/12/03 07:21:34 [INFO] agent: (LAN) joined: 1 Err: <nil>
    2018/12/03 07:21:34 [INFO] agent: Join LAN completed. Synced with 1 initial agents
    # node数量没有达到，无法开始选举
    2018/12/03 07:21:41 [ERR] agent: failed to sync remote state: No cluster leader
    2018/12/03 07:21:43 [WARN] raft: no known peers, aborting election
    2018/12/03 07:21:54 [INFO] serf: EventMemberJoin: 4eb2b75f454a 172.17.0.3
    2018/12/03 07:21:54 [INFO] consul: Adding LAN server 4eb2b75f454a (Addr: tcp/172.17.0.3:8300) (DC: dc1)
    2018/12/03 07:21:54 [INFO] serf: EventMemberJoin: 4eb2b75f454a.dc1 172.17.0.3
    2018/12/03 07:21:54 [INFO] consul: Handled member-join event for server "4eb2b75f454a.dc1" in area "wan"
    2018/12/03 07:21:58 [INFO] serf: EventMemberJoin: b603f61d1449 172.17.0.4
    2018/12/03 07:21:58 [INFO] consul: Adding LAN server b603f61d1449 (Addr: tcp/172.17.0.4:8300) (DC: dc1)
    # node数量达到，开始选举
    2018/12/03 07:21:58 [INFO] consul: Found expected number of peers, attempting bootstrap: 172.17.0.2:8300,172.17.0.3:8300,172.17.0.4:8300
    2018/12/03 07:21:58 [INFO] serf: EventMemberJoin: b603f61d1449.dc1 172.17.0.4
    2018/12/03 07:21:58 [INFO] consul: Handled member-join event for server "b603f61d1449.dc1" in area "wan"
    2018/12/03 07:22:03 [WARN] raft: Heartbeat timeout from "" reached, starting election
    # 状态迁移
    2018/12/03 07:22:03 [INFO] raft: Node at 172.17.0.2:8300 [Candidate] entering Candidate state in term 2
    # 获胜
    2018/12/03 07:22:03 [INFO] raft: Election won. Tally: 2
    2018/12/03 07:22:03 [INFO] raft: Node at 172.17.0.2:8300 [Leader] entering Leader state
    2018/12/03 07:22:03 [INFO] raft: Added peer 5b0b26fb-5e62-c390-0ced-b80e0f3293ef, starting replication
    2018/12/03 07:22:03 [INFO] raft: Added peer 3844affd-9b4e-ad3d-84f3-25fb77806e7c, starting replication
    2018/12/03 07:22:03 [INFO] consul: cluster leadership acquired
    2018/12/03 07:22:03 [INFO] consul: New leader elected: 42ddc7aa3bb6
    2018/12/03 07:22:03 [WARN] raft: AppendEntries to {Voter 3844affd-9b4e-ad3d-84f3-25fb77806e7c 172.17.0.4:8300} rejected, sending older logs (next: 1)
    2018/12/03 07:22:03 [WARN] raft: AppendEntries to {Voter 5b0b26fb-5e62-c390-0ced-b80e0f3293ef 172.17.0.3:8300} rejected, sending older logs (next: 1)
    2018/12/03 07:22:03 [INFO] raft: pipelining replication to peer {Voter 3844affd-9b4e-ad3d-84f3-25fb77806e7c 172.17.0.4:8300}
    2018/12/03 07:22:03 [INFO] raft: pipelining replication to peer {Voter 5b0b26fb-5e62-c390-0ced-b80e0f3293ef 172.17.0.3:8300}
    2018/12/03 07:22:03 [INFO] consul: member '42ddc7aa3bb6' joined, marking health alive
    2018/12/03 07:22:03 [INFO] consul: member '4eb2b75f454a' joined, marking health alive
    2018/12/03 07:22:03 [INFO] consul: member 'b603f61d1449' joined, marking health alive
==> Failed to check for updates: Get https://checkpoint-api.hashicorp.com/v1/check/consul?arch=amd64&os=linux&signature=2ba01aad-86ad-32b1-2cff-dc77537fa0dd&version=1.4.0: net/http: request canceled while waiting for connection (Client.Timeout exceeded while awaiting headers)
    2018/12/03 07:22:05 [INFO] agent: Synced node info
    2018/12/03 07:27:25 [INFO] serf: EventMemberJoin: 212149acfdcd 172.17.0.5
    2018/12/03 07:27:25 [INFO] consul: Adding LAN server 212149acfdcd (Addr: tcp/172.17.0.5:8300) (DC: dc1)
    # 添加的新节点为novoter角色，无法参与选举
    2018/12/03 07:27:25 [INFO] raft: Updating configuration with AddNonvoter (45c059ba-bd5c-5a00-f7d6-e490324e7b52, 172.17.0.5:8300) to [{Suffrage:Voter ID:f217ca95-e83c-9a1f-9e87-3b5c1f5a82a3 Address:172.17.0.2:8300} {Suffrage:Voter ID:5b0b26fb-5e62-c390-0ced-b80e0f3293ef Address:172.17.0.3:8300} {Suffrage:Voter ID:3844affd-9b4e-ad3d-84f3-25fb77806e7c Address:172.17.0.4:8300} {Suffrage:Nonvoter ID:45c059ba-bd5c-5a00-f7d6-e490324e7b52 Address:172.17.0.5:8300}]
    2018/12/03 07:27:25 [INFO] serf: EventMemberJoin: 212149acfdcd.dc1 172.17.0.5
    2018/12/03 07:27:25 [INFO] consul: Handled member-join event for server "212149acfdcd.dc1" in area "wan"
    2018/12/03 07:27:25 [INFO] raft: Added peer 45c059ba-bd5c-5a00-f7d6-e490324e7b52, starting replication
    2018/12/03 07:27:25 [WARN] raft: AppendEntries to {Nonvoter 45c059ba-bd5c-5a00-f7d6-e490324e7b52 172.17.0.5:8300} rejected, sending older logs (next: 1)
    2018/12/03 07:27:25 [INFO] consul: member '212149acfdcd' joined, marking health alive
    2018/12/03 07:27:25 [INFO] raft: pipelining replication to peer {Nonvoter 45c059ba-bd5c-5a00-f7d6-e490324e7b52 172.17.0.5:8300}
    2018/12/03 07:27:28 [INFO] serf: EventMemberJoin: edb64d232050 172.17.0.6
    2018/12/03 07:27:28 [INFO] consul: Adding LAN server edb64d232050 (Addr: tcp/172.17.0.6:8300) (DC: dc1)
    # 添加的新节点为novoter角色，无法参与选举
    2018/12/03 07:27:28 [INFO] raft: Updating configuration with AddNonvoter (46ebd85c-5e96-f9bd-81e4-0a82d3b405c7, 172.17.0.6:8300) to [{Suffrage:Voter ID:f217ca95-e83c-9a1f-9e87-3b5c1f5a82a3 Address:172.17.0.2:8300} {Suffrage:Voter ID:5b0b26fb-5e62-c390-0ced-b80e0f3293ef Address:172.17.0.3:8300} {Suffrage:Voter ID:3844affd-9b4e-ad3d-84f3-25fb77806e7c Address:172.17.0.4:8300} {Suffrage:Nonvoter ID:45c059ba-bd5c-5a00-f7d6-e490324e7b52 Address:172.17.0.5:8300} {Suffrage:Nonvoter ID:46ebd85c-5e96-f9bd-81e4-0a82d3b405c7 Address:172.17.0.6:8300}]
    2018/12/03 07:27:28 [INFO] serf: EventMemberJoin: edb64d232050.dc1 172.17.0.6
    2018/12/03 07:27:28 [INFO] consul: Handled member-join event for server "edb64d232050.dc1" in area "wan"
    2018/12/03 07:27:28 [INFO] raft: Added peer 46ebd85c-5e96-f9bd-81e4-0a82d3b405c7, starting replication
    2018/12/03 07:27:28 [INFO] consul: member 'edb64d232050' joined, marking health alive
    2018/12/03 07:27:28 [WARN] raft: AppendEntries to {Nonvoter 46ebd85c-5e96-f9bd-81e4-0a82d3b405c7 172.17.0.6:8300} rejected, sending older logs (next: 1)
    2018/12/03 07:27:28 [INFO] raft: pipelining replication to peer {Nonvoter 46ebd85c-5e96-f9bd-81e4-0a82d3b405c7 172.17.0.6:8300}
    2018/12/03 07:27:43 [INFO] autopilot: Promoting Server (ID: "45c059ba-bd5c-5a00-f7d6-e490324e7b52" Address: "172.17.0.5:8300") to voter
    2018/12/03 07:27:43 [INFO] raft: Updating configuration with AddStaging (45c059ba-bd5c-5a00-f7d6-e490324e7b52, 172.17.0.5:8300) to [{Suffrage:Voter ID:f217ca95-e83c-9a1f-9e87-3b5c1f5a82a3 Address:172.17.0.2:8300} {Suffrage:Voter ID:5b0b26fb-5e62-c390-0ced-b80e0f3293ef Address:172.17.0.3:8300} {Suffrage:Voter ID:3844affd-9b4e-ad3d-84f3-25fb77806e7c Address:172.17.0.4:8300} {Suffrage:Voter ID:45c059ba-bd5c-5a00-f7d6-e490324e7b52 Address:172.17.0.5:8300} {Suffrage:Nonvoter ID:46ebd85c-5e96-f9bd-81e4-0a82d3b405c7 Address:172.17.0.6:8300}]
    2018/12/03 07:27:43 [INFO] autopilot: Promoting Server (ID: "46ebd85c-5e96-f9bd-81e4-0a82d3b405c7" Address: "172.17.0.6:8300") to voter
    2018/12/03 07:27:43 [INFO] raft: Updating configuration with AddStaging (46ebd85c-5e96-f9bd-81e4-0a82d3b405c7, 172.17.0.6:8300) to [{Suffrage:Voter ID:f217ca95-e83c-9a1f-9e87-3b5c1f5a82a3 Address:172.17.0.2:8300} {Suffrage:Voter ID:5b0b26fb-5e62-c390-0ced-b80e0f3293ef Address:172.17.0.3:8300} {Suffrage:Voter ID:3844affd-9b4e-ad3d-84f3-25fb77806e7c Address:172.17.0.4:8300} {Suffrage:Voter ID:45c059ba-bd5c-5a00-f7d6-e490324e7b52 Address:172.17.0.5:8300} {Suffrage:Voter ID:46ebd85c-5e96-f9bd-81e4-0a82d3b405c7 Address:172.17.0.6:8300}]

````

### 源码架构

先来看Consul内部是如何做服务注册与发现的流程，下图是consul客户端向agent注册以及发现目标服务的时序图。

![](http://ljchen.net/uploads/srd-1.png)

通过上图，我们大概知道了在consul agent中，功能分为了consul server和consul agent（client）。在前面架构介绍中我们已经阐述了server和client各自的职责。

consul源码中，server和client都是在一套代码中，通过指定启动参数的形势来运行consul server。这里我们先来重点讲解一下consul client的内部架构。

### Consul Client架构

![](http://ljchen.net/uploads/srd-2.png)

上图简要描述了consul client中的各重要服务，以及它们之间的关系。

- lan serf
主要职责是维护节点之间的关系，当有节点加入或者离开的时候，所有节点都会接收到对应的event，这里的lan serf就是指对这些event做处理的handler的go routine服务。
- state sync
在consul启动的时候，会启动该服务，它监听一个channel，当其他服务有向consul server同步配置的需求的时候，就会像channel中写入event信息；然后就会触发该服务向consul server同步配置信息。这里的同步又分为全同步和部分同步，主要是为了降低网路的负担。
- gRPC router
这是对连接到consul server的gRPC连接的维护和负载均衡机制。在该服务中心，一方面会基于lan serf对consul server节点的拓扑变更事件来维护server列表，另一方面也会对到存活server的connection做定期的ping来维护连接列表；除此之外，还能够对server连接做客户端负载均衡。
- local state
是一个本地的内存数据库，一般执行sync就是从server将数据同步过来保存到该db中；平时做一些配置更改也会对应更新该db。
- api consul是提供了HTTP和CLI两种对外访问方式的，这里所谓的API并不是想说接口的细节，而指的是consul所提供对外API对应controller逻辑实现。比如下一节要讲到的服务注册的API，后面都做了什么业务逻辑，这是很重要的一部分，对于复杂的逻辑一般包括了：更新本地local state，启动对应的go routine来做事，使用gRPC向server更新数据，向sync channel发消息从而触发sync等操作。

### 服务注册流程

![](http://ljchen.net/uploads/srd-3.png)

上图是其服务注册API的controller中函数调用的一个简化流程。

- 首先s.agent.AddService函数要做的就是将接收到的服务信息做一通校验，然后整理成为local state的数据结构之后保存到本地；但是由于它是一个内存数据库，并不能够持久化，于是再将其保存到本地文件中做持久化。
- 干完这些操作之后，如果该服务没有指定healthcheck操作的话，接下来要做的就是将这个服务注册请求同步到consul server，让raft leader将数据真正持久化到server中，这部分我没有在图上体现出来，但是在代码中确实是这样实现的。
- 对于在注册的时候制定了healthcheck内容的服务，需要继续注册healthcheck。由于consul支持的healthcheck类型较多，这里对其所指定类型做了简单的校验，然后就开始干正事了。启动一个goroutine来专门为这个服务执行定期的健康检查操作，可见，如果该consul agent上注册的服务太多的话，势必消耗很多资源，这就要求我们部署方案要做好规划了。
- 当健康检查的结果与先前的结果不一致的时候，会触发对local state的更新，同时，需要局部同步该服务到consul server上的内容。为什么呢？因为服务的健康状态其实是保存到其check字段下的，而非是service的一个一级属性，这块大家可以下去查阅一下代码。另外，每次状态变更都会触发consul agent通过gRPC调用server的Catalog.Register来注册服务，我的理解其实是覆盖先前注册关于该服务的信息。

----
### Consul 中的术语
在描述架构之前，我们提供术语表以帮助澄清正在讨论的内容：


- 代理（agent） - 代理是Consul集群的每个成员上长时间运行的守护程序。它是通过运行consul agent 命令来启动的。代理能够以客户端或服务器模式运行。由于所有节点都必须运行代理，因此将节点称为客户端或服务器更简单，但代理还有其他实例。所有代理都可以运行DNS或HTTP接口，并负责运行检查并保持服务同步。


- 客户端模式（client agent） - 客户端是将所有RPC调用转发到服务器的代理。客户端是相对无状态的。客户端执行的唯一后台活动是参与LAN gossip pool（局域网 Gossip池）。这会花费非常非常小的资源并且仅消耗少量的网络带宽。


- 服务器模式(server agent) - 服务器是具有扩展责任的代理，包括参与Raft仲裁，维护群集状态，响应RPC查询，与其他数据中心交换WAN Gossip（广域网Gossip）以及将查询转发给领导者或远程数据中心。


- 数据中心 （datacenter）- 虽然数据中心的定义似乎很明显，但必须考虑细微的细节。例如，在EC2中，多个可用区域是否被视为包含单个数据中心？我们将数据中心定义为专用，低延迟和高带宽的网络环境。这排除了通过公共互联网的通信，但出于我们的目的，单个EC2区域内的多个可用区域将被视为单个数据中心的一部分。


- 共识 （consensus）- 在我们的文档中使用时，我们使用共识来表示对当选领导者的协议以及对交易顺序的协议。由于这些事务应用于有限状态机，因此我们对共识的定义意味着复制状态机的一致性。维基百科上更详细地描述了共识，此处描述了我们的实现。


- Gossip - Consul建立在Serf之上，它提供了一个完整的gossip 协议（八卦协议Gossip协议），用于多种用途。 Serf提供成员维护，故障检测和事件广播。我们对这些的使用在八卦文档中有更多描述。Gossip参与随机的节点到节点的通信，主要是通过UDP。


- LAN Gossip - 指局域网八卦池，其中包含位于同一局域网或数据中心的节点。


- WAN Gossip - 指仅包含服务器（servers）的WAN八卦池。这些服务器主要位于不同的数据中心，通常通过互联网或广域网进行通信。


- RPC - 远程过程调用。这是一种允许客户端发出服务器请求的请求/响应机制。

