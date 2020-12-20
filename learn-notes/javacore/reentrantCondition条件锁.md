### 重点概念

- Condition的队列和AQS的队列不完全一样:

```
AQS的队列头节点是不存在任何值的(thread=null),是一个虚节点;

Condition的队列头节点是存储着实实在在的元素值的,是真实节点

```

-  各种等待状态(waitStatus)的变化

```
1. 在条件队列中，新建节点的初始等待状态是CONDITION（-2）

2. 移到AQS的队列中时等待状态会更改为0(AQS队列节点的初始等待状态为0)

3. 在AQS队列中如果需要阻塞,会把它上一个节点的等待状态设置为 SIGNAL(-1)

4. 不管在Condition队列还是AQS队列中，已取消的节点的等待状态都会设置为 CANCELLED（1）

```

- AQS 中的下一个节点是next,上一个节点prev;Condition中下一个节点是nextWaiter没有上一个节点

### 构造条件锁

#### ReentrantLock.newCondition()
```
public Condition newCondition() {
        return sync.newCondition();
}
```

#### ReentrantLock.Sync.newCondition()
````
final ConditionObject newCondition() {
     return new ConditionObject();
}
````
#### AbstractQueuedSynchronizer.ConditionObject.ConditionObject()
````
public ConditionObject() {}

````

### 核心属性

- 条件锁中也维护了一个队列,为了和AQS的队列区分,我这里称为条件队列,firstWaiter是队列的头节点,lastWaiter是队列的尾节点

```
/** First node of condition queue. */
private transient Node firstWaiter;

/** Last node of condition queue. */
private transient Node lastWaiter;

// AbstractQueuedLongSynchronizer -》 static final class Node;AQS的静态内部类
// Condition中下一个节点是nextWaiter,没有上一个节点
Node nextWaiter;

// 发生了中断，但在后续不抛出中断异常，而是“补上”这次中断
private static final int REINTERRUPT =  1;
// 发生了中断，且在后续需要抛出中断异常（添加到同步队列而且是0状态）
private static final int THROW_IE    = -1;

```

### condition.await()方法

-  AbstractQueuedSynchronizer.ConditionObject.await()

```
public final void await() throws InterruptedException {
            // 如果线程中断了，抛出异常
            if (Thread.interrupted()){
                throw new InterruptedException();
            }
            // 添加新节点到Condition队列中,并返回该节点
            Node node = addConditionWaiter();            
            // 完全释放当前线程获取的锁;因为锁是可重入的,所以这里要把所获取的锁都释放,同时唤醒同步队列中等待获取的线程
            int savedState = fullyRelease(node);
            int interruptMode = 0;            
            // 是否在同步队列中
            while (!isOnSyncQueue(node)) {
                // 阻塞当前线程,线程变成wainting状态
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0){
                    break;
                }
            }
            // 上面部分是调用await()时释放自己占有的锁,并阻塞自己等待条件的出现
                            
            // 下面部分是条件已经出现(signal)，尝试去获取锁；说明中断状态发生变化
            
            // 尝试获取锁;如果没获取到会再次阻塞;当前线程执行了signal方法会经过这个，也就是重新将当前线程加入同步队列中
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE){
                interruptMode = REINTERRUPT;
            }
            // 清除取消的节点
            if (node.nextWaiter != null){
                unlinkCancelledWaiters();
            }
            // 线程中断相关
            if (interruptMode != 0){
                reportInterruptAfterWait(interruptMode);
            }
}

```
#####  添加新节点到 Condition 队列中

- AbstractQueuedSynchronizer.ConditionObject.addConditionWaiter
````
private Node addConditionWaiter() {
            Node t = lastWaiter;     
            // 如果条件队列的尾节点已取消,从头节点开始清除所有已取消的节点(If lastWaiter is cancelled, clean out.)
            if (t != null && t.waitStatus != Node.CONDITION) {
                // 取消条件队列中waitStatus不是Node.CONDITION的节点
                unlinkCancelledWaiters();
                // 重新获取都是等待调节队列的尾节点
                t = lastWaiter;
            }
            //新建一个节点，它的等待状态是CONDITION
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            //如果尾节点为空，则把新节点赋值给头节点（初始化队列）
            //否则把新节点赋值给尾节点的nextWaiter指针
            if (t == null){
                firstWaiter = node;
            } else{
                t.nextWaiter = node;
            }
            //尾节点指向新节点
            lastWaiter = node;
            //返回新节点
            return node;
}

````

#### 从头节点开始清除所有已取消的节点(不是等待某个条件的队列)

-  AbstractQueuedSynchronizer.ConditionObject.unlinkCancelledWaiters

````
private void unlinkCancelledWaiters() {
            // 条件队列头节点
            Node t = firstWaiter;
            //临时节点
            Node trail = null;
            while (t != null) {
                //条件队列后继节点，拿出来暂存防止丢失
                Node next = t.nextWaiter;
                // 条件队列头节点如果不是等待状态
                if (t.waitStatus != Node.CONDITION) {
                    //头节点的后继节点设置为null（条件队列是单向队列,后继节点设置为空就脱离了队列）
                    t.nextWaiter = null;
                    if (trail == null){
                        //条件队列头节点后移动
                        firstWaiter = next;
                    } else{
                        //trail节点不为null,则将其后继节点设置为firstWaiter的后继节点
                        trail.nextWaiter = next;
                    }
                    //如果遍历完了
                    if (next == null){
                        //如果后继节点为空了,上一次的零时节点指向lastWaiter节点
                        lastWaiter = trail;
                    }
                } else {
                    //trail指向最新的有效节点
                    trail = t;
                }
                //next为null时结束遍历
                t = next;
            }
        }
````
####  释放新添加节点所持有的锁

- AbstractQueuedSynchronizer.fullyRelease

````
final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            //获取状态变量的值，重复获取锁，这个值会一直累加;所以这个值也代表着获取锁的次数
            int savedState = getState();
            // 一次性释放所有获得的锁
            if (release(savedState)) {
                failed = false;
                //返回获取锁的次数
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed){
                node.waitStatus = Node.CANCELLED;
            }
        }
}

````

####  释放锁操作

- AbstractQueuedLongSynchronizer.release

````
public final boolean release(long arg) {
        // 释放成功
        if (tryRelease(arg)) {
            // 同步队列头节点
            Node h = head;
            #//如果同步队列不为null而且状态不是取消，则尝试唤醒后继节点
            if (h != null && h.waitStatus != 0){               
                unparkSuccessor(h);
            }
            return true;
        }
        return false;
 }
 
````

##### 释放当前线程所持有的锁

- ReentrantLock.tryRelease

````
protected final boolean tryRelease(int releases) {
             int c = getState() - releases;
             //判断当前线程是否是锁的持有者
             if (Thread.currentThread() != getExclusiveOwnerThread()){
                 throw new IllegalMonitorStateException();
             }
             boolean free = false;
             if (c == 0) {
                // 释放成功，state归0,exclusiveOwnerThread设置为null
                 free = true;
                 setExclusiveOwnerThread(null);
             }
             setState(c);
             return free;
}
````
#### 判断是否在同步队列

- AbstractQueuedLongSynchronizer.isOnSyncQueue
````
final boolean isOnSyncQueue(Node node) {
        //如果节点的状态等于条件状态或者前继节点为空也就是头节点，则不在同步队列中
        if (node.waitStatus == Node.CONDITION || node.prev == null){
            return false;
        }
        //如果后继节点不为空则一定在同步队列中
        if (node.next != null) {// If has successor, it must be on queue
            return true;
        }
        /*
         * node.prev can be non-null, but not yet on queue because
         * the CAS to place it on queue can fail. So we have to
         * traverse from tail to make sure it actually made it.  It
         * will always be near the tail in calls to this method, and
         * unless the CAS failed (which is unlikely), it will be
         * there, so we hardly ever traverse much.
         */
        return findNodeFromTail(node);
    }
````
#### 从尾到头遍历    

- AbstractQueuedLongSynchronizer.findNodeFromTail
````
private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }
````
#### 检查中断状态
````
private int checkInterruptWhileWaiting(Node node) {
    // checkInterruptWhileWaiting()：判断在阻塞过程中是否被中断。如果返回THROW_IE，则表示线程在调用signal()之前中断的；
    // 如果返回 REINTERRUPT, 则表明线程在调用signal()之后中断；如果返回0则表示没有被中断。   
    return Thread.interrupted() ?(transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :0;
  }
````

#### 设置等待条件状态
````
final boolean transferAfterCancelledWait(Node node) {
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            //设置到同步队列而且是取消状态
            enq(node);
            return true;
        }
}       
```` 
#### 是否抛出中断异常 
````
private void reportInterruptAfterWait(int interruptMode)
            throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
}                
````

#### condition.signal() 方法 

- AbstractQueuedSynchronizer.ConditionObject.signal

```
public final void signal() {
            //如果不是当前线程占有着锁，调用这个方法跑出异常
            //说明signal()也要在获取锁之后执行
            if (!isHeldExclusively()){
                throw new IllegalMonitorStateException();
            }
            //条件队列的头节点
            Node first = firstWaiter;
            //如果有等待条件的节点,则通知它条件已成立
            if (first != null){
                doSignal(first);
            }    
}
```

#### 将条件队列的头节点从条件队列转移到同步等待队列,并且,将该节点从条件队列删除

- AbstractQueuedSynchronizer.ConditionObject.doSignal

````
private void doSignal(Node first) {
            do {
                // 移到条件队列的头节点往后一位
                if((firstWaiter = first.nextWaiter) == null){
                    lastWaiter = null;
                }
                //相当于把头节点从队列中出队
                first.nextWaiter = null;
                //转移节点到AQS队列中
            } while (!transferForSignal(first) && (first = firstWaiter) != null);
}
````

##### 将节点放入等待队列并唤醒,并不需要在条件队列中移除,因为条件队列每次插入时都会把状态不为CONDITION的节点清理出去

- AbstractQueuedSynchronizer.transferForSignal

````
final boolean transferForSignal(Node node) {
        // 把节点状态改为0,也就是说即将移到AQS队列中
        // 如果失败了，说明节点已经被改成取消状态了,返回false，通过上面的循环可知会寻找下一个可用节点
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)){
            return false;
        }
        // 调用AQS的入队方法把节点移动到AQS队列中，这里的 enq()返回值是node的上一个节点，也就是旧的尾节点
        Node p = enq(node);
        // 获取上一个节点的等待状态
        int ws = p.waitStatus;
        // 如果上一个节点已取消，或者更新状态为SIGNAL失败（也就是说上一个节点已经取消）
        // 则直接唤醒当前节点对应的线程
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL)){
            LockSupport.unpark(node.thread);
        }
        //如果更新上一个节点的等待状态为SIGNAL成功了，则返回true,这时上面的的循环不成立了，退出循环，也即只通知了一个节点
        // 、
        v此时当前节点还时阻塞状态,也就是调用个signal()的时候并不会真正唤醒一个节点只是把节点从条件队列移到了AQS队列
        return true;
}
````

来自: [彤哥读源码](https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247483746&idx=1&sn=a6b5bea0cb52f23e93dd223970b2f6f9&scene=21#wechat_redirect)