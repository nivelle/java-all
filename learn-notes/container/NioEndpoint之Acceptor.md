## AbstractEndpoint endPoint的默认实现

```
## 启动 acceptor线程
protected void startAcceptorThread() {
        acceptor = new Acceptor<>(this);
        String threadName = getName() + "-Acceptor";
        acceptor.setThreadName(threadName);
        Thread t = new Thread(acceptor, threadName);
        t.setPriority(getAcceptorThreadPriority());
        t.setDaemon(getDaemon());
        t.start();
    }
    
## 关闭套接字通道    
protected void closeSocket(U socket) {
        SocketWrapperBase<S> socketWrapper = connections.get(socket);
        if (socketWrapper != null) {
            socketWrapper.close();
        }
    }
## 关闭套接字    
public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                getEndpoint().getHandler().release(this);
            } catch (Throwable e) {
                ExceptionUtils.handleThrowable(e);
                if (log.isDebugEnabled()) {
                    log.error(sm.getString("endpoint.debug.handlerRelease"), e);
                }
            } finally {
                getEndpoint().countDownConnection();
                doClose();
            }
        }
    }
```
## public class Acceptor<U> implements Runnable

```
public void run() {

        //该方法运行在 acceptor 线程中,用来接收来自网络的客户端请求，然后封装后注册到 poller 的事件队列,最终 poller 线程将要处理的请求交给 worker 线程    
        int errorDelay = 0;

        // Loop until we receive a shutdown command
        while (endpoint.isRunning()) {

            // Loop if endpoint is paused
            // running变量是所属NioEndpoint实例是否处于运行状态的标记;
            // true 表示处于运行中，false表示处于停止服务状态;NioEndpoint实例,还有另外一个状态paused用来表示服务的暂停，比如 running==true&&
            // paused==true表示NioEndpoint实例处于运行中但暂停服务状态，running==true&&paused==false才表示NioEndpoint实例正处于有效服务状态
            while (endpoint.isPaused() && endpoint.isRunning()) {
                //启动中但是处于暂停接收请求状态
                state = AcceptorState.PAUSED;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
            // 已经被通知处于停止状态了，退出接收请求服务循环
            if (!endpoint.isRunning()) {
                break;
            }
            // 逻辑走到这里表示处于正常接收请求状态 running==true&&paused==false
            state = AcceptorState.RUNNING;

            try {
                //if we have reached max connections, wait
                // 如果还没有到达最大连接数，则当前连接数量做加一操作，如果到达最大连接数则让当前接收线程处于等待状态，直到有连接被释放
                endpoint.countUpOrAwaitConnection();
                // Endpoint might have been paused while waiting for latch
                // If that is the case, don't accept new connections
                if (endpoint.isPaused()) {
                    continue;
                }

                U socket = null;
                try {
                    // Accept the next incoming connection from the server
                    // socket
                    //从服务器套接字serverSock接收下一个进入的连接请求
                    socket = endpoint.serverSocketAccept();
                } catch (Exception ioe) {
                    // We didn't get a socket
                    //获取连接异常，前面获取连接前做了加一操作的当前连接数量；要在这里做一个减一操作,表明不占用连接数量
                    endpoint.countDownConnection();
                    if (endpoint.isRunning()) {
                        // Introduce delay if necessary
                        errorDelay = handleExceptionWithDelay(errorDelay);
                        // re-throw
                        throw ioe;
                    } else {
                        break;
                    }
                }
                // Successful accept, reset the error delay
                errorDelay = 0;

                // Configure the socket
                if (endpoint.isRunning() && !endpoint.isPaused()) {
                    // setSocketOptions() will hand the socket off to
                    // an appropriate processor if successful
                    //设置套接字的一些选项，成功的话将其交给合适的processor，当前 acceptor线程的主要目的是接收请求并委托出去，本身并不执行处理逻辑
                    //这里setSocketOptions()正是设置了套接字处理的一些参数，然后把处理工作 委托给了 poller 线程，委托完成后，该acceptor线程继续执行自己的请求
                    //监听接收和委托任务
                    if (!endpoint.setSocketOptions(socket)) {
                        //如果没有设置成功，这直接关闭套接字通道
                        endpoint.closeSocket(socket);
                    }
                } else {
                    //如果系统已经被标记为不是正常运行状态，则直接关闭套接字通道
                    endpoint.destroySocket(socket);
                }
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                String msg = sm.getString("endpoint.accept.fail");
                // APR specific.
                // Could push this down but not sure it is worth the trouble.
                if (t instanceof Error) {
                    Error e = (Error) t;
                    if (e.getError() == 233) {
                        // Not an error on HP-UX so log as a warning
                        // so it can be filtered out on that platform
                        // See bug 50273
                        log.warn(msg, t);
                    } else {
                        log.error(msg, t);
                    }
                } else {
                        log.error(msg, t);
                }
            }
        }
        state = AcceptorState.ENDED;
    }

```
## public class NioEndpoint extends AbstractJsseEndpoint<NioChannel,SocketChannel> 

### public abstract class AbstractJsseEndpoint<S,U> extends AbstractEndpoint<S,U> 

```
protected boolean setSocketOptions(SocketChannel socket) {
        NioSocketWrapper socketWrapper = null;
        try {
            // Allocate channel and wrapper
            NioChannel channel = null;
            if (nioChannels != null) {
                ## 将一个Java标准SocketChannel套接字请求通道封装成一个Tomcat的NioChannel,这样对于SSL的情况和非SSL的情况就可以采用同样的处理逻辑
                channel = nioChannels.pop();
            }
            if (channel == null) {
                SocketBufferHandler bufhandler = new SocketBufferHandler(
                        socketProperties.getAppReadBufSize(),
                        socketProperties.getAppWriteBufSize(),
                        socketProperties.getDirectBuffer());
                if (isSSLEnabled()) {
                    channel = new SecureNioChannel(bufhandler, selectorPool, this);
                } else {
                    channel = new NioChannel(bufhandler);
                }
            }
            NioSocketWrapper newWrapper = new NioSocketWrapper(channel, this);
            channel.reset(socket, newWrapper);
            connections.put(socket, newWrapper);
            socketWrapper = newWrapper;

            // Set socket properties
            // Disable blocking, polling will be used
            socket.configureBlocking(false);
            socketProperties.setProperties(socket.socket());

            socketWrapper.setReadTimeout(getConnectionTimeout());
            socketWrapper.setWriteTimeout(getConnectionTimeout());
            socketWrapper.setKeepAliveLeft(NioEndpoint.this.getMaxKeepAliveRequests());
            socketWrapper.setSecure(isSSLEnabled());
            //向Poller注册新接收到的请求套接字，委托其完成相应处理，委托完成后当前线程继续自己被设定的监听接收委托任务；
            poller.register(channel, socketWrapper);
            return true;
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            try {
                log.error(sm.getString("endpoint.socketOptionsError"), t);
            } catch (Throwable tt) {
                ExceptionUtils.handleThrowable(tt);
            }
            if (socketWrapper == null) {
                destroySocket(socket);
            }
        }
        // Tell to close the socket if needed
        return false;
    }

```