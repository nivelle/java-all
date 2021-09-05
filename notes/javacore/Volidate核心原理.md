## java内存模型

### 计算机内存概要

- CPU 在执行指令时将主存中的数据复制到高速缓存中,将结果运算完毕后再将运算结果刷新到主存中。

[![rUdqM9.png](https://z3.ax1x.com/2020/12/20/rUdqM9.png)](https://imgtu.com/i/rUdqM9)

### 缓存一致性实现方式

- 通过总线加Lock锁的方法
  
- 通过缓存一致性协议(最出名：Intel的MESI协议)【M:修改；E:独占；S:共享 I:失效 】


MESI协议保证了每个缓存中使用的共享变量的副本是一致的，它的核心思想是：当CPU写数据时，如果发现操作的变量是共享变量，即在其他CPU中也存在该变量的副本，会发出信号
通知其他CPU将该变量的缓存行置为无效状态，因此当其他CPU需要读取这个变量时，发现自己缓存中缓存该变量的缓存行是无效的，那么就会从内存重新读取。

多处理器下,为了保证各个处理器通过嗅探在总线上传播的数据来检查自己缓存的值是不是过期了，当处理器发现自己缓存对应的内存地址被修改，就会将当前处理器缓存行设置成无效状态，当处理器对这个数据进行修改操作的时候，
会重新从系统内存中把数据读到处理器缓存里。


## 并发编程的三个重要概念

- 原子性

- 可见性(针对操作系统CPU的多级缓存)

- 有序性(针对JVM的指令重排序)

### volatile实现原理

- java内存模型看来，各个线程会将从共享变量从主存中拷贝到工作内存，然后执行引擎会基于工作内存中的数据进行处理。

- 针对volatile修饰的变量给java虚拟机特殊约定,线程对volatile变量的修改会立刻被其他线程所感知,即不会出现数据脏读现象,从而保证数据的"可见性"

#### 在生成汇编指令代码时会在volatile修饰的共享变量进行写操作的时候会多出Lock前缀的指令：

- Lock 前缀的指令会引起处理器缓存写回主存,对缓存行加锁
  
- 一个处理器的缓存回写到内存会导致其他处理器的缓存失效
  
- 当处理器发现本地缓存失效后，就会从内存中重读该变量数据，即可获取当前最新值

#### volatile与happens-before(先行发生)原则

- 在计算机科学中，先行产生原则是两个事件的结果之间的关系，**如果一个事件产生在另一个事件之前，结果必须反映，即使这些事件实际上是乱序执行的。**

- 对一个volatile变量的写操作先行产生与后面对这个变量的读操作

#### volatile内存语义及实现（内存屏障）

- JVM 在编译java代码时，或者CPU在执行JVM字节码时，对现有的指令顺序进行重排序

- 指令重排序的目的时为了在不改变运行结果的前提下，优化程序执行效率 **(指单线程下不影响程序执行结果,指令重排序影响多线程情况下的执行结果)**；

### JMM: 8大数据原子操作

1. `lock(锁定)`：用于主内存的变量，把一个变量标记为一条线程独占状态
2. `unlock(解锁)`：作用于主内存的变量，把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定
3. `read(读取)`：把一个变量从主内存传输到线程的工作内存，以便随后的load动作使用
4. `load(载入)`：它把read操作从主内存得到的变量值放入工作内存的变量副本中
5. `use(使用)`：把工作内存中的一个变量值传递给执行引擎
6. `assign(赋值)`：它把一个从执行引擎接收到的值赋给工作内存的变量
7. `store(存储)`：它把工作内存中的一个变量的值传送到主内存中，以便随后的write的操作
8. `write(写入)`：把store操作从工作内存中的变量的值传送到主内存的变量之中
----

### volatile 相关 问题

- （1）volatile 是如何保证可见性的？
- （2）volatile是如何禁止重排序的？
- （3）volatile的实现原理？
- （4）volatile的缺陷？


- volatile可以说是Java虚拟机提供的最轻量级的同步机制了，但是它并不容易被正确地理解，以至于很多人不习惯使用它，遇到多线程问题一律使用synchronized或其它锁来解决。

## 语义一：可见性

前面介绍Java内存模型的时候，我们说过可见性是指当一个线程修改了共享变量的值，其它线程能立即感知到这种变化。

关于Java内存模型的讲解请参考【[死磕 java同步系列之JMM（Java Memory Model）](/Users/nivellefu/IdeaProjects/java-guides/notes/javacore/JMM模型.md)】。

而普通变量无法做到立即感知这一点，变量的值在线程之间的传递均需要通过主内存来完成，比如，线程A修改了一个普通变量的值，然后向主内存回写，另外一条线程B只有在线程A的回写完成之后再从主内存中读取变量的值，才能够读取到新变量的值，也就是新变量才能对线程B可见。

在这期间可能会出现不一致的情况，比如：

- （1）线程A并不是修改完成后立即回写；

![volatile](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java同步系列/resource/volatile1.png)

（线路A修改了变量x的值为5，但是还没有回写，线程B从主内存读取到的还旧值0）

- （2）线程B还在用着自己工作内存中的值，而并不是立即从主内存读取值；

![volatile](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java同步系列/resource/volatile2.png)

（线程A回写了变量x的值为5到主内存中，但是线程B还没有读取主内存的值，依旧在使用旧值0在进行运算）

基于以上两种情况，所以，普通变量都无法做到立即感知这一点。

但是，volatile变量可以做到立即感知这一点，也就是volatile可以保证可见性。

- java内存模型规定，volatile变量的每次修改都必须立即回写到主内存中，volatile变量的每次使用都必须从主内存刷新最新的值。

![volatile](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java同步系列/resource/volatile3.png)

volatile的可见性可以通过下面的示例体现：

```java
public class VolatileTest {
    // public static int finished = 0;
    public static volatile int finished = 0;

    private static void checkFinished() {
        while (finished == 0) {
            // do nothing
        }
        System.out.println("finished");
    }

    private static void finish() {
        finished = 1;
    }

    public static void main(String[] args) throws InterruptedException {
        // 起一个线程检测是否结束
        new Thread(() -> checkFinished()).start();

        Thread.sleep(100);

        // 主线程将finished标志置为1
        finish();

        System.out.println("main finished");

    }
}
```

在上面的代码中，针对finished变量，使用volatile修饰时这个程序可以正常结束，不使用volatile修饰时这个程序永远不会结束。

因为不使用volatile修饰时，checkFinished()所在的线程每次都是读取的它自己工作内存中的变量的值，这个值一直为0，所以一直都不会跳出while循环。

使用volatile修饰时，checkFinished()所在的线程每次都是从主内存中加载最新的值，当finished被主线程修改为1的时候，它会立即感知到，进而会跳出while循环。

## 语义二：禁止重排序

- 前面介绍Java内存模型的时候，我们说过Java中的有序性可以概括为一句话：如果在本线程中观察，所有的操作都是有序的；如果在另一个线程中观察，所有的操作都是无序的。

- 前半句是指线程内表现为串行的语义，后半句是指`“指令重排序”现象和“工作内存和主内存同步延迟”`现象。

**普通变量仅仅会保证在该方法的执行过程中所有依赖赋值结果的地方都能获得正确的结果，而不能保证变量赋值操作的顺序与程序代码中的执行顺序一致，因为一个线程的方法执行过程中无法感知到这点，这就是“线程内表现为串行的语义”。**

比如，下面的代码：

```java
// 两个操作在一个线程
int i = 0;
int j = 1;
```

上面两句话没有依赖关系，JVM在执行的时候为了充分利用CPU的处理能力，可能会先执行`int j = 1;`这句，也就是重排序了，但是在线程内是无法感知的。

看似没有什么影响，但是如果是在多线程环境下呢？

我们再看一个例子：

```java
public class VolatileTest3 {
    private static Config config = null;
    private static volatile boolean initialized = false;

    public static void main(String[] args) {
        // 线程1负责初始化配置信息
        new Thread(() -> {
            config = new Config();
            config.name = "config";
            initialized = true;
        }).start();

        // 线程2检测到配置初始化完成后使用配置信息
        new Thread(() -> {
            while (!initialized) {
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
            }

            // do sth with config
            String name = config.name;
        }).start();
    }
}

class Config {
    String name;
}
```

这个例子很简单，线程1负责初始化配置，线程2检测到配置初始化完毕，使用配置来干一些事。

在这个例子中，如果initialized不使用volatile来修饰，可能就会出现重排序，比如在初始化配置之前把initialized的值设置为了true，这样线程2读取到这个值为true了，就去使用配置了，这时候可能就会出现错误。

（此处这个例子只是用于说明重排序，实际运行时很难出现。）

通过这个例子， `“如果在本线程内观察，所有操作都是有序的；在另一个线程观察，所有操作都是无序的”` 有了更深刻的理解。

所以，重排序是站在另一个线程的视角的，因为在本线程中，是无法感知到重排序的影响的。

而volatile变量是禁止重排序的，它能保证程序实际运行是按代码顺序执行的。

----
## 语义三：实现内存屏障

上面讲了volatile可以保证可见性和禁止重排序，那么它是怎么实现的呢？ 答案就是:**内存屏障**。

内存屏障有两个作用：

- （1）阻止屏障两侧的指令重排序；

- （2）强制把写缓冲区/高速缓存中的数据回写到主内存，让缓存中相应的数据失效；

https://www.infoq.com/articles/memory_barriers_jvm_concurrency

这是InfoQ英文站上面的一篇文章，我觉得写的挺好的，基本上综合了上面的两种观点，并从汇编层面分析了内存屏障的实现。

我们还是来看一个例子来理解内存屏障的影响：

```java
public class VolatileTest4 {
    // a不使用volatile修饰
    public static long a = 0;
    // 消除缓存行的影响
    public static long p1, p2, p3, p4, p5, p6, p7;
    // b使用volatile修饰
    public static volatile long b = 0;
    // 消除缓存行的影响
    public static long q1, q2, q3, q4, q5, q6, q7;
    // c不使用volatile修饰
    public static long c = 0;

    public static void main(String[] args) throws InterruptedException {
        new Thread(()->{
            while (a == 0) {
                long x = b;
            }
            System.out.println("a=" + a);
        }).start();

        new Thread(()->{
            while (c == 0) {
                long x = b;
            }
            System.out.println("c=" + c);
        }).start();

        Thread.sleep(100);

        a = 1;
        b = 1;
        c = 1;
    }
}
```

这段代码中，a和c不使用volatile修饰，b使用volatile修饰，而且我们在a/b、b/c之间各加入7个long字段消除伪共享的影响。

在a和c的两个线程的while循环中我们获取一下b，你猜怎样？如果把`long x = b;`这行去掉呢？运行试试吧。

**结论：** volatile变量的影响范围不仅仅只包含它自己，它会对其上下的变量值的读写都有影响。

## 缺陷

上面我们介绍了volatile关键字的两大语义，那么，volatile关键字是不是就是万能的了呢？

当然不是，忘了我们内存模型那章说的一致性包括的三大特性了么？

一致性主要包含三大特性：原子性、可见性、有序性。

volatile关键字可以保证可见性和有序性，那么volatile能保证原子性么？

请看下面的例子：

```java
public class VolatileTest5 {
    public static volatile int counter = 0;

    public static void increment() {
        counter++;
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(100);
        IntStream.range(0, 100).forEach(i->
                new Thread(()-> {
                    IntStream.range(0, 1000).forEach(j->increment());
                    countDownLatch.countDown();
                }).start());

        countDownLatch.await();

        System.out.println(counter);
    }
}
```

这段代码中，我们起了100个线程分别对counter自增1000次，一共应该是增加了100000，但是实际运行结果却永远不会达到100000。

让我们来看看increment()方法的字节码（IDEA下载相关插件可以查看）：

```java
0 getstatic #2 <com/coolcoding/code/synchronize/VolatileTest5.counter>
3 iconst_1
4 iadd
5 putstatic #2 <com/coolcoding/code/synchronize/VolatileTest5.counter>
8 return
```

### 可以看到counter++被分解成了四条指令：

- （1）getstatic，获取counter当前的值并入栈

- （2）iconst_1，入栈int类型的值1

- （3）iadd，将栈顶的两个值相加

- （4）putstatic，将相加的结果写回到counter中

由于counter是volatile修饰的，所以getstatic会从主内存刷新最新的值，putstatic也会把修改的值立即同步到主内存。

**但是中间的两步iconst_1和iadd在执行的过程中，可能counter的值已经被修改了，这时并没有重新读取主内存中的最新值，所以volatile在counter++这个场景中并不能保证其原子性。**

volatile关键字只能保证可见性和有序性，不能保证原子性，要解决原子性的问题，还是只能通过加锁或使用原子类的方式解决。

进而，我们得出volatile关键字使用的场景：

- （1）运算的结果并不依赖于变量的当前值，或者能够确保只有单一的线程修改变量的值；

- （2）变量不需要与其他状态变量共同参与不变约束。

说白了，就是volatile本身不保证原子性，那就要增加其它的约束条件来使其所在的场景本身就是原子的。

比如：

```java
private volatile int a = 0;

// 线程A
a = 1;

// 线程B
if (a == 1) {
    // do sth
}
```

`a = 1;`这个赋值操作本身就是原子的，所以可以使用volatile来修饰。

----
## 总结

- （1）volatile关键字可以保证可见性；

- （2）volatile关键字可以保证有序性；

- （3）volatile关键字不可以保证原子性；

- （4）volatile关键字的底层主要是通过内存屏障来实现的；

- （5）volatile关键字的使用场景必须是场景本身就是原子的；
