## 问题

（1）StampedLock是什么？

（2）StampedLock具有什么特性？

（3）StampedLock是否支持可重入？

（4）StampedLock与ReentrantReadWriteLock的对比？

### 简介

`StampedLock是java8中新增的类，它是一个更加高效的读写锁的实现`，而且它不是基于AQS来实现的，它的内部自成一片逻辑，让我们一起来学习吧。

- StampedLock具有三种模式：写模式、读模式、乐观读模式。

- ReentrantReadWriteLock中的读和写都是一种悲观锁的体现，StampedLock加入了一种新的模式——乐观读，它是指当乐观读时假定没有其它线程修改数据，读取完成后再检查下版本号有没有变化，没有变化就读取成功了，这种模式更适用于读多写少的场景。

## 使用方法

让我们通过下面的例子了解一下StampedLock三种模式的使用方法：

```java
class Point {
    private double x, y;
    private final StampedLock sl = new StampedLock();

    void move(double deltaX, double deltaY) {
        // 获取写锁，返回一个版本号（戳）
        long stamp = sl.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            // 释放写锁，需要传入上面获取的版本号
            sl.unlockWrite(stamp);
        }
    }

    double distanceFromOrigin() {
        // 乐观读
        long stamp = sl.tryOptimisticRead();
        double currentX = x, currentY = y;
        // 验证版本号是否有变化
        if (!sl.validate(stamp)) {
            // 版本号变了，乐观读转悲观读
            stamp = sl.readLock();
            try {
                // 重新读取x、y的值
                currentX = x;
                currentY = y;
            } finally {
                // 释放读锁，需要传入上面获取的版本号
                sl.unlockRead(stamp);
            }
        }
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }

    void moveIfAtOrigin(double newX, double newY) {
        // 获取悲观读锁
        long stamp = sl.readLock();
        try {
            while (x == 0.0 && y == 0.0) {
                // 转为写锁
                long ws = sl.tryConvertToWriteLock(stamp);
                // 转换成功
                if (ws != 0L) {
                    stamp = ws;
                    x = newX;
                    y = newY;
                    break;
                }
                else {
                    // 转换失败
                    sl.unlockRead(stamp);
                    // 获取写锁
                    stamp = sl.writeLock();
                }
            }
        } finally {
            // 释放锁
            sl.unlock(stamp);
        }
    }
}
```

从上面的例子我们可以与ReentrantReadWriteLock进行对比：

（1）写锁的使用方式基本一对待；

（2）读锁（悲观）的使用方式可以进行升级，通过tryConvertToWriteLock()方式可以升级为写锁；

（3）乐观读锁是一种全新的方式，它假定数据没有改变，乐观读之后处理完业务逻辑再判断版本号是否有改变，如果没改变则乐观读成功，如果有改变则转化为悲观读锁重试；

下面我们一起来学习它的源码是怎么实现的。

## 源码分析

### 主要内部类

```java
static final class WNode {
    // 前一个节点
    volatile WNode prev;
    // 后一个节点
    volatile WNode next;
    // 读线程所用的链表（实际是一个栈结果）
    volatile WNode cowait;    // list of linked readers
    // 阻塞的线程
    volatile Thread thread;   // non-null while possibly parked
    // 状态
    volatile int status;      // 0, WAITING, or CANCELLED
    // 读模式还是写模式
    final int mode;           // RMODE or WMODE
    WNode(int m, WNode p) { mode = m; prev = p; }
}
```

队列中的节点，类似于AQS队列中的节点，可以看到它组成了一个双向链表，内部维护着阻塞的线程。

### 主要属性

```java
// 一堆常量
// 读线程的个数占有低7位
private static final int LG_READERS = 7;
// 读线程个数每次增加的单位
private static final long RUNIT = 1L;
// 写线程个数所在的位置
private static final long WBIT  = 1L << LG_READERS;  // 128 = 1000 0000
// 读线程个数所在的位置
private static final long RBITS = WBIT - 1L;  // 127 = 111 1111
// 最大读线程个数
private static final long RFULL = RBITS - 1L;  // 126 = 111 1110
// 读线程个数和写线程个数的掩码
private static final long ABITS = RBITS | WBIT;  // 255 = 1111 1111
// 读线程个数的反数，高25位全部为1
private static final long SBITS = ~RBITS;  // -128 = 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1000 0000

// state的初始值
private static final long ORIGIN = WBIT << 1;  // 256 = 1 0000 0000
// 队列的头节点
private transient volatile WNode whead;
// 队列的尾节点
private transient volatile WNode wtail;
// 存储着当前的版本号，类似于AQS的状态变量state
private transient volatile long state;
```

通过属性可以看到，这是一个类似于AQS的结构，内部同样维护着一个状态变量state和一个CLH队列。

### 构造方法

```java
public StampedLock() {
    state = ORIGIN;
}
```

state的初始值为ORIGIN（256），它的二进制是 1 0000 0000，也就是初始版本号。

### writeLock()方法

获取写锁。

```java
public long writeLock() {
    long s, next;
    // ABITS = 255 = 1111 1111
    // WBITS = 128 = 1000 0000
    // state与ABITS如果等于0，尝试原子更新state的值加WBITS
    // 如果成功则返回更新的值，如果失败调用acquireWrite()方法
    return ((((s = state) & ABITS) == 0L &&
             U.compareAndSwapLong(this, STATE, s, next = s + WBIT)) ?
            next : acquireWrite(false, 0L));
}
```

我们以state等于初始值为例，则state & ABITS的结果为：

![StampedLock](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java同步系列/resource/StampedLock1.png)

此时state为初始状态，与ABITS与运算后的值为0，所以执行后面的CAS方法，s + WBITS的值为384 = 1 1000 0000。

到这里我们大胆猜测：state的高24位存储的是版本号，低8位存储的是是否有加锁，第8位存储的是写锁，低7位存储的是读锁被获取的次数，而且如果只有第8位存储写锁的话，那么写锁只能被获取一次，也就不可能重入了。

到底我们猜测的对不对呢，走着瞧^^

我们接着来分析acquireWrite()方法：

```java
private long acquireWrite(boolean interruptible, long deadline) {
    // node为新增节点，p为尾节点（即将成为node的前置节点）
    WNode node = null, p;
    
    // 第一次自旋——入队
    for (int spins = -1;;) { // spin while enqueuing
        long m, s, ns;
        // 再次尝试获取写锁
        if ((m = (s = state) & ABITS) == 0L) {
            if (U.compareAndSwapLong(this, STATE, s, ns = s + WBIT))
                return ns;
        }
        else if (spins < 0)
            // 如果自旋次数小于0，则计算自旋的次数
            // 如果当前有写锁独占且队列无元素，说明快轮到自己了
            // 就自旋就行了，如果自旋完了还没轮到自己才入队
            // 则自旋次数为SPINS常量
            // 否则自旋次数为0
            spins = (m == WBIT && wtail == whead) ? SPINS : 0;
        else if (spins > 0) {
            // 当自旋次数大于0时，当前这次自旋随机减一次自旋次数
            if (LockSupport.nextSecondarySeed() >= 0)
                --spins;
        }
        else if ((p = wtail) == null) {
            // 如果队列未初始化，新建一个空节点并初始化头节点和尾节点
            WNode hd = new WNode(WMODE, null);
            if (U.compareAndSwapObject(this, WHEAD, null, hd))
                wtail = hd;
        }
        else if (node == null)
            // 如果新增节点还未初始化，则新建之，并赋值其前置节点为尾节点
            node = new WNode(WMODE, p);
        else if (node.prev != p)
            // 如果尾节点有变化，则更新新增节点的前置节点为新的尾节点
            node.prev = p;
        else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
            // 尝试更新新增节点为新的尾节点成功，则退出循环
            p.next = node;
            break;
        }
    }

    // 第二次自旋——阻塞并等待唤醒
    for (int spins = -1;;) {
        // h为头节点，np为新增节点的前置节点，pp为前前置节点，ps为前置节点的状态
        WNode h, np, pp; int ps;
        // 如果头节点等于前置节点，说明快轮到自己了
        if ((h = whead) == p) {
            if (spins < 0)
                // 初始化自旋次数
                spins = HEAD_SPINS;
            else if (spins < MAX_HEAD_SPINS)
                // 增加自旋次数
                spins <<= 1;
            
            // 第三次自旋，不断尝试获取写锁
            for (int k = spins;;) { // spin at head
                long s, ns;
                if (((s = state) & ABITS) == 0L) {
                    if (U.compareAndSwapLong(this, STATE, s,
                                             ns = s + WBIT)) {
                        // 尝试获取写锁成功，将node设置为新头节点并清除其前置节点(gc)
                        whead = node;
                        node.prev = null;
                        return ns;
                    }
                }
                // 随机立减自旋次数，当自旋次数减为0时跳出循环再重试
                else if (LockSupport.nextSecondarySeed() >= 0 &&
                         --k <= 0)
                    break;
            }
        }
        else if (h != null) { // help release stale waiters
            // 这段代码很难进来，是用于协助唤醒读节点的
            // 我是这么调试进来的：
            // 起三个写线程，两个读线程
            // 写线程1获取锁不要释放
            // 读线程1获取锁，读线程2获取锁（会阻塞）
            // 写线程2获取锁（会阻塞）
            // 写线程1释放锁，此时会唤醒读线程1
            // 在读线程1里面先不要唤醒读线程2
            // 写线程3获取锁，此时就会走到这里来了
            WNode c; Thread w;
            // 如果头节点的cowait链表（栈）不为空，唤醒里面的所有节点
            while ((c = h.cowait) != null) {
                if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                    (w = c.thread) != null)
                    U.unpark(w);
            }
        }
        
        // 如果头节点没有变化
        if (whead == h) {
            // 如果尾节点有变化，则更新
            if ((np = node.prev) != p) {
                if (np != null)
                    (p = np).next = node;   // stale
            }
            else if ((ps = p.status) == 0)
                // 如果尾节点状态为0，则更新成WAITING
                U.compareAndSwapInt(p, WSTATUS, 0, WAITING);
            else if (ps == CANCELLED) {
                // 如果尾节点状态为取消，则把它从链表中删除
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
                    // 已超时，剔除当前节点
                    return cancelWaiter(node, node, false);
                // 当前线程
                Thread wt = Thread.currentThread();
                U.putObject(wt, PARKBLOCKER, this);
                // 把node的线程指向当前线程
                node.thread = wt;
                if (p.status < 0 && (p != h || (state & ABITS) != 0L) &&
                    whead == h && node.prev == p)
                    // 阻塞当前线程
                    U.park(false, time);  // 等同于LockSupport.park()
                    
                // 当前节点被唤醒后，清除线程
                node.thread = null;
                U.putObject(wt, PARKBLOCKER, null);
                // 如果中断了，取消当前节点
                if (interruptible && Thread.interrupted())
                    return cancelWaiter(node, node, true);
            }
        }
    }
}
```

这里对acquireWrite()方法做一个总结，这个方法里面有三段自旋逻辑：

第一段自旋——入队：

（1）如果头节点等于尾节点，说明没有其它线程排队，那就多自旋一会，看能不能尝试获取到写锁；

（2）否则，自旋次数为0，直接让其入队；

第二段自旋——阻塞并等待被唤醒 + 第三段自旋——不断尝试获取写锁：

（1）第三段自旋在第二段自旋内部；

（2）如果头节点等于前置节点，那就进入第三段自旋，不断尝试获取写锁；

（3）否则，尝试唤醒头节点中等待着的读线程；

（4）最后，如果当前线程一直都没有获取到写锁，就阻塞当前线程并等待被唤醒；

这么一大段逻辑看着比较闹心，其实真正分解下来还是比较简单的，无非就是自旋，把很多状态的处理都糅合到一个for循环里面处理了。

### unlockWrite()方法

释放写锁。

```java
public void unlockWrite(long stamp) {
    WNode h;
    // 检查版本号对不对
    if (state != stamp || (stamp & WBIT) == 0L)
        throw new IllegalMonitorStateException();
    // 这行代码实际有两个作用：
    // 1. 更新版本号加1
    // 2. 释放写锁
    // stamp + WBIT实际会把state的第8位置为0，也就相当于释放了写锁
    // 同时会进1，也就是高24位整体加1了
    state = (stamp += WBIT) == 0L ? ORIGIN : stamp;
    // 如果头节点不为空，并且状态不为0，调用release方法唤醒它的下一个节点
    if ((h = whead) != null && h.status != 0)
        release(h);
}
private void release(WNode h) {
    if (h != null) {
        WNode q; Thread w;
        // 将其状态改为0
        U.compareAndSwapInt(h, WSTATUS, WAITING, 0);
        // 如果头节点的下一个节点为空或者其状态为已取消
        if ((q = h.next) == null || q.status == CANCELLED) {
            // 从尾节点向前遍历找到一个可用的节点
            for (WNode t = wtail; t != null && t != h; t = t.prev)
                if (t.status <= 0)
                    q = t;
        }
        // 唤醒q节点所在的线程
        if (q != null && (w = q.thread) != null)
            U.unpark(w);
    }
}
```

写锁的释放过程比较简单：

（1）更改state的值，释放写锁；

（2）版本号加1；

（3）唤醒下一个等待着的节点；

### readLock()方法

获取读锁。

```java
public long readLock() {
    long s = state, next;  // bypass acquireRead on common uncontended case
    // 没有写锁占用，并且读锁被获取的次数未达到最大值
    // 尝试原子更新读锁被获取的次数加1
    // 如果成功直接返回，如果失败调用acquireRead()方法
    return ((whead == wtail && (s & ABITS) < RFULL &&
             U.compareAndSwapLong(this, STATE, s, next = s + RUNIT)) ?
            next : acquireRead(false, 0L));
}
```

获取读锁的时候先看看现在有没有其它线程占用着写锁，如果没有的话再检测读锁被获取的次数有没有达到最大，如果没有的话直接尝试获取一次读锁，如果成功了直接返回版本号，如果没成功就调用acquireRead()排队。

下面我们一起来看看acquireRead()方法，这又是一个巨长无比的方法，请**保持耐心**，我们一步步来分解：

（手机横屏看源码更方便）

```java
private long acquireRead(boolean interruptible, long deadline) {
    // node为新增节点，p为尾节点
    WNode node = null, p;
    // 第一段自旋——入队
    for (int spins = -1;;) {
        // 头节点
        WNode h;
        // 如果头节点等于尾节点
        // 说明没有排队的线程了，快轮到自己了，直接自旋不断尝试获取读锁
        if ((h = whead) == (p = wtail)) {
            // 第二段自旋——不断尝试获取读锁
            for (long m, s, ns;;) {
                // 尝试获取读锁，如果成功了直接返回版本号
                if ((m = (s = state) & ABITS) < RFULL ?
                    U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                    (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L))
                    // 如果读线程个数达到了最大值，会溢出，返回的是0
                    return ns;
                else if (m >= WBIT) {
                    // m >= WBIT表示有其它线程先一步获取了写锁
                    if (spins > 0) {
                        // 随机立减自旋次数
                        if (LockSupport.nextSecondarySeed() >= 0)
                            --spins;
                    }
                    else {
                        // 如果自旋次数为0了，看看是否要跳出循环
                        if (spins == 0) {
                            WNode nh = whead, np = wtail;
                            if ((nh == h && np == p) || (h = nh) != (p = np))
                                break;
                        }
                        // 设置自旋次数
                        spins = SPINS;
                    }
                }
            }
        }
        // 如果尾节点为空，初始化头节点和尾节点
        if (p == null) { // initialize queue
            WNode hd = new WNode(WMODE, null);
            if (U.compareAndSwapObject(this, WHEAD, null, hd))
                wtail = hd;
        }
        else if (node == null)
            // 如果新增节点为空，初始化之
            node = new WNode(RMODE, p);
        else if (h == p || p.mode != RMODE) {
            // 如果头节点等于尾节点或者尾节点不是读模式
            // 当前节点入队
            if (node.prev != p)
                node.prev = p;
            else if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                p.next = node;
                break;
            }
        }
        else if (!U.compareAndSwapObject(p, WCOWAIT,
                                         node.cowait = p.cowait, node))
            // 接着上一个elseif，这里肯定是尾节点为读模式了
            // 将当前节点加入到尾节点的cowait中，这是一个栈
            // 上面的CAS成功了是不会进入到这里来的
            node.cowait = null;
        else {
            // 第三段自旋——阻塞当前线程并等待被唤醒
            for (;;) {
                WNode pp, c; Thread w;
                // 如果头节点不为空且其cowait不为空，协助唤醒其中等待的读线程
                if ((h = whead) != null && (c = h.cowait) != null &&
                    U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                    (w = c.thread) != null) // help release
                    U.unpark(w);
                // 如果头节点等待前前置节点或者等于前置节点或者前前置节点为空
                // 这同样说明快轮到自己了
                if (h == (pp = p.prev) || h == p || pp == null) {
                    long m, s, ns;
                    // 第四段自旋——又是不断尝试获取锁
                    do {
                        if ((m = (s = state) & ABITS) < RFULL ?
                            U.compareAndSwapLong(this, STATE, s,
                                                 ns = s + RUNIT) :
                            (m < WBIT &&
                             (ns = tryIncReaderOverflow(s)) != 0L))
                            return ns;
                    } while (m < WBIT); // 只有当前时刻没有其它线程占有写锁就不断尝试
                }
                // 如果头节点未曾改变且前前置节点也未曾改
                // 阻塞当前线程
                if (whead == h && p.prev == pp) {
                    long time;
                    // 如果前前置节点为空，或者头节点等于前置节点，或者前置节点已取消
                    // 从第一个for自旋开始重试
                    if (pp == null || h == p || p.status > 0) {
                        node = null; // throw away
                        break;
                    }
                    // 超时检测
                    if (deadline == 0L)
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        // 如果超时了，取消当前节点
                        return cancelWaiter(node, p, false);
                    
                    // 当前线程
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    // 设置进node中
                    node.thread = wt;
                    // 检测之前的条件未曾改变
                    if ((h != pp || (state & ABITS) == WBIT) &&
                        whead == h && p.prev == pp)
                        // 阻塞当前线程并等待被唤醒
                        U.park(false, time);
                    
                    // 唤醒之后清除线程
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    // 如果中断了，取消当前节点
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, p, true);
                }
            }
        }
    }
    
    // 只有第一个读线程会走到下面的for循环处，参考上面第一段自旋中有一个break，当第一个读线程入队的时候break出来的
    
    // 第五段自旋——跟上面的逻辑差不多，只不过这里单独搞一个自旋针对第一个读线程
    for (int spins = -1;;) {
        WNode h, np, pp; int ps;
        // 如果头节点等于尾节点，说明快轮到自己了
        // 不断尝试获取读锁
        if ((h = whead) == p) {
            // 设置自旋次数
            if (spins < 0)
                spins = HEAD_SPINS;
            else if (spins < MAX_HEAD_SPINS)
                spins <<= 1;
                
            // 第六段自旋——不断尝试获取读锁
            for (int k = spins;;) { // spin at head
                long m, s, ns;
                // 不断尝试获取读锁
                if ((m = (s = state) & ABITS) < RFULL ?
                    U.compareAndSwapLong(this, STATE, s, ns = s + RUNIT) :
                    (m < WBIT && (ns = tryIncReaderOverflow(s)) != 0L)) {
                    // 获取到了读锁
                    WNode c; Thread w;
                    whead = node;
                    node.prev = null;
                    // 唤醒当前节点中所有等待着的读线程
                    // 因为当前节点是第一个读节点，所以它是在队列中的，其它读节点都是挂这个节点的cowait栈中的
                    while ((c = node.cowait) != null) {
                        if (U.compareAndSwapObject(node, WCOWAIT,
                                                   c, c.cowait) &&
                            (w = c.thread) != null)
                            U.unpark(w);
                    }
                    // 返回版本号
                    return ns;
                }
                // 如果当前有其它线程占有着写锁，并且没有自旋次数了，跳出当前循环
                else if (m >= WBIT &&
                         LockSupport.nextSecondarySeed() >= 0 && --k <= 0)
                    break;
            }
        }
        else if (h != null) {
            // 如果头节点不等待尾节点且不为空且其为读模式，协助唤醒里面的读线程
            WNode c; Thread w;
            while ((c = h.cowait) != null) {
                if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                    (w = c.thread) != null)
                    U.unpark(w);
            }
        }
        
        // 如果头节点未曾变化
        if (whead == h) {
            // 更新前置节点及其状态等
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
                // 第一个读节点即将进入阻塞
                long time;
                // 超时设置
                if (deadline == 0L)
                    time = 0L;
                else if ((time = deadline - System.nanoTime()) <= 0L)
                    // 如果超时了取消当前节点
                    return cancelWaiter(node, node, false);
                Thread wt = Thread.currentThread();
                U.putObject(wt, PARKBLOCKER, this);
                node.thread = wt;
                if (p.status < 0 &&
                    (p != h || (state & ABITS) == WBIT) &&
                    whead == h && node.prev == p)
                    // 阻塞第一个读节点并等待被唤醒
                    U.park(false, time);
                node.thread = null;
                U.putObject(wt, PARKBLOCKER, null);
                if (interruptible && Thread.interrupted())
                    return cancelWaiter(node, node, true);
            }
        }
    }
}
```

读锁的获取过程比较艰辛，一共有六段自旋，Oh my god，让我们来大致地分解一下：

（1）读节点进来都是先判断是头节点如果等于尾节点，说明快轮到自己了，就不断地尝试获取读锁，如果成功了就返回；

（2）如果头节点不等于尾节点，这里就会让当前节点入队，这里入队又分成了两种；

（3）一种是首个读节点入队，它是会排队到整个队列的尾部，然后跳出第一段自旋；

（4）另一种是非第一个读节点入队，它是进入到首个读节点的cowait栈中，所以更确切地说应该是入栈；

（5）不管是入队还入栈后，都会再次检测头节点是不是等于尾节点了，如果相等，则会再次不断尝试获取读锁；

（6）如果头节点不等于尾节点，那么才会真正地阻塞当前线程并等待被唤醒；

（7）上面说的首个读节点其实是连续的读线程中的首个，如果是两个读线程中间夹了一个写线程，还是老老实实的排队。

自旋，自旋，自旋，旋转的木马，让我忘了伤^^

### unlockRead()方法

释放读锁。

```java
public void unlockRead(long stamp) {
    long s, m; WNode h;
    for (;;) {
        // 检查版本号
        if (((s = state) & SBITS) != (stamp & SBITS) ||
            (stamp & ABITS) == 0L || (m = s & ABITS) == 0L || m == WBIT)
            throw new IllegalMonitorStateException();
        // 读线程个数正常
        if (m < RFULL) {
            // 释放一次读锁
            if (U.compareAndSwapLong(this, STATE, s, s - RUNIT)) {
                // 如果读锁全部都释放了，且头节点不为空且状态不为0，唤醒它的下一个节点
                if (m == RUNIT && (h = whead) != null && h.status != 0)
                    release(h);
                break;
            }
        }
        else if (tryDecReaderOverflow(s) != 0L)
            // 读线程个数溢出检测
            break;
    }
}

private void release(WNode h) {
    if (h != null) {
        WNode q; Thread w;
        // 将其状态改为0
        U.compareAndSwapInt(h, WSTATUS, WAITING, 0);
        // 如果头节点的下一个节点为空或者其状态为已取消
        if ((q = h.next) == null || q.status == CANCELLED) {
            // 从尾节点向前遍历找到一个可用的节点
            for (WNode t = wtail; t != null && t != h; t = t.prev)
                if (t.status <= 0)
                    q = t;
        }
        // 唤醒q节点所在的线程
        if (q != null && (w = q.thread) != null)
            U.unpark(w);
    }
}
```

读锁释放的过程就比较简单了，将state的低7位减1，当减为0的时候说明完全释放了读锁，就唤醒下一个排队的线程。

### tryOptimisticRead()方法

乐观读。

```java
public long tryOptimisticRead() {
    long s;
    return (((s = state) & WBIT) == 0L) ? (s & SBITS) : 0L;
}
```

如果没有写锁，就返回state的高25位，这里把写所在位置一起返回了，是为了后面检测数据有没有被写过。

### validate()方法

检测乐观读版本号是否变化。

```java
public boolean validate(long stamp) {
    // 强制加入内存屏障，刷新数据
    U.loadFence();
    return (stamp & SBITS) == (state & SBITS);
}
```

检测两者的版本号是否一致，与SBITS与操作保证不受读操作的影响。

### 变异的CLH队列

StampedLock中的队列是一种变异的CLH队列，图解如下：

![StampedLock](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java同步系列/resource/StampedLock2.png)

## 总结

StampedLock的源码解析到这里就差不多了，让我们来总结一下：

（1）StampedLock也是一种读写锁，它不是基于AQS实现的；

（2）StampedLock相较于ReentrantReadWriteLock多了一种乐观读的模式，以及读锁转化为写锁的方法；

（3）StampedLock的state存储的是版本号，确切地说是高24位存储的是版本号，写锁的释放会增加其版本号，读锁不会；

（4）StampedLock的低7位存储的读锁被获取的次数，第8位存储的是写锁被获取的次数；

（5）StampedLock不是可重入锁，因为只有第8位标识写锁被获取了，并不能重复获取；

（6）StampedLock中获取锁的过程使用了大量的自旋操作，对于短任务的执行会比较高效，长任务的执行会浪费大量CPU；

（7）StampedLock不能实现条件锁；

## 重点

StampedLock与ReentrantReadWriteLock的对比？

答：StampedLock与ReentrantReadWriteLock作为两种不同的读写锁方式，彤哥大致归纳了它们的异同点：

（1）两者都有获取读锁、获取写锁、释放读锁、释放写锁的方法，这是相同点；

（2）两者的结构基本类似，都是使用state + CLH队列；

（3）前者的state分成三段，高24位存储版本号、低7位存储读锁被获取的次数、第8位存储写锁被获取的次数；

（4）后者的state分成两段，高16位存储读锁被获取的次数，低16位存储写锁被获取的次数；

（5）前者的CLH队列可以看成是变异的CLH队列，连续的读线程只有首个节点存储在队列中，其它的节点存储的首个节点的cowait栈中；

（6）后者的CLH队列是正常的CLH队列，所有的节点都在这个队列中；

（7）前者获取锁的过程中有判断首尾节点是否相同，也就是是不是快轮到自己了，如果是则不断自旋，所以适合执行短任务；

（8）后者获取锁的过程中非公平模式下会做有限次尝试；

（9）前者只有非公平模式，一上来就尝试获取锁；

（10）前者唤醒读锁是一次性唤醒连续的读锁的，而且其它线程还会协助唤醒；

（11）后者是一个接着一个地唤醒的；

（12）前者有乐观读的模式，乐观读的实现是通过判断state的高25位是否有变化来实现的；

（13）前者各种模式可以互转，类似tryConvertToXxx()方法；

（14）前者写锁不可重入，后者写锁可重入；

（15）前者无法实现条件锁，后者可以实现条件锁；



