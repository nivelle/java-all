### 虚拟机

#### jinfo

- 查看及调整虚拟机参数
```
jinfo -flag 参数名 进程ID
```
#### jmap

- jmap 来查看堆内存初始化配置信息以及堆内存的使用情况 及生成堆快照

```
jmap -dump 8299

jmap -histo:live 8299|more

jmap -dump:file=./heap.hprof 8299
```

### jstack
```
jstack pid 命令查看线程的堆栈信息;

每个线程堆栈的信息中，都可以查看到线程 ID、线程的状态（wait、sleep、running 等状态）以及是否持有锁等
```
- -F  强制dump线程堆栈信息. 用于进程hung住， jstack <pid>命令没有响应的情况
- -m  同时打印java和本地(native)线程栈信息，m是mixed mode的简写
- -l  打印锁的额外信息

```
jstack -l 9233
2020-07-25 23:13:06
Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.191-b12 mixed mode):


"http-nio-8090-exec-1" #37 daemon prio=5 os_prio=31 tid=0x00007f923fbc5800 nid=0x5f03 waiting on condition [0x000070000bf52000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x00000007988c23f8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
        at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
        at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
        at org.apache.tomcat.util.threads.TaskQueue.take(TaskQueue.java:107)
        at org.apache.tomcat.util.threads.TaskQueue.take(TaskQueue.java:33)
        at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"VM Thread" os_prio=31 tid=0x00007f9241815000 nid=0x2b03 runnable 

"GC task thread#0 (ParallelGC)" os_prio=31 tid=0x00007f924080e000 nid=0x2407 runnable 

"GC task thread#1 (ParallelGC)" os_prio=31 tid=0x00007f923f80f000 nid=0x2203 runnable 

"GC task thread#2 (ParallelGC)" os_prio=31 tid=0x00007f923f810000 nid=0x2103 runnable 

"GC task thread#3 (ParallelGC)" os_prio=31 tid=0x00007f923f810800 nid=0x2a03 runnable 

"VM Periodic Task Thread" os_prio=31 tid=0x00007f924195e000 nid=0x4003 waiting on condition 

JNI global references: 1082

```

- "http-nio-8083-Acceptor-0" #39：是线程的名字，因此，一般我们创建线程时需要设置自己可以辩识的名字。
- daemon 表示线程是否是守护线程
- prio 表示我们为线程设置的优先级
- os_prio 表示的对应的操作系统线程的优先级，由于并不是所有的操作系统都支持线程优先级，所以可能会出现都置为0的情况
- tid 线程的id
- nid 线程对应的操作系统本地线程id，每一个java线程都有一个对应的操作系统线程，它是16进制的，因此一般在操作系统中获取到线程ID后，需要转为16进制，来对应上。
- java.lang.Thread.State: RUNNABLE 运行状态，上面已经介绍了线程的状态，若是WAITING状态，则括号中的内容说明了导致等待的原因，如parking说明是因为调用了LockSupport.park方法导致等待。通常的堆栈信息中，都会有lock标记，如- locked <0x00000000f8c85380> (a java.lang.Object)表示正在占用这个锁。
- 对于线程停顿，CPU占用等问题，可以重点看一下wait状态的线程
- 对于死锁，在Dump出来的线程栈快照可以直接报告出Java级别的死锁。

### jps

##### 查看进程ID
```
-q：只显示java进程的pid
-m：输出传递给main方法的参数，在嵌入式jvm上可能是null
-l：输出应用程序main class的完整package名 或者 应用程序的jar文件完整路径名
-v：输出传递给JVM的参数
```
##### 例子：
````
jps -l
321 
2451 org.jetbrains.jps.cmdline.Launcher
2452 com.nivelle.container.ContainerBootstrapApplication
8133 jdk.jcmd/sun.tools.jps.Jps


````