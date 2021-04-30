### NioEventLoop

[![gAv90A.png](https://z3.ax1x.com/2021/04/30/gAv90A.png)](https://imgtu.com/i/gAv90A)

#### 单线程模型的Netty

``````
EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1);
ServerBootstrap serverBootstrap = new ServerBootstrap();
serverBootstrap.group(bossEventLoopGroup);
``````

#### 多线程模型的Netty

````
EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(128);
ServerBootstrap serverBootstrap = new ServerBootstrap();
serverBootstrap.group(bossEventLoopGroup);
````

#### 主从多线程模型

````
 EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1);
 EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
 ServerBootstrap serverBootstrap = new ServerBootstrap();
 serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup);
````

### 初始化步骤

- EventLoopGroup (MultithreadEventExecutorGroup) 内部维护一个属性为EventExecutor的children的数组，大小是nThreads,这样就初始化了一个线程池

- 实例化NioEventLoopGroup 时，如果指定线程池大小，则nThreads就是指定的值，否则是CPU核数乘以2

- 在MultithreadEventExecutorGroup 中调用newChild()抽象方法来初始化children数组

- newChild()抽象方法实际上是在NioEventLoop中实现的，由它返回一个NioEventLoop实例

### 任务执行者：EventLoop

[![gAzCJP.png](https://z3.ax1x.com/2021/04/30/gAzCJP.png)](https://imgtu.com/i/gAzCJP)

- 作为I/O 线程，执行与Channel相关的IO操作，包括调用Selector等待就绪的IO事件、读写数据与数据处理等；

- 作为任务队列，执行taskQueue中的任务

- 每个channel 都有且仅有一个EventLoop与之关联，AbstractUnsafe.register()会将一个EventLoop赋值给AbstractChannel内部的eventLoop属性，完成关联

-------

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

                        // 'wakenUp.compareAndSet(false, true)' is always evaluated
                        // before calling 'selector.wakeup()' to reduce the wake-up
                        // overhead. (Selector.wakeup() is an expensive operation.)
                        //
                        // However, there is a race condition in this approach.
                        // The race condition is triggered when 'wakenUp' is set to
                        // true too early.
                        //
                        // 'wakenUp' is set to true too early if:
                        // 1) Selector is waken up between 'wakenUp.set(false)' and
                        //    'selector.select(...)'. (BAD)
                        // 2) Selector is waken up between 'selector.select(...)' and
                        //    'if (wakenUp.get()) { ... }'. (OK)
                        //
                        // In the first case, 'wakenUp' is set to true and the
                        // following 'selector.select(...)' will wake up immediately.
                        // Until 'wakenUp' is set to false again in the next round,
                        // 'wakenUp.compareAndSet(false, true)' will fail, and therefore
                        // any attempt to wake up the Selector will fail, too, causing
                        // the following 'selector.select(...)' call to block
                        // unnecessarily.
                        //
                        // To fix this problem, we wake up the selector again if wakenUp
                        // is true immediately after selector.select(...).
                        // It is inefficient in that it wakes up the selector for both
                        // the first case (BAD - wake-up required) and the second case
                        // (OK - no wake-up required).

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

### EventLoop的启动

- NioEventLoop 本身是一个SingleThreadExecutor，因此NioEventLoop的启动，其实就是NioEventLoop所绑定的本地java线程的启动

- 一个NioEventLoop对象其实就是和一个特定的线程进行绑定，并且在NioEventLoop生命周期内，其绑定的线程都不会再改变

````
public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task");
        }

        boolean inEventLoop = inEventLoop();
        addTask(task);
        if (!inEventLoop) {
            startThread();
            if (isShutdown()) {
                boolean reject = false;
                try {
                    if (removeTask(task)) {
                        reject = true;
                    }
                } catch (UnsupportedOperationException e) {
                    // The task queue does not support removal so the best thing we can do is to just move on and
                    // hope we will be able to pick-up the task before its completely terminated.
                    // In worst case we will log on termination.
                }
                if (reject) {
                    reject();
                }
            }
        }

        if (!addTaskWakesUp && wakesUpForTask(task)) {
            wakeup(inEventLoop);
        }
    }

````

