
## 构造条件锁

```
## ReentrantLock.newCondition()
public Condition newCondition() {
        return sync.newCondition();
}

## ReentrantLock.Sync.newCondition()
final ConditionObject newCondition() {
     return new ConditionObject();
}

## AbstractQueuedSynchronizer.ConditionObject.ConditionObject()
public ConditionObject() {}
     
```

## 核心属性

- 条件锁中也维护了一个队列，为了和AQS的队列区分，我这里称为条件队列，firstWaiter是队列的头节点，lastWaiter是队列的尾节点

```
/** First node of condition queue. */
private transient Node firstWaiter;

/** Last node of condition queue. */
private transient Node lastWaiter;

```

## condition.await方法

```
## AbstractQueuedSynchronizer.ConditionObject.await()
public final void await() throws InterruptedException {
            ## 如果线程中断了，抛出异常
            if (Thread.interrupted()){
                throw new InterruptedException();
            }
            ## 添加新节点到Condition队列中,并返回该节点
            Node node = addConditionWaiter();            
            ## 完全释放当前线程获取的锁;因为锁是可重入的,所以这里要把所获取的锁都释放
            int savedState = fullyRelease(node);
            int interruptMode = 0;            
            ## 是否在同步队列中
            while (!isOnSyncQueue(node)) {
                ## 阻塞当前线程
                LockSupport.park(this);
                ## 上面部分是调用await()时释放自己占有的锁,并阻塞自己等待条件的出现
                
                ## 下面部分是条件已经出现，尝试去获取锁
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0){
                    break;
                }
            }
            ## 尝试获取锁;如果没获取到会再次阻塞
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE){
                interruptMode = REINTERRUPT;
            }
            ## 清除取消的节点
            if (node.nextWaiter != null){
                unlinkCancelledWaiters();
            }
            ## 线程中断相关
            if (interruptMode != 0){
                reportInterruptAfterWait(interruptMode);
            }
}

## AbstractQueuedSynchronizer.ConditionObject.addConditionWaiter
private Node addConditionWaiter() {
            Node t = lastWaiter;
            // If lastWaiter is cancelled, clean out.
            ## 如果条件队列的尾节点已取消，从头节点开始清除所有已取消的节点
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                ## 重新获取尾节点
                t = lastWaiter;
            }
            ## 新建一个节点，它的等待状态是CONDITION
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            ## 如果尾节点为空，则把新节点赋值给头节点（初始化队列）
            ## 否则把新节点赋值给尾节点的nextWaiter指针
            if (t == null){
                firstWaiter = node;
            }else{
                t.nextWaiter = node;
            }
            ## 尾节点指向新节点
            lastWaiter = node;
            ## 返回新节点
            return node;
}

## AbstractQueuedSynchronizer.fullyRelease
final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            ## 获取状态变量的值，重复获取锁，这个值会一直累加；所以这个值也代表着获取锁的次数
            int savedState = getState();
            ## 一次性释放所有获得的锁
            if (release(savedState)) {
                failed = false;
                ## 返回获取锁的次数
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
## AbstractQueuedLongSynchronizer
public final boolean release(long arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0){
                unparkSuccessor(h);
            }
            return true;
        }
        return false;
 }
 
 ## ReentrantLock
protected final boolean tryRelease(int releases) {
             int c = getState() - releases;
             if (Thread.currentThread() != getExclusiveOwnerThread()){
                 throw new IllegalMonitorStateException();
             }
             boolean free = false;
             if (c == 0) {
                 free = true;
                 setExclusiveOwnerThread(null);
             }
             setState(c);
             return free;
}

## AbstractQueuedSynchronizer.isOnSyncQueue
final boolean isOnSyncQueue(Node node) {
        ## 如果等待状态是 CONDITION 或者 前一个指针为空，返回false
        ## 说明还没有移到AQS的队列中
        if (node.waitStatus == Node.CONDITION || node.prev == null){
            return false;
        }
        ## 如果next指针有值,说明已经移到AQS的队列中了
        if (node.next != null) {// If has successor, it must be on queue
            return true;
        }
        ## 从ASQ尾节点从开始往前寻找是否可以找到当前节点，找到了也就说明已经在AQS的队列中了
        return findNodeFromTail(node);
    }           
```

### 重点

- Condition的队列和AQS的队列不完全一样:

```
AQS的队列头节点是不存在任何值的,是一个虚节点;

Condition的队列头节点是存储着实实在在的元素值的,是真实节点

```

-  各种等待状态(waitStatus)的变化

```
1. 在条件队列中，新建节点的初始等待状态是CONDITION（-2）

2. 移到AQS的队列中时等待状态会更改为0(AQS队列节点的初始等待状态为0)

3. 在AQS队列中如果需要阻塞，会把它上一个节点的等待状态设置为SIGNAL(-1)

4. 不管在Condition队列还是AQS队列中，已取消的节点的等待状态都会设置为CANCELLED（1）

```

- AQS 中的下一个节点是next,上一个节点prev;Condition中下一个节点是nextWaiter没有上一个节点

## condition.signal 方法 

```
## AbstractQueuedSynchronizer.ConditionObject.signal
public final void signal() {
            ## 如果不是当前线程占有着锁，调用这个方法跑出异常
            ## 说明signal()也要在获取锁之后执行
            if (!isHeldExclusively()){
                throw new IllegalMonitorStateException();
            }
            ## 条件队列的头节点
            Node first = firstWaiter;
            ## 如果有等待条件的节点,则通知它条件已成立
            if (first != null){
                doSignal(first);
            }    
}

## AbstractQueuedSynchronizer.ConditionObject.doSignal
private void doSignal(Node first) {
            do {
                ## 移到条件队列的头节点往后一位
                if((firstWaiter = first.nextWaiter) == null){
                    lastWaiter = null;
                }
                ## 相当于把头节点从队列中出队
                first.nextWaiter = null;
               ## 转移节点到AQS队列中
            } while (!transferForSignal(first) &&(first = firstWaiter) != null);
}

## AbstractQueuedSynchronizer.transferForSignal
final boolean transferForSignal(Node node) {
        ## 把节点状态改为0,也就是说即将移到AQS队列中
        ## 如果失败了，说明节点已经被改成取消状态了
        ## 返回false，通过上面的循环可知会寻找下一个可用节点
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)){
            return false;
        }
        ##调用AQS的入队方法把节点移动到AQS队列中，这里的enq()返回值是node的上一个节点，也就是旧的尾节点
        Node p = enq(node);
        ## 获取上一个节点的等待状态
        int ws = p.waitStatus;
        ## 如果上一个节点已取消，或者更新状态为SIGNAL失败（也就是说上一个节点已经取消）
        ## 则直接唤醒当前节点对应的线程
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL)){
            LockSupport.unpark(node.thread);
        }
        ## 如果更新上一个节点的等待状态为SIGNAL成功了，则返回true,这时上面的的循环不成立了，退出循环，也即只通知了一个节点
        ## 此时当前节点还时阻塞状态，也就是调用个signal()的时候并不会真正唤醒一个节点只是把节点从条件队列移到了AQS队列
        return true;
}
```