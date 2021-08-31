
## 问题

- （1）AQS是什么？

- （2）AQS的定位？

- （3）AQS的实现原理？

- （4）基于AQS实现自己的锁？

## 简介

AQS的全称是AbstractQueuedSynchronizer，它的定位是为Java中几乎所有的锁和同步器提供一个基础框架。

AQS是基于FIFO的队列实现的，并且内部维护了一个状态变量state，通过原子更新这个状态变量state即可以实现加锁解锁操作。

## 核心源码

### 主要内部类

```java
static final class Node {
    // 标识一个节点是共享模式
    static final Node SHARED = new Node();
    // 标识一个节点是互斥模式
    static final Node EXCLUSIVE = null;

    // 标识线程已取消
    static final int CANCELLED =  1;
    // 标识后继节点需要唤醒
    static final int SIGNAL    = -1;
    // 标识线程等待在一个条件上
    static final int CONDITION = -2;
    // 标识后面的共享锁需要无条件的传播（共享锁需要连续唤醒读的线程）
    static final int PROPAGATE = -3;
    
    // 当前节点保存的线程对应的等待状态
    volatile int waitStatus;

    // 前一个节点
    volatile Node prev;
    
    // 后一个节点
    volatile Node next;

    // 当前节点保存的线程
    volatile Thread thread;

    // 下一个等待在条件上的节点（Condition锁时使用）
    Node nextWaiter;

    // 是否是共享模式
    final boolean isShared() {
        return nextWaiter == SHARED;
    }

    // 获取前一个节点
    final Node predecessor() throws NullPointerException {
        Node p = prev;
        if (p == null)
            throw new NullPointerException();
        else
            return p;
    }

    // 节点的构造方法
    Node() {    // Used to establish initial head or SHARED marker
    }

    // 节点的构造方法
    Node(Thread thread, Node mode) {     // Used by addWaiter
        // 把共享模式还是互斥模式存储到nextWaiter这个字段里面了
        this.nextWaiter = mode;
        this.thread = thread;
    }

    // 节点的构造方法
    Node(Thread thread, int waitStatus) { // Used by Condition
        // 等待的状态，在Condition中使用
        this.waitStatus = waitStatus;
        this.thread = thread;
    }
}
```

典型的双链表结构，节点中保存着当前线程、前一个节点、后一个节点以及线程的状态等信息。

### 主要属性

```java
// 队列的头节点
private transient volatile Node head;
// 队列的尾节点
private transient volatile Node tail;
// 控制加锁解锁的状态变量
private volatile int state;
```

定义了一个状态变量和一个队列，状态变量用来控制加锁解锁，队列用来放置等待的线程。

注意，这几个变量都要使用volatile关键字来修饰，因为是在多线程环境下操作，要保证它们的值修改之后对其它线程立即可见。

这几个变量的修改是直接使用的Unsafe这个类来操作的：

```java
// 获取Unsafe类的实例，注意这种方式仅限于jdk自己使用，普通用户是无法这样调用的
private static final Unsafe unsafe = Unsafe.getUnsafe();
// 状态变量state的偏移量
private static final long stateOffset;
// 头节点的偏移量
private static final long headOffset;
// 尾节点的偏移量
private static final long tailOffset;
// 等待状态的偏移量（Node的属性）
private static final long waitStatusOffset;
// 下一个节点的偏移量（Node的属性）
private static final long nextOffset;

static {
    try {
        // 获取state的偏移量
        stateOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("state"));
        // 获取head的偏移量
        headOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("head"));
        // 获取tail的偏移量
        tailOffset = unsafe.objectFieldOffset
            (AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
        // 获取waitStatus的偏移量
        waitStatusOffset = unsafe.objectFieldOffset
            (Node.class.getDeclaredField("waitStatus"));
        // 获取next的偏移量
        nextOffset = unsafe.objectFieldOffset
            (Node.class.getDeclaredField("next"));

    } catch (Exception ex) { throw new Error(ex); }
}

// 调用Unsafe的方法原子更新state
protected final boolean compareAndSetState(int expect, int update) {
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```

-----

### 子类需要实现的主要方法

我们可以看到AQS的全称是AbstractQueuedSynchronizer，它本质上是一个抽象类，说明它本质上应该是需要子类来实现的，那么子类实现一个同步器需要实现哪些方法呢？

```java
// 互斥模式下使用：尝试获取锁
protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
}
// 互斥模式下使用：尝试释放锁
protected boolean tryRelease(int arg) {
    throw new UnsupportedOperationException();
}
// 共享模式下使用：尝试获取锁
protected int tryAcquireShared(int arg) {
    throw new UnsupportedOperationException();
}
// 共享模式下使用：尝试释放锁
protected boolean tryReleaseShared(int arg) {
    throw new UnsupportedOperationException();
}
// 如果当前线程独占着锁，返回true
protected boolean isHeldExclusively() {
    throw new UnsupportedOperationException();
}
```

问题：这几个方法为什么不直接定义成抽象方法呢？

因为子类只要实现这几个方法中的一部分就可以实现一个同步器了，所以不需要定义成抽象方法。


---------------
### 基于AQS自己动手写一个锁

直接上代码：

```java
public class MyLockBaseOnAqs {

    // 定义一个同步器，实现AQS类
    private static class Sync extends AbstractQueuedSynchronizer {
        // 实现tryAcquire(acquires)方法
        @Override
        public boolean tryAcquire(int acquires) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }
        // 实现tryRelease(releases)方法
        @Override
        protected boolean tryRelease(int releases) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }
    }

    // 声明同步器
    private final Sync sync = new Sync();

    // 加锁
    public void lock() {
        sync.acquire(1);
    }

    // 解锁
    public void unlock() {
        sync.release(1);
    }


    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        MyLockBaseOnAqs lock = new MyLockBaseOnAqs();

        CountDownLatch countDownLatch = new CountDownLatch(1000);

        IntStream.range(0, 1000).forEach(i -> new Thread(() -> {
            lock.lock();

            try {
                IntStream.range(0, 10000).forEach(j -> {
                    count++;
                });
            } finally {
                lock.unlock();
            }
//            System.out.println(Thread.currentThread().getName());
            countDownLatch.countDown();
        }, "tt-" + i).start());

        countDownLatch.await();

        System.out.println(count);
    }
}
```

运行main()方法总是打印出10000000（一千万），说明这个锁也是可以直接使用的，当然这也是一个不可重入的锁。

是不是很简单，只需要简单地实现AQS的两个方法就完成了上一章彤哥自己动手实现的锁的功能。


## 总结

下面总结一下这一章的主要内容：

（1）AQS是Java中几乎所有锁和同步器的一个基础框架，这里说的是“几乎”，因为有极个别确实没有通过AQS来实现；

（2）AQS中维护了一个队列，这个队列使用双链表实现，用于保存等待锁排队的线程；

（3）AQS中维护了一个状态变量，控制这个状态变量就可以实现加锁解锁操作了；

（4）基于AQS自己动手写一个锁非常简单，只需要实现AQS的几个方法即可。
