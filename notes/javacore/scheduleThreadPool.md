
### 核心接口

#### Executor

```
public interface Executor {
    // 执行无返回值任务,任务提交
    void execute(Runnable command);
}
```
#### ExecutorService 是Executor的次级接口,扩展了一些功能; AbstractExecutorService 实现了 ExecutorService 接口,通过模版方法的方式提供了一些基础实现
```
public interface ExecutorService extends Executor {
    // 关闭线程池,不再接受新任务,但已经提交的任务会执行完成
    void shutdown();
    //立即关闭线程池，尝试停止正在运行的任务，未执行的任务将不再执行;被迫停止及未执行的任务将以列表的形式返回
    List<Runnable> shutdownNow();
    // 检查线程池是否已关闭
    boolean isShutdown();
    // 检查线程池是否已终止，只有在shutdown()或shutdownNow()之后调用才有可能为true
    boolean isTerminated();
    // 在指定时间内线程池达到终止状态了才会返回true
    boolean awaitTermination(long timeout, TimeUnit unit)throws InterruptedException;
    // 执行有返回值的任务,任务的返回值为task.call()的结果
    <T> Future<T> submit(Callable<T> task);
    //  执行有返回值的任务,任务的返回值为这里传入的result;当然只有当任务执行完成了调用get()时才会返回
    <T> Future<T> submit(Runnable task, T result);
    // 执行有返回值的任务，任务的返回值为null;当然只有当任务执行完成了调用get()时才会返回
    Future<?> submit(Runnable task);
    // 批量执行任务，只有当这些任务都完成了这个方法才会返回
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;
    // 在指定时间内批量执行任务，未执行完成的任务将被取消;这里的timeout是所有任务的总时间，不是单个任务的时间
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,long timeout, TimeUnit unit) throws InterruptedException;
    // 返回任意一个已完成任务的执行结果，未执行完成的任务将被取消
    <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;
    // 在指定时间内如果有任务已完成，则返回任意一个已完成任务的执行结果，未执行完成的任务将被取消
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
```

#### ScheduledExecutorService 对 ExecutorService接口的拓展,增加了一些 定时任务相关的方法

```
public interface ScheduledExecutorService extends ExecutorService {

    // 在指定延时后执行一次,无返回值
    public ScheduledFuture<?> schedule(Runnable command,long delay, TimeUnit unit);

    // 在指定延时后执行一次,有返回值
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,long delay, TimeUnit unit);

    //在指定延时后开始执行,并在之后以指定时间间隔重复执行（间隔不包含任务执行的时间),上一个任务开始的时间计时,period时间过去后,
    //检测上一个任务是否执行完毕,如果上一个任务执行完毕,则当前任务立即执行，如果上一个任务没有执行完毕，则需要等上一个任务执行完毕后立即执行
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,long initialDelay,long period,TimeUnit unit);

    //在指定延时后开始执行，并在之后以指定延时重复执行（间隔包含任务执行的时间),以上一个任务结束时开始计时,period时间过去后,立即执行
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,long initialDelay,long delay,TimeUnit unit);

}

```

## 核心属性

```
    //初始化状态和数量，状态为RUNNING，线程数为0
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

    //int 32位，低29位用来表示线程池容量，也就是2的29次方-1
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY = (1 << COUNT_BITS) - 1;

    // 高3位用来表示线程池状态
    // -1 << 29 = 11111111111111111111111111111111 << 29 = 11100000000000000000000000000000(前3位为111)
    private static final int RUNNING = -1 << COUNT_BITS;
    // 0 << 29 = 00000000000000000000000000000000 << 29 = 00000000000000000000000000000000(前3位为000)
    private static final int SHUTDOWN = 0 << COUNT_BITS;
    // 1 << 29 = 00000000000000000000000000000001 << 29 = 00100000000000000000000000000000(前3位为001)
    private static final int STOP = 1 << COUNT_BITS;
    // 2 << 29 = 00000000000000000000000000000010 << 29 = 01000000000000000000000000000000(前3位为010)
    private static final int TIDYING = 2 << COUNT_BITS;
    // 3 << 29 = 00000000000000000000000000000011 << 29 = 01100000000000000000000000000000(前3位为011)
    private static final int TERMINATED = 3 << COUNT_BITS;

    //得到状态，CAPACITY 的非操作得到的二进制位11100000000000000000000000000000，然后做在一个与操作，相当于直接取前3位的的值
    private static int runStateOf(int c) {
        return c & ~CAPACITY;
    }
    //得到线程数，也就是后29位的数字. 
    //直接跟CAPACITY做一个与操作即可，CAPACITY就是的值就 1 << 29 - 1 = 00011111111111111111111111111111。 
    //与操作的话前面3位肯定为0，相当于直接取后29位的值
    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }
    //或操作。相当于更新 数量和状态两个操作
    private static int ctlOf(int rs, int wc) {
        return rs | wc;
    }
    
```

#### 内部类: Worker（包装了任务和线程）

```
private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
        //真正工作的线程
        final Thread thread;
        //初始化任务，通过构造函数传进来
        Runnable firstTask;
        //完成任务数
        volatile long completedTasks;
        //构造方法
        Worker(Runnable firstTask) {
            //把状态位设置成-1，这样任何线程都不能得到Worker的锁，除非调用了unlock方法。
            //这个unlock方法会在runWorker方法中一开始就调用,这是为了确保Worker构造出来之后,没有任何线程能够得到它的锁,除非调用了runWorker之后,其他线程才能获得Worker的锁
            //防止它在执行前被中断,至于中断时是怎么判断的
            setState(-1); // inhibit【抑制】 interrupts until runWorker
            this.firstTask = firstTask;
            //这里把 Worker本身作为Runnable传给线程(worker就是线程要执行的任务)
            //worker 重写了Runable的run方法,也就是用的是worker的runWorker方法
            this.thread = getThreadFactory().newThread(this);
        }
        /** Delegates main run loop to outer runWorker  */
        public void run() {
            runWorker(this);
        }

        // Lock methods
        //
        // The value 0 represents the unlocked state.
        // The value 1 represents the locked state.

        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }

```

#### 线程池状态

```
    //可以接受新的任务，也可以处理阻塞队列里的任务
    private static final int RUNNING    = -1 << COUNT_BITS;
    // 不接受新的任务,但是可以处理阻塞队列里的任务
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    // 不接受新的任务,不处理阻塞队列里的任务，中断正在处理的任务
    private static final int STOP       =  1 << COUNT_BITS;
    // 过渡状态,也就是说所有的任务都执行完了，当前线程池已经没有有效的线程，这个时候线程池的状态将会TIDYING，并且将要调用terminated方法
    private static final int TIDYING    =  2 << COUNT_BITS;
    // 终止状态。terminated方法调用完成以后的状态
    private static final int TERMINATED =  3 << COUNT_BITS;

```


### 核心方法

#### ThreadPoolExecutor.execute(Runnable command) 任务提交

```
//提交任务,任务并非立即执行
public void execute(Runnable command) {
        //任务不能为空
        if (command == null){
            throw new NullPointerException();
        }
        // 控制变量（高3位存储状态，低29位存储工作线程的数量）
        int c = ctl.get();
        //如果工作线程数量小于核心数量
        if (workerCountOf(c) < corePoolSize) {
            //提交任务
            if (addWorker(command, true)){
                return;
            }
            //重新获取控制变量
            c = ctl.get();
        }
        //如果达到了核心数量且线程池是运行状态,任务入队列(线程池的线程大小比核心线程大小要大,并且线程池还在RUNNING状态,阻塞队列也没满的情况,加到阻塞队列里)
        if (isRunning(c) && workQueue.offer(command)) {
            // 获取控制变量（线程数）
            int recheck = ctl.get();
            //再次检查线程池状态,如果不是运行状态,就移除任务并执行拒绝策略
            if (! isRunning(recheck) && remove(command)){
                reject(command);
            }
            //容错检查工作线程数量是否为0，如果为0就创建一个(这个时候可能突然线程池关闭了，所以再做一层判断)
            else if (workerCountOf(recheck) == 0){
                //创建一个线程
                addWorker(null, false);
            }
        }
        //任务入队列失败，尝试创建非核心工作线程。如果创建失败则执行拒绝策略 (看第二个参数：true 是加核心线程，false和非核心工作线程)
        else if (!addWorker(command, false)){
            reject(command);//有两次会执行决绝策略
        }
    }
 
```   
#### 这个方法主要用来创建一个工作线程,并启动之,其中会做线程池状态、工作线程数量等各种检测。 
#### 第二个参数为true表示创建核心线程,false表示创建非核心线程

```  
private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
             // 获取控制变量
            int c = ctl.get();
             // 获取运行状态
            int rs = runStateOf(c);
            // Check if queue empty only if necessary.
            // 条件1:线程池不在RUNNING状态并且状态是STOP、TIDYING 或 TERMINATED中的任意一种状态            
            // 条件2:线程池不在RUNNING状态,线程池接受了新的任务
            // 条件3:线程池不在RUNNING状态,阻塞队列为空
            
            // 满足这3个条件中的任意一个的话,拒绝执行任务
            if (rs >= SHUTDOWN && ! (rs == SHUTDOWN && firstTask == null && ! workQueue.isEmpty())){
                return false;
            }
            for (;;) {
                // 线程池线程个数 
                int wc = workerCountOf(c);
                // 如果线程池线程数量超过线程池最大容量或者线程数量超过了基本大小(core参数为true，core参数为false的话判断超过最大大小)
                if (wc >= CAPACITY || wc >= (core ? corePoolSize : maximumPoolSize)){
                    return false;
                }
                // 没有超过各种大小的话,cas操作线程池线程数量+1,cas成功的话跳出循环
                if (compareAndIncrementWorkerCount(c)){
                    // 跳出retry标记的循环
                    break retry;
                }
                // 获取当前状态变量
                c = ctl.get();
                // 如果增加线程数量1失败且如果状态改变了,重新循环操作
                if (runStateOf(c) != rs){
                    // 从retry开始再次执行循环
                    continue retry;
                }
            }
        }
        ## 走到这一步说明cas操作成功了，线程池线程数量+1(addWorker调用前提是已经判定能添加)
        
        //任务是否成功启动标识
        boolean workerStarted = false;
        //任务是否添加成功标识
        boolean workerAdded = false;

        Worker w = null;
        try {
            //基于任务firstTask构造worker,这个线程第一个处理的任务
            w = new Worker(firstTask);
            // 使用Worker的属性thread，这个thread是使用ThreadFactory构造出来的
            final Thread t = w.thread;
            // ThreadFactory构造出的Thread有可能是null，做个判断
            if (t != null) {
                //得到线程池的可重入锁 
                final ReentrantLock mainLock = this.mainLock;
                // 锁住，防止并发
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    // 在锁住之后再重新检测一下状态
                    int rs = runStateOf(ctl.get());
                    // 如果线程池在RUNNING状态 或者线程池在SHUTDOWN状态并且任务是个null
                    if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
                        // 判断线程是否还活着，也就是说线程已经启动并且还没死掉
                        if (t.isAlive()) {
                            //如果存在已经启动并且还没死的线程,抛出异常
                            throw new IllegalThreadStateException();
                        }
                        //worker添加到线程池的 workers 属性中，是个HashSet
                        workers.add(w);
                        // 得到目前线程池中的线程个数
                        int s = workers.size();
                        // 如果线程池中的线程个数超过了线程池中的最大线程数时，更新一下这个最大线程数
                        if (s > largestPoolSize){
                             largestPoolSize = s;
                        }
                        // 标识一下任务已经添加成功
                        workerAdded = true;
                    }
                } finally {
                    // 释放锁
                    mainLock.unlock();
                }
                //如果任务添加成功,运行任务,改变一下任务成功启动标识
                if (workerAdded) {
                    // 启动线程,这里的t是 Worker 中的thread属性,所以相当于就是调用了Worker的run方法,也就是runWorder(Worker w)方法
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            // 如果任务启动失败，调用addWorkerFailed方法
            if (! workerStarted){
                addWorkerFailed(w);
            }
        }
        return workerStarted;
    }

```

#### ThreadPoolExecutor.runWorker(Worker w) 

- worker里面的线程重复使用。真正执行任务, 也就是在addWorker里面 t.start()运行的任务，这个线程的run()方法里面的任务worker

- worker 里面 的 run()方法调用的真正逻辑

````   
//private final class Worker extends AbstractQueuedSynchronizer implements Runnable
public void run() {
   runWorker(this);//入参是当前Worker实例本身
}      
````

```
final void runWorker(Worker w) {
         // 得到当前线程
        Thread wt = Thread.currentThread();
        //得到Worker中的任务task，也就是用户传入的task
        Runnable task = w.firstTask;
        //将Worker中的任务置空
        w.firstTask = null;
        //运行之前设置状态为0，也就意味着这边可以执行中断了。对应在构造函数里初始化设置为-1的状态
        /**
         *    protected boolean tryRelease(int unused) {
         *               setExclusiveOwnerThread(null);
         *               setState(0);
         *               return true;
         *           }
         *
         *
        **/
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
           // 如果worker中的任务不为空，继续执行，否则使用getTask获得任务。一直死循环，除非得到的任务为空才退出 
            while (task != null || (task = getTask()) != null) {
                // 如果拿到了任务，给自己上锁，表示当前Worker已经要开始执行任务了，已经不是闲置Worker
                w.lock();
                // 在执行任务之前先做一些处理:
                // 1. 如果线程池已经处于STOP状态并且当前线程没有被中断，中断线程 
                // 2. 如果线程池还处于RUNNING或SHUTDOWN状态,并且当前线程已经被中断了,重新检查一下线程池状态,如果处于STOP状态并且没有被中断,那么中断线程                
                if ((runStateAtLeast(ctl.get(), STOP) ||
                    //Thread.interrupted()会清除中断状态
                    (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) && !wt.isInterrupted()){
                    //中断线程
                    wt.interrupt();
                }
                try {
                    //任务执行前需要做什么，ThreadPoolExecutor是个空实现
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        //真正的开始执行任务，调用的是run方法，而不是start方法。这里run的时候可能会被中断，比如线程池调用了shutdownNow方法
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        //任务执行结束需要做什么，ThreadPoolExecutor是个空实现
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    // 记录执行任务的个数
                    w.completedTasks++;
                    //执行完任务之后，解锁，Worker变成闲置Worker
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            // 每次执行任务之后都要检查是否需要回收不需要的回收Worker方法
            processWorkerExit(w, completedAbruptly);
        }
    }
```

#### processWorkerExit(Worker w,Boolean completeAbruptly)

``` 
private void processWorkerExit(Worker w, boolean completedAbruptly) {
        // 如果Worker非正常结束流程调用processWorkerExit方法，worker数量减一。
        // 如果是正常结束的话，在getTask方法里worker数量已经减一了
        if (completedAbruptly) {// If abrupt, then workerCount wasn't adjusted（如果被中断,则需要先减少工作线程数）
            decrementWorkerCount();
        }
        final ReentrantLock mainLock = this.mainLock;
        //加锁，防止并发问题
        mainLock.lock();
        try {
            //记录总的完成任务数
            completedTaskCount += w.completedTasks;
            //线程池的worker集合删除掉需要回收的Worker
            workers.remove(w);
        } finally {
            //解锁
            mainLock.unlock();
        }
        // 尝试结束线程池
        tryTerminate();

        int c = ctl.get();
        //如果线程池还处于RUNNING或者SHUTDOWN状态,则要处理未处理完的任务
        if (runStateLessThan(c, STOP)) {
             //Worker是正常结束流程的话
            if (!completedAbruptly) {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty()){
                    min = 1;
                }
                // 线程数大于最小线程数，则不需要新开一个Worker
                if (workerCountOf(c) >= min){
                    return; // replacement not needed
                }
            }
            // 否则新开一个Worker代替原先的Worker,新开一个Worker需要满足以下3个条件中的任意一个：
            // 1. 用户执行的任务发生了异常
            // 2. Worker数量比线程池核心大小要小
           //  3. 阻塞队列不空但是没有任何Worker在工作
            addWorker(null, false);
        }
    }  
```
## 在回收Worker的时候线程池会尝试结束自己的运行，tryTerminate方法  

```
final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            // 满足3个条件中的任意一个，不终止线程池
                    // 1. 线程池还在运行，不能终止
                    // 2. 线程池处于TIDYING或TERMINATED状态,说明已经在关闭了，不允许继续处理
                    // 3. 线程池处于SHUTDOWN状态并且阻塞队列不为空，这时候还需要处理阻塞队列的任务，不能终止线程池
            if (isRunning(c) ||runStateAtLeast(c, TIDYING) || (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            //走到这一步说明线程池已经不在运行，阻塞队列已经没有任务，但是还要回收正在工作的Worker
            if (workerCountOf(c) != 0) { // Eligible to terminate
                //由于线程池不运行了
                //中断闲置Worker,直到回收全部的Worker。这里没有那么暴力,只中断一个,中断之后退出方法,中断了Worker之后,Worker会回收,然后还是会调用tryTerminate方法，如果还有闲置线程，那么继续中断
                interruptIdleWorkers(ONLY_ONE);
                return;
            }
            //走到这里说明worker已经全部回收了，并且线程池已经不在运行，阻塞队列已经没有任务。可以准备结束线程池了
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {## cas操作，将线程池状态改成TIDYING
                    try {
                        //调用terminated方法
                        terminated();
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0));
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
        }
    }  
``` 


####  一个worker里面可以在执行完一个任务后置为空(task=null),然后再添加一个未执行的任

** 如果发生了以下四件事中的任意一件，那么Worker需要被回收:**

- 1. Worker个数比线程池最大大小要大

- 2. 线程池处于STOP状态

- 3. 线程池处于SHUTDOWN状态并且阻塞队列为空

- 4. 使用超时时间从阻塞队列里拿数据,并且超时之后没有拿到数据(allowCoreThreadTimeOut || workerCount > corePoolSize)

#### getTask() 获取未执行的任务
```
private Runnable getTask() {
        // 如果使用超时时间并且也没有拿到任务的标识
        boolean timedOut = false; // Did the last poll() time out?
        for (;;) {
            //获取控制变量
            int c = ctl.get();
            // 获取线程池状态
            int rs = runStateOf(c);
            // 如果线程池是 SHUTDOWN 状态并且阻塞队列为空的话，worker数量减一，直接返回null (SHUTDOWN状态还会处理阻塞队列任务，但是阻塞队列为空的话就结束了),如果线程池是STOP状态的话,worker数量减1,直接返回null(STOP状态不处理阻塞队列任务)
            // 开始回收闲置Worker（控制变量-1）
            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                //一个一个减worker
                decrementWorkerCount();
                return null;
            }
            //工作线程数量
            int wc = workerCountOf(c);
            // Are workers subject to culling?
            // 是否允许超时，有两种情况（非核心线程是一定允许超时的，这里的超时其实是指取任务超时）
            //1. 是允许核心线程数超时，这种就是说所有的线程都可能超时
            //2. 是工作线程数大于了核心数量，这种肯定是允许超时的
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
            // 超时判断
            if ((wc > maximumPoolSize || (timed && timedOut)) && (wc > 1 || workQueue.isEmpty())) {
                //超时了，减少工作线程数量，并返回null
                if (compareAndDecrementWorkerCount(c)){                    
                    return null;
                }
                //减少工作线程数量失败，则重试
                continue;
            }

            try {
                //真正取任务的地方，默认情况下，只有当工作线程数量大于核心线程数量时,才会调用poll()方法触发超时调用(poll()和take()都会获取head元素，然后从队列中删除)
                //poll(timeout, unit)方法会在超时时返回null，如果timeout<=0，队列为空时直接返回null;take()方法会一直阻塞直到取到任务或抛出中断异常。
                Runnable r = timed ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS):workQueue.take();
                if (r != null){
                    //取到任务了就正常返回
                    return r;
                }
                // 没取到任务表明超时了，回到continue那个if中返回null
                timedOut = true;
            } catch (InterruptedException retry) {
                //捕获到了中断异常
                //中断标记是在调用shutDown()或者shutDownNow()的时候设置进去的；此时，会回到for循环的第一个if处判断状态是否要返回null
                timedOut = false;
            }
        }
    }
 ```
- 如果getTask返回的是null,那说明阻塞队列已经没有任务并且当前调用getTask的Worker需要被回收，那么会调用 processWorkerExit 方法进行回收 不用的worker,；

- 在getTask里 decrementWorkerCount（）和 compareAndDecrementWorkerCount（）里是正常处理的回收操作


来自: [彤哥读源码](https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247483746&idx=1&sn=a6b5bea0cb52f23e93dd223970b2f6f9&scene=21#wechat_redirect)
