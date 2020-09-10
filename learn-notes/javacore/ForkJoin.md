
### ForkJoinTask

```
public abstract class ForkJoinTask<V> implements Future<V>, Serializable 

```

### 任务状态

```
//volatie修饰的任务状态值,由ForkJoinPool或工作线程修改.
volatile int status; 
static final int DONE_MASK   = 0xf0000000;//用于屏蔽完成状态位. 
static final int NORMAL      = 0xf0000000;//表示正常完成,是负值.
static final int CANCELLED   = 0xc0000000;//表示被取消,负值,且小于NORMAL
static final int EXCEPTIONAL = 0x80000000;//异常完成,负值,且小于CANCELLED
static final int SIGNAL      = 0x00010000;//用于signal,必须不小于1<<16,默认为1<<16.
static final int SMASK       = 0x0000ffff;//后十六位的task标签

```

### 标记当前task的completion状态,同时根据情况唤醒等待该task的线程.

```
private int setCompletion(int completion) {
    for (int s;;) {
        //开启一个循环,如果当前task的status已经是各种完成(小于0),则直接返回status,这个status可能是某一次循环前被其他线程完成.
        if ((s = status) < 0){
            return s;
        }
        //尝试将原来的status设置为它与completion按位或的结果.
        if (U.compareAndSwapInt(this, STATUS, s, s | completion)) {
            if ((s >>> 16) != 0)
                //此处体现了SIGNAL的标记作用,很明显,只要task完成(包含取消或异常),或completion传入的值不小于1<<16,
                //就可以起到唤醒其他线程的作用.
                synchronized (this) {
                 notifyAll();
                }
            //cas成功,返回参数中的completion.
            return completion;
        }
    }
}

```
### 运行ForkJoinTask的核心方法

```
 final int doExec() {
        int s; boolean completed;
        //仅未完成的任务会运行,其他情况会忽略
        if ((s = status) >= 0) {
            try {
                //调用exec
                completed = exec();
            } catch (Throwable rex) {
                //发生异常,用setExceptionalCompletion设置结果
                return setExceptionalCompletion(rex);
            }
            if (completed){
                s = setCompletion(NORMAL);
            }
        }
        return s;
    }

```

### 处理异常流程

```
 //记录异常并且在符合条件时传播异常行为
 private int setExceptionalCompletion(Throwable ex) {
        //首先记录异常信息到结果
        int s = recordExceptionalCompletion(ex);
        if ((s & DONE_MASK) == EXCEPTIONAL){
            //status去除非完成态标志位(只保留前4位),等于EXCEPTIONAL.内部传播异常;钩子
            internalPropagateException(ex);
        }
        return s;
    }
    
 
```

###  记录异常完成

```
final int recordExceptionalCompletion(Throwable ex) {
        int s;
        if ((s = status) >= 0) {
            //只能是异常态的status可以记录.hash值禁止重写,不使用子类的hashcode函数.
            int h = System.identityHashCode(this);
            final ReentrantLock lock = exceptionTableLock;
            //异常锁,加锁
            lock.lock();
            try {
                // 抹除脏异常,后面叙述
                expungeStaleExceptions();
                // 异常表数组.ExceptionNode后面叙述.
                ExceptionNode[] t = exceptionTable;
                // 用hash值和数组长度进行与运算求一个初始的索引
                int i = h & (t.length - 1);
                for (ExceptionNode e = t[i]; ; e = e.next) {
                    //找到空的索引位,就创建一个新的ExceptionNode,保存this,异常对象并退出循环
                    if (e == null) {
                        t[i] = new ExceptionNode(this, ex, t[i]);
                        break;
                    }
                    if (e.get() == this) // 已设置在相同的索引位置的链表中,退出循环
                        break;
                }
                //否则e指向t[i]的next,进入下个循环,直到发现判断包装this这个ForkJoinTask的ExceptionNode已经出现在t[i]这个链表并break,
                //或者直到e是null,意味着t[i]出发开始的链表并无包装this的ExceptionNode,则将构建一个新的ExceptionNode并置换t[i],将原t[i]置为它的next.整个遍历判断和置换过程处在锁中进行
            } finally {
                lock.unlock();
            }
            //记录成功,将当前task设置为异常完成
            s = setCompletion(EXCEPTIONAL);
        }
        return s;
    }

```

### 内部等待任务完成,直到完成或超时
```
final void internalWait(long timeout) {
        int s;
        //status小于0代表已完成,直接忽略wait.未完成,则试着加上SIGNAL的标记,令完成任务的线程唤醒这个等待.
        if ((s = status) >= 0 && // force completer to issue notify
            U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
            //加锁,只有一个线程可以进入.
            synchronized (this) {
                //再次判断未完成.等待timeout,且忽略扰动异常.
                if (status >= 0){
                    try { wait(timeout); } catch (InterruptedException ie) { }
                }
                else{
                    //已完成则响醒其他等待者
                    notifyAll();
                }
            }
        }
    }

```

### externalAwaitDone 

```
//外部线程等待一个common池中的任务完成.
private int externalAwaitDone() {
    int s = ((this instanceof CountedCompleter) ? 
    //当前task是一个CountedCompleter,尝试使用common ForkJoinPool去外部帮助完成,并将完成状态返回.
             ForkJoinPool.common.externalHelpComplete(
                 (CountedCompleter<?>)this, 0) :
            //当前task不是CountedCompleter,则调用common pool尝试外部弹出该任务并进行执行,
            //status赋值doExec函数的结果,若弹出失败(其他线程先行弹出)赋0.
             ForkJoinPool.common.tryExternalUnpush(this) ? doExec() : 0);
    if (s >= 0 && (s = status) >= 0) {
        //检查上一步的结果,即外部使用common池弹出并执行的结果(不是CountedCompleter的情况),或外部尝试帮助CountedCompleter完成的结果
        //status大于0表示尝试帮助完成失败.
        //扰动标识,初值false
        boolean interrupted = false;
        do {
            //循环尝试,先给status标记SIGNAL标识,便于后续唤醒操作.
            if (U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
                synchronized (this) {
                    if (status >= 0) {
                        try {
                            //CAS成功,进同步块发现double check未完成,则等待.
                            wait(0L);
                        } catch (InterruptedException ie) {
                            //若在等待过程中发生了扰动,不停止等待,标记扰动.
                            interrupted = true;
                        }
                    }
                    else
                        //进同步块发现已完成,则唤醒所有等待线程.
                        notifyAll();
                }
            }
        } while ((s = status) >= 0);//循环条件,task未完成.
        if (interrupted)
            //循环结束,若循环中间曾有扰动,则中断当前线程.
            Thread.currentThread().interrupt();
    }
    //返回status
    return s;
}

```