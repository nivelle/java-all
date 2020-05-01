
## 读锁

```
## ReentrantReadWriteLock.ReadLock.lock()
public void lock() {
   sync.acquireShared(1);
}

## AbstractQueuedSynchronizer.acquireShared()
public final void acquireShared(int arg) {
        ## 尝试获取共享锁(返回1表示成功,返回-1表示失败)
        if (tryAcquireShared(arg) < 0){
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
            ## 读锁被获取的次数
            int r = sharedCount(c);
            ## 下面说明此时还没有写锁,尝试去更新state的值获取读锁,读锁是否需要排队(是否是公平模式)
            if (!readerShouldBlock() && r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)) {
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
                    ## 如果缓存为空,或则缓存的线程不是当前线程。 则从ThreadLocal中获取，readHolds本身是一个ThreadLocal,里面存储的是HoldCounter
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

## AbstractQueuedSynchronizer.doAcquireShared()
private void doAcquireShared(int arg) {
        ## 进入AQS的队列中
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                ## 当前节点的前一个节点
                final Node p = node.predecessor();
                ## 如果前一个节点是头节点
                if (p == head) {
                    ## 再次尝试获取读锁
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        ## 头节点后移并传播,传播即唤醒后面连续的读节点
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
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

## AbstractQueuedSynchronizer.setHeadAndPropagate()
private void  setHeadAndPropagate (Node node, int propagate) {
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


## 写锁

