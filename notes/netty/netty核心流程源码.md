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
                //判断在关闭服务方法中，状态是否已经改成关闭状态
                if (isShuttingDown()) {
                    //关闭channel
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
                    //doReadBytes(byteBuf) ：真正的读数据,-1代表正常关闭
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

### 业务处理(在worker线程里对OP_READ事件的处理)

#### handler执行资格：

- 实现了ChannelInboundHandler接口

- 实现方法 channelRead 不能家@Skip注解

#### 处理业务本质：数据在pipeline中所有的handler的channelRead()执行过程

#### 核心源码

- 业务处理详情1： AbstractNioMessageChannel->pipeline.fireChannelRead
````
public final ChannelPipeline fireChannelRead(Object msg) {
        //从Head开始
        AbstractChannelHandlerContext.invokeChannelRead(head, msg);
        return this;
    }
````
- 业务处理详情2：
````
static void invokeChannelRead(final AbstractChannelHandlerContext next, Object msg) {
        final Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);
        //NioEventLoop
        EventExecutor executor = next.executor();
        //未指定的话默认是EventLoop里面的线程来执行，也可以指定:
        // pipeline.addLast(new UnorderedThreadPoolEventExecutor(10),serverHandler)
        if (executor.inEventLoop()) {
            //执行ChannelInboudHandler 的channelRead方法
            next.invokeChannelRead(m);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    next.invokeChannelRead(m);
                }
            });
        }
    }
````
- 业务处理详情3：
````
public void channelRead(ChannelHandlerContext ctx, Object msg) {
            //继续在pipleline上传播
            ctx.fireChannelRead(msg);
 }
````

- 业务处理详情4：传播处理
````
public ChannelHandlerContext fireChannelRead(final Object msg) {
        invokeChannelRead(findContextInbound(MASK_CHANNEL_READ), msg);
        return this;
    }
//判断后续是否有资格执行的Handler，如果有的话返回，在invokeChannelRead里面执行
 private AbstractChannelHandlerContext findContextInbound(int mask) {
        AbstractChannelHandlerContext ctx = this;
        do {
            ctx = ctx.next;
        } while ((ctx.executionMask & mask) == 0);
        return ctx;
    }
````

### 发送数据(handler业务处理结果的写出)

### Write : 写数据到buffer然后flush发送buffer里面的数据,Write和Flush之间有个ChannelOutboundBuffer用作数据缓存

#### write 

- 写数据到buffer : ChannelOutboundBuffer#addMessage

#### flush

- 准备数据：ChannelOutboundBuffer##addFlush

- 真正发送：NioSocketChannel#doWrite

- 写数据写不进去时，会停止写，注册一个OP_WRITE事件，来通知什么时候可以写进去了

- OP_WRITE不是说有数据可写，而是说可以写进去；

- 批量写数据时，如果尝试写的都写进去了，接下来会尝试写更多(maxBytesPerGatheringWrite)

- 只要有数据要写，且能写，则一直尝试，直到16次（writeSpinCount）,写16次还没有写完，就直接schedule 一个task来继续写，而不是用注册事件来触发，更简洁有力

- 待写数据太多，超过一定的水位线（writeBufferWaterMask.high()）,会将可写的标志位改成false,让应用端自己做决定要不要继续写

#### 知识点

- channelHandlerContext.channel().write():从TailContext开始执行；

- channelHandlerContext.write():从当前的Context开始

----------

#### 核心源码

- 写数据详情1：write 

````
 private void write(Object msg, boolean flush, ChannelPromise promise) {
        ObjectUtil.checkNotNull(msg, "msg");
        try {
            if (isNotValidPromise(promise, true)) {
                ReferenceCountUtil.release(msg);
                // cancelled
                return;
            }
        } catch (RuntimeException e) {
            ReferenceCountUtil.release(msg);
            throw e;
        }

        final AbstractChannelHandlerContext next = findContextOutbound(flush ?(MASK_WRITE | MASK_FLUSH) : MASK_WRITE);
        //引用计数，用来检测内存泄漏
        final Object m = pipeline.touch(msg, next);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            if (flush) {
                //writeAndFlush
                next.invokeWriteAndFlush(m, promise);
            } else {
                //仅仅write
                next.invokeWrite(m, promise);
            }
        } else {
            final AbstractWriteTask task;
            if (flush) {
                task = WriteAndFlushTask.newInstance(next, m, promise);
            }  else {
                task = WriteTask.newInstance(next, m, promise);
            }
            if (!safeExecute(executor, task, promise, m)) {
                // We failed to submit the AbstractWriteTask. We need to cancel it so we decrement the pending bytes
                // and put it back in the Recycler for re-use later.
                //
                // See https://github.com/netty/netty/issues/8343.
                task.cancel();
            }
        }
    }

````

- 写数据详情2：ChannelPipeline写数据

````
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            unsafe.write(msg, promise);
        }
//真正写数据
public final void write(Object msg, ChannelPromise promise) {
            assertEventLoop();
            //write和flush之间的数据buffer
            ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            //判断是否channel已经关闭
            if (outboundBuffer == null) {
                // If the outboundBuffer is null we know the channel was closed and so
                // need to fail the future right away. If it is not null the handling of the rest
                // will be done in flush0()
                // See https://github.com/netty/netty/issues/2362
                safeSetFailure(promise, newClosedChannelException(initialCloseCause));
                // release message now to prevent resource-leak
                ReferenceCountUtil.release(msg);
                return;
            }
            int size;
            try {
                msg = filterOutboundMessage(msg);
                size = pipeline.estimatorHandle().size(msg);
                if (size < 0) {
                    size = 0;
                }
            } catch (Throwable t) {
                safeSetFailure(promise, t);
                ReferenceCountUtil.release(msg);
                return;
            }
            //把消息放到buffer里面，待flush
            outboundBuffer.addMessage(msg, size, promise);
        }
````

- 发送数据详情3： addMessage()

````
public void addMessage(Object msg, int size, ChannelPromise promise) {
        //构建一个消息，放在 linkedList 队列尾部
        Entry entry = Entry.newInstance(msg, size, total(msg), promise);
        if (tailEntry == null) {
            flushedEntry = null;
        } else {
            Entry tail = tailEntry;
            tail.next = entry;
        }
        tailEntry = entry;
        if (unflushedEntry == null) {
             //队列尾部元素也就是unflushedEntry
            unflushedEntry = entry;
        }

        // increment pending bytes after adding message to the unflushed arrays.
        // See https://github.com/netty/netty/issues/1619
        incrementPendingOutboundBytes(entry.pendingSize, false);
    }

````

- 发送数据详情4： incrementPendingOutboundBytes()

````

private void incrementPendingOutboundBytes(long size, boolean invokeLater) {
        if (size == 0) {
            return;
        }

        long newWriteBufferSize = TOTAL_PENDING_SIZE_UPDATER.addAndGet(this, size);
         //判断待发送的数据的size是否高于水位线
        if (newWriteBufferSize > channel.config().getWriteBufferHighWaterMark()) {
            //设置不可写标志，让应用决定是否继续写数据
            setUnwritable(invokeLater);
        }
    }

````
- 发送数据详情1：flush

````
public ChannelHandlerContext flush() {
        //找下一级的handler
        final AbstractChannelHandlerContext next = findContextOutbound(MASK_FLUSH);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            //执行flash
            next.invokeFlush();
        } else {
            Tasks tasks = next.invokeTasks;
            if (tasks == null) {
                next.invokeTasks = tasks = new Tasks(next);
            }
            safeExecute(executor, tasks.invokeFlushTask, channel().voidPromise(), null);
        }

        return this;
    }

```` 
- 发送数据详情2： invokeFlush0

````
 private void invokeFlush0() {
        try {
            ((ChannelOutboundHandler) handler()).flush(this);
        } catch (Throwable t) {
            notifyHandlerException(t);
        }
    }
````

- 发送数据详情3：flush

````
public final void flush() {
            assertEventLoop();

            ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            //outboundBuffer == null表明channel关闭了
            if (outboundBuffer == null) {
                return;
            }
            //addFlush
            outboundBuffer.addFlush();
            //最后一步写出数据
            flush0();
        }
````

- 发送数据详情4： addFlush

````
 
public void addFlush() {
        // There is no need to process all entries if there was already a flush before and no new messages
        // where added in the meantime.
        //
        // See https://github.com/netty/netty/issues/2577
        //将unflushed 数据转成flushed
        Entry entry = unflushedEntry;
        if (entry != null) {
            if (flushedEntry == null) {
                // there is no flushedEntry yet, so start with the entry
                flushedEntry = entry;
            }
            do {
                flushed ++;
                if (!entry.promise.setUncancellable()) {
                    // Was cancelled so make sure we free up memory and notify about the freed bytes
                    int pending = entry.cancel();
                    decrementPendingOutboundBytes(pending, false, true);
                }
                entry = entry.next;
            } while (entry != null);

            // All flushed so reset unflushedEntry
            unflushedEntry = null;
        }
    }

````
- 发送数据详情5： flush0()

````
  protected void flush0() {
            if (inFlush0) {
                // Avoid re-entrance
                return;
            }

            final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            if (outboundBuffer == null || outboundBuffer.isEmpty()) {
                return;
            }

            inFlush0 = true;

            // Mark all pending write requests as failure if the channel is inactive.
            if (!isActive()) {
                try {
                    if (isOpen()) {
                        outboundBuffer.failFlushed(new NotYetConnectedException(), true);
                    } else {
                        // Do not trigger channelWritabilityChanged because the channel is closed already.
                        outboundBuffer.failFlushed(newClosedChannelException(initialCloseCause), false);
                    }
                } finally {
                    inFlush0 = false;
                }
                return;
            }

            try {
                //执行写出逻辑
                doWrite(outboundBuffer);
            } catch (Throwable t) {
                if (t instanceof IOException && config().isAutoClose()) {
                    /**
                     * Just call {@link #close(ChannelPromise, Throwable, boolean)} here which will take care of
                     * failing all flushed messages and also ensure the actual close of the underlying transport
                     * will happen before the promises are notified.
                     *
                     * This is needed as otherwise {@link #isActive()} , {@link #isOpen()} and {@link #isWritable()}
                     * may still return {@code true} even if the channel should be closed as result of the exception.
                     */
                    initialCloseCause = t;
                    close(voidPromise(), t, newClosedChannelException(t), false);
                } else {
                    try {
                        shutdownOutput(voidPromise(), t);
                    } catch (Throwable t2) {
                        initialCloseCause = t;
                        close(voidPromise(), t2, newClosedChannelException(t), false);
                    }
                }
            } finally {
                inFlush0 = false;
            }
        }

````

- 发送数据详情6： doWrite(outboundBuffer)

````
 protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        SocketChannel ch = javaChannel();
         // 有数据要写，且能写入，最多尝试16次
        int writeSpinCount = config().getWriteSpinCount();
        do {
            if (in.isEmpty()) {
                // All written so clear OP_WRITE
                //数据写完，不需要写16次
                clearOpWrite();
                // Directly return here so incompleteWrite(...) is not called.
                return;
            }

            // Ensure the pending writes are made of ByteBufs only.
            int maxBytesPerGatheringWrite = ((NioSocketChannelConfig) config).getMaxBytesPerGatheringWrite();
            //最多返回1024个数据，总的字节尽量不超过maxBytesPerGatheringWrite
            ByteBuffer[] nioBuffers = in.nioBuffers(1024, maxBytesPerGatheringWrite);
            int nioBufferCnt = in.nioBufferCount();

            // Always us nioBuffers() to workaround data-corruption.
            // See https://github.com/netty/netty/issues/2761
            switch (nioBufferCnt) {
                case 0:
                    // We have something else beside ByteBuffers to write so fallback to normal writes.
                    writeSpinCount -= doWrite0(in);
                    break;
                case 1: {
                    // Only one ByteBuf so use non-gathering write
                    // Zero length buffers are not added to nioBuffers by ChannelOutboundBuffer, so there is no need
                    // to check if the total size of all the buffers is non-zero.
                    ByteBuffer buffer = nioBuffers[0];
                    int attemptedBytes = buffer.remaining();
                    //对于单个数据，直接调用了channel的write(buffer)方法，也就是JDK socketChannel
                    final int localWrittenBytes = ch.write(buffer);
                    if (localWrittenBytes <= 0) {
                        incompleteWrite(true);
                        return;
                    }
                    adjustMaxBytesPerGatheringWrite(attemptedBytes, localWrittenBytes, maxBytesPerGatheringWrite);
                    //从channelOutboundBuffer中移除已经写出的数据
                    in.removeBytes(localWrittenBytes);
                    --writeSpinCount;
                    break;
                }
                default: {
                    // Zero length buffers are not added to nioBuffers by ChannelOutboundBuffer, so there is no need
                    // to check if the total size of all the buffers is non-zero.
                    // We limit the max amount to int above so cast is safe
                    long attemptedBytes = in.nioBufferSize();
                    final long localWrittenBytes = ch.write(nioBuffers, 0, nioBufferCnt);
                    if (localWrittenBytes <= 0) {
                        //缓存区数据满了，写不进去了，注册写事件,OP_WRITE 是可以写进去不是说有数据可写
                        //注册OP_WRITE 事件监听，等能写进去的时候，通知写
                        //也就是在NioEventLoop中执行一个写 ch.unsafe().forceFlush()
                        incompleteWrite(true);
                        return;
                    }
                    // Casting to int is safe because we limit the total amount of data in the nioBuffers to int above.
                    adjustMaxBytesPerGatheringWrite((int) attemptedBytes, (int) localWrittenBytes,maxBytesPerGatheringWrite);
                    //移除已经写完的数据，未写完的process 标记一下进度
                    in.removeBytes(localWrittenBytes);
                    --writeSpinCount;
                    break;
                }
            }
        } while (writeSpinCount > 0);
        //写了16次数据，还是没有写完，直接schedule一个新的flush task 出来，而不是注册写事件。  
        incompleteWrite(writeSpinCount < 0);
    }
````

- 发送数据详情7：  adjustMaxBytesPerGatheringWrite((int) attemptedBytes, (int) localWrittenBytes,maxBytesPerGatheringWrite);

````

private void adjustMaxBytesPerGatheringWrite(int attempted, int written, int oldMaxBytesPerGatheringWrite) {
        // By default we track the SO_SNDBUF when ever it is explicitly set. However some OSes may dynamically change
        // SO_SNDBUF (and other characteristics that determine how much data can be written at once) so we should try
        // make a best effort to adjust as OS behavior changes.
        //一次就写完了，所以扩大一次写入的数据量
        if (attempted == written) {
            if (attempted << 1 > oldMaxBytesPerGatheringWrite) {
                ((NioSocketChannelConfig) config).setMaxBytesPerGatheringWrite(attempted << 1);
            }
        //一次写不完,所以尝试缩小写入的量
        } else if (attempted > MAX_BYTES_PER_GATHERING_WRITE_ATTEMPTED_LOW_THRESHOLD && written < attempted >>> 1) {
            ((NioSocketChannelConfig) config).setMaxBytesPerGatheringWrite(attempted >>> 1);
        }
    }
````


### 断开连接, 多路复用器收到OP_READ事件，处理器：NioSocketChannel.NioSocketChannelUnsafe.read():

- 接受数据

- 判断接受的数据大小是否< 0.如果是，说明是关闭，开始执行关闭

  - 关闭channel(包含cancel多路复用器的key)
  - 清理消息：不接受新消息，fail掉所有queue中的消息
  - 触发fireChannelInactive和fireChannelUnregistered

- 关闭连接，会触发OP_READ方法。读取字节数是-1代表关闭

- 数据读取进行时,强行关闭，触发IO Exception,进而继续关闭

- Channel 的关闭包含了 SelectionKey的cancel

#### 核心源码

- 关闭连接详情1：相应读事件

````
 public final void read() {
            final ChannelConfig config = config();
            if (shouldBreakReadReady(config)) {
                clearReadPending();
                return;
            }
            final ChannelPipeline pipeline = pipeline();
            final ByteBufAllocator allocator = config.getAllocator();
            final RecvByteBufAllocator.Handle allocHandle = recvBufAllocHandle();
            allocHandle.reset(config);

            ByteBuf byteBuf = null;
            boolean close = false;
            try {
                do {
                    byteBuf = allocHandle.allocate(allocator);
                     //doReadBytes(byteBuf) ：真正的读数据,-1代表正常关闭
                    allocHandle.lastBytesRead(doReadBytes(byteBuf));
                    if (allocHandle.lastBytesRead() <= 0) {
                        // nothing was read. release the buffer.
                        //释放缓存，byteBuf设置成null
                        byteBuf.release();
                        byteBuf = null;
                        close = allocHandle.lastBytesRead() < 0;
                        //-1 表示要关闭
                        if (close) {
                            // There is nothing left to read as we received an EOF.
                            //设置readPending 为false 表示不在继续读
                            readPending = false;
                        }
                        break;
                    }

                    allocHandle.incMessagesRead(1);
                    readPending = false;
                    pipeline.fireChannelRead(byteBuf);
                    byteBuf = null;
                } while (allocHandle.continueReading());

                allocHandle.readComplete();
                pipeline.fireChannelReadComplete();

                if (close) {
                    //真正关闭连接
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

- 关闭连接详情2：closeOnRead(ChannelPipeline pipeline)
````
 private void closeOnRead(ChannelPipeline pipeline) {
            //判断input是否关闭
            if (!isInputShutdown0()) {
                //判断是否支持半关闭：如果是，则先关闭读，触发事件
                if (isAllowHalfClosure(config())) {
                    shutdownInput();
                    pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
                } else {
                    close(voidPromise());
                }
            } else {
                inputClosedSeenErrorOnRead = true;
                pipeline.fireUserEventTriggered(ChannelInputShutdownReadComplete.INSTANCE);
            }
        }
````

- 关闭连接详情3：close(final ChannelPromise promise)

````
public final void close(final ChannelPromise promise) {
            assertEventLoop();

            ClosedChannelException closedChannelException = new ClosedChannelException();
            close(promise, closedChannelException, closedChannelException, false);
        }


private void close(final ChannelPromise promise, final Throwable cause,
                           final ClosedChannelException closeCause, final boolean notify) {
            if (!promise.setUncancellable()) {
                return;
            }

            if (closeInitiated) {
                if (closeFuture.isDone()) {
                    // Closed already.
                    safeSetSuccess(promise);
                } else if (!(promise instanceof VoidChannelPromise)) { // Only needed if no VoidChannelPromise.
                    // This means close() was called before so we just register a listener and return
                    closeFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            promise.setSuccess();
                        }
                    });
                }
                return;
            }

            closeInitiated = true;

            final boolean wasActive = isActive();
            final ChannelOutboundBuffer outboundBuffer = this.outboundBuffer;
            //不再接收消息
            this.outboundBuffer = null; // Disallow adding any messages and flushes to outboundBuffer.
            //需要逗留到数据收发完成或设置的时间，所以提交到另外的Executor 中执行
            //提前deregister 包含 selection key 的 cancel的原因之一：cancel掉各种感兴趣的事件，不再监听各种事件
            //单独返回一个EventExecutor 防止影响正在进行的业务
            Executor closeExecutor = prepareToClose();
            if (closeExecutor != null) {
                closeExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Execute the close.
                            //走到这里，说明solinger(逗留时间)
                            doClose0(promise);
                        } finally {
                            // Call invokeLater so closeAndDeregister is executed in the EventLoop again!
                            invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (outboundBuffer != null) {
                                        // Fail all the queued messages
                                        outboundBuffer.failFlushed(cause, notify);
                                        outboundBuffer.close(closeCause);
                                    }
                                    fireChannelInactiveAndDeregister(wasActive);
                                }
                            });
                        }
                    }
                });
            } else {
                try {
                    // Close the channel and fail the queued messages in all cases.
                    doClose0(promise);
                } finally {
                    if (outboundBuffer != null) {
                        // Fail all the queued messages.
                       
                        outboundBuffer.failFlushed(cause, notify);
                        //outboundBuffer关闭
                        outboundBuffer.close(closeCause);
                    }
                }
                if (inFlush0) {
                    invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            fireChannelInactiveAndDeregister(wasActive);
                        }
                    });
                } else {
                    
                    fireChannelInactiveAndDeregister(wasActive);
                }
            }
        }
````

- 关闭连接详情4：NioScoketChannel()->doClose0

````
protected void doClose() throws Exception {
        super.doClose();
         //jdk 
        javaChannel().close();
    }

public final void close() throws IOException {
        synchronized (closeLock) {
            if (!open)
                return;
            open = false;
            //把SelectionKey 从 selector上cancel掉
            implCloseChannel();
        }
    }

````

- 关闭连接详情8:fireChannelInactiveAndDeregister

````
 private void deregister(final ChannelPromise promise, final boolean fireChannelInactive) {
            if (!promise.setUncancellable()) {
                return;
            }

            if (!registered) {
                safeSetSuccess(promise);
                return;
            }

            // As a user may call deregister() from within any method while doing processing in the ChannelPipeline,
            // we need to ensure we do the actual deregister operation later. This is needed as for example,
            // we may be in the ByteToMessageDecoder.callDecode(...) method and so still try to do processing in
            // the old EventLoop while the user already registered the Channel to a new EventLoop. Without delay,
            // the deregister operation this could lead to have a handler invoked by different EventLoop and so
            // threads.
            //
            // See:
            // https://github.com/netty/netty/issues/4435
            invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        //cancel selectionKey
                        doDeregister();
                    } catch (Throwable t) {
                        logger.warn("Unexpected exception occurred while deregistering a channel.", t);
                    } finally {
                        if (fireChannelInactive) {
                            //做一个 Channel Inactive的操作
                            pipeline.fireChannelInactive();
                        }
                        // Some transports like local and AIO does not allow the deregistration of
                        // an open channel.  Their doDeregister() calls close(). Consequently,
                        // close() calls deregister() again - no need to fire channelUnregistered, so check
                        // if it was registered.
                        if (registered) {
                            registered = false;
                            //做一个 Channel unRegistered的操作
                            pipeline.fireChannelUnregistered();
                        }
                        safeSetSuccess(promise);
                    }
                }
            });
        }
````

- doDeregister()

````
 void cancel(SelectionKey key) {
        //没有特殊情况（配置so linger）,下面这个cancel :实际没有"执行"，因为在关闭channel 的时候执行过了
        key.cancel();
        cancelledKeys ++;
        //下面是优化：当处理一批事件时，发现很多链接都断了（默认256），这个时候后面的事件可能都失效了，所以select again 下
        if (cancelledKeys >= CLEANUP_INTERVAL) {
            cancelledKeys = 0;
            needsToSelectAgain = true;
        }
    }
````
-----
### 服务关闭

- boosGroup.shutdownGracefully()

- workerGroup.shutdownGracefully()

#### 关闭所有Group 中的 NioEventLoop

- 修改NioEventLoop的state标志位

- NioEventLoop判断State执行退出

#### 核心源码

- 服务关闭详情1：shutdownGracefully()

- shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)

 //@param quietPeriod the quiet period as described in the documentation
 //@param timeout     the maximum amount of time to wait until the executor is {@linkplain #shutdown()}regardless if a task was submitted during the quiet period
````

public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        if (quietPeriod < 0) {
            throw new IllegalArgumentException("quietPeriod: " + quietPeriod + " (expected >= 0)");
        }
        if (timeout < quietPeriod) {
            throw new IllegalArgumentException("timeout: " + timeout + " (expected >= quietPeriod (" + quietPeriod + "))");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }

        if (isShuttingDown()) {
            return terminationFuture();
        }

        boolean inEventLoop = inEventLoop();
        boolean wakeup;
        int oldState;
        for (;;) {
            if (isShuttingDown()) {
                return terminationFuture();
            }
            int newState;
            wakeup = true;
            oldState = state;
            if (inEventLoop) {
                newState = ST_SHUTTING_DOWN;
            } else {              
                switch (oldState) {
                    case ST_NOT_STARTED:
                    case ST_STARTED:  //oldState =2 表示已经启动过了
                        newState = ST_SHUTTING_DOWN; //状态改为正在关闭状态
                        break;
                    default:
                        newState = oldState;
                        wakeup = false;
                }
            }
            if (STATE_UPDATER.compareAndSet(this, oldState, newState)) {
                break;
            }
        }
        gracefulShutdownQuietPeriod = unit.toNanos(quietPeriod);
        gracefulShutdownTimeout = unit.toNanos(timeout);

        if (ensureThreadStarted(oldState)) {
            return terminationFuture;
        }

        if (wakeup) {
            taskQueue.offer(WAKEUP_TASK);
            if (!addTaskWakesUp) {
                wakeup(inEventLoop);
            }
        }

        return terminationFuture();
    }

````

- 服务关闭详情2： 在EventLoop run()方法中判断关闭状态是否是关闭

````
private void closeAll() {
        //去除canceled的key
        selectAgain();
        Set<SelectionKey> keys = selector.keys();
        Collection<AbstractNioChannel> channels = new ArrayList<AbstractNioChannel>(keys.size());
        for (SelectionKey k: keys) {
            Object a = k.attachment();
            if (a instanceof AbstractNioChannel) {
                channels.add((AbstractNioChannel) a);
            } else {
                k.cancel();
                @SuppressWarnings("unchecked")
                NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
                invokeChannelUnregistered(task, k, null);
            }
        }

        for (AbstractNioChannel ch: channels) {
            //close 掉所有的channel
            ch.unsafe().close(ch.unsafe().voidPromise());
        }
    }

````

- 服务关闭详情3：confirmShutdown()

````
  protected boolean confirmShutdown() {
        if (!isShuttingDown()) {
            return false;
        }

        if (!inEventLoop()) {
            throw new IllegalStateException("must be invoked from an event loop");
        }
        //关闭到所有的Scheduled 的 task
        cancelScheduledTasks();

        if (gracefulShutdownStartTime == 0) {
            gracefulShutdownStartTime = ScheduledFutureTask.nanoTime();
        }
        //有task/hook在里面，执行他们，并且不让关闭，因为静默期又有任务了
        if (runAllTasks() || runShutdownHooks()) {
            if (isShutdown()) {
                // Executor shut down - no new tasks anymore.
                return true;
            }

            // There were tasks in the queue. Wait a little bit more until no tasks are queued for the quiet period or
            // terminate if the quiet period is 0.
            // See https://github.com/netty/netty/issues/4241
            if (gracefulShutdownQuietPeriod == 0) {
                return true;
            }
            taskQueue.offer(WAKEUP_TASK);
            return false;
        }

        final long nanoTime = ScheduledFutureTask.nanoTime();
        //是否超过最大允许时间，如果是，需要关闭了，不再等待。
        if (isShutdown() || nanoTime - gracefulShutdownStartTime > gracefulShutdownTimeout) {
            return true;
        }
        //如果静默期做了任务，不关闭，sleep 100ms,再检查下
        if (nanoTime - lastExecutionTime <= gracefulShutdownQuietPeriod) {
            // Check if any tasks were added to the queue every 100ms.
            // TODO: Change the behavior of takeTask() so that it returns on timeout.
            taskQueue.offer(WAKEUP_TASK);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
            }

            return false;
        }

        // No tasks were added for last quiet period - hopefully safe to shut down.
        // (Hopefully because we really cannot make a guarantee that there will be no execute() calls by a user.)
        return true;
    }

````

- 服务关闭详情4：cancelScheduledTasks()

````
//cancel 掉所有的Scheduled的tasks
protected void cancelScheduledTasks() {
        assert inEventLoop();
        PriorityQueue<ScheduledFutureTask<?>> scheduledTaskQueue = this.scheduledTaskQueue;
        if (isNullOrEmpty(scheduledTaskQueue)) {
            return;
        }

        final ScheduledFutureTask<?>[] scheduledTasks =
                scheduledTaskQueue.toArray(new ScheduledFutureTask<?>[0]);

        for (ScheduledFutureTask<?> task: scheduledTasks) {
            task.cancelWithoutRemove(false);
        }

        scheduledTaskQueue.clearIgnoringIndexes();
    }


````