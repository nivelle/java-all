### 主要内部类

#### WNode

````
 //队列中的节点,类似于AQS队列中的节点,可以看到它组成了一个双向链表,内部维护着阻塞的线程。
 static final class WNode {
        //前一个节点
        volatile WNode prev;
        //后一个节点
        volatile WNode next;
        //读线程所用的链表,实际是一个栈结果
        volatile WNode cowait;    // list of linked readers
        //阻塞的线程
        volatile Thread thread;   // non-null while possibly parked
        //状态
        volatile int status;      // 0, WAITING, or CANCELLED
        //读模式还是写模式
        final int mode;           // RMODE or WMODE
        WNode(int m, WNode p) { mode = m; prev = p; }
    }

````

#### 主要属性

````
//读线程的个数占有低7位
private static final int LG_READERS = 7;
// 读线程个数每次增加的单位
private static final long RUNIT = 1L;
// 写线程个数所在的位置    
private static final long WBIT  = 1L << LG_READERS; //128=1000 0000
// 读线程个数所在的位置
private static final long RBITS = WBIT - 1L; //127=111 1111
//最大读线程个数
private static final long RFULL = RBITS - 1L;//126 = 111 1110
//读线程个数和写线程个数的掩码
private static final long ABITS = RBITS | WBIT;
//读线程个数的反数，高25位全部为1
private static final long SBITS = ~RBITS;  // -128 = 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1000 0000
//state的初始值
private static final long ORIGIN = WBIT << 1;  // 256 = 1 0000 0000
//队列的头节点
private transient volatile WNode whead
//队列的尾节点
private transient volatile WNode wtail;
//存储着当前的版本号，类似于AQS的状态变量state
private transient volatile long state;

````

#### writeLock()

````

public long writeLock() {
        long s, next; 
        // ABITS = 255 = 1111 1111
        // WBITS = 128 = 1000 0000
        // state与ABITS如果等于0，尝试原子更新state的值加WBITS
        // 如果成功则返回更新的值，如果失败调用acquireWrite()方法
        return ((((s = state) & ABITS) == 0L && U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ?next : acquireWrite(false, 0L));
    }

````

#### 结论：

state的高24位存储的是版本号，低8位存储的是是否有加锁，第8位存储的是写锁，低7位存储的是读锁被获取的次数，而且如果只有第8位存储写锁的话，那么写锁只能被获取一次，也就不可能重入了

#### acquireRead(boolean interruptible, long deadline)
````
  private long acquireWrite(boolean interruptible, long deadline) {
        // node为新增节点,p为尾节点
        WNode node = null, p;
        //第一段自旋--入队
        for (int spins = -1;;) { // spin while enqueuing
            long m, s, ns;
            //再次尝试获取写锁
            if ((m = (s = state) & ABITS) == 0L) {
                if (U.compareAndSwapLong(this, STATE, s, ns = s + WBIT))
                    return ns;
            }
            else if (spins < 0){
                // 如果自旋次数小于0,则计算自旋的次数,如果当前有写锁独占且队列无元素，则自旋，如果自旋完还没入队，则自旋次数为SPINS常量，否则自选次数为0
                spins = (m == WBIT && wtail == whead) ? SPINS : 0;
            }
            else if (spins > 0) {
                //当自旋次数大于0时,当前这次自旋随机减一次自旋次数
                if (LockSupport.nextSecondarySeed() >= 0){
                    --spins;
                }
            }
            else if ((p = wtail) == null) { // initialize queue
                //如果队列未初始化，新建一个空节点并初始化头节点和尾节点
                WNode hd = new WNode(WMODE, null);
                if (U.compareAndSwapObject(this, WHEAD, null, hd))
                    wtail = hd;
            }
            else if (node == null){
               //如果新增节点还未初始化，则新建之，并赋值其前置节点为尾节点
                node = new WNode(WMODE, p);
            }
            else if (node.prev != p){
               //如果尾节点有变化，则更新新增节点的前置节点为新的尾节点
                node.prev = p;
            }
            //尝试更新新增节点为新的尾节点成功，则退出循环
            else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                p.next = node;
                break;
            }
        }
        //第二次自旋——阻塞并等待唤醒
        for (int spins = -1;;) {
            // h为头节点，np为新增节点的前置节点，pp为前前置节点，ps为前置节点的状态
            WNode h, np, pp; int ps;
            //如果头节点等于前置节点，说明快轮到自己了
            if ((h = whead) == p) {
                if (spins < 0){
                    //初始化自旋次数
                    spins = HEAD_SPINS;
                 }
                else if (spins < MAX_HEAD_SPINS){
                    spins <<= 1;// 增加自旋次数
                }
                //第三次自旋，不断尝试获取写锁
                for (int k = spins;;) { // spin at head
                    long s, ns;
                    if (((s = state) & ABITS) == 0L) {
                        if (U.compareAndSwapLong(this, STATE, s,
                                                 ns = s + WBIT)) {
                            //尝试获取写锁成功，将node设置为新头节点并清除其前置节点(gc)
                            whead = node;
                            node.prev = null;
                            return ns;
                        }
                    }
                    //随机立减自旋次数，当自旋次数减为0时跳出循环再重试
                    else if (LockSupport.nextSecondarySeed() >= 0 && --k <= 0){
                        break;
                    }
                }
            }
            else if (h != null) { // help release stale waiters
                WNode c; Thread w;
                //如果头节点的cowait链表（栈）不为空，唤醒里面的所有节点
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null)
                        U.unpark(w);
                }
            }
            //如果头节点没有变化
            if (whead == h) {
                if ((np = node.prev) != p) {
                    if (np != null)
                        (p = np).next = node;   // stale
                }
                else if ((ps = p.status) == 0)
                    //如果尾节点状态为0，则更新成WAITING
                    U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
                else if (ps == CANCELLED) {
                    //如果尾节点状态为取消，则把它从链表中删除
                    if ((pp = p.prev) != null) {
                        node.prev = pp;
                        pp.next = node;
                    }
                }
                else {
                    // 有超时时间的处理
                    long time; // 0 argument to park means no timeout
                    if (deadline == 0L)
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        //已超时，剔除当前节点
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    //把node的线程指向当前线程
                    node.thread = wt;
                    if (p.status < 0 && (p != h || (state & ABITS) != 0L) &&
                        whead == h && node.prev == p)
                        //阻塞当前线程
                        U.park(false, time);  // emulate LockSupport.park
                    //当前节点被唤醒后，清除线程
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    //如果中断了，取消当前节点
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }

````

#### 结论：

- 第一段自旋--入队

(1) 如果头节点等于尾节点，说明没有其他线程排队，那就继续自旋，舱尝试获取到写锁;

(2) 否则，自旋次数为0，直接让其入队

- 第二段自旋--阻塞并等待被唤醒+第三段自旋————不断尝试获取写锁

(1) 第三段自旋在第二段自旋内部

(2) 如果头节点等于前置节点，那就进入第三段自旋，不断尝试获取写锁;

(3) 否则，尝试唤醒头节点中等待着的读线程;

(4) 最后，如果当前线程一直都没有获取写锁，那就阻塞当前线程并等待被唤醒

#### unlockWrite 写锁释放

````
public void unlockWrite(long stamp) {
        WNode h;
        // 检查版本号是否正确
        if (state != stamp || (stamp & WBIT) == 0L){
            throw new IllegalMonitorStateException();
        }
        //1. 更新版本号加1
        //2. 释放写锁
        // stamp+WBIT 实际会把state的第8位置为0,也就相当于释放了写锁
        // 同时会进1，也就是高24位整体加1
        state = (stamp += WBIT) == 0L ? ORIGIN : stamp;
        //如果头节点不为空，并且状态不为0，调用release方法唤醒它的下一个节点
        if ((h = whead) != null && h.status != 0){
            release(h);
        }
    }

````

#### release

````
private void release(WNode h) {
        if (h != null) {
            WNode q; Thread w;
            //将其状态改为0
            U.compareAndSwapInt(h, WSTATUS, WAITING, 0);
            //如果头节点的下一个节点为空或者其状态为已取消
            if ((q = h.next) == null || q.status == CANCELLED) {
                //从尾节点向前遍历找到一个可用的节点
                for (WNode t = wtail; t != null && t != h; t = t.prev)
                    if (t.status <= 0)
                        q = t;
            }
            // 唤醒q 节点所在的线程
            if (q != null && (w = q.thread) != null)
                U.unpark(w);
        }
    }

````

#### 结论：

(1)更改state的值，释放写锁

(2)版本号加1

(3)唤醒下一个等待着的节点


#### readLock() 获取读锁
 
````
public long readLock() {
        long s = state, next;  // bypass acquireRead on common uncontended case
        //没有写锁占着,并且读锁被获取的次数未达到最大值
        //尝试原子更新读锁被获取的次数加1
        //如果成功直接返回，如果失败调用acquireRead()方法
        return ((whead == wtail && (s & ABITS) < RFULL &&U.compareAndSwapLong(this, STATE, s, next = s + RUNIT)) ? next : acquireRead(false, 0L));
    }

````

#### acquireRead(boolean interruptible, long deadline)

````
    private long acquireRead(boolean interruptible, long deadline) {
        // node 为新增节点，p为尾节点
        WNode node = null, p;
        //第一段自旋————入队
        for (int spins = -1;;) {
            // 头节点
            WNode h;
            //如果头节点等于尾节点，说明没有排队的线程，直接自旋不断尝试获取读锁
            if ((h = whead) == (p = wtail)) {
                //第二段自旋————不断尝试获取读锁
                for (long m, s, ns;;) {
                     //尝试获取读锁，如果成功了直接返回版本号
                    if ((m = (s = state) & ABITS) < RFULL ?
                        U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                        (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L))
                        //如果读线程个数达到了最大值,会溢出，返回的是0
                        return ns;
                    else if (m >= WBIT) {
                        //随机立减自旋次数
                        if (spins > 0) {
                            if (LockSupport.nextSecondarySeed() >= 0)
                                --spins;
                        }
                        else {
                            //如果自旋次数为0了,看看是否要跳出循环
                            if (spins == 0) {
                                WNode nh = whead, np = wtail;
                                if ((nh == h && np == p) || (h = nh) != (p = np))
                                    break;
                            }
                            //设置自旋次数
                            spins = SPINS;
                        }
                    }
                }
            }
            //如果尾节点为空,初始化头节点和尾节点
            if (p == null) { // initialize queue
                WNode hd = new WNode(WMODE, null);
                if (U.compareAndSwapObject(this, WHEAD, null, hd))
                    wtail = hd;
            }
            else if (node == null)
                //如果新增节点为空，初始化
                node = new WNode(RMODE, p);
            else if (h == p || p.mode != RMODE) {
                //如果头节点等于尾节点不是读模式，当前节点入队
                if (node.prev != p)
                    node.prev = p;
                else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                    p.next = node;
                    break;
                }
            }
            else if (!U.compareAndSwapObject(p, WCOWAIT,node.cowait = p.cowait, node))
                //接着上一个else if ,这里是尾节点为读模式了，将当前节点加入到尾节点的cowait中，这是一个栈，上面的
                //CAS成功了是不会进入到这里来的
                node.cowait = null;
            else {
                //第三段自旋————阻塞当前线程并等待被唤醒
                for (;;) {
                    WNode pp, c; Thread w;
                    // 如果头节点不为空且其cowait不为空，协助唤醒其中等待的读线程
                    if ((h = whead) != null && (c = h.cowait) != null &&
                        U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null) // help release
                        U.unpark(w);
                    
                    //如果头节点等待前前置节点或者等于前置节点或者前前置节点为空
                    if (h == (pp = p.prev) || h == p || pp == null) {
                        long m, s, ns;
                        //第四段自旋————又是不断尝试获取锁
                        do {
                            if ((m = (s = state) & ABITS) < RFULL ?
                                U.compareAndSwapLong(this, STATE, s,
                                                     ns = s + RUNIT) :
                                (m < WBIT &&
                                 (ns = tryIncReaderOverflow(s)) != 0L))
                                return ns;
                        } while (m < WBIT);//只有当前时刻没有其他线程占有写锁就不断尝试
                    }
                    //如果头节点未曾改变且前前置节点也未曾改，阻塞当前线程
                    if (whead == h && p.prev == pp) {
                        long time;
                        //如果前前置节点为空，或者头节点等于前置节点，或者前置节点已取消，从第一个for自旋开始重试
                        if (pp == null || h == p || p.status > 0) {
                            node = null; // throw away
                            break;
                        }
                        //超时检测
                        if (deadline == 0L)
                            time = 0L;
                        else if ((time = deadline - System.nanoTime()) <= 0L)
                            //如果超时了，取消当前节点
                            return cancelWaiter(node, p, false);
                        
                        //当前线程
                        Thread wt = Thread.currentThread();
                        U.putObject(wt, PARKBLOCKER, this);
                        //设置进node中
                        node.thread = wt;
                        //检测之前的条件未曾改变
                        if ((h != pp || (state & ABITS) == WBIT) &&
                            whead == h && p.prev == pp)
                            //阻塞当前线程并等待被唤醒
                            U.park(false, time);
                        
                        //唤醒之后清除线程
                        node.thread = null;
                        U.putObject(wt, PARKBLOCKER, null);
                        if (interruptible && Thread.interrupted())
                            //如果中断了，取消当前节点
                            return cancelWaiter(node, p, true);
                    }
                }
            }
        }
        //只有第一个读线程会走到下面for循环处，参考上面第一段自旋中有一个break,当第一个读线程入队的时候break出来的
        
        //第五段自旋————这里单独搞一个自旋针对第一个读线程
        for (int spins = -1;;) {
            WNode h, np, pp; int ps;
            //如果头节点等于尾节点，快轮到自己，不断尝试获取读锁
            if ((h = whead) == p) {
                //设置自旋次数
                if (spins < 0)
                    spins = HEAD_SPINS;
                else if (spins < MAX_HEAD_SPINS)
                    spins <<= 1;
                    
                //第六段自旋————不断尝试获取读锁
                for (int k = spins;;) { // spin at head
                    long m, s, ns;
                    //不断尝试获取读锁
                    if ((m = (s = state) & ABITS) < RFULL ?
                        U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                        (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L)) {
                        //获取到了读锁
                        WNode c; Thread w;
                        whead = node;
                        node.prev = null;
                        //唤醒当前节点中所有等待着的读线程，
                        //因为当前节点是第一个读节点，所以它是在队列中的，其他读节点都是挂在这个节点的cowait栈中的
                        while ((c = node.cowait) != null) {
                            if (U.compareAndSwapObject(node, WCOWAIT,
                                                       c, c.cowait) &&
                                (w = c.thread) != null)
                                U.unpark(w);
                        }
                        //返回版本号
                        return ns;
                    }
                    //如果当前有其他线程占有写锁，并且没有自旋次数了，跳出当前循环
                    else if (m >= WBIT &&
                             LockSupport.nextSecondarySeed() >= 0 && --k <= 0)
                        break;
                }
            }
            else if (h != null) {
                //如果头节点不等于尾节点且不为空且为读模式，协助唤醒里面的读线程
                WNode c; Thread w;
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null)
                        U.unpark(w);
                }
            }
            //如果头节点未曾变化
            if (whead == h) {
                //更新前置节点及其状态
                if ((np = node.prev) != p) {
                    if (np != null)
                        (p = np).next = node;   // stale
                }
                else if ((ps = p.status) == 0)
                    U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
                else if (ps == CANCELLED) {
                    if ((pp = p.prev) != null) {
                        node.prev = pp;
                        pp.next = node;
                    }
                }
                else {
                   //第一个读节点即将进入阻塞
                    long time;
                    if (deadline == 0L)
                        //超时设置
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        //如果超时了取消当前节点
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    node.thread = wt;
                    if (p.status < 0 &&
                        (p != h || (state & ABITS) == WBIT) &&
                        whead == h && node.prev == p)
                        //阻塞第一个读节点并等待被唤醒
                        U.park(false, time);
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }


````

#### 结论:

(1). 读节点进来都是判断是头节点如果等于尾节点，说明快到自己了，不断尝试获取读锁，如果成功了就返回;

(2). 如果头节点不等于尾节点，这里就会让当前节点入队，这里入队又分成了两种

(3). 一种是首个读节点入队，它是会排队到整个队列的尾部，然后跳出第一段自旋

(4). 另一种是非第一个读节点入队，它是进入到首个读节点的cowait栈中，所以更确切地说应该是入栈;

(5). 不管是入队还是入栈后，都会再次检测头节点是不是等于尾节点了，如果相等，则会再次不断尝试获取读锁；

(6). 如果头节点不等于尾节点，那么才会真正地阻塞当前线程并等待被唤醒；

(7). 首个读节点其实是连续的读线程中的首个，如果是两个读线程中夹了一个写线程，还继续排队

####  unlockRead(long stamp) //释放读锁

````
public void unlockRead(long stamp) {
        long s, m; WNode h;
        //检查版本号
        for (;;) {
            if (((s = state) & SBITS) != (stamp & SBITS) ||
                (stamp & ABITS) == 0L || (m = s & ABITS) == 0L || m == WBIT)
                throw new IllegalMonitorStateException();
            //读线程个数正常
            if (m < RFULL) {
                //释放一次读锁
                if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                    //如果读锁全部释放了，且头节点不为空且状态不为0，唤醒它的下一个节点
                    if (m == RUNIT && (h = whead) != null && h.status != 0)
                        release(h);
                    break;
                }
            }
            else if (tryDecReaderOverflow(s) != 0L)
                //读线程个数溢出检测
                break;
        }
    }
````
#### release(WNode h)

````

private void release(WNode h) {
        if (h != null) {
            WNode q; Thread w;
            //将其状态改为0
            U.compareAndSwapInt(h, WSTATUS, WAITING, 0);
            //如果头节点的下一个节点为空或者其状态为已取消
            if ((q = h.next) == null || q.status == CANCELLED) {
                //从尾节点向前遍历找到一个可用的节点
                for (WNode t = wtail; t != null && t != h; t = t.prev)
                    if (t.status <= 0)
                        q = t;
            }
            //唤醒q节点所在的线程
            if (q != null && (w = q.thread) != null)
                U.unpark(w);
        }
    }
    
````

##### 结论：读锁释放的过程就比较简单了，将state的低7位减1，当减为0的时候说明完全释放了读锁，就唤醒下一个排队的线程

#### tryOptimisticRead()方法 乐观读锁

````
public long tryOptimisticRead() {
        long s;
        return (((s = state) & WBIT) == 0L) ? (s & SBITS) : 0L;
    }

````
##### 结论：如果没有写锁，就返回state的高25位，这里把写所在位置一起返回了，是为了后面检测数据有没有被写过。
         
#### validate()方法

````
public boolean validate(long stamp) {
    // 强制加入内存屏障，刷新数据
    U.loadFence();
    return (stamp & SBITS) == (state & SBITS);
}

````

#### 变异的CLH队列

- StampedLock 中的队列是一种变异的CLH队列

#### StampedLock与 ReentrantReadWriteLock 的对比

(1) StampedLock 的state分成三段，高24位存储版本号，低7位存储锁被获取的次数，第8位存储写锁被获取的次数

(2) ReentrantReadWriteLock 的state分成两段，高16位存储读锁被获取的次数，低16位存储写锁被获取的次数

(3) StampedLock 的CLH队列可以看成是变异的CLH队列，连续的读线程只有首个节点存储在队列中，其它的节点存储的首个节点的cowait栈中;ReentrantReadWriteLock的CLH队列是正常的CLH队列，所有的节点都在这个队列中

(4) StampedLock 获取锁的过程中有判断首尾节点是否相同，也就是是不是快轮到自己了，如果是则不断自旋，所以适合执行短任务;ReentrantReadWriteLock 获取锁的过程中非公平模式下会做有限次尝试

(5) StampedLock 只有非公平模式，一上来就尝试获取锁

(6) StampedLock 唤醒读锁是一次性唤醒连续的读锁的，而且其它线程还会协助唤醒;ReentrantReadWriteLock 是一个接着一个地唤醒的

(7) StampedLock 有乐观读的模式，乐观读的实现是通过判断state的高25位是否有变化来实现的，各种模式可以互转，类似tryConvertToXxx()方法

(8) StampedLock 写锁不可重入，ReentrantReadWriteLock 后者写锁可重入

(9) StampedLock无法实现条件锁，ReentrantReadWriteLock 可以实现条件锁

[![CLH变异队列](https://s1.ax1x.com/2020/11/07/B5WxSA.png)](https://imgchr.com/i/B5WxSA)












