

## Executor

```
public interface Executor {
    ## 执行无返回值任务
    void execute(Runnable command);
}
```
## ExecutorService 是Executor的次级接口,扩展了一些功能; AbstractExecutorService 实现了 ExecutorService 接口,通过模版方法的方式提供了一些基础实现
```
public interface ExecutorService extends Executor {
    ## 关闭线程池，不再接受新任务，但已经提交的任务会执行完成
    void shutdown();
    ## 立即关闭线程池，尝试停止正在运行的任务，未执行的任务将不再执行;被迫停止及未执行的任务将以列表的形式返回
    List<Runnable> shutdownNow();
    ## 检查线程池是否已关闭
    boolean isShutdown();
    ## 检查线程池是否已终止，只有在shutdown()或shutdownNow()之后调用才有可能为true
    boolean isTerminated();
    ## 在指定时间内线程池达到终止状态了才会返回true
    boolean awaitTermination(long timeout, TimeUnit unit)throws InterruptedException;
    ## 执行有返回值的任务，任务的返回值为task.call()的结果
    <T> Future<T> submit(Callable<T> task);
    ##  执行有返回值的任务,任务的返回值为这里传入的result;当然只有当任务执行完成了调用get()时才会返回
    <T> Future<T> submit(Runnable task, T result);
    ## 执行有返回值的任务，任务的返回值为null;当然只有当任务执行完成了调用get()时才会返回
    Future<?> submit(Runnable task);
    ## 批量执行任务，只有当这些任务都完成了这个方法才会返回
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;
    ## 在指定时间内批量执行任务，未执行完成的任务将被取消;这里的timeout是所有任务的总时间，不是单个任务的时间
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,long timeout, TimeUnit unit) throws InterruptedException;
    ## 返回任意一个已完成任务的执行结果，未执行完成的任务将被取消
    <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;
    ## 在指定时间内如果有任务已完成，则返回任意一个已完成任务的执行结果，未执行完成的任务将被取消
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
```

## ScheduledExecutorService 对 ExecutorService接口的拓展,增加了一些 定时任务相关的方法

```
public interface ScheduledExecutorService extends ExecutorService {

    ## 在指定延时后执行一次,无返回值
    public ScheduledFuture<?> schedule(Runnable command,long delay, TimeUnit unit);

    ## 在指定延时后执行一次,有返回值
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,long delay, TimeUnit unit);

    ## 在指定延时后开始执行，并在之后以指定时间间隔重复执行（间隔不包含任务执行的时间),相当于之后的延时以任务开始计算
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,long initialDelay,long period,TimeUnit unit);

    ## 在指定延时后开始执行，并在之后以指定延时重复执行（间隔包含任务执行的时间),相当于之后的延时以任务结束计算
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,long initialDelay,long delay,TimeUnit unit);

}

```
### 核心属性

```
   ## 初始化状态和数量，状态为RUNNING，线程数为0
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

    ## int 32位，低29位用来表示线程池容量，也就是2的29次方-1
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY = (1 << COUNT_BITS) - 1;

    // 高3位用来表示线程池状态
    ## -1 << 29 = 11111111111111111111111111111111 << 29 = 11100000000000000000000000000000(前3位为111)
    private static final int RUNNING = -1 << COUNT_BITS;
    ## 0 << 29 = 00000000000000000000000000000000 << 29 = 00000000000000000000000000000000(前3位为000)
    private static final int SHUTDOWN = 0 << COUNT_BITS;
    ## 1 << 29 = 00000000000000000000000000000001 << 29 = 00100000000000000000000000000000(前3位为001)
    private static final int STOP = 1 << COUNT_BITS;
    ## 2 << 29 = 00000000000000000000000000000010 << 29 = 01000000000000000000000000000000(前3位为010)
    private static final int TIDYING = 2 << COUNT_BITS;
    ## 3 << 29 = 00000000000000000000000000000011 << 29 = 01100000000000000000000000000000(前3位为011)
    private static final int TERMINATED = 3 << COUNT_BITS;

    ## 得到状态，CAPACITY的非操作得到的二进制位11100000000000000000000000000000，然后做在一个与操作，相当于直接取前3位的的值
    private static int runStateOf(int c) {
        return c & ~CAPACITY;
    }
    ## 得到线程数，也就是后29位的数字. 
    ## 直接跟CAPACITY做一个与操作即可，CAPACITY就是的值就 1 << 29 - 1 = 00011111111111111111111111111111。 
    ## 与操作的话前面3位肯定为0，相当于直接取后29位的值
    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }
    ## 或操作。相当于更新数量和状态两个操作
    private static int ctlOf(int rs, int wc) {
        return rs | wc;
    }

```

### 线程池状态

```
    ## 可以接受新的任务，也可以处理阻塞队列里的任务
    private static final int RUNNING    = -1 << COUNT_BITS;
    ## 不接受新的任务，但是可以处理阻塞队列里的任务
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    ## 不接受新的任务，不处理阻塞队列里的任务，中断正在处理的任务
    private static final int STOP       =  1 << COUNT_BITS;
    ## 过渡状态，也就是说所有的任务都执行完了，当前线程池已经没有有效的线程，这个时候线程池的状态将会TIDYING，并且将要调用terminated方法
    private static final int TIDYING    =  2 << COUNT_BITS;
    ## 终止状态。terminated方法调用完成以后的状态
    private static final int TERMINATED =  3 << COUNT_BITS;

```

### ThreadPoolExecutor.execute(Runnable command)

```
## 提交任务，任务并非立即执行
public void execute(Runnable command) {
        ## 任务不能为空
        if (command == null){
            throw new NullPointerException();
        }
        ## 控制变量（高3位存储状态，低29位存储工作线程的数量
        int c = ctl.get();
        ## 如果工作线程数量小于核心数量
        if (workerCountOf(c) < corePoolSize) {
            ## 重新获取下控制变量(交给核心线程)
            if (addWorker(command, true)){
                return;
            }
            ## 重新获取控制变量
            c = ctl.get();
        }
        ## 如果达到了核心数量且线程池是运行状态，任务入队列(线程池的线程大小比基本大小要大，并且线程池还在RUNNING状态，阻塞队列也没满的情况，加到阻塞队列里)
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            ## 再次检查线程池状态，如果不是运行状态，就移除任务并执行拒绝策略
            if (! isRunning(recheck) && remove(command)){
                reject(command);
            }
            ## 容错检查工作线程数量是否为0，如果为0就创建一个(这个时候可能突然线程池关闭了，所以再做一层判断)
            else if (workerCountOf(recheck) == 0){
                addWorker(null, false);
            }
        }
        ## 任务入队列失败，尝试创建非核心工作线程
        else if (!addWorker(command, false)){
            reject(command);
        }
    }
    
## 这个方法主要用来创建一个工作线程，并启动之，其中会做线程池状态、工作线程数量等各种检测。    
private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            ## 获取控制变量
            int c = ctl.get();
            ## 获取运行状态
            int rs = runStateOf(c);
            // Check if queue empty only if necessary.
            ## 条件1:线程池不在RUNNING状态并且状态是STOP、TIDYING或TERMINATED中的任意一种状态
            ## 条件2:线程池不在RUNNING状态，线程池接受了新的任务
            ## 条件3:线程池不在RUNNING状态，阻塞队列为空
            if (rs >= SHUTDOWN && ! (rs == SHUTDOWN && firstTask == null && ! workQueue.isEmpty())){
                return false;
            }
            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    int rs = runStateOf(ctl.get());

                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

``` 

来自: [彤哥读源码](https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247483746&idx=1&sn=a6b5bea0cb52f23e93dd223970b2f6f9&scene=21#wechat_redirect)
























