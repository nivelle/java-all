### socket

[![rILkaF.png](https://s3.ax1x.com/2020/12/28/rILkaF.png)](https://imgchr.com/i/rILkaF)

建立连接后，数据的传输就不再是单向的，而是双向的，这也是TCP的一个显著特性。

#### TCP三次握手

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