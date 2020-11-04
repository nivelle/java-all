### AQS是基于FIFO的队列实现的,并且内部维护了一个状态变量state,通过原子更新这个状态变量state即可以实现加锁解锁操作。

#### 核心类

```
static final class Node {
        //标识一个节点是等待中的共享模式
        static final Node SHARED = new Node();
        //标识一个节点是等待中的互斥模式
        static final Node EXCLUSIVE = null;
        /** waitStatus value to indicate thread has cancelled */
        //等待状态标识,线程已经取消
        static final int CANCELLED =  1;
        /** waitStatus value to indicate successor's thread needs unparking */
        //等待状态标识,后继节点需要唤醒时，当前节点状态为-1
        static final int SIGNAL    = -1;
        /** waitStatus value to indicate thread is waiting on condition */
        //等待状态标识,一个线程在等待一个条件
        static final int CONDITION = -2;
        /**
         * waitStatus value to indicate the next acquireShared should unconditionally【无条件】 propagate【传播】
         */
        //等待标识,后面的共享锁需要无条件传播（共享锁需要连续唤醒读的线程）
        static final int PROPAGATE = -3;
        
        // waitStatus = 0 //None of the above
        /**
         * Status field, taking on only the values:
         *   SIGNAL:     The successor of this node is (or will soon be)
         *               blocked (via park), so the current node must
         *               unpark its successor when it releases or
         *               cancels. To avoid races, acquire methods must
         *               first indicate they need a signal,
         *               then retry the atomic acquire, and then,
         *               on failure, block.
         *   CANCELLED:  This node is cancelled due to timeout or interrupt.
         *               Nodes never leave this state. In particular,
         *               a thread with cancelled node never again blocks.
         *   CONDITION:  This node is currently on a condition queue.
         *               It will not be used as a sync queue node
         *               until transferred, at which time the status
         *               will be set to 0. (Use of this value here has
         *               nothing to do with the other uses of the
         *               field, but simplifies mechanics.)
         *   PROPAGATE:  A releaseShared should be propagated to other
         *               nodes. This is set (for head node only) in
         *               doReleaseShared to ensure propagation
         *               continues, even if other operations have
         *               since intervened.
         *   0:          None of the above
         *
         * The values are arranged numerically to simplify use.
         * Non-negative values mean that a node doesn't need to
         * signal. So, most code doesn't need to check for particular
         * values, just for sign.
         *
         * The field is initialized to 0 for normal sync nodes, and
         * CONDITION for condition nodes.  It is modified using CAS
         * (or when possible, unconditional volatile writes).
         */
        //当前节点对应的等待状态,它是volatile修饰的,可能状态如上;
        volatile int waitStatus;
        //前一个节点
        volatile Node prev;
        //后一个节点
        volatile Node next;
        //当前节点保存的线程,也是volatile修饰的
        volatile Thread thread;
        //下一个等待在条件上的节点（Condition锁时使用）
        Node nextWaiter;
        //下一个等待条件上的节点是否时共享模式
        final boolean isShared() {
            return nextWaiter == SHARED;
        }
        //获取前一个节点
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else{
                return p;
            }
        }
        //构造函数，把节点是共享模式还是互斥模式设置到nextWaiter字段里面
        Node(Thread thread, Node mode) { 
            this.nextWaiter = mode;
            this.thread = thread;
        }
        //等待的状态，在Condition中使用
        Node(Thread thread, int waitStatus) {
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

```

#### 核心属性

1. **定义了一个状态变量和一个队列，状态变量用来控制加锁解锁，队列用来放置等待的线程。**

2. **这几个变量的修改是直接使用的Unsafe这个类来操作的**

```
//队列的头节点，transient和volatile修饰
private transient volatile Node head;

//队列的尾节点，transient和volatile修饰
private transient volatile Node tail;

// 控制加锁解锁的状态变量，volatile修饰
private volatile int state;

```

#### 抽象方法

````
    //互斥模式下使用:尝试获取锁
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
     }
    // 互斥模式下使用：尝试释放锁
    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }
    //共享模式下使用：尝试获取锁
    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    //共享模式下使用：尝试释放锁
    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }
    //如果当前线程独占着锁，返回true    
    protected boolean isHeldExclusively() {
            throw new UnsupportedOperationException();
    }
````


### ReentrantLock 源码解读

#### 静态抽象内部类 

```
//同步锁实现,静态抽象内部类,实现了公平锁和非公平锁的共有逻辑
abstract static class Sync extends AbstractQueuedSynchronizer
//非公平锁
static final class NonfairSync extends Sync
//公平锁
static final class FairSync extends Sync 
```

#### 公平锁实现

- ReentrantLock.lock()

```
public void lock() {
     sync.lock();//调用静态抽象内部类的lock方法，抽象方法
}
```
- FairSync.lock()//静态内部类,公平锁的实现

```
final void lock() {
     //这里的sync是公平锁，所以是FairSync的实例
     acquire(1);
}

// AbstractQueuedSynchronizer.acquire() //这是AQS抽象类的核心方法,尝试获取锁,如果获取失败就排队
public final void acquire(int arg) {
         //首先尝试获取锁,获取失败就放到等待队列
        // addWaiter()这里传入的节点模式为独占模式;arg默认为1  与操作 前面获取成功则不需要再检查后面的入队方法
        if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)){
            selfInterrupt();
        }
    }
```
 
- ReentrantLock.FairSync.tryAcquire() //公平锁抽象类FairSync尝试使用cas获取锁 

```
protected final boolean tryAcquire(int acquires) {
            //获取当前线程
            final Thread current = Thread.currentThread();
            //查看当前状态变量的值              
            int c = getState();
            //如果当前同步状态值等于0,说明没有线程占有锁
            if (c == 0) {
                // 如果没有其它线程在排队,那么当前线程尝试更新state的值为1;如果成功了,则说明当前线程获取了锁【非公平锁则不用!hasQueuedPredecessors()】
                // hasQueuedPredecessors:判断当前线程是否位于 CLH 同步队列中的第一个。如果是则返回flase,否则返回true
                // 判断当前节点在等待队列中是否有前驱节点的判断,如果有前驱节点说明有线程比当前线程更早的请求资源,根据公平性,当前线程请求资源失败。如果当前节点没有前驱节点的话,才有做后面的逻辑判断的必要性
                if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                    //当前线程获取了锁，把自己设置到exclusiveOwnerThread变量中
                    setExclusiveOwnerThread(current);
                    //返回true说明获取锁成功
                    return true;
                }
            }
            //如果当前锁已经被占用,而且占用的线程是当前线程,因为可重入,状态值+1 (可重入锁)
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                //如果溢出了，则报错
                if (nextc < 0){
                    throw new Error("Maximum lock count exceeded");
                }
                //设置到state中;这里不需要CAS更新state;因为当前线程占有着锁，其它线程只会CAS把state从0更新成1,是不会成功的所以不存在竞争,自然不需要使用CAS来更新
                setState(nextc);
                return true;
            }
            //当前线程尝试获取锁失败
            return false;
}
```

- AbstractQueuedSynchronizer.addWaiter()//调用这个方法，说明上面tryAcquire(int acquires)尝试获取锁方法失败了,可能已经有别的线程占有了锁

```
private Node addWaiter(Node mode) {//mode= Node.EXCLUSIVE
        //新建一个节点,初始化的waitStatus是默认值0, 在下一个节点进来 队列的时候 在acquireQueued 方法里面修改状态, 同时阻塞下一个进来排队的线程。
        Node node = new Node(Thread.currentThread(), mode);
        //这里先尝试把新节点加到尾节点后面,如果成功了就返回新节点;如果没成功再调用enq()方法不断尝试
        Node pred = tail;
        //如果尾节点不为空，则尝试将节点放在尾节点的后面
        if (pred != null) {
            //将新节点的前置节点设置为尾节点
            node.prev = pred;
            //设置新的尾节点,设置成功,cas操作
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                //返回新创建的尾节点
                return node;
            }
        }
        //如果上面创建尾节点失败,或者尾节点为null,则调用enq()方法；多个节点竞争加入到队列里面的情形
        enq(node);
        return node;
}

```
-- AbstractQueuedSynchronizer.enq() //循环尝试加入到尾巴节点，直到成功;尾巴节点为null或者多个节点争着加入到CLH同步队列

```
private Node enq(final Node node) {
        //自旋,不断尝试
        for (;;) {
            Node t = tail;
            // 如果尾节点为null,说明还未初始化
            if (t == null) {
                //头节点理论上代表获取锁的线程,它不属于队列。所以head节点的thread=null,状态初始为0,然后有后继节点尝试获取锁的时候则被设置为-1(-1 代表后记节点需要被唤醒;shouldParkAfterFailedAcquire()方法里)
                //初始化头节点和尾节点(new Node()方法可见,head 是不包含线程的假节点),第一次进入这个方法的时候初始化了等待队列，第二次自旋循环才能跳出
                //没有获得锁的线程，在队列为空的时候首先初始化队列,head=tail
                if (compareAndSetHead(new Node())){
                    tail = head;
                 }
            } else {
                //新加入节点的前置节点设置为尾节点
                node.prev = t;
                //设置新加入节点为尾节点
                if (compareAndSetTail(t, node)) {
                    //设置旧尾节点的下一个节点为新节点
                    t.next = node;
                    return t;
                }
            }
        }
    }

```

- AbstractQueuedSynchronizer.acquireQueued() //调用上面的 addWaiter()方法[包括enq()方法]成功使得新节点已经成功入队,下面这个方法是尝试让当前节点来获取锁的(arg=1)
```
final boolean acquireQueued(final Node node, int arg) {
        // 失败标识
        boolean failed = true;
        try {
            //中断标识
            boolean interrupted = false;
            //自旋
            for (;;) {
                //当前节点的前置节点
                final Node p = node.predecessor();
                //如果当前节点的前一个节点为head节点,则说明轮到自己获取锁了
                //调用ReentrantLock.FairSync.tryAcquire()方法再次尝试获取锁
                if (p == head && tryAcquire(arg)) {
                    //重新设置头节点,断开原来头节点 尝试获取锁成功,这里同时只会有一个线程在执行,所以不需要CAS更新;将当前节点设置为新的头节点
                    setHead(node);
                    //并把节点从链表中删除,已经获取锁了
                    p.next = null; // help GC
                    failed = false;//未失败
                    return interrupted;
                }
                //只有在前置节点设置为-1时才调用parkAndCheckInterrupt方法是否需要阻塞, parkAndCheckInterrupt是真正的阻塞方法，唤醒后的线程重新执行上面的for循环
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()){
                    interrupted = true;//中断了
                }
            }
        } finally {
            //如果未获取锁
            if (failed){
                //取消再次获取锁
                cancelAcquire(node);
            }
        }
    }
    
```

- AbstractQueuedSynchronizer.shouldParkAfterFailedAcquire()
```
//这个方法在 acquireQueued 方法循环里使用第一次调用如果前置节点不为SIGNAL（-1）则会把它设置为-1,会把前一个节点的等待状态设置为SIGNAL,并返回false, 第二次调用才会返回true
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        //上一个节点的等待状态,注意Node的waitStatus字段我们在上面创建Node的时候并没有指定,也就是说使用的是默认值        
        // static final int CANCELLED =  1;            
        // static final int SIGNAL    = -1;        
        // static final int CONDITION = -2;
        // static final int PROPAGATE = -3;                
        int ws = pred.waitStatus;
        if (ws == Node.SIGNAL){ //如果等待状态为SIGNAL(等待唤醒),直接返回true
            /*
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             */
            return true;
        }
        if (ws > 0) { //如果前一个节点的状态大于0,也就是已取消状态
            /*
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             */
            // 把前面所有取消状态的节点都从链表中删除
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            
            pred.next = node;
        } else {
            /*
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             */
            //如果前一个节点的状态<=0,则把其状态设置为等待唤醒（初始化队列，head初始状态为0，第一次调用该方法先从0设置为-1）
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    } 
```
    
- AbstractQueuedSynchronizer.parkAndCheckInterrupt() //阻塞当前线程,直到锁释放唤醒该阻塞线程
 
```
private final boolean parkAndCheckInterrupt() {
        // 阻塞当前线程，底层调用的是Unsafe的park()方法
        LockSupport.park(this);
        // 返回是否已中断
        return Thread.interrupted();
    }   
```

#### 非公平锁实现

- ReentrantLock.lock()

```
public void lock() {
        sync.lock();
    }
```
- ReentrantLock.NonfairSync.lock() 这个方法在公平锁模式下直接调用的 acquire(1)
````
final void lock() {
    if (compareAndSetState(0, 1)){ //直接尝试CAS更新状态变量
         setExclusiveOwnerThread(Thread.currentThread()); //如果更新成功，说明获取到锁，把当前线程设为独占线程
    } else{
        acquire(1);
    }
}
````
- ReentrantLock.NonfairSync.tryAcquire()

```
protected final boolean tryAcquire(int acquires) {
      return nonfairTryAcquire(acquires);
}
```
-  ReentrantLock.Sync.nonfairTryAcquire()

```
final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            // 如果状态变量的值为0，再次尝试CAS更新状态变量的值;相对于公平锁模式少了!hasQueuedPredecessors()条件
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
}

```

#### 尝试获取锁

- ReentrantLock.tryLock()

```
public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
 }
```
- abstractQueuedSynchronizer.tryAcquireNanos()

````
public final boolean tryAcquireNanos(int arg, long nanosTimeout) throws InterruptedException {
    // 如果线程中断了，抛出异常
    if (Thread.interrupted()){
        throw new InterruptedException();
    }
   //尝试获取锁一次 
   return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout);
}
````
- AbstractQueuedSynchronizer.doAcquireNanos()

````
private boolean doAcquireNanos(int arg, long nanosTimeout) throws InterruptedException {
        // 如果时间已经到期了，直接返回false
        if (nanosTimeout <= 0L){
            return false;
        }
        // 到期时间
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                //如果到期了，就直接返回false
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L){
                    return false;
                }
                //只有到期时间大于1000纳秒，才阻塞;小于等于1000纳秒，直接自旋解决就得了
                if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold){
                    LockSupport.parkNanos(this, nanosTimeout);
                }
                if (Thread.interrupted()){
                    throw new InterruptedException();
                }
            }
        } finally {
            if (failed){
                cancelAcquire(node);
            }
        }
    }

````

#### 释放锁

- java.util.concurrent.locks.ReentrantLock.unlock()
```

public void unlock() {
        sync.release(1);
}

```

- java.util.concurrent.locks.AbstractQueuedSynchronizer.release

````
public final boolean release(int arg) {
        //调用AQS实现类的tryRelease()方法释放锁
        if (tryRelease(arg)) {
            //如果头节点不为空,且等待状态不是0,就唤醒下一个节点;
            //在每个节点阻塞之前会把其上一个节点的等待状态设为SIGNAL(-1),SIGNAL的准确理解应该是唤醒下一个等待的线程
            Node h = head;
            if (h != null && h.waitStatus != 0){
                unparkSuccessor(h);
            }
            return true;
        }
        return false;
}
````
- java.util.concurrent.locks.ReentrantLock.Sync.tryRelease

````
protected final boolean tryRelease(int releases) {
            // 如果当前线程不是占有着锁的线程，抛出异常
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread()){
                throw new IllegalMonitorStateException();
            }
            //如果状态变量的值为0了,说明完全释放了锁;这也就是为什么重入锁调用了多少次lock()就要调用多少次unlock()的原因,如果不这样做,会导致锁不会完全释放,别的线程永远无法获取到锁
            boolean free = false;
            if (c == 0) {
                free = true;
                //清空占用的线程
                setExclusiveOwnerThread(null);
            }
            //设置状态量
            setState(c);
            return free;
}
````

#### 释放锁后唤醒后继节点

````
private void unparkSuccessor(Node node) {
        /*
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         */
        //如果头节点的等待状态小于0，就把它设置为0
        int ws = node.waitStatus;
        if (ws < 0){
            compareAndSetWaitStatus(node, ws, 0);
        }
        /*
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         */
        //头节点的下一个节点
        Node s = node.next;
        //如果下一个节点为空，或者其等待状态大于0（已取消）
        if (s == null || s.waitStatus > 0) {
            s = null;
            //从尾节点向前遍历取到队列最前面的那个状态不是已取消状态的节点
            for (Node t = tail; t != null && t != node; t = t.prev){
                if (t.waitStatus <= 0){
                    s = t;
                }
            }
        }
        if (s != null){
            // 唤醒线程
            LockSupport.unpark(s.thread);
        }
}
    
````

#### 核心属性

```
private final Sync sync;

```

### 核心方法:ReentrantLock 实现了Lock接口，Lock接口里面定义了java中锁应该实现的几个方法

```
1. void lock();//获取锁
                  
2. void lockInterruptibly() throws InterruptedException;//获取锁（可中断）

3. boolean tryLock();//尝试获取锁，如果没获取到锁，就返回false

4. boolean tryLock(long time, TimeUnit unit) throws InterruptedException;//尝试获取锁，如果没获取到锁，就等待一段时间，这段时间内还没获取到锁就返回false

5. void unlock();//释放锁

6. Condition newCondition();// 条件锁

```

### 公平锁

```

static final class FairSync extends Sync {

        // 调用的sync属性的lock()方法,这里的sync是公平锁
        final void lock() {
            //调用AQS的acquire()方法获取锁,这里传的值为1
            acquire(1);
        }
        
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }

```

#### 非公平锁

```
static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
         // 非公平的锁的特点是不直接排队,先去尝试获取锁,如果获取不到才去排队
        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

```

### 核心内部类

```
// 抽象类Sync实现了AQS的部分方
abstract static class Sync extends AbstractQueuedSynchronizer 

//NonfairSync实现了Sync，主要用于非公平锁的获取
static final class NonfairSync extends Sync

//FairSync实现了Sync，主要用于公平锁的获取
static final class FairSync extends Sync

```

来自: [彤哥读源码](https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247483746&idx=1&sn=a6b5bea0cb52f23e93dd223970b2f6f9&scene=21#wechat_redirect)
