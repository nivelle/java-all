
一、 简介
1. 负载均衡的类型

负载均衡可以采用硬件设备（例如常常听见的 F5），也可以采用软件负载
商用硬件负载设备成本通常较高（一台几十万甚至上百万），所以一般 情况下会采用软件负载
软件负载解决的两个核心问题是：选谁、转发，其中最著名的是 lvs
2. lvs 是什么？

英文全称是 Linux Virtual Server，即 Linux 虚拟服务器
由 章 文 嵩 博 士 发 起 的 自 由 软 件 项 目 ， 它 的 官 方 站 点 是 www.linuxvirtualserver.org
Linux2.4 内核以后，LVS 已经是 Linux 标准内核的一部分
可以将请求分发给后端真实服务器处理
有许多比较著名网站和组织都在使用 LVS 架设的集群系统，例如：Linux 的门户网站（www.linux.com）、向 RealPlayer 提供音频视频服务而闻 名的 Real 公司（www.real.com ）、全球最大的开源网站 （sourceforge.net）等
提供了多种调度算法
轮询调度（Round-Robin Scheduling）
加权轮询调度（Weighted Round-Robin Scheduling）
最小连接调度（Least-Connection Scheduling）
加权最小连接调度（Weighted Least-Connection Scheduling）
基于局部性的最少链接（Locality-Based Least Connections  Scheduling）
带 复 制 的 基 于 局 部 性 最 少 链 接 （ Locality-Based Least  Connections with Replication Scheduling）
目标地址散列调度（Destination Hashing Scheduling）
源地址散列调度（Source Hashing Scheduling）
最短预期延时调度（Shortest Expected Delay Scheduling）
不 排 队 调 度 （ Never Queue Scheduling ）对应: rr|wrr|lc|wlc|lblc|lblcr|dh|sh|sed|nq
有三种转发规则
NAT：简单理解，就是数据进出都通过 LVS，性能不是很好。
TUNL：简单理解：隧道
DR:最高效的负载均衡规则
3. lvs 的体系结构

最前端的负载均衡层，用 Load Balancer 表示
中间的服务器集群层，用 Server Array 表示
最底端的数据共享存储层，用 Shared Storage 表示
在用户看来，所有的内部应用都是透明的，用户只是在使用一个虚拟服 务器提供的高性能服务
4. keepAlived 是什么？

因为所有的请求都要经过负载均衡，所以负载均衡必然是非常重要，不 能挂掉，说白了就是要 keep the lvs alived。
提供的功能就是可以配置 2 台 LVS，一台主机，一台备机。并且检测任 何一个节点是否还活着。
5. lvs 的优点？

抗负载能力强，因为 lvs 工作方式的逻辑是非常之简单，而且工作在网络 4 层仅做请求分发之用，没有流量，所以在效率上基本不需要太过考虑。
有完整的双机热备方案，当节点出现故障时，lvs 会自动判别，所以系统整体是非常稳定的。
基本上能支持所有应用，因为 lvs 工作在 4 层，所以它可以对几乎所有应用做负载均衡，包括 http、数据库、聊天室等等。
6. lvs 负载均衡机制

lvs 是四层负载均衡，也就是说建立在 OSI 模型的第四层——传输层之 上
传输层上有 TCP/UDP，lvs 支持 TCP/UDP 的负载均衡
因为 LVS 是四层负载均衡，因此它相对于其它高层负载均衡的解决办法， 比如 DNS 域名轮流解析、应用层负载的调度、客户端的调度等，它的效 率是非常高的
lvs 的转发可以通过修改 IP 地址实现（NAT 模式）
lvs 的转发还可以通过修改直接路由实现（DR 模式）
7. lvs 与 nginx 对比？

负载度    lvs 优于 nginx
稳定度    lvs 优于 nginx
服务器性能要求 lvs 优于 nginx
网络层数的效率 lvs 优于 nginx  网络七层：应用层、会话层、表示层、传输层、网络层、链路层、 物理层
功能多少   nginx 优于 lvs
8. lvs+keepAlived 的应用场景？

大型网站负载均衡
二.lvs搭建
1.规划
2台webserver

1台主lvs

1台备lvs

2.两台webServer的配置
（1）在线安装httpd，第一台和第二台都执行



（2）设置开机自启动



（3）开启http服务器



（4）通过浏览器访问http服务器，默认端口是80



（5）创建html文件，写入内容，区分两个节点







（6）创建文件并写入如下内容



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
（7）给脚本赋权限



（8）启动脚本



（9）用ifconfig查看效果



（10）第二台http也同样做上面步骤，增加路由，SNS_VIP要跟第一台一样







（11）查看系统内核版本，2.4以后就有lvs，不用安装



（12）lvs的机器安装keepalived，做心跳检查用的



（13）查看配置文件安装位置



（14）创建并覆盖上面的配置文件



（15）编辑刚创建的文件，加入以下内容

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
（16）开启LVS机器的keepalived



3.LVS测试
（1）转发，访问虚拟IP，真是服务器提供服务，为了解决浏览器缓存看不到效果的问题，访问的URL后加 a=Math.random() 随机数



（2）故障移除，停其中一个服务，认为down掉了







（3）故障恢复自动添加



4.LVS备机搭建
（1）步骤同主机搭建，一直到配置文件，如下

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

5.LVS主备测试
（1）主LVS模拟挂掉



（2）测试后，证明主备自动切换，LVS高可用

（3）主LVS启动，自动上位
