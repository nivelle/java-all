### 一、 简介

#### 1. 负载均衡的类型

- 负载均衡可以采用硬件设备（例如常常听见的 F5），也可以采用软件负载
- 商用硬件负载设备成本通常较高（一台几十万甚至上百万），所以一般 情况下会采用软件负载
- 软件负载解决的两个核心问题是：选谁、转发，其中最著名的是 lvs
#### 2. lvs 是什么？

- 英文全称是 Linux Virtual Server，即 Linux 虚拟服务器
- 由 章 文 嵩 博 士 发 起 的 自 由 软 件 项 目 ， 它 的 官 方 站 点 是 www.linuxvirtualserver.org
- Linux2.4 内核以后，LVS 已经是 Linux 标准内核的一部分
- 可以将请求分发给后端真实服务器处理
- 有许多比较著名网站和组织都在使用 LVS 架设的集群系统，例如：Linux 的门户网站（www.linux.com）、向 RealPlayer 提供音频视频服务而闻 名的 Real 公司（www.real.com ）、全球最大的开源网站 （sourceforge.net）等
- 提供了多种调度算法
  - 轮询调度（Round-Robin Scheduling）
  - 加权轮询调度（Weighted Round-Robin Scheduling）
  - 最小连接调度（Least-Connection Scheduling）
  - 加权最小连接调度（Weighted Least-Connection Scheduling）
  - 基于局部性的最少链接（Locality-Based Least Connections  Scheduling）
  - 带 复 制 的 基 于 局 部 性 最 少 链 接 （ Locality-Based Least  Connections with Replication Scheduling）
  - 目标地址散列调度（Destination Hashing Scheduling）
  - 源地址散列调度（Source Hashing Scheduling）
  - 最短预期延时调度（Shortest Expected Delay Scheduling）
  - 不 排 队 调 度 （ Never Queue Scheduling ）对应: rr|wrr|lc|wlc|lblc|lblcr|dh|sh|sed|nq

- 有三种转发规则
  - NAT：简单理解，就是数据进出都通过 LVS，性能不是很好。
  - UNL：简单理解：隧道
  - DR:最高效的负载均衡规则

#### 3. lvs 的体系结构

- 最前端的负载均衡层，用 Load Balancer 表示
- 中间的服务器集群层，用 Server Array 表示
- 最底端的数据共享存储层，用 Shared Storage 表示
- 在用户看来，所有的内部应用都是透明的，用户只是在使用一个虚拟服 务器提供的高性能服务

#### 4. keepAlived 是什么？

- 因为所有的请求都要经过负载均衡，所以负载均衡必然是非常重要，不 能挂掉，说白了就是要 keep the lvs alived。
- 提供的功能就是可以配置 2 台 LVS，一台主机，一台备机。并且检测任 何一个节点是否还活着。

#### 5. lvs 的优点？

- 抗负载能力强，因为 lvs 工作方式的逻辑是非常之简单，而且工作在网络 4 层仅做请求分发之用，没有流量，所以在效率上基本不需要太过考虑。
- 有完整的双机热备方案，当节点出现故障时，lvs 会自动判别，所以系统整体是非常稳定的。
- 基本上能支持所有应用，因为 lvs 工作在 4 层，所以它可以对几乎所有应用做负载均衡，包括 http、数据库、聊天室等等。

#### 6. lvs 负载均衡机制

- lvs 是四层负载均衡，也就是说建立在 OSI 模型的第四层——传输层之 上
- 传输层上有 TCP/UDP，lvs 支持 TCP/UDP 的负载均衡
- 因为 LVS 是四层负载均衡，因此它相对于其它高层负载均衡的解决办法， 比如 DNS 域名轮流解析、应用层负载的调度、客户端的调度等，它的效 率是非常高的
- lvs 的转发可以通过修改 IP 地址实现（NAT 模式）
- lvs 的转发还可以通过修改直接路由实现（DR 模式）
#### 7. lvs 与 nginx 对比？
- 负载度    lvs 优于 nginx
- 稳定度    lvs 优于 nginx
- 服务器性能要求 lvs 优于 nginx
- 网络层数的效率 lvs 优于 nginx  网络七层：应用层、会话层、表示层、传输层、网络层、链路层、 物理层
- 功能多少   nginx 优于 lvs

#### 8. lvs+keepAlived 的应用场景？

- 大型网站负载均衡

### 二.lvs搭建
- 1.规划
- 2台webserver

- 1台主lvs

- 1台备lvs

#### 2.两台webServer的配置
- （1）在线安装httpd，第一台和第二台都执行

![](https://img-blog.csdn.net/20180914193832912?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- （2）设置开机自启动

![](https://img-blog.csdn.net/20180914193841694?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
- （3）开启http服务器
![](https://img-blog.csdn.net/20180914193847560?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- （4）通过浏览器访问http服务器，默认端口是80

![](https://img-blog.csdn.net/20180914193853207?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- （5）创建html文件，写入内容，区分两个节点

![](https://img-blog.csdn.net/20180914193859515?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- （6）创建文件并写入如下内容
![](https://img-blog.csdn.net/20180914193908354?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

````shell
#!/bin/bash
#description : start realserver
SNS_VIP=192.168.20.50 #定义了一个VIP变量，必须跟真是服务在一个网段
/etc/rc.d/init.d/functions  
case "$1" in  
start)  
echo " start LVS of REALServer"  
/sbin/ifconfig lo:0 $SNS_VIP broadcast $SNS_VIP netmask 255.255.255.255 up  #增加一个本地路由 lo:0
echo "1" >/proc/sys/net/ipv4/conf/lo/arp_ignore  
echo "2" >/proc/sys/net/ipv4/conf/lo/arp_announce  
echo "1" >/proc/sys/net/ipv4/conf/all/arp_ignore  
echo "2" >/proc/sys/net/ipv4/conf/all/arp_announce  
;;  
stop)  
/sbin/ifconfig lo:0 down  
echo "close LVS Directorserver"  
echo "0" >/proc/sys/net/ipv4/conf/lo/arp_ignore  
echo "0" >/proc/sys/net/ipv4/conf/lo/arp_announce  
echo "0" >/proc/sys/net/ipv4/conf/all/arp_ignore  
echo "0" >/proc/sys/net/ipv4/conf/all/arp_announce  
;;  
*)  
echo "Usage: $0 {start|stop}"  
exit 1  
esac

````
####（7）给脚本赋权限

![](https://img-blog.csdn.net/20180914194053179?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

####（8）启动脚本

![](https://img-blog.csdn.net/20180914194059161?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

####（9）用ifconfig查看效果

![](https://img-blog.csdn.net/2018091419410575?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

####（10）第二台http也同样做上面步骤，增加路由，SNS_VIP要跟第一台一样

![](https://img-blog.csdn.net/2018091419411255?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

![](https://img-blog.csdn.net/20180914194129405?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

####（11）查看系统内核版本，2.4以后就有lvs，不用安装

####（12）lvs的机器安装keepalived，做心跳检查用的
####（13）查看配置文件安装位置

####（14）创建并覆盖上面的配置文件

####（15）编辑刚创建的文件，加入以下内容
```shell
global_defs {                       
#   notification_email {             
#   }
#   smtp_connect_timeout 30
        router_id LVS_DEVEL             
}
vrrp_instance VI_1 {            
        state MASTER     #配置LVS是主机的状态        
        interface eno16777736     #配置LVS机器对外开放的IP       
        virtual_router_id 51        
        priority 100                  
        advert_int 1           
        authentication {        
                auth_type PASS
                auth_pass 1111
        }
        virtual_ipaddress {         
                192.168.20.50    #LVS的对内IP
        }
}
virtual_server 192.168.20.50 80 {
        delay_loop 6           
        lb_algo wrr            
        lb_kind DR         #使用LVSDR模式                 
        nat_mask 255.255.255.0   
        persistence_timeout 0    
        protocol TCP                          
        real_server 192.168.20.233 80 {    #真实服务的IP 
                weight 1        #配置加权轮询的权重             
                TCP_CHECK {                     
                        connect_timeout 10   
                        nb_get_retry 3
                        delay_before_retry 3
                        connect_port 80
                }
        }
        real_server 192.168.20.203 80 {
                weight 2
                TCP_CHECK {
                        connect_timeout 10
                        nb_get_retry 3
                        delay_before_retry 3
                        connect_port 80
                }
        }
}
```
####（16）开启LVS机器的keepalived


### 3.LVS测试

- （1）转发，访问虚拟IP，真是服务器提供服务，为了解决浏览器缓存看不到效果的问题，访问的URL后加 a=Math.random() 随机数
![](https://img-blog.csdn.net/20180914194430456?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- （2）故障移除，停其中一个服务，认为down掉了

![](https://img-blog.csdn.net/20180914194443877?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- （3）故障恢复自动添加
![](https://img-blog.csdn.net/20180914194451335?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)
### 4.LVS备机搭建

- （1）步骤同主机搭建，一直到配置文件，如下

````shell
global_defs {

#   notification_email {

#   }

#   smtp_connect_timeout 30

        router_id LVS_DEVEL             

}

vrrp_instance VI_1 {

        state BACKUP     #配置LVS是主机的状态        

        interface eno16777736     #配置LVS机器对外开放的IP       

        virtual_router_id 51        

        priority 100                  

        advert_int 1           

        authentication {        

                auth_type PASS

                auth_pass 1111

        }

        virtual_ipaddress {         

                192.168.20.50    #LVS的对内IP

        }

}

virtual_server 192.168.20.50 80 {
delay_loop 6

        lb_algo wrr            

        lb_kind DR         #使用LVSDR模式                 

        nat_mask 255.255.255.0   

        persistence_timeout 0    

        protocol TCP                          

        real_server 192.168.20.233 80 {    #真实服务的IP

                weight 1        #配置加权轮询的权重             

                TCP_CHECK {                     

                        connect_timeout 10   

                        nb_get_retry 3

                        delay_before_retry 3

                        connect_port 80

                }

        }

        real_server 192.168.20.203 80 {
                weight 2

                TCP_CHECK {
                        connect_timeout 10

                        nb_get_retry 3

                        delay_before_retry 3

                        connect_port 80

                }

        }

}
````
### 5.LVS主备测试
- （1）主LVS模拟挂掉

![](https://img-blog.csdn.net/2018091419464994?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3N1cGVyX2xpeGlhbmc=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

- （2）测试后，证明主备自动切换，LVS高可用

- （3）主LVS启动，自动上位


-----

### 1 什么是LVS

LVS是一种基于TCP/IP的负载均衡技术，也就是L4的的，转发效率极高(数据结构使用hash)。是由国内开源第一人，章文嵩博士期间开发完成。

LVS由前端的负载均衡器(Load Balancer，LB)和后端的真实服务器(Real Server，RS)群组成。RS间可通过局域网或广域网连接。LVS的这种结构对用户是透明的，用户只能看见一台作为LB的虚拟服务器(Virtual Server)，而看不到提供服务的RS群。

当用户的请求发往虚拟服务器，LB根据设定的包转发策略和负载均衡调度算法将用户请求转发给RS。RS再将用户请求结果返回给用户。同请求包一样，应答包的返回方式也与包转发策略有关。

![](https://img-blog.csdnimg.cn/20190427114701507.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTE1NjM5MDM=,size_16,color_FFFFFF,t_70)
2 原理
2.1 后端调度算法
• Round Robin
• Weighted round robin
• Source ip hash(来自于同一个ip的请求调度到同一个real server)
• Destination hash
• Least connections
Weighted Least Connection(default)

2.2 如何实现

![](https://img-blog.csdnimg.cn/20190427123012167.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTE1NjM5MDM=,size_16,color_FFFFFF,t_70)
客户端请求vip+port后，经过input链，如果ipvs发现报文访问的vip+port与我们定义的lvs集群规则相符合。经过PREROUTING链，经检查本机路由表，送往INPUT链；在进入netfilter的INPUT链时，ipvs强行将请求报文通过ipvsadm定义的集群服务策略的路径改为FORWORD链，将报文转发至后端真实提供服务的主机。

3 怎么用
启动了两个http容器，然后用ipvsadm命令添加了3条规则

docker run -itd --name test1 -h test1 strm/helloworld-http:latest  // 容器ip是172.17.0.4
docker run -itd --name test2 -h test2 strm/helloworld-http:latest  // 容器ip是172.17.0.5
sudo ipvsadm -A -t 10.0.2.15:80 -s rr  // rr表示用的是轮询算法
sudo ipvsadm -a -t 10.0.2.15:80 -r 172.17.0.4 -m
sudo ipvsadm -a -t 10.0.2.15:80 -r 172.17.0.5 -m
sudo ipvsadm -Ln   // 可以看到输出的内容
IP Virtual Server version 1.2.1 (size=4096)
Prot LocalAddress:Port Scheduler Flags
-> RemoteAddress:Port           Forward Weight ActiveConn InActConn
TCP  10.0.2.15:80 rr
-> 172.17.0.4:80                Masq    1      0          0         
-> 172.17.0.5:80                Masq    1      0          0         

测试如下：因为用的是rr，所以流量是平均分配到后端的实例。

for in in `seq 1 100`; do curl -s 10.0.2.15:80; echo; done | sort | uniq -c   
100
50 <html><head><title>HTTP Hello World</title></head><body><h1>Hello from test1</h1></body></html
50 <html><head><title>HTTP Hello World</title></head><body><h1>Hello from test2</h1></body></html

Keepalived
参考

1 什么是Keepalived
keepalived是一个类似于layer3, 4 & 5交换机制的软件，也就是我们平时说的第3层、第4层和第5层交换。Keepalived的作用是检测web服务器的状态，如果有一台web服务器死机，或工作出现故障，Keepalived将检测到，并将有故障的web服务器从系统中剔除，当web服务器工作正常后Keepalived自动将web服务器加入到服务器群中，这些工作全部自动完成。

功能比较全面：内置了LVS和检测到异常状态发邮件等功能。

2 原理
典型的Web服务器架构图如下，两个运行keepalived的Server 暴露相同的VIP，对外提供VIP，和Keepalived Server运行LVS，将流量负载均衡到后端真是主机。

![](https://img-blog.csdnimg.cn/20190427144044480.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTE1NjM5MDM=,size_16,color_FFFFFF,t_70)
Layer3,4&5工作在IP/TCP协议栈的IP层，TCP层，及应用层,原理分别如下：
Layer3：Keepalived会定期向服务器群中的服务器发送一个ICMP的数据包,如果发现某台服务的IP地址没有激活，Keepalived便报告这台服务器失效，并将它从服务器群中剔除，这种情况的典型例子是某台服务器被非法关机。Layer3的方式是以服务器的IP地址是否有效作为服务器工作正常与否的标准。

Layer4:Layer4主要以TCP端口的状态来决定服务器工作正常与否。如web server的服务端口一般是80，如果Keepalived检测到80端口没有启动，则Keepalived将把这台服务器从服务器群中剔除。

Layer5：Layer5就是工作在具体的应用层了，比Layer3,Layer4要复杂一点，在网络上占用的带宽也要大一些。Keepalived将根据用户的设定检查服务器程序的运行是否正常，如果与用户的设定不相符，则Keepalived将把服务器从服务器群中剔除。

所以一般来讲，keepalived 会和负载均衡器配合使用。keepalived 提供负载均衡器的高可用。

3 怎么用
在 haproxy-master 机器上：

vrrp_script chk_haproxy {
script "killall -0 haproxy"  # verify haproxy's pid existance
interval 2                   # check every 2 seconds
weight -2                    # if check failed, priority will minus 2
}

vrrp_instance VI_1 {
state MASTER                 # Start-up default state
interface ens18              # Binding interface
virtual_router_id 51         # VRRP VRID(0-255), for distinguish vrrp's multicast
priority 105                 # VRRP PRIO
virtual_ipaddress {          # VIP, virtual ip
192.168.0.146
}
track_script {               # Scripts state we monitor
chk_haproxy              
}
}
=
在 haproxy-backup 机器上：

vrrp_script chk_haproxy {
script "killall -0 haproxy"
interval 2
weight -2
}

vrrp_instance VI_1 {
state BACKUP
interface ens18
virtual_router_id 51
priority 100
virtual_ipaddress {
192.168.0.146
}
track_script {             
chk_haproxy              
}
}

我们为两台机器（master、backup）安装了 Keepalived 服务并设定了上述配置。

可以发现，我们绑定了一个虚拟 IP (VIP, virtual ip): 192.168.0.146，在 haproxy-master + haproxy-backup 上用 Keepalived 组成了一个集群。在集群初始化的时候，haproxy-master 机器的 被初始化为 MASTER。

间隔 2 seconds() 会定时执行

关于 参数的使用：

检测失败，并且 weight 为正值：无操作
检测失败，并且 weight 为负值：priority = priority - abs(weight)
检测成功，并且 weight 为正值：priority = priority + weight
检测成功，并且 weight 为负值：无操作
weight 默认值为 0，对此如果感到迷惑可以参考：HAProxy github code

故障切换工作流程：

当前的 MASTER 节点

一个 Keepalived 服务中可以有个 0 个或者多个 vrrp_instance
可以有多个绑定同一个 VIP 的 Keepalived 服务（一主多备），本小节中只是写了两个注意 <virtual_router_id>，同一组 VIP 绑定的多个 Keepalived 服务的 <virtual_router_id> 必须相同；多组 VIP 各自绑定的 Keepalived 服务一定与另外组不相同。否则前者会出现丢失节点，后者在初始化的时候会出错。

HAProxy
参考

1 什么是HAProxy
HAProxy提供高可用性、负载均衡。HAProxy特别适用于那些负载特大的web站点，这些站点通常又需要会话保持或七层处理。

LVS只工作在4层，没有流量产生，使用范围广，对操作员的网络素质要求较高；HAproxy及支持7层也支持4层的负载均衡，更专业；

其功能是用来提供基于cookie的持久性，基于内容的交换，过载保护的高级流量管制，自动故障切换，以正则表达式为基础的标题控制运行事件，基于Web的报表，高级日志记录以帮助排除故障的应用或网络及其他功能。HAProxy系统自带管理页面。

HAProxy 的 Proxy 配置可以分为如下几个部分：
defaults，frontend，backend，listen

defaults 包含一些全局的默认环境变量，frontend 定义了如何把数据 forward 到 backend，backend 描述了一组接收 forward 来的数据的后端服务。listen 则把 frontend 和 backend 组合到了一起，定义出一个完整的 proxy。
![](https://img-blog.csdnimg.cn/20190427151944163.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTE1NjM5MDM=,size_16,color_FFFFFF,t_70)
2 原理
下面是一段简单的 HAProxy 的配置：

listen app1-cluster
bind *:4000
mode http
maxconn 300
balance roundrobin
server server1 192.168.0.189:4004 maxconn 300 check
server server2 192.168.0.190:4004 maxconn 300 check
server server3 192.168.0.191:4004 maxconn 300 check

listen app2-cluster
bind *:5000
mode http
maxconn 300
balance roundrobin
server server1 192.168.0.189:5555 maxconn 300 check
server server2 192.168.0.190:5555 maxconn 300 check
server server3 192.168.0.191:5555 maxconn 300 check

HAProxy 的配置文件中定义了两个 listen 模块，分别监听在 4000、5000 端口。监听在 4000 端口的模块，使用 roundrobin （轮询）负载均衡算法，把请求分发到了三个后端服务。

3 怎么用
3.1 创建后端服务
首先搭建两台服务器，每台服务器上启动一个APP，这里用容器起后端Server。
![](https://img-blog.csdnimg.cn/20190427155926192.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTE1NjM5MDM=,size_16,color_FFFFFF,t_70)
host1(192.168.82.31)

docker run -itd --name host1 -h host1 -p 8080:80 strm/helloworld-http:latest
1
host2(192.168.17.145):

docker run -itd --name host2 -h host2 -p 8080:80 strm/helloworld-http:latest
1
3.2 使用 HAProxy 做业务服务的高可用和负载均衡
我们现在有两台机器：ha-proxy-master、ha-proxy-backup。（为了方便，下边直接用 Docker 做了）。使用 haproxy:1.7.9 版本的 Docker 镜像。haproxy-master、haproxy-backup为了方便就使用host1 和 host2了，下面说的ha-proxy-master-ip，其实就是host1的ip。

haproxy.cfg如下：

global
daemon
maxconn 30000
log 127.0.0.1 local0 info
log 127.0.0.1 local1 warning

defaults
mode http
option http-keep-alive
option httplog
timeout connect 5000ms
timeout client 10000ms
timeout server 50000ms
timeout http-request 20000ms

# custom your own frontends && backends && listen conf
#CUSTOM

listen app1-cluster
bind *:8181
mode http
maxconn 300
balance roundrobin
server server1 192.168.82.31:8080 maxconn 300 check
server server2 192.168.17.145:8080 maxconn 300 check

listen stats
bind *:1080
stats refresh 30s
stats uri /stats


编译Docker image:

docker build -t custom-haproxy:1.7.9 .
1
在ha-proxy-master和ha-proxy-backup节点运行：

docker run -it --net=host --privileged --name haproxy-1 -d custom-haproxy:1.7.9
1
然后可以通过ha-proxy-master-ip:1080/stats 看到状态
![](https://img-blog.csdnimg.cn/20190427162136988.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTE1NjM5MDM=,size_16,color_FFFFFF,t_70)
也可以通过ha-proxy-master-ip:8181 去访问后端的服务。并且是轮流的访问到host1和host2。用ha-proxy-backup-ip也是一样的。

3.3 使用 Keepalived 做 HAProxy 服务的高可用
在上一小节中，在 ha-proxy-master、ha-proxy-backup 两台机器上搭了两套相同的 HAProxy 服务。我们希望在一个连续的时间段，只由一个节点为我们提供服务，并且在这个节点挂掉后另外一个节点能顶上。

本小节的目标是在haproxy-master、haproxy-backup 上分别搭 Keepalived 服务，并区分主、备节点，以及关停掉一台机器后保证 HAProxy 服务仍然正常运行。

两台ha-proxy机器上安装：

[root@instance-t9flxirx ~]# yum install -y keepalived
[root@instance-t9flxirx ~]# keepalived -v
Keepalived v1.3.5 (03/19,2017), git commit v1.3.5-6-g6fa32f2
1
2
3
在ha-proxy-master上添加如下：

vi /etc/keepalived/keepalived.conf
vrrp_script chk_haproxy {
script "killall -0 haproxy"
interval 2                   
}

vrrp_instance VI_1 {
state MASTER                 
interface eth0              
virtual_router_id 51        
priority 105                
virtual_ipaddress {         
192.168.17.146
}
track_script {
chk_haproxy
}
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
在ha-proxy-slave节点添加如下：

vi /etc/keepalived/keepalived.conf
vrrp_script chk_haproxy {
script "killall -0 haproxy"
interval 2
}

vrrp_instance VI_1 {
state BACKUP
interface eth0
virtual_router_id 51
priority 100
virtual_ipaddress {
192.168.17.146
}
track_script {
chk_haproxy
}
}

然后都重启服务：

systemctl restart keepalived
1
这个时候在ha-proxy-master上的eth0下，会配置secondary ip为192.168.17.146，该IP就是VIP。

[root@instance-t9flxirx ~]# ip addr | grep 146
inet 192.168.17.146/32 scope global eth0
1
2
至此，一个简单的HAProxy + Keepalived 服务了。整体结构图如下所示：

![](https://img-blog.csdnimg.cn/20190427164809553.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3UwMTE1NjM5MDM=,size_16,color_FFFFFF,t_70)
将ha-proxy-master 停机，则会发现VIP将配置到ha-proxy-slave上。但是服务正常可用。

Nginx
nginx重点是web服务器，替换的是apache，同时具备lb的作用，haproxy是单纯的lb，可以对照lvs进行比较
