
## tomcat 网络 I/O 模型优化

### 阻塞式IO
 
**阻塞发生的环节**

- connect 阻塞：当客户端发起 TCP 连接请求，通过系统调用 connect 函数，TCP 连接的建立需要完成三次握手过程，客户端需要等待服务端发送回来的 ACK 以及 SYN 信号，同样服务端也需要阻塞等待客户端确认连接的 ACK 信号，这就意味着 TCP 的每个 connect 都会阻塞等待，直到确认连接。

- accept 阻塞：一个阻塞的 socket 通信的服务端接收外来连接，会调用 accept 函数，如果没有新的连接到达，调用进程将被挂起，进入阻塞状态

- read、write 阻塞：当一个 socket 连接创建成功之后，服务端用 fork 函数创建一个子进程， 调用 read 函数等待客户端的数据写入，如果没有数据写入，调用子进程将被挂起，进入阻塞状态。

### 非阻塞式IO

**使用 fcntl 可以把以上三种操作都设置为非阻塞操作。如果没有数据返回，就会直接返回一个 EWOULDBLOCK 或 EAGAIN 错误，此时进程就不会一直被阻塞。**

当我们把以上操作设置为了非阻塞状态，我们需要设置一个线程对该操作进行轮询检查

### I/O 复用

- select() 函数：它的用途是，在超时时间内，监听用户感兴趣的文件描述符上的可读可写和异常事件的发生。Linux 操作系统的内核将所有外部设备都看做一个文件来操作，对一个文件的读写操作会调用内核提供的系统命令，返回一个文件描述符（fd）

**调用后 select() 函数会阻塞，直到有描述符就绪或者超时，函数返回。**

![select函数调用.png](https://i.loli.net/2020/04/02/BkHql2Z8Wp9UReu.png)

- poll() 函数: 在每次调用 select() 函数之前，系统需要把一个 fd 从用户态拷贝到内核态，这样就给系统带来了一定的性能开销。再有单个进程监视的 fd 数量默认是 1024，我们可以通过修改宏定义甚至重新编译内核的方式打破这一限制。但由于 fd_set 是基于数组实现的，在新增和删除 fd 时，数量过大会导致效率降低。

  1. poll() 和 select()：二者在本质上差别不大。poll() 管理多个描述符也是通过轮询，根据描述符的状态进行处理，但 poll() 没有最大文件描述符数量的限制。 

  2. 存在一个相同的缺点，那就是包含大量文件描述符的数组被整体复制到用户态和内核的地址空间之间，而无论这些文件描述符是否就绪，他们的开销都会随着文件描述符数量的增加而线性增大。
  
- epoll(): Linux 在 2.6 内核版本中提供了一个 epoll 调用，epoll 使用事件驱动的方式代替轮询扫描 fd。
  
  epoll 事先通过 epoll_ctl() 来注册一个文件描述符，将文件描述符存放到内核的一个事件表中，这个事件表是基于红黑树实现的，所以在大量 I/O 请求的场景下，插入和删除的性能比 select/poll 的数组 fd_set 要好，因此 epoll 的性能更胜一筹，而且不会受到 fd 数量的限制。

  ```
  epoll_ctl() 函数中的 epfd 是由 epoll_create() 函数生成的一个 epoll 专用文件描述符。op 代表操作事件类型，fd 表示关联文件描述符，event 表示指定监听的事件类型。一旦某个文件描述符就绪时，内核会采用类似 callback 的回调机制，迅速激活这个文件描述符，当进程调用 epoll_wait() 时便得到通知，之后进程将完成相关 I/O 操作。
  
  ```
  
## tomcat 零拷贝

- Linux 内核中的 mmap 函数可以代替 read、write 的 I/O 读写操作，实现用户空间和内核空间共享一个缓存数据。mmap 将用户空间的一块地址和内核空间的一块地址同时映射到相同的一块物理内存地址，不管是用户空间还是内核空间都是虚拟地址，最终要通过地址映射映射到物理内存地址。这种方式避免了内核空间与用户空间的数据交换。I/O 复用中的 epoll 函数中就是使用了 mmap 减少了内存拷贝。

- 在 Java 的 NIO 编程中，则是使用到了 Direct Buffer 来实现内存的零拷贝。Java 直接在 JVM 内存空间之外开辟了一个物理内存空间，这样内核和用户进程都能共享一份缓存数据

## tomcat 线程模型优化

Reactor 模型是同步 I/O 事件处理的一种常见模型，其核心思想是将 I/O 事件注册到多路复用器上，一旦有 I/O 事件触发，多路复用器就会将事件分发到事件处理器中，执行就绪的 I/O 事件操作。该模型有以下三个主要组件:

- 事件接收器 Acceptor：主要负责接收请求连接；

- 事件分离器 Reactor：接收请求后，会将建立的连接注册到分离器中，依赖于循环监听多路复用器 Selector，一旦监听到事件，就会将事件 dispatch 到事件处理器；

- 事件处理器 Handlers：事件处理器主要是完成相关的事件处理，比如读写 I/O 操作。


```

- acceptorThreadCount：该参数代表 Acceptor 的线程数量，在请求客户端的数据量非常巨大的情况下，可以适当地调大该线程数量来提高处理请求连接的能力，默认值为 1。

- maxThreads：专门处理 I/O 操作的 Worker 线程数量，默认是 200，可以根据实际的环境来调整该参数，但不一定越大越好。

- acceptCount：Tomcat 的 Acceptor 线程是负责从 accept 队列中取出该 connection，然后交给工作线程去执行相关操作，这里的 acceptCount 指的是 accept 队列的大小。当 Http 关闭 keep alive，在并发量比较大时，可以适当地调大这个值。而在 Http 开启 keep alive 时，因为 Worker 线程数量有限，Worker 线程就可能因长时间被占用，而连接在 accept 队列中等待超时。如果 accept 队列过大，就容易浪费连接。

- maxConnections：表示有多少个 socket 连接到 Tomcat 上。在 BIO 模式中，一个线程只能处理一个连接，一般 maxConnections 与 maxThreads 的值大小相同；在 NIO 模式中，一个线程同时处理多个连接，maxConnections 应该设置得比 maxThreads 要大的多，默认是 10000。

```

## tomcat结构

### server

1. <Server>元素 它代表整个容器,是Tomcat实例的顶层元素.由org.apache.catalina.Server接口来定义.

2. 一个“Server”是一个提供完整的JVM的独立组件，它可以包含一个或多个 “Service”实例。服务器在指定的端口上监听shutdown命令

3. 启动Server  在端口8005处等待关闭命令  如果接受到”SHUTDOWN”字符串则关闭服务器

```

telnet localhost 8005  输入：SHUTDOWN 结果：关闭tomcat

```
### server -> service

1. 该元素由org.apache.catalina.Service接口定义,它包含一个<Engine>元素,以及一个或多个<Connector>,这些Connector元素共享用同一个Engine元素

2. 一个“Service”是一个或多个共用一个单独“Container”(容器)的“Connectors” 组合（因此，应用程序在容器中可见）。通常，这个容器是一个“Engine” （引擎），但这不是必须的


### service -> connector

1. <Connector>元素 由Connector接口定义.<Connector>元素代表与客户程序实际交互的给件,它负责接收客户请求,以及向客户返回响应结果.

### service -> engine

1. <Engine>元素 每个Service元素只能有一个Engine元素.处理在同一个<Service>中所有<Connector>元素接收到的客户请求.由org.apahce.catalina.Engine接口定义.

2. Engine用来处理Connector收到的Http请求  它将匹配请求和自己的虚拟主机，并把请求转交给对应的Host来处理  默认虚拟主机是localhost

### engine -> host

1. 它由Host接口定义.一个Engine元素可以包含多个<Host>元素.每个<Host>的元素定义了一个虚拟主机.它包含了一个或多个Web应用.

2. appBase : 指 定虚拟主机的目录,可以指定绝对目录,也可以指定相对于<CATALINA_HOME>的相对目录.如果没有此项,默认 为<CATALINA_HOME>/webapps. 它将匹配请求和自己的Context的路径,并把请求转交给对应的Context来处理

3. autoDeploy:如果此项设为true,表示Tomcat服务处于运行状态时,能够监测appBase下的文件,如果有新有web应用加入进来,会自运发布这个WEB应用

4. unpackWARs:如果此项设置为true,表示把WEB应用的WAR文件先展开为开放目录结构后再运行.如果设为false将直接运行为WAR文件

5. deployOnStartup:如果此项设为true,表示Tomcat服务器启动时会自动发布appBase目录下所有的Web应用.如果Web应用 中的server.xml没有相应的<Context>元素,将采用Tomcat默认的Context


### host-> context

1. < Context>元素 它由Context接口定义.是使用最频繁的元素.每个<Context元素代表了运行在虚拟主机上的单个Web应用

2. path: 该Context的路径名是""

3. docBase : 该Context的根目录是webapps/docBaseName/

4. reloadable:如果这个属性设为true,  Tomcat服务器在运行状态下会监视在WEB-INF/classes和Web-INF/lib目录CLASS文件的改运.如果监视到有class文件 被更新,服务器自重新加载Web应用

5. cookies: 指定是否通过Cookies来支持Session,默认值为true

### host-> value

### host-> logger

### host-> realm

## tomcat处理请求过程

1. 请求被发送到本机端口8080,被在那里侦听的Coyote HTTP/1.1 Connector获得

2. Connector把该请求交给它所在的Service的Engine来处理，并等待来自Engine的回应

3. Engine获得请求localhost/wsota/wsota_index.jsp，匹配它所拥有的所有虚拟主机Host

4. Engine匹配到名为localhost的Host（即使匹配不到也把请求交给该Host处理，因为该Host被定义为该Engine的默认主机）

5. localhost Host获得请求/wsota/wsota_index.jsp，匹配它所拥有的所有Context

6. Host匹配到路径为/wsota的Context（如果匹配不到就把该请求交给路径名为”"的Context去处理）

7. path=”/wsota”的Context获得请求/wsota_index.jsp，在它的mapping table中寻找对应的servlet

8. Context匹配到URL PATTERN为*.jsp的servlet，对应于JspServlet类

9. 构造HttpServletRequest对象和HttpServletResponse对象，作为参数调用JspServlet的doGet或doPost方法

10. Context把执行完了之后的HttpServletResponse对象返回给Host 

11. Host把HttpServletResponse对象返回给Engine 

12. Engine把HttpServletResponse对象返回给Connector

13. Connector把HttpServletResponse对象返回给客户browser


![Tomcat结构图](https://s1.ax1x.com/2020/07/18/Ug0DpV.jpg)
