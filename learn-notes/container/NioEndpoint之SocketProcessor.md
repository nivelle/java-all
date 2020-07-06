## SocketProcessor

### Socket NIO 发生时从Poller到SocketProcessor的调用链
```
NioEndpoint$Poller.run()
	=> processkey()
		=> NioEndpoint父类AbstractEndpoint.processSocket()
		=> createSocketProcessor() // 创建SocketProcessor,随后在Worker线程池上执行其逻辑
```

### AbstractEndpoint.processSocket
```
public boolean processSocket(SocketWrapperBase<S> socketWrapper, SocketEvent event, boolean dispatch) {
        try {
            if (socketWrapper == null) {
                return false;
            } else {
                SocketProcessorBase<S> sc = null;
                if (this.processorCache != null) {
                    // 尝试循环利用之前回收的SocketProcessor对象，如果没有可回收利用的则创建新的SocketProcessor对象
                    sc = (SocketProcessorBase)this.processorCache.pop();
                }

                if (sc == null) {
                    // 创建新的SocketProcessor对象
                    sc = this.createSocketProcessor(socketWrapper, event);
                } else {
                    //循环利用回收的SocketProcessor对象
                    sc.reset(socketWrapper, event);
                }
                //找到Worker线程池或者Worker线程
                Executor executor = this.getExecutor();
                if (dispatch && executor != null) {
                    // 如果worker线程池被设置并且要起分发：dispatch==true,则在worker线程池上执行刚刚封装的SocketProcessor的设定逻辑
                    executor.execute(sc);
                } else {
                    //否则在当前线程上直接执行刚刚封装的SocketProcessor的设定逻辑
                    sc.run();
                }

                return true;
            }
        } catch (RejectedExecutionException var6) {
            this.getLog().warn(sm.getString("endpoint.executor.fail", new Object[]{socketWrapper}), var6);
            return false;
        } catch (Throwable var7) {
            ExceptionUtils.handleThrowable(var7);
            this.getLog().error(sm.getString("endpoint.process.fail"), var7);
            return false;
        }
    }
```

#### NioEndpoint重写了父类AbstractEndpoint的方法createSocketProcessor

```
@Override
    protected SocketProcessorBase<NioChannel> createSocketProcessor(
            SocketWrapperBase<NioChannel> socketWrapper, SocketEvent event) {
        return new SocketProcessor(socketWrapper, event);
    }

```
### SocketProcessor

```
 protected class SocketProcessor extends SocketProcessorBase<NioChannel> {

        public SocketProcessor(SocketWrapperBase<NioChannel> socketWrapper, SocketEvent event) {
            super(socketWrapper, event);
        }

        @Override
        protected void doRun() {
            // 该方法将会执行于 tomcat 的worker线程中，比如 http-nio-8080-exec-1
            // 获取待处理的客户端请求
            NioChannel socket = socketWrapper.getSocket();
            SelectionKey key = socket.getIOChannel().keyFor(socket.getSocketWrapper().getPoller().getSelector());
            Poller poller = NioEndpoint.this.poller;
            if (poller == null) {
                socketWrapper.close();
                return;
            }

            try {
                // 这里的handshake是用来处理https的握手过程
                // 如果是http不需要该握手阶段，下面会将该标志设置为0，表示握手已经完成
                int handshake = -1;

                try {
                    if (key != null) {
                        if (socket.isHandshakeComplete()) {
                            // No TLS handshaking required. Let the handler
                            // process this socket / event combination.
                            handshake = 0;
                        } else if (event == SocketEvent.STOP || event == SocketEvent.DISCONNECT ||
                                event == SocketEvent.ERROR) {
                            // Unable to complete the TLS handshake. Treat it as
                            // if the handshake failed.
                            handshake = -1;
                        } else {
                            handshake = socket.handshake(key.isReadable(), key.isWritable());
                            // The handshake process reads/writes from/to the
                            // socket. status may therefore be OPEN_WRITE once
                            // the handshake completes. However, the handshake
                            // happens when the socket is opened so the status
                            // must always be OPEN_READ after it completes. It
                            // is OK to always set this as it is only used if
                            // the handshake completes.
                            event = SocketEvent.OPEN_READ;
                        }
                    }
                } catch (IOException x) {
                    handshake = -1;
                    if (log.isDebugEnabled()) log.debug("Error during SSL handshake",x);
                } catch (CancelledKeyException ckx) {
                    handshake = -1;
                }
                if (handshake == 0) {
                    //处理握手完成或者不需要握手的情况
                    SocketState state = SocketState.OPEN;
                    // Process the request from this socket
                    if (event == null) {
                        //默认是读事件处理 这里的getHandler()返回AbstractProtocol.ConnectionHandler,
                        //在Http11NioProtocol对象构造期间被创建并设置到当前NioEndpoint对象                      
                        state = getHandler().process(socketWrapper, SocketEvent.OPEN_READ);
                    } else {
                        //响应指定事件处理，这里的getHandler()返回AbstractProtocol.ConnectionHandler,在Http11NioProtocol对象构造期间被创建并设置到当前NioEndpoint对象
                        state = getHandler().process(socketWrapper, event);
                    }
                    if (state == SocketState.CLOSED) {
                        poller.cancelledKey(key, socketWrapper);
                    }
                } else if (handshake == -1 ) {
                    getHandler().process(socketWrapper, SocketEvent.CONNECT_FAIL);
                    poller.cancelledKey(key, socketWrapper);
                } else if (handshake == SelectionKey.OP_READ){
                    socketWrapper.registerReadInterest();
                } else if (handshake == SelectionKey.OP_WRITE){
                    socketWrapper.registerWriteInterest();
                }
            } catch (CancelledKeyException cx) {
                poller.cancelledKey(key, socketWrapper);
            } catch (VirtualMachineError vme) {
                ExceptionUtils.handleThrowable(vme);
            } catch (Throwable t) {
                log.error(sm.getString("endpoint.processing.fail"), t);
                poller.cancelledKey(key, socketWrapper);
            } finally {
                socketWrapper = null;
                event = null;
                //return to cache
                if (running && !paused && processorCache != null) {
                    processorCache.push(this);
                }
            }
        }
    }


```