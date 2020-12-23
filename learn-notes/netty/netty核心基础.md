## 核心基础

### 启动服务

#### 1. 用户线程

1. 创建selector
2. 创建server socket channel
3. 初始化 server socket channel
4. 给server socket channel 从boss group 中选择一个NioEventLoop

#### 2. boss thread 线程
1. 将server socket channel 注册到选择的 NioEventLoop 的 selector 
2. 绑定地址启动
3. 注册接受连接事件(OP_ACCEPT)到selector上

##### Selector 是在new NioEventLoopGroup() 创建一批NioEventLoop时创建

- Selector selector = sun.nio.ch.SelectorProviderImpl.openSelector()
- ServerSocketChannel serverSocketChannel = provider.openServerSocketChannel();

##### 第一次register 并不是监听OP_ACCEPT 而是 0
- selectionKey = javaChannel().register(eventLoop().unwrappedSelector(),0,this);

##### 最终监听 OP_ACCEPT 是通过bind后的fireChannelActive()来触发的
- javaChannel().bind(lockAddress,config.getBacklog()); 
##### NioEventLoop 是通过register操作的执行来完成启动的
- selectionKey.interestOps(OP_ACCEPT);
##### 类似ChannelInitializer ，一些handler可以设计成一次行的，用完即移除。

-----
### 构建连接

#### 1. boss thread 线程
1. NioEventLoop 中的selector 轮询创建连接事件(OP_ACCEPT)

- selector.select()/selectNot()/select(timeoutMillis)发现OP_ACCEPT事件，处理
  - SocketChannel socketChannel = serverSocketChannel.accept()
  - selectionKey = javaChannel().register(eventLoop().unwrappedSelector(),0,this);
  - selectionKey.interestOps(OP_READ);
2. 创建 socket channel
3. 初始化 socket channel 并从 worker group 中选择一个 NioEventLoop

#### 2. worker thread 
1. 将socket channel 注册到选择的NioEventLoop 的 selector
2. 注册读事件（OP_READ）到selector上

#### 创建连接的初始化和注册是通过 pipeline.fireChannelRead 在 ServerBootstrapAcceptor中完成的

#### 第一次 Register 并不是监听OP_READ ,而是0：

````
selectionKey = javaChannel().register(eventLoop().unwrappedSelector(),0,this)
````

#### 最终监听OP_READ是通过 "register"完成的fireChannelActive 来触发的

#### workers NioEventLoop 是通过register操作来执行启动

#### 接受连接的读操作,不会尝试读取更多次(16次)

---------------

### 接受数据

- 自适应数据大小的分配器（AdaptiveRecvByteBufAllocator）

- 连续读（defaultMaxMessagesPerRead）

#### 多路复用器(selector)接收到OP_READ事件

#### 处理OP_READ事件：NioSocketChannel.NioSocketChannelUnsafe.read()

- 分配一个初始1024字节的byte buffer来接受数据

````
sun.nio.ch.SocketChannelImpl#read(java.nit.ByteBuffer)

NioSocketChannel read()是读数据，NioServerSocketChannel read()是创建连接

piple.fireChannelReadComplete()：一次读事件处理完成

piple.fireChannelRead(byteBuf):一次读数据完成，一次读事件处理可能会包含多次读数据操作
````
- 从Channel 接受数据到byte buffer

- 记录实际接受数据大小，调整下次分配byte buffer 大小

- 触发pipeline.fireChannelRead(byteBuf)把读取到的数据传播出去

- 判断接受 byte buffer 是否满载而归：是，尝试继续读取直到没有数据或满16次；否，结束本轮读取，等待下次OP_READ事件

````
AdaptiveRecvByteBufAllocator 对 byteBuf 的猜测：放大果断，缩小谨慎[需要连续两次判断]
````

-----------

### 业务处理

#### handler执行资格：

- 实现了ChannelInboundHandler接口

- 实现方法 channelRead 不能家@Skip注解

#### 处理业务本质：数据在pipeline中所有的handler的channelRead()执行过程

Handler要实现io.netty.channel.ChannelInboundHandler#channelRead(ChannelHandlerContext ctx,Object msg)，且不能加注释@Skip 才能被执行到，中途可退出，不保证执行到Tail Handler

#### 默认处理线程就是Channel 绑定的NioEventLoop线程，也可以设置其他

````
pipeline.addLast(new UnorderedThreadPoolEventExecutor(10),serverHandler)
````

### 发送数据

#### Write : 写数据到buffer

````
ChannelOutboundBuffer# addMessage
````
#### Flush:发送buffer里面的数据

````
AbstractChannel.AbstractUnsafe#flush
````
- 准备数据：ChannelOutboundBuffer##addFlush

- 发送：NioSocketChannel#doWrite