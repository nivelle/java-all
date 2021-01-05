### Poller

#### Tomcat NioEndpoint 内部类Poller实现了Runnable接口，主要用来作为独立的后台线程来完成以下轮询服务 :

1. acceptor线程接收到的连接请求注册到所包含的NIO selector;从 acceptor 线程接受请求，注册到 Poller （所包含的 NIO selector）

2. 所包含的NIO selector关注的NIO事件中，某些事件发生时交给相应的处理器处理

   (1). 注册到 Poller （所包含的 NIO selector）的事件被 Poller 线程消费;
   
   (2). Poller 线程消费事件，处理 NIO SelectionKey, 交给 worker 线程进行相应的请求处理。
   
   
```
public class Poller implements Runnable {

        //java NIO selector 记录变量
        private Selector selector;
        
        // PollerEvent事件队列,同步队列,因为对PollerEvent的操作牵涉到多线程,所以才用同步队列。
        // 比如该队列事件的注册者和该队列事件消费者可能是不同的线程，更具体的来讲，tomcat的连接请求接收线程acceptor接收到连接后就会把连接套接字注册到Poller的该事件队列，
        // 而poller线程本身也在一直运行并消费该事件队列，这里提到的是两个不同的线程在操作同一个队列对象，所以要用同步队列。
        
        //该事件队列中的事件会在该Poller实例所附属的线程的执行循环中被消费和处理
        private final SynchronizedQueue<PollerEvent> events = new SynchronizedQueue<>();
        
        //记录当前poller轮询器是否被通知要关闭轮询线程
        private volatile boolean close = false;
        // Optimize expiration handling
        private long nextExpiration = 0;

        private AtomicLong wakeupCounter = new AtomicLong(0);

        private volatile int keyCount = 0;

        public Poller() throws IOException {
            //该Poller对象维持一个自己的Java NIO Selector对象
            this.selector = Selector.open();
        }

        public int getKeyCount() { return keyCount; }

        public Selector getSelector() { return selector; }

        /**
         * Destroy the poller.
         * 
         * 设计给当前轮询器poller所属的NioEndpoint实例使用，用于关闭该轮询器
         */
        protected void destroy() {
            // Wait for polltime before doing anything, so that the poller threads
            // exit, otherwise parallel closure of sockets which are still
            // in the poller can cause problems
            // 用于告诉轮询线程要结束了
            close = true;
            selector.wakeup();
        }
        //往PollerEvent事件队列中添加事件，封装给当前Poller实例自己使用的私有方法
        private void addEvent(PollerEvent event) {
            events.offer(event);
            if (wakeupCounter.incrementAndGet() == 0) {
                selector.wakeup();
            }
        }

        /**
         * Add specified socket and associated pool to the poller. The socket will
         * be added to a temporary array, and polled first after a maximum amount
         * of time equal to pollTime (in most cases, latency will be much lower,
         * however).
         *
         * @param socketWrapper to add to the poller
         * @param interestOps Operations for which to register this socket with the Poller
         */
        public void add(NioSocketWrapper socketWrapper, int interestOps) {
            PollerEvent r = null;
            if (eventCache != null) {
                // eventCache是当前Poller实例所属NioEndpoint实例的PollerEvent循环回收缓存，eventCache存在的目的是为了循环回收使用用过的PollerEvent对象，降低GC成本
                r = eventCache.pop();
            }
            //如果没有可循环回收使用的PollerEvent对象则新建一个，否则重用循环回收缓存中获取的PollerEvent对象
            if (r == null) {
                r = new PollerEvent(socketWrapper.getSocket(), interestOps);
            } else {
                r.reset(socketWrapper.getSocket(), interestOps);
            }
            //往队列中放入待处理事件PollerEvent
            addEvent(r);
            if (close) {
                processSocket(socketWrapper, SocketEvent.STOP, false);
            }
        }

        /**
         * Processes events in the event queue of the Poller.
         *
         * @return <code>true</code> if some events were processed, <code>false</code> if queue was empty
         * 处理PollerEvent事件队列中的所有事件 队列中有需要处理的事件则返回true，否则返回false
         */
        public boolean events() {
            //用于标记该次方法调用是否处理过PollerEvent事件
            boolean result = false;

            PollerEvent pe = null;
            //从队列中循环取出PollerEvent并处理，直到队列中所有的事件都被处理完
            for (int i = 0, size = events.size(); i < size && (pe = events.poll()) != null; i++ ) {
                //队列中只要存在任何一个事件被处理则当前方法返回true
                result = true;
                NioChannel channel = pe.getSocket();
                NioSocketWrapper socketWrapper = channel.getSocketWrapper();
                int interestOps = pe.getInterestOps();
                if (interestOps == OP_REGISTER) {
                    try {
                        channel.getIOChannel().register(getSelector(), SelectionKey.OP_READ, socketWrapper);
                    } catch (Exception x) {
                        log.error(sm.getString("endpoint.nio.registerFail"), x);
                    }
                } else {
                    final SelectionKey key = channel.getIOChannel().keyFor(getSelector());
                    if (key == null) {
                        // The key was cancelled (e.g. due to socket closure)
                        // and removed from the selector while it was being
                        // processed. Count down the connections at this point
                        // since it won't have been counted down when the socket
                        // closed.
                        socketWrapper.close();
                    } else {
                        final NioSocketWrapper attachment = (NioSocketWrapper) key.attachment();
                        if (attachment != null) {
                            // We are registering the key to start with, reset the fairness counter.
                            try {
                                int ops = key.interestOps() | interestOps;
                                // 这里表明对将要注册的目标套接字socket关注的操作是OP_READ,读数据
                                attachment.interestOps(ops);
                                key.interestOps(ops);
                            } catch (CancelledKeyException ckx) {
                                cancelledKey(key, socketWrapper);
                            }
                        } else {
                            cancelledKey(key, attachment);
                        }
                    }
                }
                if (running && !paused && eventCache != null) {
                    //处理完事件如果仍处于服务状态则重置并回收该PollerEvent对象
                    pe.reset();
                    eventCache.push(pe);
                }
            }

            return result;
        }

        /**
         * Registers a newly created socket with the poller.
         *
         * @param socket    The newly created socket
         * @param socketWrapper The socket wrapper
         * 
         * 向Poller对象注册一个新创建的套接字socket; 典型应用 : tomcat acceptor线程每接收到一个连接请求，就会调用某个poller对象的该方法
         */
        public void register(final NioChannel socket, final NioSocketWrapper socketWrapper) {
            socketWrapper.interestOps(SelectionKey.OP_READ);//this is what OP_REGISTER turns into.
            PollerEvent event = null;
            if (eventCache != null) {
                //eventCache是当前Poller实例所属NioEndpoint实例的PollerEvent循环回收缓存，
                //eventCache存在的目的是为了循环回收使用用过的PollerEvent对象，降低GC成本
                event = eventCache.pop();
            }
            //这里表明将要添加的PollerEvent事件的执行会是将目标套接字执行操作OP_REGISTER,
            //注册到相应的 Java NIO Selector实例
            if (event == null) {
                event = new PollerEvent(socket, OP_REGISTER);
            } else {
                event.reset(socket, OP_REGISTER);
            }
            //添加PollerEvent事件到队列
            addEvent(event);
        }
        //处理取消的SelectionKey,关闭相应的套接字通道(连接)，调整连接数量记录
        public void cancelledKey(SelectionKey sk, SocketWrapperBase<NioChannel> socketWrapper) {
            try {
                // If is important to cancel the key first, otherwise a deadlock may occur between the
                // poller select and the socket channel close which would cancel the key
                if (sk != null) {
                    sk.attach(null);
                    if (sk.isValid()) {
                        sk.cancel();
                    }
                }
            } catch (Throwable e) {
                ExceptionUtils.handleThrowable(e);
                if (log.isDebugEnabled()) {
                    log.error(sm.getString("endpoint.debug.channelCloseFail"), e);
                }
            } finally {
                if (socketWrapper != null) {
                    socketWrapper.close();
                }
            }
        }

        /**
         * The background thread that adds sockets to the Poller, checks the
         * poller for triggered events and hands the associated socket off to an
         * appropriate processor as events occur.
         * tomcat的后台poller线程的主逻辑 , 循环处理以下几件事情 :
         *
         * 1. 每次循环处理PollerEvent事件队列中所有的事件
         * 
         * 2. 每次循环处理NIO selector所关注的事件中发生的事件(所有请求的处理，实际上这里都委托给了worker线程)
         *  
         * 3. 超时处理:每次循环中特定条件满足时执行一次超时处理
         * 
         * 4. 结束检测:如果被通知结束，执行结束逻辑，也就是该run()方法内的while-loop的结束
         *
         */
        @Override
        public void run() {
            // Loop until destroy() is called
            while (true) {

                boolean hasEvents = false;

                try {
                    if (!close) {
                        // 没有收到停止消息,处理PollerEvent事件队列中所有的事件
                        hasEvents = events();
                        if (wakeupCounter.getAndSet(-1) > 0) {
                            // If we are here, means we have other stuff to do
                            // Do a non blocking select
                            keyCount = selector.selectNow();
                        } else {
                            keyCount = selector.select(selectorTimeout);
                        }
                        wakeupCounter.set(0);
                    }
                    if (close) {
                        //收到结束通知，poller线程停止前先处理掉PollerEvent队列中的事件
                        events();
                        //poller关闭前的超时处理
                        timeout(0, false);
                        try {
                           //结束Java NIO selector，也就是关闭接收和处理服务
                            selector.close();
                        } catch (IOException ioe) {
                            log.error(sm.getString("endpoint.nio.selectorCloseFail"), ioe);
                        }
                        //被通知结束并且处理完收尾工作，现在结束整个线程的while-loop
                        break;
                    }
                } catch (Throwable x) {
                    //出现异常不退出，记日志然后 poller 线程 while-loop继续执行	
                    ExceptionUtils.handleThrowable(x);
                    log.error(sm.getString("endpoint.nio.selectorLoopError"), x);
                    continue;
                }
                // Either we timed out or we woke up, process events first
                if (keyCount == 0) {
                    hasEvents = (hasEvents | events());
                }

                Iterator<SelectionKey> iterator =
                    keyCount > 0 ? selector.selectedKeys().iterator() : null;
                // Walk through the collection of ready keys and dispatch
                // any active event.
                while (iterator != null && iterator.hasNext()) {
                    SelectionKey sk = iterator.next();
                    NioSocketWrapper socketWrapper = (NioSocketWrapper) sk.attachment();
                    // Attachment may be null if another thread has called
                    // cancelledKey()
                    if (socketWrapper == null) {
                        iterator.remove();
                    } else {
                        iterator.remove();
                        //处理有待处理事件的SelectionKey,其实真正的处理都委托给了 worker 线程
                        processKey(sk, socketWrapper);
                    }
                }

                // Process timeouts
                timeout(keyCount,hasEvents);
            }

            getStopLatch().countDown();
        }

        protected void processKey(SelectionKey sk, NioSocketWrapper socketWrapper) {
            try {
                if (close) {
                    //被通知关闭了，对参数SelectionKey执行取消处理
                    cancelledKey(sk, socketWrapper);
                } else if (sk.isValid() && socketWrapper != null) {
                     //如果参数SelectionKey有效并且带有附件
                    if (sk.isReadable() || sk.isWritable()) {
                        if (socketWrapper.getSendfileData() != null) {
                           
                            processSendfile(sk, socketWrapper, false);
                        } else {
                            unreg(sk, socketWrapper, sk.readyOps());
                            boolean closeSocket = false;
                            // Read goes before write
                            if (sk.isReadable()) {
                                 // 处理Socket NIO读操作
                                 // 1. 处理Socket NIO读操作 processSocket()是所属NioEndpoint实例的方法，方法实现位于类 AbstractEndpoint。
                                 // 2. 如果有线程池，他会将具体操作交给SocketProcessor和线程池完成,
                                 // 3. 如果没有线程池，他会将具体操作交给SocketProcessor和当前线程完成, 
                                if (socketWrapper.readOperation != null) {
                                    if (!socketWrapper.readOperation.process()) {
                                        //处理失败，需要关闭参数SelectionKey对应的套接字通道
                                        closeSocket = true;
                                    }
                                } else if (!processSocket(socketWrapper, SocketEvent.OPEN_READ, true)) {
                                    closeSocket = true;
                                }
                            }
                            if (!closeSocket && sk.isWritable()) {
                                if (socketWrapper.writeOperation != null) {
                                    if (!socketWrapper.writeOperation.process()) {
                                        closeSocket = true;
                                    }
                                    //处理Socket NIO写操作 processSocket()是所属NioEndpoint实例的方法，
                                    //如果有线程池，他会将具体操作交给SocketProcessor和线程池完成,
                                    //如果没有线程池，他会将具体操作交给SocketProcessor和当前线程完成 
                                } else if (!processSocket(socketWrapper, SocketEvent.OPEN_WRITE, true)) {
                                    // 处理失败，需要关闭参数SelectionKey对应的套接字通道
                                    closeSocket = true;
                                }
                            }
                            if (closeSocket) {
                                //处理失败，需要关闭参数SelectionKey对应的套接字通道，
                                // 现在对其执行取消操作  
                                cancelledKey(sk, socketWrapper);
                            }
                        }
                    }
                } else {
                    // Invalid key
                    //invalid key，对于无效的SelectionKey，做取消操作
                    cancelledKey(sk, socketWrapper);
                }
            } catch (CancelledKeyException ckx) {
                // 出现异常，作取消操作 
                cancelledKey(sk, socketWrapper);
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                log.error(sm.getString("endpoint.nio.keyProcessingError"), t);
            }
        }

        public SendfileState processSendfile(SelectionKey sk, NioSocketWrapper socketWrapper,
                boolean calledByProcessor) {
            NioChannel sc = null;
            try {
                unreg(sk, socketWrapper, sk.readyOps());
                SendfileData sd = socketWrapper.getSendfileData();

                if (log.isTraceEnabled()) {
                    log.trace("Processing send file for: " + sd.fileName);
                }

                if (sd.fchannel == null) {
                    // Setup the file channel
                    File f = new File(sd.fileName);
                    @SuppressWarnings("resource") // Closed when channel is closed
                    FileInputStream fis = new FileInputStream(f);
                    sd.fchannel = fis.getChannel();
                }

                // Configure output channel
                sc = socketWrapper.getSocket();
                // TLS/SSL channel is slightly different
                WritableByteChannel wc = ((sc instanceof SecureNioChannel) ? sc : sc.getIOChannel());

                // We still have data in the buffer
                if (sc.getOutboundRemaining() > 0) {
                    if (sc.flushOutbound()) {
                        socketWrapper.updateLastWrite();
                    }
                } else {
                    long written = sd.fchannel.transferTo(sd.pos, sd.length, wc);
                    if (written > 0) {
                        sd.pos += written;
                        sd.length -= written;
                        socketWrapper.updateLastWrite();
                    } else {
                        // Unusual not to be able to transfer any bytes
                        // Check the length was set correctly
                        if (sd.fchannel.size() <= sd.pos) {
                            throw new IOException(sm.getString("endpoint.sendfile.tooMuchData"));
                        }
                    }
                }
                if (sd.length <= 0 && sc.getOutboundRemaining()<=0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Send file complete for: " + sd.fileName);
                    }
                    socketWrapper.setSendfileData(null);
                    try {
                        sd.fchannel.close();
                    } catch (Exception ignore) {
                    }
                    // For calls from outside the Poller, the caller is
                    // responsible for registering the socket for the
                    // appropriate event(s) if sendfile completes.
                    if (!calledByProcessor) {
                        switch (sd.keepAliveState) {
                        case NONE: {
                            if (log.isDebugEnabled()) {
                                log.debug("Send file connection is being closed");
                            }
                            poller.cancelledKey(sk, socketWrapper);
                            break;
                        }
                        case PIPELINED: {
                            if (log.isDebugEnabled()) {
                                log.debug("Connection is keep alive, processing pipe-lined data");
                            }
                            if (!processSocket(socketWrapper, SocketEvent.OPEN_READ, true)) {
                                poller.cancelledKey(sk, socketWrapper);
                            }
                            break;
                        }
                        case OPEN: {
                            if (log.isDebugEnabled()) {
                                log.debug("Connection is keep alive, registering back for OP_READ");
                            }
                            reg(sk, socketWrapper, SelectionKey.OP_READ);
                            break;
                        }
                        }
                    }
                    return SendfileState.DONE;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("OP_WRITE for sendfile: " + sd.fileName);
                    }
                    if (calledByProcessor) {
                        add(socketWrapper, SelectionKey.OP_WRITE);
                    } else {
                        reg(sk, socketWrapper, SelectionKey.OP_WRITE);
                    }
                    return SendfileState.PENDING;
                }
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Unable to complete sendfile request:", e);
                }
                if (!calledByProcessor && sc != null) {
                    poller.cancelledKey(sk, socketWrapper);
                }
                return SendfileState.ERROR;
            } catch (Throwable t) {
                log.error(sm.getString("endpoint.sendfile.error"), t);
                if (!calledByProcessor && sc != null) {
                    poller.cancelledKey(sk, socketWrapper);
                }
                return SendfileState.ERROR;
            }
        }

        protected void unreg(SelectionKey sk, NioSocketWrapper socketWrapper, int readyOps) {
            // This is a must, so that we don't have multiple threads messing with the socket
            reg(sk, socketWrapper, sk.interestOps() & (~readyOps));
        }

        protected void reg(SelectionKey sk, NioSocketWrapper socketWrapper, int intops) {
            sk.interestOps(intops);
            socketWrapper.interestOps(intops);
        }
        //超时处理
        protected void timeout(int keyCount, boolean hasEvents) {
            long now = System.currentTimeMillis();
            // Poller线程的每个运行循环loop中都会调用该方法，但是不要每个循环loop中都要真正处理超时，因为这会增加很多工作量，而且已经发生了的超时timeout稍微多等个几秒钟也能承受 但是，在以下几种情况下必须要处理超时 : 
            // This method is called on every loop of the Poller. Don't process
            // timeouts on every loop of the Poller since that would create too
            // much load and timeouts can afford to wait a few seconds.
            // However, do process timeouts if any of the following are true:
            // - the selector simply timed out (suggests there isn't much load)
            // - the nextExpiration time has passed
            // - the server socket is being closed
            if (nextExpiration > 0 && (keyCount > 0 || hasEvents) && (now < nextExpiration) && !close) {
                //判断是否不需要处理超时，不需要处理的话直接返回
                return;
            }
            int keycount = 0;
            try {
                for (SelectionKey key : selector.keys()) {
                    keycount++;
                    try {
                        NioSocketWrapper socketWrapper = (NioSocketWrapper) key.attachment();
                        if (socketWrapper == null) {
                            // We don't support any keys without attachments
                            // 取消SelectionKey:没有附件的key不支持，关闭其对应的socket
                            cancelledKey(key, null);
                        } else if (close) {
                            key.interestOps(0);
                            // Avoid duplicate stop calls
                            //要关闭服务了
                            socketWrapper.interestOps(0);
                            cancelledKey(key, socketWrapper);
                        } else if ((socketWrapper.interestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ ||
                                  (socketWrapper.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
                             
                            //正常服务状态下，感兴趣的操作有读写操作，现在检查是否有超时发生 
                            boolean readTimeout = false;
                            boolean writeTimeout = false;
                            // Check for read timeout
                            //检查是否读超时
                            if ((socketWrapper.interestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                                long delta = now - socketWrapper.getLastRead();
                                long timeout = socketWrapper.getReadTimeout();
                                if (timeout > 0 && delta > timeout) {
                                    readTimeout = true;
                                }
                            }
                            // Check for write timeout
                            //检查是否写超时
                            if (!readTimeout && (socketWrapper.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
                                long delta = now - socketWrapper.getLastWrite();
                                long timeout = socketWrapper.getWriteTimeout();
                                if (timeout > 0 && delta > timeout) {
                                    writeTimeout = true;
                                }
                            }
                            // 如果发生了读超时或者写超时，调用所属NioEndpoint的processSocket()
                            // 处理 SocketTimeoutException异常
                            if (readTimeout || writeTimeout) {
                                key.interestOps(0);
                                // Avoid duplicate timeout calls
                                socketWrapper.interestOps(0);
                                socketWrapper.setError(new SocketTimeoutException());
                                if (readTimeout && socketWrapper.readOperation != null) {
                                    if (!socketWrapper.readOperation.process()) {
                                        cancelledKey(key, socketWrapper);
                                    }
                                } else if (writeTimeout && socketWrapper.writeOperation != null) {
                                    if (!socketWrapper.writeOperation.process()) {
                                        cancelledKey(key, socketWrapper);
                                    }
                                } else if (!processSocket(socketWrapper, SocketEvent.ERROR, true)) {
                                    cancelledKey(key, socketWrapper);
                                }
                            }
                        }
                    } catch (CancelledKeyException ckx) {
                        // 处理失败，处理取消的SelectionKey,关闭其连接
                        cancelledKey(key, (NioSocketWrapper) key.attachment());
                    }
                }
            } catch (ConcurrentModificationException cme) {
                // See https://bz.apache.org/bugzilla/show_bug.cgi?id=57943
                log.warn(sm.getString("endpoint.nio.timeoutCme"), cme);
            }
            // For logging purposes only
            long prevExp = nextExpiration;
            nextExpiration = System.currentTimeMillis() +
                    socketProperties.getTimeoutInterval();
            if (log.isTraceEnabled()) {
                log.trace("timeout completed: keys processed=" + keycount +
                        "; now=" + now + "; nextExpiration=" + prevExp +
                        "; keyCount=" + keyCount + "; hasEvents=" + hasEvents +
                        "; eval=" + ((now < prevExp) && (keyCount>0 || hasEvents) && (!close) ));
            }

        }
    }
    
```



### PollerEvent //往Poller对象的事件队列插入的待处理的事件的抽象，可以被Poller缓存循环回收利用以避免GC成本

```
public static class PollerEvent {
        //待操作的 NioChannel
        private NioChannel socket;
        //在待操作的 NioChannel上所关注的操作
        private int interestOps;

        public PollerEvent(NioChannel ch, int intOps) {
            reset(ch, intOps);
        }

        public void reset(NioChannel ch, int intOps) {
            socket = ch;
            interestOps = intOps;
        }

        public NioChannel getSocket() {
            return socket;
        }

        public int getInterestOps() {
            return interestOps;
        }

        public void reset() {
            reset(null, 0);
        }

        @Override
        public String toString() {
            return "Poller event: socket [" + socket + "], socketWrapper [" + socket.getSocketWrapper() +
                    "], interestOps [" + interestOps + "]";
        }
    }

```