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
                // 如果自旋次数小于0，则计算自旋的次数，如果当前有写锁独占且队列无元素，则自旋，如果自旋完还没入队，则自旋次数为SPINS常量，否则自选次数为0
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
                while ((c = h.cowait) != null) {
                    if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait) &&
                        (w = c.thread) != null)
                        U.unpark(w);
                }
            }
            if (whead == h) {
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
                    long time; // 0 argument to park means no timeout
                    if (deadline == 0L)
                        time = 0L;
                    else if ((time = deadline - System.nanoTime()) <= 0L)
                        return cancelWaiter(node, node, false);
                    Thread wt = Thread.currentThread();
                    U.putObject(wt, PARKBLOCKER, this);
                    node.thread = wt;
                    if (p.status < 0 && (p != h || (state & ABITS) != 0L) &&
                        whead == h && node.prev == p)
                        U.park(false, time);  // emulate LockSupport.park
                    node.thread = null;
                    U.putObject(wt, PARKBLOCKER, null);
                    if (interruptible && Thread.interrupted())
                        return cancelWaiter(node, node, true);
                }
            }
        }
    }

````