🖕欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

（手机横屏看源码更方便）

---

## 问题

（1）CyclicBarrier是什么？

（2）CyclicBarrier具有什么特性？

（3）CyclicBarrier与CountDownLatch的对比？

## 简介

CyclicBarrier，回环栅栏，它会阻塞一组线程直到这些线程同时达到某个条件才继续执行。它与CountDownLatch很类似，但又不同，CountDownLatch需要调用countDown()方法触发事件，而CyclicBarrier不需要，它就像一个栅栏一样，当一组线程都到达了栅栏处才继续往下走。

## 使用方法

```java
public class CyclicBarrierTest {
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(3);

        for (int i = 0; i < 3; i++) {
            new Thread(()->{
                System.out.println("before");
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("after");
            }).start();
        }
    }
}    
```

这段方法很简单，使用一个CyclicBarrier使得三个线程保持同步，当三个线程同时到达`cyclicBarrier.await();`处大家再一起往下运行。

## 源码分析

### 主要内部类

```java
private static class Generation {
    boolean broken = false;
}
```

Generation，中文翻译为代，一代人的代，用于控制CyclicBarrier的循环使用。

比如，上面示例中的三个线程完成后进入下一代，继续等待三个线程达到栅栏处再一起执行，而CountDownLatch则做不到这一点，CountDownLatch是一次性的，无法重置其次数。

### 主要属性

```java
// 重入锁
private final ReentrantLock lock = new ReentrantLock();
// 条件锁，名称为trip，绊倒的意思，可能是指线程来了先绊倒，等达到一定数量了再唤醒
private final Condition trip = lock.newCondition();
// 需要等待的线程数量
private final int parties;
// 当唤醒的时候执行的命令
private final Runnable barrierCommand;
// 代
private Generation generation = new Generation();
// 当前这一代还需要等待的线程数
private int count;
```

通过属性可以看到，CyclicBarrier内部是通过重入锁的条件锁来实现的，那么你可以脑补一下这个场景吗？

彤哥来脑补一下：假如初始时`count = parties = 3`，当第一个线程到达栅栏处，count减1，然后把它加入到Condition的队列中，第二个线程到达栅栏处也是如此，第三个线程到达栅栏处，count减为0，调用Condition的signalAll()通知另外两个线程，然后把它们加入到AQS的队列中，等待当前线程运行完毕，调用lock.unlock()的时候依次从AQS的队列中唤醒一个线程继续运行，也就是说实际上三个线程先依次（排队）到达栅栏处，再依次往下运行。

以上纯属彤哥脑补的内容，真实情况是不是如此呢，且往后看。

### 构造方法

```java
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    // 初始化parties
    this.parties = parties;
    // 初始化count等于parties
    this.count = parties;
    // 初始化都到达栅栏处执行的命令
    this.barrierCommand = barrierAction;
}

public CyclicBarrier(int parties) {
    this(parties, null);
}
```

构造方法需要传入一个parties变量，也就是需要等待的线程数。

### await()方法

每个需要在栅栏处等待的线程都需要显式地调用await()方法等待其它线程的到来。

```java
public int await() throws InterruptedException, BrokenBarrierException {
    try {
        // 调用dowait方法，不需要超时
        return dowait(false, 0L);
    } catch (TimeoutException toe) {
        throw new Error(toe); // cannot happen
    }
}

private int dowait(boolean timed, long nanos)
    throws InterruptedException, BrokenBarrierException,
           TimeoutException {
    final ReentrantLock lock = this.lock;
    // 加锁
    lock.lock();
    try {
        // 当前代
        final Generation g = generation;
        
        // 检查
        if (g.broken)
            throw new BrokenBarrierException();

        // 中断检查
        if (Thread.interrupted()) {
            breakBarrier();
            throw new InterruptedException();
        }
        
        // count的值减1
        int index = --count;
        // 如果数量减到0了，走这段逻辑（最后一个线程走这里）
        if (index == 0) {  // tripped
            boolean ranAction = false;
            try {
                // 如果初始化的时候传了命令，这里执行
                final Runnable command = barrierCommand;
                if (command != null)
                    command.run();
                ranAction = true;
                // 调用下一代方法
                nextGeneration();
                return 0;
            } finally {
                if (!ranAction)
                    breakBarrier();
            }
        }

        // 这个循环只有非最后一个线程可以走到
        for (;;) {
            try {
                if (!timed)
                    // 调用condition的await()方法
                    trip.await();
                else if (nanos > 0L)
                    // 超时等待方法
                    nanos = trip.awaitNanos(nanos);
            } catch (InterruptedException ie) {
                if (g == generation && ! g.broken) {
                    breakBarrier();
                    throw ie;
                } else {
                    // We're about to finish waiting even if we had not
                    // been interrupted, so this interrupt is deemed to
                    // "belong" to subsequent execution.
                    Thread.currentThread().interrupt();
                }
            }
            
            // 检查
            if (g.broken)
                throw new BrokenBarrierException();

            // 正常来说这里肯定不相等
            // 因为上面打破栅栏的时候调用nextGeneration()方法时generation的引用已经变化了
            if (g != generation)
                return index;
            
            // 超时检查
            if (timed && nanos <= 0L) {
                breakBarrier();
                throw new TimeoutException();
            }
        }
    } finally {
        lock.unlock();
    }
}
private void nextGeneration() {
    // 调用condition的signalAll()将其队列中的等待者全部转移到AQS的队列中
    trip.signalAll();
    // 重置count
    count = parties;
    // 进入下一代
    generation = new Generation();
}
```

dowait()方法里的整个逻辑分成两部分：

（1）最后一个线程走上面的逻辑，当count减为0的时候，打破栅栏，它调用nextGeneration()方法通知条件队列中的等待线程转移到AQS的队列中等待被唤醒，并进入下一代。

（2）非最后一个线程走下面的for循环逻辑，这些线程会阻塞在condition的await()方法处，它们会加入到条件队列中，等待被通知，当它们唤醒的时候已经更新换“代”了，这时候返回。

## 图解

![CyclicBarrier](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java同步系列/resource/CyclicBarrier.png)

学习过前面的章节，看这个图很简单了，看不懂的同学还需要把推荐的内容好好看看哦^^

## 总结

（1）CyclicBarrier会使一组线程阻塞在await()处，当最后一个线程到达时唤醒（只是从条件队列转移到AQS队列中）前面的线程大家再继续往下走；

（2）CyclicBarrier不是直接使用AQS实现的一个同步器；

（3）CyclicBarrier基于ReentrantLock及其Condition实现整个同步逻辑；

## 彩蛋

CyclicBarrier与CountDownLatch的异同？

（1）两者都能实现阻塞一组线程等待被唤醒；

（2）前者是最后一个线程到达时自动唤醒；

（3）后者是通过显式地调用countDown()实现的；

（4）前者是通过重入锁及其条件锁实现的，后者是直接基于AQS实现的；

（5）前者具有“代”的概念，可以重复使用，后者只能使用一次；

（6）前者只能实现多个线程到达栅栏处一起运行；

（7）后者不仅可以实现多个线程等待一个线程条件成立，还能实现一个线程等待多个线程条件成立（详见CountDownLatch那章使用案例）；

## 推荐阅读

1、[死磕 java同步系列之开篇](https://mp.weixin.qq.com/s/gdQpO7kqnWT41gFd4vXTlQ)

2、[死磕 java魔法类之Unsafe解析](https://mp.weixin.qq.com/s/0s-u-MysppIaIHVrshp9fA)

3、[死磕 java同步系列之JMM（Java Memory Model）](https://mp.weixin.qq.com/s/jownTN--npu3o8B4c3sbeA)

4、[死磕 java同步系列之volatile解析](https://mp.weixin.qq.com/s/TROZ4BhcDImwHvhAl_I_6w)

5、[死磕 java同步系列之synchronized解析](https://mp.weixin.qq.com/s/RT7VreIh9PU03HhE3WSLjg)

6、[死磕 java同步系列之自己动手写一个锁Lock](https://mp.weixin.qq.com/s/1RU5jh7UcXGtKlae8tusVA)

7、[死磕 java同步系列之AQS起篇](https://mp.weixin.qq.com/s/nAqgec8GscULz6DkkYFINg)

8、[死磕 java同步系列之ReentrantLock源码解析（一）——公平锁、非公平锁](https://mp.weixin.qq.com/s/52Ib23kbmqqkWAZtlZF-zA)

9、[死磕 java同步系列之ReentrantLock源码解析（二）——条件锁](https://mp.weixin.qq.com/s/iipAVWynBUZazhSvBwMB5g)

10、[死磕 java同步系列之ReentrantLock VS synchronized](https://mp.weixin.qq.com/s/o8ZFXDoKhj237SsrqGeJPQ)

11、[死磕 java同步系列之ReentrantReadWriteLock源码解析](https://mp.weixin.qq.com/s/aOQwZ0S8at-64xIXo8fLfA)

12、[死磕 java同步系列之Semaphore源码解析](https://mp.weixin.qq.com/s/ft0_PU7Tgz7920yKy-xisQ)

13、[死磕 java同步系列之CountDownLatch源码解析](https://mp.weixin.qq.com/s/QHFXKVybKz_iwgC8reGfPQ)

14、[死磕 java同步系列之AQS终篇](https://mp.weixin.qq.com/s/QHFXKVybKz_iwgC8reGfPQ)

15、[死磕 java同步系列之StampedLock源码解析](https://mp.weixin.qq.com/s/6RaFax0ivM6UoDdo5qhtwQ)

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)
