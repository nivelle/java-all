### channel

- 是socket的抽象，为用户提供了关于socket状态（是连接还是断开）以及对socket的读写操作。

- 每当netty建立一个连接、都创建一个与其对应的channel 实例

### ServerBootstrap

````
 public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup) {
        super.group(parentGroup);
        ObjectUtil.checkNotNull(childGroup, "childGroup");
        if (this.childGroup != null) {
            throw new IllegalStateException("childGroup set already");
        }
        this.childGroup = childGroup;
        return this;
    }

````

#### NioServerSocketChannel的创建

````
serverBootstrap.channel(NioServerSocketChannel.class);

````

#### 服务端Channel的初始化

[![gAtKIJ.png](https://z3.ax1x.com/2021/04/30/gAtKIJ.png)](https://imgtu.com/i/gAtKIJ)

- 调用NioServerSocketChannel.newSocket(DEFAULT_SELECTOR_PROVIDER)方法创建一个新的java NIO 原生ServerSocketChannel对象

- 实例话AbstractChannel对象并给属性赋值

#### boosGroup 和 workerGroup

调用bind()方法来监听一个本地端口。bind方法触发的调用链如下**AbstractBootstrap.bind()->AbstractBootstrap().doBind()->
AbstractBootstrap.initAndRegister()**;

- boosGroup 的初始化

````
   final ChannelFuture initAndRegister() {
        Channel channel = null;
        try {
            channel = channelFactory.newChannel();
            init(channel);
        } catch (Throwable t) {
            if (channel != null) {
                // channel can be null if newChannel crashed (eg SocketException("too many open files"))
                channel.unsafe().closeForcibly();
                // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
                return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
            }
            // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
            return new DefaultChannelPromise(new FailedChannel(), GlobalEventExecutor.INSTANCE).setFailure(t);
        }
        //boosGroup 和 NioServerSocketChannel 关联起来
        ChannelFuture regFuture = config().group().register(channel);
        if (regFuture.cause() != null) {
            if (channel.isRegistered()) {
                channel.close();
            } else {
                channel.unsafe().closeForcibly();
            }
        }

        // If we are here and the promise is not failed, it's one of the following cases:
        // 1) If we attempted registration from the event loop, the registration has been completed at this point.
        //    i.e. It's safe to attempt bind() or connect() now because the channel has been registered.
        // 2) If we attempted registration from the other thread, the registration request has been successfully
        //    added to the event loop's task queue for later execution.
        //    i.e. It's safe to attempt bind() or connect() now:
        //         because bind() or connect() will be executed *after* the scheduled registration task is executed
        //         because register(), bind(), and connect() are all bound to the same thread.

        return regFuture;
    }

````

- workerGroup 的初始化

````
void init(Channel channel) {
        setChannelOptions(channel, options0().entrySet().toArray(newOptionArray(0)), logger);
        setAttributes(channel, attrs0().entrySet().toArray(newAttrArray(0)));

        ChannelPipeline p = channel.pipeline();

        final EventLoopGroup currentChildGroup = childGroup;
        final ChannelHandler currentChildHandler = childHandler;
        final Entry<ChannelOption<?>, Object>[] currentChildOptions =
                childOptions.entrySet().toArray(newOptionArray(0));
        final Entry<AttributeKey<?>, Object>[] currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(0));
        // 将 workerGroup中的某个EventLoop 和 NioSocketChannel关联 
        p.addLast(new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(final Channel ch) {
                final ChannelPipeline pipeline = ch.pipeline();
                ChannelHandler handler = config.handler();
                if (handler != null) {
                    pipeline.addLast(handler);
                }

                ch.eventLoop().execute(new Runnable() {
                    @Override
                    public void run() {
                        pipeline.addLast(new ServerBootstrapAcceptor(
                                ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                    }
                });
            }
        });
    }

````

#### 服务端Selector事件轮训

````
  private static void doBind0(
            final ChannelFuture regFuture, final Channel channel,
            final SocketAddress localAddress, final ChannelPromise promise) {

        // This method is invoked before channelRegistered() is triggered.  Give user handlers a chance to set up
        // the pipeline in its channelRegistered() implementation.
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                if (regFuture.isSuccess()) {
                    channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    promise.setFailure(regFuture.cause());
                }
            }
        });
    }

````

- Select 事件轮询是从EventLoop的execute()方法开始的

- 在EventLoop的execute()方法中，会为每个任务都创建一个独立的线程，并保存到无锁话串行任务队列

- 线程任务队列的每个任务实际调用的是NioEventLoop的run()方法

- 在run()方法中调用processSelectedKeys()处理轮询事件
