🖕欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

（手机横屏看源码更方便）

---

## 问题

（1）CountDownLatch是什么？

（2）CountDownLatch具有哪些特性？

（3）CountDownLatch通常运用在什么场景中？

（4）CountDownLatch的初始次数是否可以调整？

## 简介

CountDownLatch，可以翻译为倒计时器，但是似乎不太准确，它的含义是允许一个或多个线程等待其它线程的操作执行完毕后再执行后续的操作。

CountDownLatch的通常用法和Thread.join()有点类似，等待其它线程都完成后再执行主任务。

## 类结构

![CountDownLatch](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java同步系列/resource/CountDownLatch.png)

CountDownLatch中只包含了Sync一个内部类，它没有公平/非公平模式，所以它算是一个比较简单的同步器了。

这里还要注意一点，CountDownLatch没有实现Serializable接口，所以它不是可序列化的。

## 源码分析

### 内部类Sync

```java
private static final class Sync extends AbstractQueuedSynchronizer {
    private static final long serialVersionUID = 4982264981922014374L;
    
    // 传入初始次数
    Sync(int count) {
        setState(count);
    }
    // 获取还剩的次数
    int getCount() {
        return getState();
    }
    // 尝试获取共享锁
    protected int tryAcquireShared(int acquires) {
        // 注意，这里state等于0的时候返回的是1，也就是说count减为0的时候获取总是成功
        // state不等于0的时候返回的是-1，也就是count不为0的时候总是要排队
        return (getState() == 0) ? 1 : -1;
    }
    // 尝试释放锁
    protected boolean tryReleaseShared(int releases) {
        for (;;) {
            // state的值
            int c = getState();
            // 等于0了，则无法再释放了
            if (c == 0)
                return false;
            // 将count的值减1
            int nextc = c-1;
            // 原子更新state的值
            if (compareAndSetState(c, nextc))
                // 减为0的时候返回true，这时会唤醒后面排队的线程
                return nextc == 0;
        }
    }
}
```

Sync重写了tryAcquireShared()和tryReleaseShared()方法，并把count存到state变量中去。

这里要注意一下，上面两个方法的参数并没有什么卵用。

### 构造方法

```java
public CountDownLatch(int count) {
    if (count < 0) throw new IllegalArgumentException("count < 0");
    this.sync = new Sync(count);
}
```

构造方法需要传入一个count，也就是初始次数。

### await()方法

```java
// java.util.concurrent.CountDownLatch.await()
public void await() throws InterruptedException {
    // 调用AQS的acquireSharedInterruptibly()方法
    sync.acquireSharedInterruptibly(1);
}
// java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireSharedInterruptibly()
public final void acquireSharedInterruptibly(int arg)
        throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    // 尝试获取锁，如果失败则排队
    if (tryAcquireShared(arg) < 0)
        doAcquireSharedInterruptibly(arg);
}
```

await()方法是等待其它线程完成的方法，它会先尝试获取一下共享锁，如果失败则进入AQS的队列中排队等待被唤醒。

根据上面Sync的源码，我们知道，state不等于0的时候tryAcquireShared()返回的是-1，也就是说count未减到0的时候所有调用await()方法的线程都要排队。

## countDown()方法

```java
// java.util.concurrent.CountDownLatch.countDown()
public void countDown() {
    // 调用AQS的释放共享锁方法
    sync.releaseShared(1);
}
// java.util.concurrent.locks.AbstractQueuedSynchronizer.releaseShared()
public final boolean releaseShared(int arg) {
    // 尝试释放共享锁，如果成功了，就唤醒排队的线程
    if (tryReleaseShared(arg)) {
        doReleaseShared();
        return true;
    }
    return false;
}
```

countDown()方法，会释放一个共享锁，也就是count的次数会减1。

根据上面Sync的源码，我们知道，tryReleaseShared()每次会把count的次数减1，当其减为0的时候返回true，这时候才会唤醒等待的线程。

注意，doReleaseShared()是唤醒等待的线程，这个方法我们在前面的章节中分析过了。

## 使用案例

这里我们模拟一个使用场景，我们有一个主线程和5个辅助线程，等待主线程准备就绪了，5个辅助线程开始运行，等待5个辅助线程运行完毕了，主线程继续往下运行，大致的流程图如下：

![CountDownLatch](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java同步系列/resource/CountDownLatch1.png)

我们一起来看看这段代码应该怎么写：

```java
public class CountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            new Thread(()->{
                try {
                    System.out.println("Aid thread is waiting for starting.");
                    startSignal.await();
                    // do sth
                    System.out.println("Aid thread is doing something.");
                    doneSignal.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // main thread do sth
        Thread.sleep(2000);
        System.out.println("Main thread is doing something.");
        startSignal.countDown();

        // main thread do sth else
        System.out.println("Main thread is waiting for aid threads finishing.");
        doneSignal.await();

        System.out.println("Main thread is doing something after all threads have finished.");

    }
}
```

这段代码分成两段：

第一段，5个辅助线程等待开始的信号，信号由主线程发出，所以5个辅助线程调用startSignal.await()方法等待开始信号，当主线程的事儿干完了，调用startSignal.countDown()通知辅助线程开始干活。

第二段，主线程等待5个辅助线程完成的信号，信号由5个辅助线程发出，所以主线程调用doneSignal.await()方法等待完成信号，5个辅助线程干完自己的活儿的时候调用doneSignal.countDown()方法发出自己的完成的信号，当完成信号达到5个的时候，唤醒主线程继续执行后续的逻辑。

## 总结

（1）CountDownLatch表示允许一个或多个线程等待其它线程的操作执行完毕后再执行后续的操作；

（2）CountDownLatch使用AQS的共享锁机制实现；

（3）CountDownLatch初始化的时候需要传入次数count；

（4）每次调用countDown()方法count的次数减1；

（5）每次调用await()方法的时候会尝试获取锁，这里的获取锁其实是检查AQS的state变量的值是否为0；

（6）当count的值（也就是state的值）减为0的时候会唤醒排队着的线程（这些线程调用await()进入队列）；

## 彩蛋

（1）CountDownLatch的初始次数是否可以调整？

答：前面我们学习Semaphore的时候发现，它的许可次数是可以随时调整的，那么，CountDownLatch的初始次数能随时调整吗？答案是不能的，它没有提供修改（增加或减少）次数的方法，除非使用反射作弊。

（2）CountDownLatch为什么使用共享锁？

答：前面我们分析ReentrantReadWriteLock的时候学习过AQS的共享锁模式，比如当前锁是由一个线程获取为互斥锁，那么这时候所有需要获取共享锁的线程都要进入AQS队列中进行排队，当这个互斥锁释放的时候，会一个接着一个地唤醒这些连续的排队的等待获取共享锁的线程，注意，这里的用语是“一个接着一个地唤醒”，也就是说这些等待获取共享锁的线程不是一次性唤醒的。

说到这里，是不是很明白了？因为CountDownLatch的await()多个线程可以调用多次，当调用多次的时候这些线程都要进入AQS队列中排队，当count次数减为0的时候，它们都需要被唤醒，继续执行任务，如果使用互斥锁则不行，互斥锁在多个线程之间是互斥的，一次只能唤醒一个，不能保证当count减为0的时候这些调用了await()方法等待的线程都被唤醒。

（3）CountDownLatch与Thread.join()有何不同？

答：Thread.join()是在主线程中调用的，它只能等待被调用的线程结束了才会通知主线程，而CountDownLatch则不同，它的countDown()方法可以在线程执行的任意时刻调用，灵活性更大。

## 推荐阅读

1、 [死磕 java同步系列之开篇](https://mp.weixin.qq.com/s/gdQpO7kqnWT41gFd4vXTlQ)

2、 [死磕 java魔法类之Unsafe解析](https://mp.weixin.qq.com/s/0s-u-MysppIaIHVrshp9fA)

3、 [死磕 java同步系列之JMM（Java Memory Model）](https://mp.weixin.qq.com/s/jownTN--npu3o8B4c3sbeA)

4、 [死磕 java同步系列之volatile解析](https://mp.weixin.qq.com/s/TROZ4BhcDImwHvhAl_I_6w)

5、 [死磕 java同步系列之synchronized解析](https://mp.weixin.qq.com/s/RT7VreIh9PU03HhE3WSLjg)

6、 [死磕 java同步系列之自己动手写一个锁Lock](https://mp.weixin.qq.com/s/1RU5jh7UcXGtKlae8tusVA)

7、 [死磕 java同步系列之AQS起篇](https://mp.weixin.qq.com/s/nAqgec8GscULz6DkkYFINg)

8、 [死磕 java同步系列之ReentrantLock源码解析（一）——公平锁、非公平锁](https://mp.weixin.qq.com/s/52Ib23kbmqqkWAZtlZF-zA)

9、 [死磕 java同步系列之ReentrantLock源码解析（二）——条件锁](https://mp.weixin.qq.com/s/iipAVWynBUZazhSvBwMB5g)

10、 [死磕 java同步系列之ReentrantLock VS synchronized](https://mp.weixin.qq.com/s/o8ZFXDoKhj237SsrqGeJPQ)

11、 [死磕 java同步系列之ReentrantReadWriteLock源码解析](https://mp.weixin.qq.com/s/aOQwZ0S8at-64xIXo8fLfA)

12、 [死磕 java同步系列之Semaphore源码解析](https://mp.weixin.qq.com/s/ft0_PU7Tgz7920yKy-xisQ)

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)