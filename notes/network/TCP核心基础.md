### socket

[![rILkaF.png](https://s3.ax1x.com/2020/12/28/rILkaF.png)](https://imgchr.com/i/rILkaF)

建立连接后，数据的传输就不再是单向的，而是双向的，这也是TCP的一个显著特性。

### TCP三次握手

#### 服务端准备连接过程

- 创建套接字

````

int socket(int domain, int type, int protocol)

1. domain: 就是指 PF_INET、PF_INET6 以及 PF_LOCAL 等，表示什么样的套接字

2. type: SOCK_STREAM: 表示的是字节流，对应 TCP；SOCK_DGRAM： 表示的是数据报，对应 UDP;SOCK_RAW: 表示的是原始套接字
````

- bind:把套接字和套接字地址绑定，标记该套接字


- listen:通过listen函数，将原来套接字转换为被动套接字，告诉操作系统内核，监听指定地址端口的请求

````

int listen (int socketfd, int backlog)

1. 第一个参数是套接字描述符

2. 第二个参数backlog,在linux 中表示已经完成（ESTABLISHED）且未accept的队列大小，决定了可以结束的并发数目。

````

- accept: 客户端连接请求到达，服务器端应答成功，建立连接，这个时候操作系统内核把这个事件通知到应用程序，并让应用程序感知到这个连接。

````

int accept(int listensockfd, struct sockaddr *cliaddr, socklen_t *addrlen)

1. 第一个参数 listensockfd 是套接字，可以叫它为 listen 套接字，因为这就是前面通过 bind，listen 一系列操作而得到的套接字

2. cliadd 是通过指针方式获取的客户端的地址，addrlen 告诉我们地址的大小

3. 返回值是一个全新的描述字，代表了与客户端的连接

````

#### 客户端准备连接过程

- 创建套接字

```

int socket(int domain, int type, int protocol)

```

- connect:客户端和服务端建立连接，通过connect完成

````


int connect(int sockfd, const struct sockaddr *servaddr, socklen_t addrlen)


1. sockfd 是连接套接字，通过前面讲述的 socket 函数创建

2. *servaddr 代表指向套接字地址结构的指针

3. addrlen 该结构的大小

````

客户在调用函数 connect 前不必非得调用 bind 函数，因为如果需要的话，内核会确定源 IP 地址，并按照一定的算法选择一个临时端口作为源端口。

### connect 函数触发了TCP 三次握手

#### 握手失败

1. 三次握手无法建立，客户端发出的SYN包没有任何响应，于是返回TIMEOUT错。--ip写错

2. 客户端收到了RST(复位)回答，这时客户端会立即返回CONNECTION REFUSED 错误。这种情况比较常见于客户端发送连接请求时的请求端口写错，因为RST是TCP在发送错误时发生的一种TCP分节。产生RET三个条件：目的地为某端口的SYN
到达，然而该端口上没有正在监听的服务器；TCP想取消一个已有连接；TCP接受到一个根本不存在的连接上的分节。--端口错误
   
3. 客户端发出的SYN包在网路上引起"destination unreachable”，即目的不可达错误。 --客户端和服务端路由不通

#### 握手成功

- 客户端connect阻塞，服务端accept阻塞

- 操作系统内核网路协议栈进行三次握手

1. 客户端协议栈向服务器端发送SYN包，并告诉服务端当前发送序列号j, 客户端 进入 SYNC_SENT状态；

2. 服务端协议栈收到这个包之后，和客户端进行ACK应答，应答值为j+1,表示对SYN 包j的确认，同时服务器也发送一个SYN包，告诉客户端当前我的发送序列号为k,服务器端进入SYNC_RCVD 状态

3. 客户端收到ACK之后，使得应用程序从connect调用返回，表示客户端导服务端的单向连接建立成功，客户端的状态为ESTABLISHED,同时客户端也会对服务器端的SYN包进行应答，应答数据为k+1

4. 应答包到达服务器端后，服务器端协议栈使得accept阻塞调用返回，这个时候服务器端到客户单的单向连接也建立成功，服务器端也进入ESTABLISHED状态

#### 为什么需要三次握手

- 第一次和第二次握手：客户端可以确认 客户端发送和客户端接受能力，以及服务端的收接受和发送能力

- 此时服务端，只能确认服务端的接受能力，不能确认服务端的发送能力

- 所以第三次握手，服务端收到客户端握手后，确认了服务端的发送能力，以及客户端的接收能力

### 套接字读写

#### 发送数据

````

ssize_t write (int socketfd, const void *buffer, size_t size)
ssize_t send (int socketfd, const void *buffer, size_t size, int flags)
ssize_t sendmsg(int sockfd, const struct msghdr *msg, int flags)

````
- 第一个函数是常见的文件写函数，如果把 socketfd 换成文件描述符，就是普通的文件写入。

- 如果想指定选项，发送带外数据，就需要使用第二个带 flag 的函数。所谓带外数据，是一种基于 TCP 协议的紧急数据，用于客户端 - 服务器在特定场景下的紧急处理。

- 如果想指定多重缓冲区传输数据，就需要使用第三个函数，以结构体 msghdr 的方式发送数据。

#### 发送缓冲区

- tcp握手成功后，操作系统内核会为每个连接创建配套的基础设施，发送缓冲区

- 发送缓冲区的大小可以通过套接字选项来改变，当我们的应用程序调用 write 函数时，实际所做的事情是把数据从应用程序中拷贝到操作系统内核的发送缓冲区中，并不一定是把数据通过套接字写出去。

- 若缓冲区满，则write函数阻塞，直到有足够的空间容纳要发送的数据。write函数返回

#### 读取数据

````

ssize_t read (int socketfd, void *buffer, size_t size)

````

read 函数要求操作系统内核从套接字描述字socketfd读取多少个字节，并将结果存储到buffer中。返回值告诉实际读取的字节数。如果返回为0，表示EOF(end-of-file),这在网络中表示对端发送了FIN包，要处理断连情况；

如果返回值为-1，表示出错。


### 四次挥手： TIME_WAIT 详解

[![rordj1.png](https://s3.ax1x.com/2020/12/28/rordj1.png)](https://imgchr.com/i/rordj1)

- TIME_WAIT 停留持续时间是固定的，是最长分节生命期MSL(maximum segment lifetime)的两倍，2MSL. TCP_TIME_WAIT_LEN 值为60秒，linux 系统停留在TIME_WAIT的时间为固定的60s

- 只有发起连接终止的一方会进入TIME_WAIT状态。

- TCP是双向的，因此关闭是双向的；

1. 主动关闭方应用调用close函数，该端TCP发送一个FIN包，表示需要关闭连接，之后主动关闭方进入FIN_WAIT_1状态

2. 接收到FIN包的对端执行被动关闭，这个FIN由TCPx协议处理。 TCP协议栈为FIN包插入一个文件结束符EOF到接收缓冲区中，应用程序可以通过read调用来感知这个FIN包。这个EOF会被放在已经排队等候的其他已接收的数据之后，这意味接收端要处理这种异常，表示EOF之后无
额外数据到达。被动关闭方进入CLOSE_WAIT状态
   
3. 被动关闭方将读到这个EOF，应用程序也调用close关闭它的套接字，这导致它的TCP也发送一个FIN包。 被动关闭方进入LAST_ACK

4. 主动关闭方接收到对方的FIN包，并确认这个FIN包。主动关闭方进入TIME_WAIT状态，而接收到ACK的被动关闭方进入CLOSED状态。经过2MSL时间之后，主动关闭方也进入CLOSED状态。

#### 为什么有TIME_WAIT状态

1. 确保最后的ACK能让被动关闭方接收，从而帮助其正常关闭。

假设报文传输出错，需要重传。如果主机1 的 ACK 报文没有传输成功，那么主机2 就会重新发送FIN报文。 如果主机1没有维护TIME_WAIT 状态，直接进入CLOSED状态，它就会失去当前状态上下文，只能回复一个RET操作，从而导致被动关闭方出现错误 现在主机1处于TIME_WAIT的状态，可以在接收到FIN报文之后，重新发送一个ACK 报文，使得主机2进入正常的CLOSED 状态

2. 相同地址的连接 和 报文迷走 有关,问了让旧连接的重复分节在网络中自然迷失

经过2MSL时间，让两个方向上的分组都被丢弃，使得原来连接的分组在网路中都自然消失，再出现的分组都是新化身所产生的。 2MSL的时间都是从主机1接收到FIN后发送ACK开始计时，如果在TIME_WAIT时间内，因为主机1的ACK没有传输到主机2，主机1又接收到了主机2重发的FIN报文，那么2MSL将重新计时。

3. MSL RFC793协议规定为2分钟，linux 实际设置为30秒

#### TIME_WAIT危害

- 内存资源占用

- 对端口资源的占用，一个TCP 连接至少消耗一个本地端口。一般可以开启的端口为 32768~61000，也可以通过net.ipv4.ip_local_port_range 指定，如果TIME_WAIT 状态过多，会导致无法创建新连接。

#### 优化TIME_WAIT

- 调低 TCP_TIMEWAIT_LEN,重新编译

- SO_LINGER 设置：设置调用close或者shutdown 关闭连接时的行为

- SO_LINGER

- net.ipv4.tcp_tw_reuse：客户端与服务端主机时间不同步，客户端发送的消息会直接被拒绝

##### setsockopt 设置SO_REUSEADDR ，解决端口复用，告诉内核即使TIME_WAIT状态的套接字，也可以继续使用它作为新的套集字使用


### 连接关闭

- close 

````
int close(int sockfd)
````
1. 这个函数会对套接字引用计数减一，一旦发现套接字引用计数到0，就会对套接字进行彻底释放，并且会关闭TCP两个方向的数据流

2. 在输入方向，系统内核会将该套接字设置为不可读，任何读操作都会返回异常

3. 在输出方向，系统内核尝试将发送缓冲区的数据发送给对端，并且最后向对端发送一个Fin报文，接下来如果再对套接字进行写操作会返回异常

4. 如果对端没有检测到套接字已经关闭，还继续发送报文，就会受到一个RST报文。

- shutdown

````
int shutdown(int sockfd,int howto)

````

- SHUT_RD(0):关闭连接的读方向，对该套接字进行读操作直接返回EOF.套接字上接收缓冲区已有数据将被丢弃，如果再有新的数据到达，会对数据进行ACK，然后丢弃。

- SHUT_WR(1):关闭连接的写方向，被称为"半关闭”的连接。此时，不管套接字引用数值是多少，都会直接关闭连接的写方向。套接字上放生缓冲区已有的数据将立即发送出去，并发送一个FIN 报文给对端

- SHUT_RDWR(2):相当于SHUT_RD和SHUT_WR 操作各一次，关闭套接字的读和写两个方向

#### 比较

- close 会关闭连接，并释放所有连接对应的资源，shutdown 并不会释放掉套接字和所有资源

- close 存在引用计数的概念，并不一定导致该套接字不可用；showdown则不管引用计数，直接使得该套接字不可用，如果有别的进程企图使用该套接字，将会受到影响

- close 的引用计数导致不一定会发出FIN报文，而showdown 则总是会发出FIN结束报文，这在我们打算关闭连接通知对端的时候，非常重要。


### Keep-Alive

定义一个时间段，在这个时间段内，如果没有任何连接相关的活动，TCP保活机制会开始作用，每隔一个时间间隔，发送一个探测报文，该探测报文包含的数据非常少，如果连续几个探测报文都没有得到响应，则认为当前的TCP链接
已经死亡，系统内核将错误信息通知给上层应用程序。

#### 配置参数

- net.ipv4.tcp_keepalive_time:默认7200秒，两小时内没有任何连接相关活动，则开启保活机制

- net.ipv4.tcp_keepalive_intvl：默认值75秒

- net.ipv4.tcp_keepalive_probes：默认为9


#### TCP保活机制默认是关闭的，可分别在连接的两个方向开启，也可单独在一个方向开启。

- 如果开启服务端到客户端检测，则可以在客户端非正常断连的情况下清除在服务器端保留的脏数据

- 如果开启客户端导服务端的检测，可以在服务端无响应的情况下，重新发起连接

