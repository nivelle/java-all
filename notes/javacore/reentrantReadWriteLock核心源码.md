
## 读锁

### ReadLock.lock
```
## ReentrantReadWriteLock.ReadLock.lock()
public void lock() {
   sync.acquireShared(1);
}

## 共享模式获取资源
## AbstractQueuedSynchronizer.acquireShared()
public final void acquireShared(int arg) {
        ## 尝试获取共享锁(返回1表示成功,返回-1表示失败)
        if (tryAcquireShared(arg) < 0){//模版方法，这里以读写锁为例
            ## 失败了就可能要排队
            doAcquireShared(arg);
        }
}

## ReentrantReadWriteLock.Sync.tryAcquireShared()
protected final int tryAcquireShared(int unused) {           
            Thread current = Thread.currentThread();
            ## 状态变量的值
            ## 在读写锁模式下,高16位存储的是共享锁(读锁)被获取的次数,低16位存储的是互斥锁(写锁)被获取的次数
            int c = getState();
            ## 互斥锁的次数,如果其它线程获得了写锁,直接返回-1
            if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current){
                return -1;
            }
            ## 读锁被获取的次数（state高16位）
            int r = sharedCount(c);
            
            ## readerShouldBlock:如果是当前线程尝试获取锁则返回true
            ## 下面说明此时还没有写锁,尝试去更新state的值获取读锁,读锁是否需要排队(是否是公平模式:公平模式需要排队，非公平模式不需要排毒)           
            if (!readerShouldBlock() && r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)) { ## SHARED_UNIT = 1<<16 = 65535
                ## 读锁获取成功
                if (r == 0) {
                    ## 如果之前还没有线程获取读书，记录第一个读者为当前线程
                    firstReader = current;
                    firstReaderHoldCount = 1;
                } else if (firstReader == current) {
                    ## 如果有线程获取了读锁且是当前线程是第一个读者，则把其重入次数+1
                    firstReaderHoldCount++;
                } else {
                    ## 如果有线程获取了读锁且当前线程不是第一个读者,则从缓存中获取重入次数保存器
                    HoldCounter rh = cachedHoldCounter;
                    ## 如果缓存为空,或则缓存的线程不是当前线程。 则从ThreadLocal中获取,readHolds本身是一个ThreadLocal,里面存储的是HoldCounter
                    ## holdCounter存的是最近一个线程的存储的读锁次数
                    if (rh == null || rh.tid != getThreadId(current)){
                        ## get()的时候会初始化rh
                        cachedHoldCounter = rh = readHolds.get();
                    }
                    else if (rh.count == 0){
                        ## 如果rh的次数为0,把它放到ThreadLocal中去
                        readHolds.set(rh);
                    }
                    ## 重入的次数+1(初始化次数为0)
                    rh.count++;
                }
                return 1;
            }
            ## 通过这个方法再去尝试获取读锁(如果之前其它线程获取了写锁，一样返回-1表示失败)
            return fullTryAcquireShared(current);
}
## ReentrantReadWriteLock.Sync.fullTryAcquireShared
final int fullTryAcquireShared(Thread current) {
            /*
             * This code is in part redundant with that in
             * tryAcquireShared but is simpler overall by not
             * complicating tryAcquireShared with interactions between
             * retries and lazily reading hold counts.
             */
            HoldCounter rh = null;
            for (;;) {
                int c = getState();
                if (exclusiveCount(c) != 0) {
                    if (getExclusiveOwnerThread() != current)
                        return -1;
                    // else we hold the exclusive lock; blocking here
                    // would cause deadlock.
                } else if (readerShouldBlock()) {
                    // Make sure we're not acquiring read lock reentrantly
                    if (firstReader == current) {
                        // assert firstReaderHoldCount > 0;
                    } else {
                        if (rh == null) {
                            rh = cachedHoldCounter;
                            if (rh == null || rh.tid != getThreadId(current)) {
                                rh = readHolds.get();
                                if (rh.count == 0)
                                    readHolds.remove();
                            }
                        }
                        if (rh.count == 0)
                            return -1;
                    }
                }
                if (sharedCount(c) == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (sharedCount(c) == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        if (rh == null)
                            rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                        cachedHoldCounter = rh; // cache for release
                    }
                    return 1;
                }
            }
        }
 
## 实现上和acquire()方法差不多，就是多判断了是否还有剩余资源，挨个唤醒后继节点              
## AbstractQueuedSynchronizer.doAcquireShared()
private void doAcquireShared(int arg) {
        ## 进入AQS的队列中
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                ## 当前节点的前置节点
                final Node p = node.predecessor();
                ## 如果前一个节点是头节点
                if (p == head) {
                    ## 再次尝试获取读锁
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        ## 头节点后移并传播,传播即唤醒后面连续的读节点
                        setHeadAndPropagate(node, r);
                        
                        p.next = null; // help GC
                        if (interrupted){
                            selfInterrupt();
                        }
                        failed = false;
                        return;
                    }
                }
                ## 没获取到读锁，阻塞并等待被唤醒
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()){
                    interrupted = true;
                }
            }
        } finally {
            if (failed){
                cancelAcquire(node);
            }
        }
}

## 设置头节点，且如果还有剩余资源，唤醒后继节点获取资源
## AbstractQueuedSynchronizer.setHeadAndPropagate()
private void setHeadAndPropagate (Node node, int propagate) {
        ## h为旧的头节点
        Node h = head;
        ## 设置当前节点为新头节点
        setHead(node);
        ## 如果旧的头节点或新的头节点为空
        ## 或者其等待状态小于0(表示状态为SIGNAL/PROPAGATE)
        if (propagate > 0 || h == null || h.waitStatus < 0 || (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            ## 如果下一个节点为空，或者是需要获取读锁的节点
            if (s == null || s.isShared()){
                ## 唤醒下一个节点
                doReleaseShared();
            }
        }
}

## AbstractQueuedSynchronizer.doReleaseShared()
## 这个方法只会唤醒一个节点
private void doReleaseShared() {
       
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                ## 如果头节点状态为SIGNAL，说明要唤醒下一个节点                  
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)){
                        continue;  
                    }
                    ## 唤醒下一个节点            
                    unparkSuccessor(h);
                }
                ## 把头节点的状态改为PROPAGATE成功才会跳到下面的if条件
                else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE)){
                    continue;  
                }                  
            }
            ## 唤醒后head头节点没有变,则退出循环
            if (h == head) {                  
                break;
            }    
        }
}

```
### ReadLock.unlock()

```
## java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock.unlock
public void unlock() {
    sync.releaseShared(1);
}

## java.util.concurrent.locks.AbstractQueuedSynchronizer.releaseShared
public final boolean releaseShared(int arg) {
        ## 如果尝试释放成功了，就唤醒下一个节点
        if (tryReleaseShared(arg)) {
            ## 这个方法实际是唤醒下一个节点
            doReleaseShared();
            return true;
        }
        return false;
}

## java.util.concurrent.locks.ReentrantReadWriteLock.Sync.tryReleaseShared
protected final boolean tryReleaseShared(int unused) {
            Thread current = Thread.currentThread();
            if (firstReader == current) {
                ## 如果第一个读者(读线程)是当前线程,就把它重入的次数-1，如果减到0了就把第一个读者置为空
                if (firstReaderHoldCount == 1){
                    firstReader = null;
                }
                else{
                    firstReaderHoldCount--;
                }
            } else {
                ## 如果第一个读者不是当前线程,一样地把它重入的次数-1
                HoldCounter rh = cachedHoldCounter;
                if (rh == null || rh.tid != getThreadId(current)){
                    rh = readHolds.get();
                }
                int count = rh.count;
                if (count <= 1) {
                    readHolds.remove();
                    if (count <= 0){
                        throw unmatchedUnlockException();
                    }
                }
                --rh.count;
            }
            for (;;) {
                ## 共享锁获取的次数-1
                ## 如果减到0，说明完全释放了，才返回true
                int c = getState();
                int nextc = c - SHARED_UNIT;
                if (compareAndSetState(uy kmxzc, nextc)){                
                    return nextc == 0;
                }
            }
        }
        
## java.util.concurrent.locks.AbstractQueuedSynchronizer.doReleaseShared
private void doReleaseShared() {
      
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                ## 如果头节点状态为SIGNAL 说明要唤醒下一个节点
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)){
                        continue;  
                    }
                    ## 唤醒下一个节点             
                    unparkSuccessor(h);
                }
                ## 把头节点的状态改为PROPAGATE成功才会跳到下面的if
                else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                
            }
            ## 如果唤醒后head没变，则跳出循环
            if (h == head){                 
                break;
            }
        }
    }       
```

## 写锁

### WriteLock.lock()


```
## java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock.unlock()
public void lock() {
            sync.acquire(1);
}

## 独占模式获取资源
## java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire()
public final void acquire(int arg) {
         ##先尝试获取锁;如果失败，则会进入队列中排队，后面的逻辑跟ReentrantLock一模一样了
        if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)){
            selfInterrupt();
        }
}   

```     
1）tryAcquire()尝试获取资源。

2）如果获取失败，则通过addWaiter(Node.EXCLUSIVE), arg)方法把当前线程添加到等待队列队尾，并标记为独占模式。

3）插入等待队列后，并没有放弃获取资源，acquireQueued()自旋尝试获取资源。根据前置节点状态状态判断是否应该继续获取资源。如果前驱是头结点，继续尝试获取资源；

4）在每一次自旋获取资源过程中，失败后调用shouldParkAfterFailedAcquire(Node, Node)检测当前节点是否应该park()。若返回true，则调用parkAndCheckInterrupt()中断当前节点中的线程。若返回false，则接着自旋获取资源。当acquireQueued(Node,int)返回true，则将当前线程中断；false则说明拿到资源了。

5）在进行是否需要挂起的判断中，如果前置节点是SIGNAL状态，就挂起，返回true。如果前置节点状态为CANCELLED，就一直往前找，直到找到最近的一个处于正常等待状态的节点，并排在它后面，返回false，acquireQueed()接着自旋尝试，回到3）。

6）前置节点处于其他状态，利用CAS将前置节点状态置为SIGNAL。当前置节点刚释放资源，状态就不是SIGNAL了，导致失败，返回false。但凡返回false，就导致acquireQueed()接着自旋尝试。

7）最终当tryAcquire(int)返回false，acquireQueued(Node,int)返回true，调用selfInterrupt()，中断当前线程。


```
## java.util.concurrent.locks.ReentrantReadWriteLock.Sync.tryAcquire()
protected final boolean tryAcquire(int acquires) {
           
            Thread current = Thread.currentThread();
            ## 状态变量state的值
            int c = getState();
            ## 互斥锁被获取的次数
            int w = exclusiveCount(c);
            if (c != 0) {
                ## 如果c!=0且w==0，说明共享锁被获取的次数不为0(如果共享锁被获取的次数不为-0，或者被其他线程获取了互斥锁）,那么就返回false,获取写锁失败
                if (w == 0 || current != getExclusiveOwnerThread()){
                    return false;
                }
                ## 检出溢出
                if (w + exclusiveCount(acquires) > MAX_COUNT){
                    throw new Error("Maximum lock count exceeded");
                }
                ## 到这里就说明当前线程已经获取了写锁，这里是重入，直接把state+1即可
                setState(c + acquires);
                ## 获取写锁成功
                return true;
            }
            ## 如果c=0，就尝试更新state的值（非公平模式writeShouldBlok返回false）
            ## 如果失败了，说明获取写锁失败，返回false
            ## 如果成功了，说明获取写锁成功，把自己设置为占有者
            if (writerShouldBlock() || !compareAndSetState(c, c + acquires)){
                return false;
            }
            setExclusiveOwnerThread(current);
            return true;
        }    

```



### WriteLock.unlock()

```
## java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock.unlock()
public void unlock() {
            sync.release(1);
        }
        
## java.util.concurrent.locks.AbstractQueuedSynchronizer.release()
public final boolean release(int arg) {
        ## 如果尝试释放锁成功（完全释放锁）,就尝试唤醒下一个节点
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }
    
## java.util.concurrent.locks.ReentrantReadWriteLock.Sync.tryRelease()
protected final boolean tryRelease(int releases) {
            ## 如果写锁不是当前线程占有者，抛出异常
            if (!isHeldExclusively()){
                throw new IllegalMonitorStateException();
            }
            ## 状态变量的值-1
            int nextc = getState() - releases;
            ## 是否完全是否锁
            boolean free = exclusiveCount(nextc) == 0;
            if (free)
                setExclusiveOwnerThread(null);
            ## 设置状态变量的值
            setState(nextc);
            ## 如果完全释放了写锁，返回true
            return free;
}

```

来自: [彤哥读源码](https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247483746&idx=1&sn=a6b5bea0cb52f23e93dd223970b2f6f9&scene=21#wechat_redirect)