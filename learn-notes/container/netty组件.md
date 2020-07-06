
### Bootstrap、ServerBootstrap

```
一个Netty应用通常由一个Bootstrap开始,主要作用是配置整个Netty程序,串联各个组件,Netty中Bootstrap类是客户端程序的启动引导类,ServerBootstrap是服务端启动引导类。

```

### Future、ChannelFuture

```
在Netty中所有的IO操作都是异步的,不能立刻得知消息是否被正确处理,但是可以过一会等它执行完成或者直接注册一个监听,

具体的实现就是通过Future和ChannelFutures,他们可以注册一个监听,当操作执行成功或失败时监听会自动触发注册的监听事件

```

### Channel

#### Netty网络通信的组件,能够用于执行网络I/O操作。 Channel为用户提供:

- 当前网络连接的通道的状态

- 网络连接的配置参数

- 提供异步的网络I/O操作(如建立连接，读写，绑定端口)，异步调用意味着任何I / O调用都将立即返回，并且不保证在调用结束时所请求的I / O操作已完成。调用立即返回一个ChannelFuture实例,通过注册监听器到ChannelFuture上,可以I / O操作成功、失败或取消时回调通知调用方。

- 支持关联I/O操作与对应的处理程序

** 不同协议、不同的阻塞类型的连接都有不同的 Channel 类型与之对应，下面是一些常用的 Channel 类型 **

- NioSocketChannel，异步的客户端 TCP Socket 连接

- NioServerSocketChannel，异步的服务器端 TCP Socket 连接

- NioDatagramChannel，异步的 UDP 连接

- NioSctpChannel，异步的客户端 Sctp 连接

- NioSctpServerChannel，异步的 Sctp 服务器端连接 这些通道涵盖了 UDP 和 TCP网络 IO以及文件 IO

### Selector

Netty基于Selector对象实现I/O多路复用,通过 Selector, 一个线程可以监听多个连接的Channel事件, 当向一个Selector中注册Channel 后,

Selector 内部的机制就可以自动不断地查询(select) 这些注册的Channel是否有已就绪的I/O事件(例如可读, 可写, 网络连接完成等)，这样程序就可以很简单地使用一个线程高效地管理多个 Channel.

### NioEventLoop

NioEventLoop中维护了一个线程和任务队列,支持异步提交执行任务,线程启动时会调用NioEventLoop的run方法,执行I/O任务和非I/O任务:

- I/O任务 即selectionKey中ready的事件，如accept、connect、read、write等，由processSelectedKeys方法触发。

- 非IO任务 添加到taskQueue中的任务，如register0、bind0等任务，由runAllTasks方法触发


### NioEventLoopGroup

NioEventLoopGroup，主要管理eventLoop的生命周期，可以理解为一个线程池，内部维护了一组线程，每个线程(NioEventLoop)负责处理多个Channel上的事件，而一个Channel只对应于一个线程。

### ChannelHandler

ChannelHandler是一个接口，处理I / O事件或拦截I / O操作，并将其转发到其ChannelPipeline(业务处理链)中的下一个处理程序。

ChannelHandler本身并没有提供很多方法,因为这个接口有许多的方法需要实现,方便使用期间，可以继承它的子类:

- ChannelInboundHandler用于处理入站I / O事件

- ChannelOutboundHandler用于处理出站I / O操作

或者使用以下适配器类：

- ChannelInboundHandlerAdapter用于处理入站I / O事件

- ChannelOutboundHandlerAdapter用于处理出站I / O操作

- ChannelDuplexHandler用于处理入站和出站事件

### ChannelHandlerContext

保存Channel相关的所有上下文信息，同时关联一个ChannelHandler对象

### ChannelPipline

保存ChannelHandler的List，用于处理或拦截Channel的入站事件和出站操作。 ChannelPipeline实现了一种高级形式的拦截过滤器模式，使用户可以完全控制事件的处理方式，以及Channel中各个的ChannelHandler如何相互交互。







