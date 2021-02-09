### linux 网络

#### 网络模型

[![ySjszt.png](https://s3.ax1x.com/2021/01/28/ySjszt.png)](https://imgchr.com/i/ySjszt)

- 传输层在应用程序数据前面加了TCP头

- 网络层在TCP数据包层前加了IP头

- 网络接口层，在IP数据包前后分别加了帧头和帧尾

[![ySvult.md.png](https://s3.ax1x.com/2021/01/28/ySvult.md.png)](https://imgchr.com/i/ySvult)

- 网卡硬中断只处理最核心的网卡数据读取或者发送

- 协议栈中的大部分逻辑，都在软中断中处理

#### 网络包接收流程

1. 内核分配一个主内存地址段（DMA缓冲区)，网卡设备可以在DMA缓冲区中读写数据 

2. 当来了一个网络包，网卡将网络包写入DMA缓冲区也就是收包队列，通过硬中断，高速处理程序已经收到了网络包 

3. 硬中断处理程序锁定当前DMA缓冲区，然后将网络包拷贝到另一块内存区(sk_buff)，清空并解锁当前DMA缓冲区，然后通过软中断通知内核收到了新的网络包。 
   
4. 当发送数据包时，与上述相反。链路层将数据包封装完毕后，放入网卡的DMA缓冲区，并调用系统硬中断，通知网卡从缓冲区读取并发送数据。

#### 网络包发送流程

1. 调用Socket API 发送网络包，属于系统调用，陷入到内核态的套接字层中

2. 套接字层会把数据包放到Socket发送缓存区中

3. 网络协议栈从Socket发送缓冲区中，取出数据包；按照TCP/IP栈，从上倒下逐层处理；传输层和网络层，分别增加TCP和IP头，执行路由查找确认下一跳的IP，按照MTU大小进行分片

4. 分片后的网络层，发送到网络接口层，进行物理地址寻址，找到下一跳的MAC地址。添加帧头帧尾，放到发包队列中。这一切完成后，会有软中断通知驱动程序：发包队列中有新的网络帧需要发送

### 网络配置

#### ifconfig

````
root@jessy:~# ifconfig
eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 172.17.131.190  netmask 255.255.240.0  broadcast 172.17.143.255
        inet6 fe80::216:3eff:fe2c:8c0  prefixlen 64  scopeid 0x20<link>
        ether 00:16:3e:2c:08:c0  txqueuelen 1000  (Ethernet)
        RX packets 171747  bytes 230331559 (230.3 MB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 34810  bytes 5289066 (5.2 MB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0

lo: flags=73<UP,LOOPBACK,RUNNING>  mtu 65536
        inet 127.0.0.1  netmask 255.0.0.0
        inet6 ::1  prefixlen 128  scopeid 0x10<host>
        loop  txqueuelen 1000  (Local Loopback)
        RX packets 5837  bytes 496792 (496.7 KB)
        RX errors 0  dropped 【丢弃包】0  overruns【超限数据包】 0  frame 0
        TX packets 5837  bytes 496792 (496.7 KB)
        TX errors 0  dropped 0 overruns 0  carrier【carrier错误：双工模式不匹配，物理电缆问题】 0  collisions【碰撞数据包】 0


````
#### netstat

````
root@jessy:~# netstat -nlp | head -n 3
Active Internet connections (only servers)
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name    
tcp        0      0 127.0.0.53:53           0.0.0.0:*               LISTEN      463/systemd-resolve 


````

#### ss

````
# ss -ltnp | head -n 3
State    Recv-Q    Send-Q        Local Address:Port        Peer Address:Port                                                                                    
LISTEN   0         128           127.0.0.53%lo:53               0.0.0.0:*        users:(("systemd-resolve",pid=463,fd=13))                                      
LISTEN   0         128                 0.0.0.0:22               0.0.0.0:*        users:(("sshd",pid=705,fd=3))     

````

##### 当套接字处于连接状态（Established）时

- Recv-Q 表示套接字缓冲还没有被应用程序取走的字节数（即接收队列长度）

- Send-Q 表示还没有被远端主机确认的字节数（即发送队列长度）

##### 当套接字处于监听状态（Listening）时

- Recv-Q 表示全连接队列的长度

- Send-Q 表示全连接队列的最大长度。

### ping 

基于ICMP协议

````
 ping -c3 114.114.114.114
PING 114.114.114.114 (114.114.114.114) 56(84) bytes of data.
64 bytes from 114.114.114.114: icmp_seq=1 ttl=72 time=44.6 ms
64 bytes from 114.114.114.114: icmp_seq=2 ttl=82 time=44.6 ms
64 bytes from 114.114.114.114: icmp_seq=3 ttl=77 time=44.6 ms

--- 114.114.114.114 ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 2003ms
rtt min/avg/max/mdev = 44.606/44.624/44.659/0.024 ms


````

### C10K 问题

#### I/O模型优化

1. 使用非阻塞I/O 和水平触发通知，比如 select 或者 poll

````
1. 应用程序每次调用 select 和 poll 时，还需要把文件描述符的集合，从用户空间传入内核空间，由内核修改后，再传出到用户空间中

2. select 使用固定长度的位相量，表示文件描述符的集合，因此会有最大描述符数量的限制。

3. poll 改进了 select 的表示方法，换成了一个没有固定长度的数组，这样就没有了最大描述符数量的限制（当然还会受到系统文件描述符限制）

````
2. 使用非阻塞I/O和边缘触发通知，比如 epoll

````
1. epoll 使用红黑树，在内核中管理文件描述符的集合，这样，就不需要应用程序在每次操作时都传入、传出这个集合。

2. epoll 使用事件驱动的机制，只关注有 I/O 事件发生的文件描述符，不需要轮询扫描整个集合。

````

3. 使用异步I/O

#### 工作模式优化

- 主进程 + 多个worker子进程

````
1. 主进程执行 bind() + listen() 后，创建多个子进程；

2. 在每个子进程中，都通过 accept() 或 epoll_wait() ，来处理相同的套接字

````
- 监听到相同端口的多进程模型。在这种方式下，所有的进程都监听相同的接口，并且开启 SO_REUSEPORT 选项，由内核负责将请求负载均衡到这些监听进程中去