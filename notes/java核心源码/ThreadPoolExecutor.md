
---
## 问题
1. 几种常见的线程池有哪些，以及他们的使用场景
2. 线程池都有哪几种工作队列
3. 简单的描述一下线程池的工作原理

## 简介
**线程池** 是一种线程使用模式，同时也是一种池化技术。在java中，类似的池化技术有很多，例如jvm的String常量池，数据库连接池。
如果把此逻辑进行拓展，甚至连单例模式或者redis缓存也算是一种池化技术。而池化技术最初的目的，就是减少计算机的资源消耗。

JDK中的关于**并行执行任务的框架**主要有两个 Executor框架 和 fork-join框架。

对于Executor框架而言，最核心的就是ThreadPoolExecutor类。JDK提供的四大默认线程池就是由此类提供支持。

-----------
## 1. ThreadPoolExecutor 的继承关系

![ThreadPoolExecutor的继承关系图](https://gitee.com/alan-tang-tt/yuan/raw/master/%E6%AD%BB%E7%A3%95%20java%E5%B9%B6%E5%8F%91%E5%8C%85/resource/%E7%BA%BF%E7%A8%8B%E6%B1%A0/1.1%E7%BB%A7%E6%89%BF%E5%9B%BE.png)

从图内可以大致知道这样的继承关系 :

1. Executor是一个顶层接口，打开这个接口后发现只有一个方法,此方法就是用来执行传进去的任务的
```java
public interface Executor {

    /**
     * 1. 执行给定的指令(Runnable 实现类)
     * 2. 此指令将会在新开线程内 或 线程池内 或 带有返回的线程内执行
     */
    void execute(Runnable command);
}
```
2. 接下来ExecutorService接口继承了Executor接口，打开并摘取一部分重要的方法
```java
public interface ExecutorService extends Executor {
    // 线程池关闭，线程池里的线程就随它去
    void shutdown();
    
    //立刻停止！马上！正在执行的给我立马停！那些个没执行的返回来
    List<Runnable> shutdownNow();
    
    //判断线程池是不是被停了
    boolean isShutdown();

    //判断关闭（调用shutdown方法之后）线程池里面的线程是不是执行完了
    boolean isTerminated();

    //调用此方法之后就一直等待着线程池里面的线程执行完了之后，才能继续，两个参数控制等待的行为
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;

     //用来提交有返回值的线程
    <T> Future<T> submit(Callable<T> task);

    //提交一个线程，指定一个结果，当线程完成的时候返回自定义的结果
    //相当于submit(Callable<T> task) 的另外一种实现
    <T> Future<T> submit(Runnable task, T result);

    //提交一个runnable 的线程，执行完了就返回null(Future.get()之后返回)
    //返回的是一个Future，当调用get(),代码就会卡在那，等着返回
    //返回的即是是null,我也不care，目的只有“我知道这个任务已经结束了，可以开始下面的事情了”
    Future<?> submit(Runnable task);

    //相当于批量的submit(Callable<T> task)方法，一口气把想执行的callable task丢进线程池里面
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

    //在批量提交的方法上增加时间相关的参数
    //如果超时，那么list里面的某个任务会被取消
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
        throws InterruptedException;

    //提交一大堆的线程，这么多线程里面一个成功就可以，只要给我一个返回值
    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;

    //这个道理类似，就是在invokeAny(Collection<? extends Callable<T>> tasks) 上增加时间的控制
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```
3. 那么再然后就是**AbstractExecutorService**这个类基本上实现了ExecutorService的行为
   
4. 最重要的压轴类就是**ThreadPoolExecutor**，这个类继承了AbstractExecutorService类

----
## 2. ThreadPoolExecutor类的重要field


### 2.1 按照功能划分-线程池的状态

 首先奉上各种线程池状态的迁移图及其关系

![线程池的状态迁移图](https://gitee.com/alan-tang-tt/yuan/raw/master/%E6%AD%BB%E7%A3%95%20java%E5%B9%B6%E5%8F%91%E5%8C%85/resource/%E7%BA%BF%E7%A8%8B%E6%B1%A0/2.2%E7%8A%B6%E6%80%81%E8%BF%81%E7%A7%BB.png)

```java
    //AQS只提供了state一个int型变量，此时将state高16位表示为读状态，低16位表示为写状态。
    //这里的clt同样也是，它表示了两个概念：
    //1. workerCount：当前有效的线程数
    //2. runState：当前线程池的五种状态，Running、Shutdown、Stop、Tidying、Terminate。
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    //c & 高3位为1，低29位为0的~CAPACITY，用于获取高3位保存的线程池状态
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    //c & 高3位为0，低29位为1的CAPACITY，用于获取低29位的线程数量
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    //参数rs表示runState，参数wc表示workerCount，即根据runState和workerCount打包合并成ctl
    //高三位和低29位合并为一个有意义的AtomicInteger
    private static int ctlOf(int rs, int wc) { return rs | wc; }


    private static final int CAPACITY = (1 << COUNT_BITS) – 1;    //低29位表示最大线程数，2^29-1
    //定一个常量，在之后定义状态值时候使用
    private static final int COUNT_BITS = Integer.SIZE - 3;       //32-3=29，线程数量所占位数

    //线程池处在RUNNING状态时，能够接收新任务，以及对已添加的任务进行处理。
    //线程池的初始化状态是RUNNING。当线程池被一旦被创建，就处于RUNNING状态，这个时候线程池中的任务数为0
    private static final int RUNNING    = -1 << COUNT_BITS; // int型变量高3位（含符号位）101表RUNING

    //如果调用了shutdown()方法，则线程池处于SHUTDOWN状态
    //线程池处在SHUTDOWN状态时，不接收新任务，但能处理已添加的任务。
    private static final int SHUTDOWN   =  0 << COUNT_BITS; //高3位000

    //如果调用了shutdownNow()方法，则线程池处于STOP状态
    //线程池处在STOP状态时，不接收新任务，不处理已添加的任务，并且会中断正在处理的任务。
    private static final int STOP       =  1 << COUNT_BITS; ////高3位001

    //当所有的任务已终止，当前类记录的”任务数量”为0，线程池会变为TIDYING状态。当线程池变为TIDYING状态时，会执行钩子函数terminated()
    private static final int TIDYING    =  2 << COUNT_BITS; //高3位010

    //线程池彻底终止，就变成TERMINATED状态。 
    private static final int TERMINATED =  3 << COUNT_BITS; //高3位011


```

### 2.2 线程池的执行方法相关的核心field

![ThreadPoolExecutor类的全部field](https://gitee.com/alan-tang-tt/yuan/raw/master/%E6%AD%BB%E7%A3%95%20java%E5%B9%B6%E5%8F%91%E5%8C%85/resource/%E7%BA%BF%E7%A8%8B%E6%B1%A0/2.1%E7%B1%BB%E4%B8%8Bfield.png)
在这里面我们挑选重要的成员变量进行说明
```java
    //以下6个成员变量直接由构造方法传入
    //1.核心池的大小，当线程池中的线程数目大于这个参数时，提交的任务会被放进任务缓存队列
    private volatile int corePoolSize;
    //2.线程池最大能容忍的线程数
    private volatile int maximumPoolSize;
    //3.任务缓存队列，用来存放等待执行的任务
    private final BlockingQueue<Runnable> workQueue;
    //4.线程存活时间
    private volatile long keepAliveTime;
    //5.线程工厂，用来创建线程
    private volatile ThreadFactory threadFactory;
    //6.任务拒绝策略
    private volatile RejectedExecutionHandler handler;
    
    //用来记录已经执行完毕的任务个数
    private long completedTaskCount;
    //线程池中当前的线程数
    private volatile int poolSize;
    //是否允许为核心线程设置存活时间
    private volatile boolean allowCoreThreadTimeOut;
    //用来存放工作线程的set集
    private final HashSet<Worker> workers = new HashSet<Worker>();
    //线程池的主要状态锁 很多方法强烈依赖于这个锁
    private final ReentrantLock mainLock = new ReentrantLock();     

```

### 2.3 线程池的辅助性支持field与inner class,一些相关的方法
```java
//这是个线程包装类，时时刻刻在运行，不断地从任务列表搞任务执行
private final class Worker extends AbstractQueuedSynchronizer implements Runnable {...}
//以下四个inner类可以观察到都实现了RejectedExecutionHandler接口，这就是拒绝策略实体类
//是直接定义到ThreadPoolExecutor类内的
public static class CallerRunsPolicy implements RejectedExecutionHandler {...}
public static class AbortPolicy implements RejectedExecutionHandler {...}
public static class DiscardPolicy implements RejectedExecutionHandler {...}
public static class DiscardOldestPolicy implements RejectedExecutionHandler {...}
```

## 3. ThreadPoolExecutor类构造方法
ThreadPoolExecutor类构造方法 的构造方法总共有四个，有些参数可以缺省，那么就用默认的代替，最终调用的就是下面所看到的构造方法
```java
    // 这些参数代表什么已经在上个小节内叨叨过了，在此不再重复，只关心构造函数内的逻辑
    // ThreadFactory 可以缺省，代替为Executors.defaultThreadFactory()
    // RejectedExecutionHandler 可以缺省 代替为 ThreadPoolExecutor.defaultHandler(内部类AbortPolicy的实例)
    public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,
                              TimeUnit unit,BlockingQueue<Runnable> workQueue,ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        /**
         * 1. 检查数值型的入参是否符合要求
         * 2. 检查引用类型入参是否符合要求，只要一个null就抛异常
         * 3. 该赋值的就赋值，各回各家，各找爹妈
         * 4. keepAliveTime 通过TimeUnit(时间类型)和 keepAliveTime(时间数值)最终确定标准类型的时间长度
         * */
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
```
## 4. ThreadPoolExecutor类的核心方法(核心逻辑)
### 4.1 提交任务 execute()
从ExecutorService接口定义可以发现，提交任务的方式是有很多的，然而最终都和顶级接口定义的execute()方法分不开

因此我们以这个为突破口，刚好，前面小节内提供的小案例就是使用execute()方法提交的任务
```java
public void execute(Runnable command) {
        // 如果传入为空，则抛异常
        if (command == null)
            throw new NullPointerException();
        //由它可以获取到当前有效的线程数和线程池的状态
        int c = ctl.get();
        //获取当前正在运行线程数是否小于核心线程池，是则新创建一个线程执行任务，否则将任务放到任务队列中
        //新建一个线程，赋给worker实例，worker线程调用command的run方法
        
        if (workerCountOf(c) < corePoolSize) { 
            //addWorker(command, true)失败的原因可能是：
            //  1. 线程池已经shutdown，shutdown的线程池不再接收新任务
            //  2. workerCountOf(c) < corePoolSize 判断后，由于并发，别的线程先创建了worker线程，导致workerCount>=corePoolSize
            //  在addWorker中创建工作线程执行任务
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        //当前核心线程池中全部线程都在运行
        //联系上一步的分析，没有办法产生worker实例，那么在这里尝试放入队列 失败可能是队列已满
        if (isRunning(c) && workQueue.offer(command)) {
            //成功进入此逻辑
            //表示 : 线程池处于运行状态，且任务插入任务队列成功
            //------------------------------------

            //再次或得到当前线程池状态，因为线程池时时刻刻状态都会发生改变
            int recheck = ctl.get();

            //线程池不处于运行状态，则使刚刚的任务出队
            // 情况1：! isRunning(recheck) 先执行，整体是true，也就是不处于运行状态，则执行remove(command)
            //    remove(command)也可能执行失败，比如刚好有一个线程执行完毕，并消耗了这个任务,会导致remove(command)失败
            // 情况2：! isRunning(recheck) 先执行,整体是false,还处于运行状态，则短路
            if (! isRunning(recheck) && remove(command))
                //抛出RejectedExceptionException异常
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        //插入队列不成功，且当前线程数数量小于最大线程池数量，此时则创建新线程执行任务，创建失败抛出异常
        else if (!addWorker(command, false))
            //抛出RejectedExceptionException异常
            reject(command);
    }
```
流程主逻辑有：

(1) 如果线程池当前线程数量少于corePoolSize，则addWorker(command, true)创建新worker线程，如创建成功返回，如没创建成功，则执行后续步骤；

(2) 如果线程池还在running状态，将task加入workQueue阻塞队列中，如果加入成功，进行double-check，如果加入失败，则执行后续步骤；

(3) 如果线程池不是running状态 或者 无法入队列，尝试开启新线程，扩容至maxPoolSize，如果addWork(command, false)失败了，拒绝当前command

直接上个图！！！

![execute的执行顺序](https://gitee.com/alan-tang-tt/yuan/raw/master/%E6%AD%BB%E7%A3%95%20java%E5%B9%B6%E5%8F%91%E5%8C%85/resource/%E7%BA%BF%E7%A8%8B%E6%B1%A0/3.1execute.png)

### 4.2 addWorker()
```java
/**
 * 此方法可以有四种形态
 * 1. addWorker(command, true) // 线程数小于corePoolSize时，放一个需要处理的task进Workers Set。如果Workers Set长度超过corePoolSize，就返回false
 * 2. addWorker(command, false) //当队列被放满时，就尝试将这个新来的task直接放入Workers Set，而此时Workers Set的长度限制是maximumPoolSize。如果线程池也满了的话就返回false
 * 3. addWorker(null, false) //放入一个空的task进workers Set，长度限制是maximumPoolSize。这样一个task为空的worker在线程执行的时候会去任务队列里拿任务，这样就相当于创建了一个新的线程，只是没有马上分配任务
 * 4. addWorker(null, true) //这个方法就是放一个null的task进Workers Set，而且是在小于corePoolSize时，如果此时Set中的数量已经达到corePoolSize那就返回false，什么也不干。实际使用中是在prestartAllCoreThreads()方法，这个方法用来为线程池预先启动corePoolSize个worker等待从workQueue中获取任务执行
 *
 */
private boolean addWorker(Runnable firstTask, boolean core) {
        //外层循环标志
        //break retry; 指的是跳出这个大循环
        //continue retry; 指的是继续这个大循环
        //外层循环,线程池状态检查，以及workerCount + 1
        retry:
        for (;;) {
            //外层循环，一值刷新c与rs值
            int c = ctl.get();
            int rs = runStateOf(c);

           /**  
            *  状态检查：
            *  
            *  线程池的state越小越是运行状态，runnbale=-1，shutdown=0,stop=1,tidying=2，terminated=3
            * 1、如果线程池state已经至少是shutdown状态了
            * 2、并且以下3个条件任意一个是false:
            *  a) rs == SHUTDOWN  //（隐含:rs>=SHUTDOWN）为false情况:线程池状态已经超过shutdown，可能是stop、tidying、terminated其中一个，即线程池已经终止
            *  b) firstTask == null //（隐含:rs==SHUTDOWN）为false情况:firstTask不为空 且 rs==SHUTDOWN，return false，(场景是在线程池已经shutdown后，还要添加新的任务，则拒绝)
            *  c) !workQueue.isEmpty() //（隐含:rs==SHUTDOWN，firstTask==null）为false情况：workQueue为空，当firstTask为空时是为了创建一个没有任务的线程，再从workQueue中获取任务如果workQueue已经为空，那么就没有添加新worker线程的必要了
            *  
            *  return false，即无法addWorker()
            **/
            if (rs >= SHUTDOWN && ! (rs == SHUTDOWN && firstTask == null &&! workQueue.isEmpty())){
                 return false;
             }
            //内层循环，负责worker数量+1
            for (;;) {
                int wc = workerCountOf(c);
                //如果worker数量>线程池最大上限CAPACITY（即使用int低29位可以容纳的最大值）
                //或者( worker数量>corePoolSize 或  worker数量>maximumPoolSize )，即已经超过了给定的边界
                if (wc >= CAPACITY ||bwc >= (core ? corePoolSize : maximumPoolSize)){
                      return false;
                }
                //调用unsafe CAS操作，使得worker数量+1，成功则跳出retry循环
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                //CAS worker数量+1失败，再次读取ctl
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS失败时因为workerCount改变了，继续内层循环尝试CAS对worker数量+1
            }
        }

        /**
         * worker数量+1成功的后续操作
         * 添加到workers Set集合，并启动worker线程
         */
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            /**
             * 1、构造函数，默认设置worker这个AQS锁的同步状态state=-1
             * 2、将firstTask设置给worker的成员变量firstTask,首个执行的任务
             * 3、使用worker自身这个runnable，调用ThreadFactory创建一个线程，并设置给worker的成员变量thread
             **/
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // 当获取到锁后，再次检查
                    int rs = runStateOf(ctl.get());
                    //如果线程池在运行running<shutdown 或者 线程池已经shutdown，且firstTask==null（可能是workQueue中仍有未执行完成的任务，创建没有初始任务的worker线程执行）
                    //worker数量-1的操作在addWorkerFailed()
                    if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()){// precheck that t is startable   线程已经启动，抛非法线程状态异常
                           throw new IllegalThreadStateException();
                         }
                        //workers是一个HashSet<Worker>
                        workers.add(w);
                        //设置最大的池大小largestPoolSize，workerAdded设置为true
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                //如果往HashSet中添加worker成功，启动线程
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            //如果启动线程失败
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }
```
##### 总结: addWorker 这里面就干了两件事情，

1. 使用循环CAS操作来将线程数加1；
2. 新建一个线程并启用。

![addworker的流程图](https://gitee.com/alan-tang-tt/yuan/raw/master/%E6%AD%BB%E7%A3%95%20java%E5%B9%B6%E5%8F%91%E5%8C%85/resource/%E7%BA%BF%E7%A8%8B%E6%B1%A0/3.2addworker.png)

### 4.3 Worker内部类
```java
/**
 * 这个类就是线程池里面一直运行着的线程，准确的说是线程的包装类(见名知意:干活的)
 * 
 * Worker类功能上管理着运行线程的中断状态 和 一些指标
 * Worker类实现了Runnable，因此既是一个可运行的任务，也是一把锁（不可重入）
 * Worker类继承了AQS，简化在执行任务时的获取，释放锁，实现了Runnable，因此既是一个可运行的任务，也是一把锁（不可重入）
 * 
 * 1. 防止了中断: 在运行中的任务，只会唤醒(中断)在 等待从workQueue中获取任务的线程
 * 2. 控制上使用AQS锁:当运行时上锁，就不能中断，TreadPoolExecutor的shutdown()方法中断前都要获取worker锁
 * 3. 只有在等待从workQueue中获取任务getTask()时才能中断
 * 
 * Worker类实现了一个简单的不可重入的互斥锁，而不是用ReentrantLock可重入锁
 * 为了让线程真正开始后才可以中断，初始化lock状态为负值(-1)，在开始runWorker()时将state置为0，而state>=0才可以中断
 *
 */
private final class Worker extends AbstractQueuedSynchronizer implements Runnable {

        private static final long serialVersionUID = 6138294804551838833L;

        //利用ThreadFactory和 Worker这个Runnable创建的线程对象
        //工厂产生线程失败的话，则赋值null
        Runnable firstTask;
        //每个线程的task计数器
        volatile long completedTasks;

        Worker(Runnable firstTask) {
          /**
           *  1、将AQS的state置为-1(大于0代表锁已经被获取)，在调用runWorker()前,禁止interrupt中断;
           *     中断方法interruptIfStarted()在设置中断标志的时候，会先判断 getState()>=0
           *  2、待执行的任务会以参数传入，并赋予firstTask
           *  3、根据当前worker创建一个线程对象,当前worker是一个runnable任务,也就是不会用参数的firstTask创建线程，而是调用当前worker.run()时调用firstTask.run()
           *
           **/
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);
        }

        public void run() {
            //runWorker()是ThreadPoolExecutor的方法
            runWorker(this);
        }

        //getState()返回的是0 表示unlocked状态;返回时1 则是locked 状态
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        
        /**
         * 尝试获取锁
         * 重写AQS的tryAcquire()，AQS本来就是让子类来实现的
         */
        protected boolean tryAcquire(int unused) {
            //尝试一次将state从0设置为1，即“锁定”状态，但由于每次都是state 0->1，而不是+1，那么说明不可重入
            //且state==-1时也不会获取到锁,初始化的时候设置为-1，只能在runWorker的时候设置为0才可以获取锁
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        /**
         * 尝试释放锁
         * 不是state-1，而是置为0
         */
        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()        { acquire(1); }
        public boolean tryLock()  { return tryAcquire(1); }
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        /**
         * 中断（如果运行）
         * shutdownNow时会循环对worker线程执行
         * 且不需要获取worker锁，即使在worker运行时也可以中断
         */
        void interruptIfStarted() {
            Thread t;
            //如果state>=0、t!=null、且t没有被中断
            //new Worker()时state==-1，说明不能中断
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }
```
- (1)初始AQS状态为-1，此时不允许中断interrupt()，只有在worker线程启动了，执行了runWorker()，将state置为0，才能中断。

- (2)shutdown()线程池时，会对每个worker执行tryLock()上锁，而Worker类这个AQS的tryAcquire()方法是固定将state从0->1，故初始状态state==-1时tryLock()失败，不能interrupt()

- (3)shutdownNow()线程池时，不用tryLock()上锁，但调用worker.interruptIfStarted()终止worker，interruptIfStarted()也有state>0才能interrupt的逻辑

- (4)为了防止某种情况下，在运行中的worker被中断，runWorker()每次运行任务时都会lock()上锁，而shutdown()这类可能会终止worker的操作需要先获取worker的锁，这样就防止了中断正在运行的线程


### 4.4 runWorker() 方法详解

- 重复地从队列中获取任务并执行


```java

/**
 *
 * 1. 我们可能使用一个初始化任务开始，即firstTask为null，然后只要线程池在运行，我们就从getTask()获取任务 如果getTask()返回null，则worker由于改变了线程池状态或参数配置而退出
 *    其它退出因为外部代码抛异常了，这会使得 completedAbruptly为true，这会导致在 processWorkerExit()方法中替换当前线程
 *
 * 2. 在任何任务执行之前，都需要对worker加锁去防止在任务运行时，其它的“线程池中断操作” ，调用clearInterruptsForTaskRun保证-除非线程池正在 stoping，否则线程不会被设置中断标示
 *
 * 3. 每个任务执行前会调用beforeExecute()其中可能抛出一个异常，这种情况下会导致线程死亡（跳出循环，且completedAbruptly==true）会导致没有执行任务
 *
 * 4. 假定beforeExecute()正常完成，汇总任何抛出的异常送给 afterExecute 因为我们不能在Runnable.run()方法中重新上抛Throwable，我们将Throwables包装到Errors上抛(会到线程的UncaughtExceptionHandler去处理）
 *   (任何上抛的异常都会导致线程死亡)
 *
 * 5. 任务执行结束后，调用afterExecute()，也可能抛异常，也会导致线程die
 * 
 **/
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    /**
     * 允许中断
     * new Worker()时 state==-1,此处是调用Worker类的tryRelease()方法将state置为0->setState(0);
     * 而interruptIfStarted()中只有state>=0才允许调用中断
    */
    w.unlock(); 
    //是否“突然完成”，如果是由于异常导致的进入finally，那么completedAbruptly==true就是突然完成的
    boolean completedAbruptly = true; 
    try {
        /**
         * 
         * 如果task不为null，或者从阻塞队列中getTask()不为null
         */
        while (task != null || (task = getTask()) != null) {
            //上锁，不是为了防止并发执行任务,为了在shutdown()时不终止正在运行的worker, shutdown()的时候也需要获取锁
            w.lock(); 
            /**
             * 
             * clearInterruptsForTaskRun操作:
             * 
             * 确保只有在线程stoping时，才会被设置中断标示，否则清除中断标示
             * 
             * 1、如果线程池状态>=stop，且当前线程没有设置中断状态，wt.interrupt()
             * 
             * 2、如果一开始判断线程池状态<stop,但Thread.interrupted()为true,即线程已经被中断，又清除了中断标示,那么再次判断线程池状态是否>=stop
             *      如果 是，再次设置中断标示，wt.interrupt()
             *      如果 否，不做操作，清除中断标示后进行后续步骤
             */
            if ((runStateAtLeast(ctl.get(), STOP) || (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) &&
                !wt.isInterrupted())
                //调用interrupt()中断
                wt.interrupt(); 
             
            try {
                //执行前（子类实现）
                beforeExecute(wt, task);
                 
                Throwable thrown = null;
                try {
                    task.run();
                } 
                catch (RuntimeException x) {
                    thrown = x; throw x;
                } 
                catch (Error x) {
                    thrown = x; throw x;
                } 
                catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } 
                finally {
                    //执行后（子类实现）
                    afterExecute(task, thrown);
                }
            } 
            finally {
                task = null; //task置为null
                w.completedTasks++; //完成任务数+1
                w.unlock(); //解锁
            }
        }
         
        completedAbruptly = false;
    } 
    finally {
        //处理worker的退出流程
        processWorkerExit(w, completedAbruptly);
    }
}
```
#### runWorker() 执行流程：

- (1)Worker线程启动后,通过Worker类的run()方法调用runWorker(this)

- (2)执行任务之前,首先worker.unlock()，将AQS的state置为0，允许中断当前worker线程

- (3)开始执行firstTask，调用task.run()，在执行任务前会上锁worker.lock()，在执行完任务后会解锁，为了防止在任务运行时被线程池一些中断操作中断

- (4)在任务执行前后，可以根据业务场景自定义beforeExecute() 和 afterExecute()方法

- (5)无论在beforeExecute()、task.run()、afterExecute()发生异常上抛，都会导致worker线程终止，进入processWorkerExit()处理worker退出的流程

- (6)如正常执行完当前task后，会通过getTask()从阻塞队列中获取新任务，当队列中没有任务，且获取任务超时，那么当前worker也会进入退出流程

上一波图：看得更舒服

![runWorker图](https://gitee.com/alan-tang-tt/yuan/raw/master/%E6%AD%BB%E7%A3%95%20java%E5%B9%B6%E5%8F%91%E5%8C%85/resource/%E7%BA%BF%E7%A8%8B%E6%B1%A0/4.4runWorker(\).png)


### 4.5 getTask()  --  从队列获取任务

```java
/**
 * 以下情况会返回null
 * 1. 线程池内有超过了maximumPoolSize设置的线程数量（自定义设置了setMaximumPoolSize()）
 * 2. 线程池被stop
 * 3. 线程池被shutdown，并且workQueue空了
 * 4. 线程等待任务超时
 */
private Runnable getTask() {
    //标记上次执行poll() 是否超时
    boolean timedOut = false;
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
 
        /**
         *
         * 线程池状态检查以及任务队列检查：判断是否可以满足从workQueue中获取任务的条件一
         * 1. 对线程池状态的判断，两种情况会workerCount-1，并且返回null
         *   a. 线程池状态为shutdown，且workQueue为空（反映了shutdown状态的线程池还是要执行workQueue中剩余的任务的）
         *   b. 线程池状态为stop（shutdownNow()会导致变成STOP）（此时不用考虑workQueue的情况）
         */
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            //循环的CAS减少worker数量
            decrementWorkerCount(); 
            return null;
        }
        // 是否需要定时从workQueue中获取
        boolean timed;
       /**
        * 
        * 
        * 判断是否可以满足从workQueue中获取任务的条件二
        * 2. 线程数量是否超过maximumPoolSize 或者 获取任务是否超时
        *  a. 线程数量超过maximumPoolSize可能是线程池在运行时被调用了setMaximumPoolSize()被改变了大小,否则已经addWorker()成功不会超过maximumPoolSize
        *  b. 如果当前线程数量>corePoolSize，才会检查是否获取任务超时，这也体现了当线程数量达到maximumPoolSize后,如果一直没有新任务，会逐渐终止worker线程直到corePoolSize
        **/
        for (;;) {
            int wc = workerCountOf(c);
            //allowCoreThreadTimeOut默认为false
            //如果allowCoreThreadTimeOut为true，说明corePoolSize和maximum都需要超时检查
            timed = allowCoreThreadTimeOut || wc > corePoolSize;
            //如果当前执行线程数<maximumPoolSize，并且timedOut 和 timed 任一为false，
            //跳出循环，开始从workQueue获取任务
            if (wc <= maximumPoolSize && !(timedOut && timed))
                break;
            
            /**
             * 如果到了这一步，说明要么线程数量超过了maximumPoolSize（可能maximumPoolSize被修改了）
             * 要么既需要计时timed==true,也超时了timedOut==true
             * worker数量-1，减一执行一次就行了，然后返回null, 在runWorker()中会有逻辑减少worker线程
             * 如果本次减一失败,继续内层循环再次尝试减一
             */
            if (compareAndDecrementWorkerCount(c))
                return null;
             
            //如果减数量失败，再次读取ctl
            c = ctl.get();  // Re-read ctl
             
            //如果线程池运行状态发生变化，继续外层循环;如果状态没变，继续内层循环
            if (runStateOf(c) != rs)
                continue retry;
        }
 
        try {
            /**
             * 
             * 2. 如果满足获取任务条件,根据是否需要定时获取调用不同方法：
             *   A、workQueue.poll():使用  LockSupport.parkNanos(this, nanosTimeout) 挂起一段时间,interrupt()时不会抛异常，但会有中断响应
             *      如果在keepAliveTime时间内，阻塞队列还是没有任务，返回null
             *   B、workQueue.take():使用 LockSupport.park(this) 挂起，interrupt()时不会抛异常，但会有中断响应
             *      如果阻塞队列为空，当前线程会被挂起等待；当队列中有任务加入时，线程被唤醒，take方法返回任务
             *
             **/
            Runnable r = timed ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :    //大于corePoolSize
                workQueue.take();                                        //小于等于corePoolSize
        
            //如获取到了任务就返回
            if (r != null)
                return r;
            
            //没有返回，说明超时,那么在下一次内层循环时会进入worker count减一的步骤
            timedOut = true;
        } 
        /**
          * blockingQueue的take()阻塞使用LockSupport.park(this)进入wait状态的，
          * 对LockSupport.park(this)进行interrupt不会抛异常，但还是会有中断响应
          * 但AQS的ConditionObject的await()对中断状态做了判断，会报告中断状态 reportInterruptAfterWait(interruptMode)
          * 就会上抛InterruptedException，在此处捕获，重新开始循环
          * 如果是由于shutdown()等操作导致的空闲worker中断响应，在外层循环判断状态时，可能return null
          */
        catch (InterruptedException retry) { 
            timedOut = false; //响应中断，重新开始，中断状态会被清除
        }
    }
}
```
### getTask() 总结

#### 1、首先判断是否可以满足从workQueue中获取任务的条件，不满足return null
    
- A、线程池状态是否满足：
  - (a).shutdown状态 + workQueue为空 或 stop状态,都不满足,因为被shutdown后还是要执行workQueue剩余的任务,但workQueue也为空,就可以退出了
  - (b).stop状态 shutdownNow()操作会使线程池进入stop，此时不接受新任务，中断正在执行的任务，workQueue中的任务也不执行了，故return null返回

- B、线程数量是否超过maximumPoolSize 或 获取任务是否超时
  - （a）线程数量超过maximumPoolSize可能是线程池在运行时被调用了setMaximumPoolSize()被改变了大小，否则已经addWorker()成功不会超过maximumPoolSize
  - （b）如果 当前线程数量>corePoolSize，才会检查是否获取任务超时，这也体现了当线程数量达到maximumPoolSize后，如果一直没有新任务，会逐渐终止worker线程直到corePoolSize
  
#### 2、如果满足获取任务条件，根据是否需要定时获取调用不同方法：

- A、workQueue.poll()：如果在keepAliveTime时间内，阻塞队列还是没有任务，返回null
- B、workQueue.take()：如果阻塞队列为空，当前线程会被挂起等待；当队列中有任务加入时，线程被唤醒，take方法返回任务

#### 3、在阻塞从workQueue中获取任务时,可以被interrupt()中断，代码中捕获了InterruptedException，重置timedOut为初始值false，再次执行第1步中的判断，满足就继续获取任务，不满足return null，会进入worker退出的流程 

-------------

### 4.6 processWorkerExit() worker 退出机制
```java
private void processWorkerExit(Worker w, boolean completedAbruptly) {
    /**
     * 1. 判断是否是突然中止，以决定是否worker线程数量需要-1
     *    
     *    如果是突然终止，说明是task执行时异常情况导致，即run()方法执行时发生了异常，那么正在工作的worker线程数量需要-1
     *    如果不是突然终止，说明是worker线程没有task可执行了，不用-1，因为已经在getTask()方法中-1了
     */
    if (completedAbruptly) 
        decrementWorkerCount();
 
    /**
     * 2.从Workers Set中移除worker
     */
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        //把worker的完成任务数加到线程池的完成任务数
        completedTaskCount += w.completedTasks; 
        //从HashSet<Worker>中移除
        workers.remove(w); 
    } finally {
        mainLock.unlock();
    }
 
    /**
     * 3、在对线程池有负效益的操作时，都需要“尝试终止”线程池,主要是判断线程池是否满足终止的状态:
     * 如果状态满足，但还有线程池还有线程，尝试对其发出中断响应，使其能进入退出流程
     * 没有线程了，更新状态为tidying->terminated
     */
    tryTerminate();
 
    /**
     * 4、是否需要增加worker线程
     * 线程池状态是running 或 shutdown
     * 如果当前线程是突然终止的，addWorker()
     * 如果当前线程不是突然终止的，但当前线程数量 < 要维护的线程数量，addWorker()
     * 故如果调用线程池shutdown()，直到workQueue为空前，线程池都会维持corePoolSize个线程，然后再逐渐销毁这corePoolSize个线程
     */
    int c = ctl.get();
    //如果状态是running、shutdown，即tryTerminate()没有成功终止线程池，尝试再添加一个worker
    if (runStateLessThan(c, STOP)) {
        //不是突然完成的，即没有task任务可以获取而完成的，计算min，并根据当前worker数量判断是否需要addWorker()
        if (!completedAbruptly) {
            // allowCoreThreadTimeOut默认为false，即min默认为corePoolSize
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
             
            //如果min为0，即不需要维持核心线程数量，且workQueue不为空，至少保持一个线程
            if (min == 0 && ! workQueue.isEmpty())
                min = 1;
             
            //如果线程数量大于最少数量，直接返回，否则下面至少要addWorker一个
            if (workerCountOf(c) >= min)
                return;
        }
        
        //添加一个worker
        //只要worker是completedAbruptly突然终止的，或者线程数量小于要维护的数量，就新添一个worker线程，即使是shutdown状态
        addWorker(null, false);
    }
}
```
---------

## 线程池关闭

线程池关闭依赖于两个方法

- (1) shutdown() : 不会立即终止线程池，而是要等所有任务缓存队列中的任务都执行完后才终止，但再也不会接受新的任务

- (2) shutdownNow() : 立即终止线程池，并尝试打断正在执行的任务，并且清空任务缓存队列，返回尚未执行的任务

那么接下来，我们一个一个看

### shutdown() 方法

```java
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        //打锁
        mainLock.lock();
        try {
            //监测调用方能正常关闭线程，一般不需理会
            checkShutdownAccess();
            //原子性的修改线程池的状态为STOP状态
            advanceRunState(SHUTDOWN);
            //遍历线程池里的所有工作线程，然后调用线程的interrupt方法
            interruptIdleWorkers();
            //专门用于ScheduledThreadPoolExecutor的处理方法
            onShutdown();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    private void advanceRunState(int targetState) {
        //一直在循环让ctl发生更改,只要不成功就一直重试
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) ||
                ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
                break;
        }
    }

    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    //方法还是很简单的，参数onlyOne决定是不是全部中断或者就中断一个
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    final void tryTerminate() {
        //一直重试去更改线程池状态(-> TERMINATED)
        for (;;) {
            int c = ctl.get();
            /**
             * 线程池是否需要终止
             * 如果以下3中情况任一为true，return，不进行终止
             * 1、还在运行状态
             * 2、状态是TIDYING、或 TERMINATED，已经终止过了
             * 3、SHUTDOWN 且 workQueue不为空
             */
            if (isRunning(c) ||
                runStateAtLeast(c, TIDYING) ||
                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))
                return;
            /**
             * 只有shutdown状态 且 workQueue为空，或者 stop状态能执行到这一步
             * 如果此时线程池还有线程（正在运行任务，正在等待任务）
             * 中断唤醒一个正在等任务的空闲worker
             * 唤醒后再次判断线程池状态，会return null，进入processWorkerExit()流程
             */
            if (workerCountOf(c) != 0) { // Eligible to terminate
                //中断workers集合中的空闲任务，参数为true，只中断一个
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            //如果状态是SHUTDOWN，workQueue也为空了，正在运行的worker也没有了，开始terminated
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        terminated();
                    } finally {
                        //将线程池的ctl变成TERMINATED
                        ctl.set(ctlOf(TERMINATED, 0));
                        //唤醒调用了 等待线程池终止的线程 awaitTermination() 
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // 如果上面的CAS判断false，再次循环
        }
    }
```

### shutdownNow()
```java
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            //使用cas更改线程池状态
            advanceRunState(STOP);
            //不管怎么的，立马中断线程
            interruptWorkers();
            //还没有开始执行的task抽出来
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers)
                w.interruptIfStarted();
        } finally {
            mainLock.unlock();
        }
    }

    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        //如果workQueue是是延时队列或者其他类型的阻塞队列
        //可能会发生drainTo方法调用完之后没什么作用，那么就只能一个一个来了
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }

```

## 阻塞队列

在线程池的构造函数里，有一个参量是BlockingQueue,这个是存放task的一种阻塞队列。

接下来就简单介绍jdk中提供的几种队列(此队列的源码分析请查看集合源码分析篇，此处不再过多描述)

- (1)ArrayBlockingQueue：基于数组结构的有界阻塞队列，按FIFO排序任务；

- (2)LinkedBlockingQuene：基于链表结构的阻塞队列，按FIFO排序任务，吞吐量通常要高于ArrayBlockingQuene；

- (3)SynchronousQuene：一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于(2)

- (4)PriorityBlockingQuene：具有优先级的无界阻塞队列；

## 拒绝策略

在线程池的构造函数里，有一个参量是RejectHandle

当线程池的任务缓存队列已满并且线程池中的线程数目达到maximumPoolSize，如果还有任务到来就会采取任务拒绝策略

所有的拒绝策略实现类都作为ThreadPoolExecutor的内部类

```java
    //由调用线程处理该任务
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        
        public CallerRunsPolicy() { }
        //线程自己的事情自己做
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    //丢弃任务并抛出RejectedExecutionException异常。
    public static class AbortPolicy implements RejectedExecutionHandler {
        
        public AbortPolicy() { }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
        }
    }

    //也是丢弃任务，但是不抛出异常。
    public static class DiscardPolicy implements RejectedExecutionHandler {

        public DiscardPolicy() { }

        //什么都不干，很骚气，从未见过如此厚颜无耻的代码
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        
        }
    }

    //丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {

        public DiscardOldestPolicy() { }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
```

## ThreadPoolExecutor类和JDK提供的四个线程池关系

```java
    //可以直接使用jdk帮我们配好了的默认的线程池
    //写法都是Executors.xxx()
    //底层都是调用ThreadPoolExecutor的构造方法，参数不同而已
    ExecutorService xxx = Executors.newCachedThreadPool();
```
- (1)newCachedThreadPool创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。

- (2)newFixedThreadPool 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。

- (3)newScheduledThreadPool 创建一个定长线程池，支持定时及周期性任务执行。

- (4)newSingleThreadExecutor 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。

#### 不建议使用默认线程池配置

1)FixedThreadPool 和 SingleThreadPool: 允许的请求队列长度为 Integer.MAX_VALUE，可能会堆积大量的请求，从而导致 OOM。

2)CachedThreadPool 和 ScheduledThreadPool: 允许的创建线程数量为 Integer.MAX_VALUE，可能会创建大量的线程，从而导致 OOM。

