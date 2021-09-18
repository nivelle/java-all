## 问题

（1）ReentrantLock有哪些优点？

（2）ReentrantLock有哪些缺点？

（3）ReentrantLock是否可以完全替代synchronized？

## 简介

synchronized是Java原生提供的用于在多线程环境中保证同步的关键字，底层是通过修改对象头中的MarkWord来实现的。

ReentrantLock是Java语言层面提供的用于在多线程环境中保证同步的类，底层是通过原子更新状态变量state来实现的。

既然有了synchronized的关键字来保证同步了，为什么还要实现一个ReentrantLock类呢？它们之间有什么异同呢？

## ReentrantLock VS synchronized

直接上表格：（手机横屏查看更方便）

|功能|ReentrantLock|synchronized|
|---|---|---|
|可重入|支持|支持|
|非公平|支持（默认） |支持 |
|加锁/解锁方式|需要手动加锁、解锁，一般使用try..finally..保证锁能够释放|手动加锁，无需刻意解锁 |
|按key锁|不支持，比如按用户id加锁|支持，synchronized加锁时需要传入一个对象|
|公平锁|支持，new ReentrantLock(true) |不支持 |
|中断|支持，lockInterruptibly() |不支持 |
|尝试加锁| 支持，tryLock()|不支持 |
|超时锁|支持，tryLock(timeout, unit) |不支持 |
|获取当前线程获取锁的次数|支持，getHoldCount()|不支持 |
|获取等待的线程|支持，getWaitingThreads() |不支持 |
|检测是否被当前线程占有| 支持，isHeldByCurrentThread()| 不支持|
|检测是否被任意线程占有|支持，isLocked() |不支持 |
|条件锁|可支持多个条件，condition.await()，condition.signal()，condition.signalAll() |只支持一个，obj.wait()，obj.notify()，obj.notifyAll() |

## 对比测试

在测试之前，我们先预想一下结果，随着线程数的不断增加，ReentrantLock（fair）、ReentrantLock（unfair）、synchronized三者的效率怎样呢？

我猜测应该是ReentrantLock（unfair）> synchronized > ReentrantLock（fair）。

到底是不是这样呢？

直接上测试代码：（为了全面对比，彤哥这里把AtomicInteger和LongAdder也拿来一起对比了）

```java
public class ReentrantLockVsSynchronizedTest {
    public static AtomicInteger a = new AtomicInteger(0);
    public static LongAdder b = new LongAdder();
    public static int c = 0;
    public static int d = 0;
    public static int e = 0;

    public static final ReentrantLock fairLock = new ReentrantLock(true);
    public static final ReentrantLock unfairLock = new ReentrantLock();


    public static void main(String[] args) throws InterruptedException {
        System.out.println("-------------------------------------");
        testAll(1, 100000);
        System.out.println("-------------------------------------");
        testAll(2, 100000);
        System.out.println("-------------------------------------");
        testAll(4, 100000);
        System.out.println("-------------------------------------");
        testAll(6, 100000);
        System.out.println("-------------------------------------");
        testAll(8, 100000);
        System.out.println("-------------------------------------");
        testAll(10, 100000);
        System.out.println("-------------------------------------");
        testAll(50, 100000);
        System.out.println("-------------------------------------");
        testAll(100, 100000);
        System.out.println("-------------------------------------");
        testAll(200, 100000);
        System.out.println("-------------------------------------");
        testAll(500, 100000);
        System.out.println("-------------------------------------");
//        testAll(1000, 1000000);
        System.out.println("-------------------------------------");
        testAll(500, 10000);
        System.out.println("-------------------------------------");
        testAll(500, 1000);
        System.out.println("-------------------------------------");
        testAll(500, 100);
        System.out.println("-------------------------------------");
        testAll(500, 10);
        System.out.println("-------------------------------------");
        testAll(500, 1);
        System.out.println("-------------------------------------");
    }

    public static void testAll(int threadCount, int loopCount) throws InterruptedException {
        testAtomicInteger(threadCount, loopCount);
        testLongAdder(threadCount, loopCount);
        testSynchronized(threadCount, loopCount);
        testReentrantLockUnfair(threadCount, loopCount);
//        testReentrantLockFair(threadCount, loopCount);
    }

    public static void testAtomicInteger(int threadCount, int loopCount) throws InterruptedException {
        long start = System.currentTimeMillis();

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < loopCount; j++) {
                    a.incrementAndGet();
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();

        System.out.println("testAtomicInteger: result=" + a.get() + ", threadCount=" + threadCount + ", loopCount=" + loopCount + ", elapse=" + (System.currentTimeMillis() - start));
    }

    public static void testLongAdder(int threadCount, int loopCount) throws InterruptedException {
        long start = System.currentTimeMillis();

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < loopCount; j++) {
                    b.increment();
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();

        System.out.println("testLongAdder: result=" + b.sum() + ", threadCount=" + threadCount + ", loopCount=" + loopCount + ", elapse=" + (System.currentTimeMillis() - start));
    }

    public static void testReentrantLockFair(int threadCount, int loopCount) throws InterruptedException {
        long start = System.currentTimeMillis();

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < loopCount; j++) {
                    fairLock.lock();
                    // 消除try的性能影响
//                    try {
                        c++;
//                    } finally {
                        fairLock.unlock();
//                    }
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();

        System.out.println("testReentrantLockFair: result=" + c + ", threadCount=" + threadCount + ", loopCount=" + loopCount + ", elapse=" + (System.currentTimeMillis() - start));
    }

    public static void testReentrantLockUnfair(int threadCount, int loopCount) throws InterruptedException {
        long start = System.currentTimeMillis();

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < loopCount; j++) {
                    unfairLock.lock();
                    // 消除try的性能影响
//                    try {
                        d++;
//                    } finally {
                        unfairLock.unlock();
//                    }
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();

        System.out.println("testReentrantLockUnfair: result=" + d + ", threadCount=" + threadCount + ", loopCount=" + loopCount + ", elapse=" + (System.currentTimeMillis() - start));
    }

    public static void testSynchronized(int threadCount, int loopCount) throws InterruptedException {
        long start = System.currentTimeMillis();

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                for (int j = 0; j < loopCount; j++) {
                    synchronized (ReentrantLockVsSynchronizedTest.class) {
                        e++;
                    }
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();

        System.out.println("testSynchronized: result=" + e + ", threadCount=" + threadCount + ", loopCount=" + loopCount + ", elapse=" + (System.currentTimeMillis() - start));
    }

}
```

运行这段代码，你会发现结果大大出乎意料，真的是不测不知道，一测吓一跳，运行后发现以下规律：

`随着线程数的不断增加，synchronized的效率竟然比ReentrantLock非公平模式要高！`

彤哥的电脑上大概是高3倍左右，我的运行环境是4核8G，java版本是8，请大家一定要在自己电脑上运行一下，并且最好能给我反馈一下。

彤哥又使用Java7及以下的版本运行了，发现在Java7及以下版本中synchronized的效率确实比ReentrantLock的效率低一些。

## 总结

（1）synchronized是Java原生关键字锁；

（2）ReentrantLock是Java语言层面提供的锁；

（3）ReentrantLock的功能非常丰富，解决了很多synchronized的局限性；

（4）至于在非公平模式下，ReentrantLock与synchronized的效率孰高孰低，彤哥给出的结论是随着Java版本的不断升级，synchronized的效率只会越来越高；

## 彩蛋

既然ReentrantLock的功能更丰富，而且效率也不低，我们是不是可以放弃使用synchronized了呢？

答：我认为不是。因为synchronized是Java原生支持的，随着Java版本的不断升级，Java团队也是在不断优化synchronized，所以我认为在功能相同的前提下，最好还是使用原生的synchronized关键字来加锁，这样我们就能获得Java版本升级带来的免费的性能提升的空间。

另外，在Java8的ConcurrentHashMap中已经把ReentrantLock换成了synchronized来分段加锁了，这也是Java版本不断升级带来的免费的synchronized的性能提升。

## 推荐阅读

1. [死磕 java同步系列之ReentrantLock源码解析（二）——条件锁](https://mp.weixin.qq.com/s/iipAVWynBUZazhSvBwMB5g)

2. [死磕 java同步系列之ReentrantLock源码解析（一）——公平锁、非公平锁](https://mp.weixin.qq.com/s/52Ib23kbmqqkWAZtlZF-zA)

3. [死磕 java同步系列之AQS起篇](https://mp.weixin.qq.com/s/nAqgec8GscULz6DkkYFINg)

4. [死磕 java同步系列之自己动手写一个锁Lock](https://mp.weixin.qq.com/s/1RU5jh7UcXGtKlae8tusVA)

5. [死磕 java魔法类之Unsafe解析](https://mp.weixin.qq.com/s/0s-u-MysppIaIHVrshp9fA)

6. [死磕 java同步系列之JMM（Java Memory Model）](https://mp.weixin.qq.com/s/jownTN--npu3o8B4c3sbeA)

7. [死磕 java同步系列之volatile解析](https://mp.weixin.qq.com/s/TROZ4BhcDImwHvhAl_I_6w)

8. [死磕 java同步系列之synchronized解析](https://mp.weixin.qq.com/s/RT7VreIh9PU03HhE3WSLjg)

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)
