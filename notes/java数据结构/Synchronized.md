🖕欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

（手机横屏看源码更方便）

---

## 问题

（1）synchronized的特性？

（2）synchronized的实现原理？

（3）synchronized是否可重入？

（4）synchronized是否是公平锁？

（5）synchronized的优化？

（6）synchronized的五种使用方式？

## 简介

synchronized关键字是Java里面最基本的同步手段，它经过编译之后，会在同步块的前后分别生成 monitorenter 和 monitorexit 字节码指令，这两个字节码指令都需要一个引用类型的参数来指明要锁定和解锁的对象。

## 实现原理

在学习Java内存模型的时候，我们介绍过两个指令：lock 和 unlock。

lock，锁定，作用于主内存的变量，它把主内存中的变量标识为一条线程独占状态。

unlock，解锁，作用于主内存的变量，它把锁定的变量释放出来，释放出来的变量才可以被其它线程锁定。

但是这两个指令并没有直接提供给用户使用，而是提供了两个更高层次的指令 monitorenter 和 monitorexit 来隐式地使用 lock 和 unlock 指令。

而 synchronized 就是使用 monitorenter 和 monitorexit 这两个指令来实现的。

根据JVM规范的要求，在执行monitorenter指令的时候，首先要去尝试获取对象的锁，如果这个对象没有被锁定，或者当前线程已经拥有了这个对象的锁，就把锁的计数器加1，相应地，在执行monitorexit的时候会把计数器减1，当计数器减小为0时，锁就释放了。

我们还是来上一段代码，看看编译后的字节码长啥样来学习：

```java
public class SynchronizedTest {

    public static void sync() {
        synchronized (SynchronizedTest.class) {
            synchronized (SynchronizedTest.class) {
            }
        }
    }

    public static void main(String[] args) {

    }
}
```

我们这段代码很简单，只是简单地对SynchronizedTest.class对象加了两次synchronized，除此之外，啥也没干。

编译后的sync()方法的字节码指令如下，为了便于阅读，彤哥特意加上了注释：

```java
// 加载常量池中的SynchronizedTest类对象到操作数栈中
0 ldc #2 <com/coolcoding/code/synchronize/SynchronizedTest>
// 复制栈顶元素
2 dup
// 存储一个引用到本地变量0中，后面的0表示第几个变量
3 astore_0
// 调用monitorenter，它的参数变量0，也就是上面的SynchronizedTest类对象
4 monitorenter
// 再次加载常量池中的SynchronizedTest类对象到操作数栈中
5 ldc #2 <com/coolcoding/code/synchronize/SynchronizedTest>
// 复制栈顶元素
7 dup
// 存储一个引用到本地变量1中
8 astore_1
// 再次调用monitorenter，它的参数是变量1，也还是SynchronizedTest类对象
9 monitorenter
// 从本地变量表中加载第1个变量
10 aload_1
// 调用monitorexit解锁，它的参数是上面加载的变量1
11 monitorexit
// 跳到第20行
12 goto 20 (+8)
15 astore_2
16 aload_1
17 monitorexit
18 aload_2
19 athrow
// 从本地变量表中加载第0个变量
20 aload_0
// 调用monitorexit解锁，它的参数是上面加载的变量0
21 monitorexit
// 跳到第30行
22 goto 30 (+8)
25 astore_3
26 aload_0
27 monitorexit
28 aload_3
29 athrow
// 方法返回，结束
30 return
```

按照彤哥的注释读起来，字节码比较简单，我们的synchronized锁定的是SynchronizedTest类对象，可以看到它从常量池中加载了两次SynchronizedTest类对象，分别存储在本地变量0和本地变量1中，解锁的时候正好是相反的顺序，先解锁变量1，再解锁变量0，实际上变量0和变量1指向的是同一个对象，所以synchronized是可重入的。

至于，被加锁的对象具体在对象头中是怎么存储的，彤哥这里就不细讲了，有兴趣的可以看看《Java并发编程的艺术》这本书。

公众号后台回复“JMM”可领取这本书籍的pdf版。

## 原子性、可见性、有序性

前面讲解Java内存模型的时候我们说过内存模型主要就是用来解决缓存一致性的问题的，而缓存一致性主要包括原子性、可见性、有序性。

那么，synchronized关键字能否保证这三个特性呢？

还是回到Java内存模型上来，synchronized关键字底层是通过monitorenter和monitorexit实现的，而这两个指令又是通过lock和unlock来实现的。

而lock和unlock在Java内存模型中是必须满足下面四条规则的：

（1）一个变量同一时刻只允许一条线程对其进行lock操作，但lock操作可以被同一个线程执行多次，多次执行lock后，只有执行相同次数的unlock操作，变量才能被解锁。

（2）如果对一个变量执行lock操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前，需要重新执行load或assign操作初始化变量的值；

（3）如果一个变量没有被lock操作锁定，则不允许对其执行unlock操作，也不允许unlock一个其它线程锁定的变量；

（4）对一个变量执行unlock操作之前，必须先把此变量同步回主内存中，即执行store和write操作；

通过规则（1），我们知道对于lock和unlock之间的代码，同一时刻只允许一个线程访问，所以，synchronized是具有原子性的。

通过规则（1）（2）和（4），我们知道每次lock和unlock时都会从主内存加载变量或把变量刷新回主内存，而lock和unlock之间的变量（这里是指锁定的变量）是不会被其它线程修改的，所以，synchronized是具有可见性的。

通过规则（1）和（3），我们知道所有对变量的加锁都要排队进行，且其它线程不允许解锁当前线程锁定的对象，所以，synchronized是具有有序性的。

综上所述，synchronized是可以保证原子性、可见性和有序性的。

## 公平锁 VS 非公平锁

通过上面的学习，我们知道了synchronized的实现原理，并且它是可重入的，那么，它是否是公平锁呢？

直接上菜：

```java
public class SynchronizedTest {

    public static void sync(String tips) {
        synchronized (SynchronizedTest.class) {
            System.out.println(tips);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Thread(()->sync("线程1")).start();
        Thread.sleep(100);
        new Thread(()->sync("线程2")).start();
        Thread.sleep(100);
        new Thread(()->sync("线程3")).start();
        Thread.sleep(100);
        new Thread(()->sync("线程4")).start();
    }
}
```

在这段程序中，我们起了四个线程，且分别间隔100ms启动，每个线程里面打印一句话后等待1000ms，如果synchronized是公平锁，那么打印的结果应该依次是 线程1、2、3、4。

但是，实际运行的结果几乎不会出现上面的样子，所以，synchronized是一个非公平锁。

## 锁优化

Java在不断进化，同样地，Java中像synchronized这种古老的东西也在不断进化，比如ConcurrentHashMap在jdk7的时候还是使用ReentrantLock加锁的，在jdk8的时候已经换成了原生的synchronized了，可见synchronized有原生的支持，它的进化空间还是很大的。

那么，synchronized有哪些进化中的状态呢？

我们这里稍做一些简单地介绍：

（1）偏向锁，是指一段同步代码一直被一个线程访问，那么这个线程会自动获取锁，降低获取锁的代价。

（2）轻量级锁，是指当锁是偏向锁时，被另一个线程所访问，偏向锁会升级为轻量级锁，这个线程会通过自旋的方式尝试获取锁，不会阻塞，提高性能。

（3）重量级锁，是指当锁是轻量级锁时，当自旋的线程自旋了一定的次数后，还没有获取到锁，就会进入阻塞状态，该锁升级为重量级锁，重量级锁会使其他线程阻塞，性能降低。

## 总结

（1）synchronized在编译时会在同步块前后生成monitorenter和monitorexit字节码指令；

（2）monitorenter和monitorexit字节码指令需要一个引用类型的参数，基本类型不可以哦；

（3）monitorenter和monitorexit字节码指令更底层是使用Java内存模型的lock和unlock指令；

（4）synchronized是可重入锁；

（5）synchronized是非公平锁；

（6）synchronized可以同时保证原子性、可见性、有序性；

（7）synchronized有三种状态：偏向锁、轻量级锁、重量级锁；

## 彩蛋——synchronized的五种使用方式

通过上面的分析，我们知道synchronized是需要一个引用类型的参数的，而这个引用类型的参数在Java中其实可以分成三大类：类对象、实例对象、普通引用，使用方式分别如下：

```java
public class SynchronizedTest2 {

    public static final Object lock = new Object();

    // 锁的是SynchronizedTest.class对象
    public static synchronized void sync1() {

    }

    public static void sync2() {
        // 锁的是SynchronizedTest.class对象
        synchronized (SynchronizedTest.class) {

        }
    }

    // 锁的是当前实例this
    public synchronized void sync3() {

    }

    public void sync4() {
        // 锁的是当前实例this
        synchronized (this) {

        }
    }

    public void sync5() {
        // 锁的是指定对象lock
        synchronized (lock) {

        }
    }
}
```

在方法上使用synchronized的时候要注意，会隐式传参，分为静态方法和非静态方法，静态方法上的隐式参数为当前类对象，非静态方法上的隐式参数为当前实例this。

另外，多个synchronized只有锁的是同一个对象，它们之间的代码才是同步的，这一点在使用synchronized的时候一定要注意。

## 推荐阅读

1. [死磕 java同步系列之JMM（Java Memory Model）](https://mp.weixin.qq.com/s/jownTN--npu3o8B4c3sbeA)

2. [死磕 java同步系列之volatile解析](https://mp.weixin.qq.com/s/TROZ4BhcDImwHvhAl_I_6w)

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)
