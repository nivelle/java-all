## 核心基础

### 启动服务(创建 OP_ACCEPT事件监听)

#### 1. 用户线程

1. 创建selector
2. 创建server socket channel
3. 初始化 server socket channel
4. 给server socket channel 从boss group 中选择一个NioEventLoop

#### 2. boss thread 线程
1. 将server socket channel 注册到选择的 NioEventLoop 的 selector 
2. 绑定地址启动
3. 注册接受连接事件(OP_ACCEPT)到selector上

#### 核心源码

#### 绑定

- 绑定

````
 private ChannelFuture doBind(final SocketAddress localAddress) {
        //1.创建一个ServerSocketChannel
        //2.初始化
        //3.register: 把我们的ServerSocketChannel 注册register 到 NioEventLoop 的 selector 上
        final ChannelFuture regFuture = initAndRegister();
        final Channel channel = regFuture.channel();
        if (regFuture.cause() != null) {
            return regFuture;
        }

        if (regFuture.isDone()) {
            // At this point we know that the registration was complete and successful.
            ChannelPromise promise = channel.newPromise();
            //
            doBind0(regFuture, channel, localAddress, promise);
            return promise;
        } else {
            // Registration future is almost always fulfilled already, but just in case it's not.
            final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
            regFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    Throwable cause = future.cause();
                    if (cause != null) {
                        // Registration on the EventLoop failed so fail the ChannelPromise directly to not cause an
                        // IllegalStateException once we try to access the EventLoop of the Channel.
                        promise.setFailure(cause);
                    } else {
                        // Registration was successful, so set the correct executor to use.
                        // See https://github.com/netty/netty/issues/2586
                        promise.registered();

                        doBind0(regFuture, channel, localAddress, promise);
                    }
                }
            });
            return promise;
        }
    }

````

#### initAndRegister 初始化和注册操作

- 绑定->初始化和注册

````
final ChannelFuture initAndRegister() {
        Channel channel = null;
        try {
            //创建 NioServerSocketChannel
            channel = channelFactory.newChannel();
            //初始化NioServerSicjetChannel
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
        //注册逻辑：
        // AbstractChannel register(EventLoop eventLoop, final ChannelPromise promise)
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

- 初始化和注册操作->初始化逻辑

````
void init(Channel channel) {
        setChannelOptions(channel, options0().entrySet().toArray(newOptionArray(0)), logger);
        setAttributes(channel, attrs0().entrySet().toArray(newAttrArray(0)));
        //获取管道
        ChannelPipeline p = channel.pipeline();

        final EventLoopGroup currentChildGroup = childGroup;
        final ChannelHandler currentChildHandler = childHandler;
        final Entry<ChannelOption<?>, Object>[] currentChildOptions = childOptions.entrySet().toArray(newOptionArray(0));
        final Entry<AttributeKey<?>, Object>[] currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(0));
        // ChannelInitializer 一次性、初始化 handler：
        // 负责添加一个serverBootstrapAcceptor handler ,添加后，自行移除
        // ServerBootstrapAcceptor handler 负责接收客户端连接创建后，对连接的初始化工作
        p.addLast(new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(final Channel ch) {
                final ChannelPipeline pipeline = ch.pipeline();
                ChannelHandler handler = config.handler();
                if (handler != null) {
                    pipeline.addLast(handler);
                }
                //创建 ServerBootstrapAcceptor 作为一个Task添加进 eventLoop
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


- 初始化和注册操作->注册详情1

```
 public final void register(EventLoop eventLoop, final ChannelPromise promise) {
            if (eventLoop == null) {
                throw new NullPointerException("eventLoop");
            }
            if (isRegistered()) {
                promise.setFailure(new IllegalStateException("registered to an event loop already"));
                return;
            }
            if (!isCompatible(eventLoop)) {
                promise.setFailure(new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
                return;
            }
            AbstractChannel.this.eventLoop = eventLoop;
            //判断自己的线程是不是NioEventLoop里面的线程
            if (eventLoop.inEventLoop()) {
                register0(promise);
            } else {
                try {
                    //将注册逻辑作为一个task，让eventLoop去执行
                    eventLoop.execute(new Runnable() {
                        @Override
                        public void run() {
                            register0(promise);
                        }
                    });
                } catch (Throwable t) {
                    logger.warn(
                            "Force-closing a channel whose registration task was not accepted by an event loop: {}",
                            AbstractChannel.this, t);
                    closeForcibly();
                    closeFuture.setClosed();
                    safeSetFailure(promise, t);
                }
            }
        }

```

-  初始化和注册操作->注册详情2：eventLoop.execute

``````
private void execute(Runnable task, boolean immediate) {
        boolean inEventLoop = this.inEventLoop();
        // 把 register 这个task 放到NioEventLoop里面的queue里面
        this.addTask(task);
        if (!inEventLoop) {
            //未启动线程的话启动线程，然后去执行task任务
            this.startThread();
            if (this.isShutdown()) {
                boolean reject = false;

                try {
                    if (this.removeTask(task)) {
                        reject = true;
                    }
                } catch (UnsupportedOperationException var6) {
                }

                if (reject) {
                    reject();
                }
            }
        }

        if (!this.addTaskWakesUp && immediate) {
            this.wakeup(inEventLoop);
        }

    }

``````
- 初始化和注册操作->注册详情3

````
 protected void doRegister() throws Exception {
        boolean selected = false;
        for (;;) {
            try {
                //jdk 实现：将当前的channel 注册到 NioEventLoop的selector上
                //register(Selector sel, int ops, Object att)
                // ops= 0:感兴趣的事件是0，并不是OP_ACCEPT 为0，这时候还没bind,没有active，真正注册OP_ACCEPT事件是在FireChannelActive()来触发的
                // att= this: attachment
                selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
                return;
            } catch (CancelledKeyException e) {
                if (!selected) {
                    // Force the Selector to select now as the "canceled" SelectionKey may still be
                    // cached and not removed because no Select.select(..) operation was called yet.
                    eventLoop().selectNow();
                    selected = true;
                } else {
                    // We forced a select operation on the selector before but the SelectionKey is still cached
                    // for whatever reason. JDK bug ?
                    throw e;
                }
            }
        }
    }

````

#### 绑定详情

- 绑定详情1：AbstractBootstrap -> doBind0
````
 private static void doBind0(final ChannelFuture regFuture, final Channel channel,final SocketAddress localAddress, final ChannelPromise promise) {

        // This method is invoked before channelRegistered() is triggered.  Give user handlers a chance to set up
        // the pipeline in its channelRegistered() implementation.
        // 
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

- 绑定详情2：DefaultChannelPipeline -> bind

````

public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
            assertEventLoop();

            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            // See: https://github.com/netty/netty/issues/576
            if (Boolean.TRUE.equals(config().getOption(ChannelOption.SO_BROADCAST)) &&
                localAddress instanceof InetSocketAddress &&
                !((InetSocketAddress) localAddress).getAddress().isAnyLocalAddress() &&
                !PlatformDependent.isWindows() && !PlatformDependent.maybeSuperUser()) {
                // Warn a user about the fact that a non-root user can't receive a
                // broadcast packet on *nix if the socket is bound on non-wildcard address.
                logger.warn(
                        "A non-root user can't receive a broadcast packet if the socket " +
                        "is not bound to a wildcard address; binding to a non-wildcard " +
                        "address (" + localAddress + ") anyway as requested.");
            }

            boolean wasActive = isActive();
            try {
                //实际执行绑定
                doBind(localAddress);
            } catch (Throwable t) {
                safeSetFailure(promise, t);
                closeIfClosed();
                return;
            }
            // 也就是第一次的绑定操作,channel转换为active
            if (!wasActive && isActive()) {
                invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pipeline.fireChannelActive();
                    }
                });
            }

            safeSetSuccess(promise);
        }

````

- 绑定详情3： NioServerSocketChannel

````
 protected void doBind(SocketAddress localAddress) throws Exception {
        if (PlatformDependent.javaVersion() >= 7) {
            javaChannel().bind(localAddress, config.getBacklog());
        } else {
            javaChannel().socket().bind(localAddress, config.getBacklog());
        }
    }

````
- 绑定详情4：channelActive(ChannelHandlerContext ctx)
````
 public void channelActive(ChannelHandlerContext ctx) {
            //继续传播
            ctx.fireChannelActive();
            //注册读事件：读包括创建连接和读数据
            readIfIsAutoRead();
        }

// 实际上就是注册 OP_ACCEPT/OP_READ事件：创建连接或者读事件
public void read(ChannelHandlerContext ctx) {
            unsafe.beginRead();
        }
//开始读
protected void doBeginRead() throws Exception {
        // Channel.read() or ChannelHandlerContext.read() was called
        final SelectionKey selectionKey = this.selectionKey;
        if (!selectionKey.isValid()) {
            return;
        }

        readPending = true;
        //假设之前没有监听readInterstOp ,则监听readInterstOp
        final int interestOps = selectionKey.interestOps();
        if ((interestOps & readInterestOp) == 0) {
            //真正注册监听连接事件，也就是 interstOps = 16
            // 创建连接的时候，注册读事件监听，也就是 interstOps = 1
            selectionKey.interestOps(interestOps | readInterestOp);
        }
    }
````
-----

### 构建连接(也就是对OP_ACCEPT事件的处理，然后在workerEventLoop创建OP_READ事件监听)

#### 1. boss thread 线程

1. NioEventLoop 中的selector 轮询创建连接事件(OP_ACCEPT)
2. 创建 socket channel
3. 初始化 socket channel 并从 worker group 中选择一个 NioEventLoop

#### 2. worker thread 线程
1. 将socket channel 注册到选择的NioEventLoop 的 selector
2. 注册读事件（OP_READ）到selector上

#### 核心源码

- NioEventLoop 核心事件处理类

````
 protected void run() {
        for (;;) {
            try {
                try {
                    switch (selectStrategy.calculateStrategy(selectNowSupplier, hasTasks())) {
                    case SelectStrategy.CONTINUE:
                        continue;

                    case SelectStrategy.BUSY_WAIT:
                        // fall-through to SELECT since the busy-wait is not supported with NIO

                    case SelectStrategy.SELECT:
                        select(wakenUp.getAndSet(false));
                        if (wakenUp.get()) {
                            selector.wakeup();
                        }
                        // fall through
                    default:
                    }
                } catch (IOException e) {
                    // If we receive an IOException here its because the Selector is messed up. Let's rebuild
                    // the selector and retry. https://github.com/netty/netty/issues/8566
                    rebuildSelector0();
                    handleLoopException(e);
                    continue;
                }

                cancelledKeys = 0;
                needsToSelectAgain = false;
                final int ioRatio = this.ioRatio;
                if (ioRatio == 100) {
                    try {
                        //处理OP_ACCEPT事件
                        processSelectedKeys();
                    } finally {
                        // Ensure we always run tasks.
                        runAllTasks();
                    }
                } else {
                    final long ioStartTime = System.nanoTime();
                    try {
                        processSelectedKeys();
                    } finally {
                        // Ensure we always run tasks.
                        final long ioTime = System.nanoTime() - ioStartTime;
                        runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                    }
                }
            } catch (Throwable t) {
                handleLoopException(t);
            }
            // Always handle shutdown even if the loop processing threw an exception.
            try {
                if (isShuttingDown()) {
                    closeAll();
                    if (confirmShutdown()) {
                        return;
                    }
                }
            } catch (Throwable t) {
                handleLoopException(t);
            }
        }
    }

````

- 处理创建连接事件1：

````
private void processSelectedKeys() {
        if (selectedKeys != null) {
            //不用JDK的selector.selectedKeys(),性能更好(1%~2%),垃圾回收更少
            processSelectedKeysOptimized();
        } else {
            processSelectedKeysPlain(selector.selectedKeys());
        }
    }
````

- 处理创建连接事件2：

````
private void processSelectedKeysOptimized() {
        for (int i = 0; i < selectedKeys.size; ++i) {
            final SelectionKey k = selectedKeys.keys[i];
            // null out entry in the array to allow to have it GC'ed once the Channel close
            // See https://github.com/netty/netty/issues/2363
            selectedKeys.keys[i] = null;
            //呼应channel的register中的this，也就是NioServerSocketChannel
            final Object a = k.attachment();
            //开始处理selectionKey
            if (a instanceof AbstractNioChannel) {
                processSelectedKey(k, (AbstractNioChannel) a);
            } else {
                @SuppressWarnings("unchecked")
                NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
                processSelectedKey(k, task);
            }

            if (needsToSelectAgain) {
                // null out entries in the array to allow to have it GC'ed once the Channel close
                // See https://github.com/netty/netty/issues/2363
                selectedKeys.reset(i + 1);

                selectAgain();
                i = -1;
            }
        }
    }

````

- 处理创建连接事件3：

````
private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
        final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
        if (!k.isValid()) {
            final EventLoop eventLoop;
            try {
                eventLoop = ch.eventLoop();
            } catch (Throwable ignored) {
                // If the channel implementation throws an exception because there is no event loop, we ignore this
                // because we are only trying to determine if ch is registered to this event loop and thus has authority
                // to close ch.
                return;
            }
            // Only close ch if ch is still registered to this EventLoop. ch could have deregistered from the event loop
            // and thus the SelectionKey could be cancelled as part of the deregistration process, but the channel is
            // still healthy and should not be closed.
            // See https://github.com/netty/netty/issues/5125
            if (eventLoop != this || eventLoop == null) {
                return;
            }
            // close the channel if the key is not valid anymore
            unsafe.close(unsafe.voidPromise());
            return;
        }

        try {
            //获取事件：OP_ACCEPT =16 也就是连接事件
            int readyOps = k.readyOps();
            // We first need to call finishConnect() before try to trigger a read(...) or write(...) as otherwise
            // the NIO JDK channel implementation may throw a NotYetConnectedException.
            if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
                // remove OP_CONNECT as otherwise Selector.select(..) will always return without blocking
                // See https://github.com/netty/netty/issues/924
                int ops = k.interestOps();
                ops &= ~SelectionKey.OP_CONNECT;
                k.interestOps(ops);

                unsafe.finishConnect();
            }

            // Process OP_WRITE first as we may be able to write some queued buffers and so free memory.
            //处理写事件
            if ((readyOps & SelectionKey.OP_WRITE) != 0) {
                // Call forceFlush which will also take care of clear the OP_WRITE once there is nothing left to write
                ch.unsafe().forceFlush();
            }

            // Also check for readOps of 0 to workaround possible JDK bug which may otherwise lead
            // to a spin loop
            //处理读请求或者接入连接
            if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
                unsafe.read();
            }
        } catch (CancelledKeyException ignored) {
            unsafe.close(unsafe.voidPromise());
        }
    }

````
- 处理创建连接事件4：AbstractNioMessageChannel->read()

````
 //初始化操作
 public void read() {
             assert eventLoop().inEventLoop();
             final ChannelConfig config = config();
             final ChannelPipeline pipeline = pipeline();
             final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
             allocHandle.reset(config);
 
             boolean closed = false;
             Throwable exception = null;
             try {
                 try {
                     do {
                         //真正处理连接事件
                         //将创建的SocketChannel个数返回
                         int localRead = doReadMessages(readBuf);
                         if (localRead == 0) {
                             break;
                         }
                         if (localRead < 0) {
                             closed = true;
                             break;
                         }
                         //记录创建次数
                         allocHandle.incMessagesRead(localRead);
                     } while (allocHandle.continueReading());
                 } catch (Throwable t) {
                     exception = t;
                 }
 
                 int size = readBuf.size();
                 for (int i = 0; i < size; i ++) {
                     readPending = false;
                    // 把创建连接的结果通过 fireChannelRead给传播出去了，传播的过程就是pipleline当中handler的执行
                    // 也就是：ServerBootstrapAcceptor 的执行，负责初始化 SocketChannel
                     pipeline.fireChannelRead(readBuf.get(i));
                 }
                 readBuf.clear();
                 allocHandle.readComplete();
                 pipeline.fireChannelReadComplete();
 
                 if (exception != null) {
                     closed = closeOnReadError(exception);
 
                     pipeline.fireExceptionCaught(exception);
                 }
 
                 if (closed) {
                     inputShutdown = true;
                     if (isOpen()) {
                         close(voidPromise());
                     }
                 }
             } finally {
                 // Check if there is a readPending which was not processed yet.
                 // This could be for two reasons:
                 // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
                 // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
                 //
                 // See https://github.com/netty/netty/issues/2254
                 if (!readPending && !config.isAutoRead()) {
                     removeReadOp();
                 }
             }
         }
     }

//判断是否继续读，循环退出条件->DefaultMaxMessagesRecvByteBufAllocator#allocHandle.continueReading()
 public boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier) {
            //  respectMaybeMoreData=false:表明不慎重对待可能的更多数据，只要有数据，就一直读16次，读不到可能浪费一次系统call
            // maybeMoreDataSupplier=true :默认选项，表明慎重，会判断有更多数据的可能性(maybeMoreDataSupplier),但是这个判断不是所有情况都准，所以才加了respectMaybeMoreData
            return config.isAutoRead() &&
                   (!respectMaybeMoreData || maybeMoreDataSupplier.get()) &&
                   totalMessages < maxMessagePerRead &&
                   totalBytesRead > 0;
        }

````



- 处理创建连接事件5 -> doReadMessages

````
protected int doReadMessages(List<Object> buf) throws Exception {
        //接受新连接创建SocketChannel
        SocketChannel ch = SocketUtils.accept(javaChannel());

        try {
            if (ch != null) {
                buf.add(new NioSocketChannel(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            logger.warn("Failed to create a new channel from an accepted socket.", t);

            try {
                ch.close();
            } catch (Throwable t2) {
                logger.warn("Failed to close a socket.", t2);
            }
        }

        return 0;
    }

````
- 处理创建连接事件6： accept(final ServerSocketChannel serverSocketChannel)

````
public static SocketChannel accept(final ServerSocketChannel serverSocketChannel) throws IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<SocketChannel>() {
                @Override
                public SocketChannel run() throws IOException {
                    //非阻塞模式下，没有连接请求时，返回null;接受创建连接请求，创建 socketChannel 
                    return serverSocketChannel.accept();
                }
            });
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getCause();
        }
    }

````

- 处理创建连接事件7： ServerBootstrapAcceptor->channelRead ;//AbstractNioMessageChannel->read() -> pipeline.fireChannelRead()

````
 public void channelRead(ChannelHandlerContext ctx, Object msg) {
            final Channel child = (Channel) msg;

            child.pipeline().addLast(childHandler);

            setChannelOptions(child, childOptions, logger);
            setAttributes(child, childAttrs);

            try {
                //worker-> NioEventLoop 中注册连接处理handler
                childGroup.register(child).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            forceClose(child, future.cause());
                        }
                    }
                });
            } catch (Throwable t) {
                forceClose(child, t);
            }
        }
````

- 处理创建连接事件8：

````
 public final void register(EventLoop eventLoop, final ChannelPromise promise) {
            if (eventLoop == null) {
                throw new NullPointerException("eventLoop");
            }
            if (isRegistered()) {
                promise.setFailure(new IllegalStateException("registered to an event loop already"));
                return;
            }
            if (!isCompatible(eventLoop)) {
                promise.setFailure(
                        new IllegalStateException("incompatible event loop type: " + eventLoop.getClass().getName()));
                return;
            }

            AbstractChannel.this.eventLoop = eventLoop;
            //当前线程是否是选定的eventLoop线程，当前线程是boos线程
            if (eventLoop.inEventLoop()) {
                register0(promise);
            } else {
                try {
                   //创建并注册OP_READ事件
                    eventLoop.execute(new Runnable() {
                        @Override
                        public void run() {
                            register0(promise);
                        }
                    });
                } catch (Throwable t) {
                    logger.warn(
                            "Force-closing a channel whose registration task was not accepted by an event loop: {}",
                            AbstractChannel.this, t);
                    closeForcibly();
                    closeFuture.setClosed();
                    safeSetFailure(promise, t);
                }
            }
        }
````

#### 处理创建连接事件9：AbstractChannel->register0(ChannelPromise promise)

``````
 private void register0(ChannelPromise promise) {
            try {
                // check if the channel is still open as it could be closed in the mean time when the register
                // call was outside of the eventLoop
                if (!promise.setUncancellable() || !ensureOpen(promise)) {
                    return;
                }
                boolean firstRegistration = neverRegistered;
                 //真正创建
                doRegister();
                neverRegistered = false;
                registered = true;

                // Ensure we call handlerAdded(...) before we actually notify the promise. This is needed as the
                // user may already fire events through the pipeline in the ChannelFutureListener.
                pipeline.invokeHandlerAddedIfNeeded();

                safeSetSuccess(promise);
                //传播连接创建注册事件
                pipeline.fireChannelRegistered();
                // Only fire a channelActive if the channel has never been registered. This prevents firing
                // multiple channel actives if the channel is deregistered and re-registered.
                // server socket 的注册不会进下面的if,server socket 接受连接创建的socket 可以进去，因为在服务启动的时候，已经bind成功
                if (isActive()) {
                    if (firstRegistration) {
                        //在handler中触发创建连接事件，注册OP_ACCEPT/OP_READ 事件也就是创建连接或读事件
                        pipeline.fireChannelActive();
                    } else if (config().isAutoRead()) {
                        //创建连接事件成功，开始注册一个OP_READ事件监听器，处理读事件
                        beginRead();
                    }
                }
            } catch (Throwable t) {
                // Close the channel directly to avoid FD leak.
                closeForcibly();
                closeFuture.setClosed();
                safeSetFailure(promise, t);
            }
        }
``````

#### 创建连接事件10：

````
 protected void doRegister() throws Exception {
        boolean selected = false;
        for (;;) {
            try {
                selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
                return;
            } catch (CancelledKeyException e) {
                if (!selected) {
                    // Force the Selector to select now as the "canceled" SelectionKey may still be
                    // cached and not removed because no Select.select(..) operation was called yet.
                    eventLoop().selectNow();
                    selected = true;
                } else {
                    // We forced a select operation on the selector before but the SelectionKey is still cached
                    // for whatever reason. JDK bug ?
                    throw e;
                }
            }
        }
    }
````

-  创建连接的初始化和注册是通过 pipeline.fireChannelRead 在 ServerBootstrapAcceptor中完成的

- 第一次 Register 并不是监听OP_READ ,而是0：

- 最终监听 OP_READ 是通过 "register"完成后的fireChannelActive 来触发的

- workers NioEventLoop 是通过register操作来执行启动

- 接受连接的读操作,不会尝试读取更多次(16次)

---------------

### 接受数据(在worker 线程里对OP_READ事件的处理)

- 自适应数据大小的分配器（AdaptiveRecvByteBufAllocator）

- 连续读（defaultMaxMessagesPerRead）

#### 核心源码

- 接收数据详情1：AbstractNioByteChannel->read()

````
 public final void read() {
            final ChannelConfig config = config();
            if (shouldBreakReadReady(config)) {
                clearReadPending();
                return;
            }
            final ChannelPipeline pipeline = pipeline();
            //byteBuf 的分配器
            final ByteBufAllocator allocator = config.getAllocator();
            //AdaptiveRecvByteAllocator#HandleImpl
            final RecvByteBufAllocator.Handle allocHandle = recvBufAllocHandle();
            allocHandle.reset(config);

            ByteBuf byteBuf = null;
            boolean close = false;
            try {
                do { 
                    //分配合适的大小ByteBuf,默认1024
                    byteBuf = allocHandle.allocate(allocator);
                    //读并且记录读了多少，如果读满了，下次continue的话就直接扩容
                    //doReadBytes(byteBuf) ：真正的读数据
                    allocHandle.lastBytesRead(doReadBytes(byteBuf));
                    if (allocHandle.lastBytesRead() <= 0) {
                        // nothing was read. release the buffer.
                        byteBuf.release();
                        byteBuf = null;
                        close = allocHandle.lastBytesRead() < 0;
                        if (close) {
                            // There is nothing left to read as we received an EOF.
                            readPending = false;
                        }
                        break;
                    }
                    //记录读的次数
                    allocHandle.incMessagesRead(1);
                    readPending = false;
                    //pipleline 上执行，业务逻辑处理就是在这里
                    pipeline.fireChannelRead(byteBuf);
                    byteBuf = null;
                } while (allocHandle.continueReading());
                //记录这次读事件总共读了多少字节数据，计算下次分配大小
                allocHandle.readComplete();
                //完成本次读事件处理逻辑
                pipeline.fireChannelReadComplete();

                if (close) {
                    closeOnRead(pipeline);
                }
            } catch (Throwable t) {
                handleReadException(pipeline, byteBuf, t, close, allocHandle);
            } finally {
                // Check if there is a readPending which was not processed yet.
                // This could be for two reasons:
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
                //
                // See https://github.com/netty/netty/issues/2254
                if (!readPending && !config.isAutoRead()) {
                    removeReadOp();
                }
            }
        }
    }
````
- 接收数据详情2：NioSocketChannel->read()

````
  protected int doReadBytes(ByteBuf byteBuf) throws Exception {
        final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
        allocHandle.attemptedBytesRead(byteBuf.writableBytes());
        return byteBuf.writeBytes(javaChannel(), allocHandle.attemptedBytesRead());
    }
````
- 接收数据详情3：AbstractByteBuf ->writeBytes()

````
 public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        ensureWritable(length);
        // -1 : EOF 正常关闭，IO Exception 表示读数据是被关闭
        int writtenBytes = setBytes(writerIndex, in, length);
        if (writtenBytes > 0) {
            writerIndex += writtenBytes;
        }
        return writtenBytes;
    }
````
- 接收数据详情4：UnPooledDirectByteBuf ->setBytes()

````
 public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        ensureAccessible();
        ByteBuffer tmpBuf = internalNioBuffer();
        tmpBuf.clear().position(index).limit(index + length);
        try {
            //把数据保存到ByteBuffer里面
            return in.read(tmpBuf);
        } catch (ClosedChannelException ignored) {
            return -1;
        }
    }

````

#### 处理OP_READ事件：NioSocketChannel.NioSocketChannelUnsafe.read()

- NioSocketChannel read()是读数据，NioServerSocket read()是创建连接

- 从Channel 接受数据到byte buffer

- 记录实际接受数据大小，调整下次分配byte buffer 大小

- 触发pipeline.fireChannelRead(byteBuf)把读取到的数据传播出去

- 判断接受 byte buffer 是否满载而归：是，尝试继续读取直到没有数据或满16次；否，结束本轮读取，等待下次OP_READ事件


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

- 写数据写不进去时，会停止写，注册一个OP_WRITE事件，来通知什么时候可以写进去了

- OP_WRITE不是说有数据可写，而是说可以写进去；

- 批量写数据时，如果尝试写的都写进去了，接下来会尝试写更多(maxBytesPerGatheringWrite)

- 只要有数据要写，且能写，则一直尝试，直到16次（writeSpinCount）,写16次还没有写完，就直接schedule 一个task来继续写，而不是用注册事件来触发，更简洁有力

- 待写数据太多，超过一定的水位线（writeBufferWaterMask.high()）,会将可写的标志位改成false,让应用端自己做决定要不要继续写

- channelHandlerContext.channel().write():从TailContext开始执行；

- channelHandlerContext.write():从当前的Context开始

----------

### 断开连接

#### 多路复用器收到OP_READ事件，处理器：NioSocketChannel.NioSocketChannelUnsafe.read():

- 接受数据

- 判断接受的数据大小是否< 0.如果是，说明是关闭，开始执行关闭

  - 关闭channel(包含cancel多路复用器的key)
  - 清理消息：不接受新消息，fail掉所有queue中的消息
  - 触发fireChannelInactive和fireChannelUnregistered

- 关闭连接，会触发OP_READ方法。读取字节数是-1代表关闭

- 数据读取进行时，强行关闭，触发IO Exception,进而继续关闭

- Channel 的关闭包含了 SelectionKey的cancel

-----
### 服务关闭

- boosGroup.shutdownGracefully()

- workerGroup.shutdownGracefully()

#### 关闭所有Group 中的NioEventLoop

- 修改NioEventLoop 的state标志位

- NioEventLoop 判断State执行退出