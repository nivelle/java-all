



本次分享探讨的JVM调优是指server端运行的JVM调优，适应版本为[1.6– 1.7], 不涉及最新的1.8版本。

假设线程池、连接池、程序代码等都已经做过优化，效果（系统吞吐量、响应性能）仍然不理想，我们就可以考虑JVM调优了。




一、 JVM调优知识背景简介


1、堆与栈的概念

堆和栈是程序运行的关键：栈是运行时的单位，而堆是存储的单位。


栈解决程序的运行问题，即程序如何执行，或者说如何处理数据；堆解决的是数据存储的问题，即数据怎么

放、放在哪儿。


在Java中一个线程就会相应有一个线程栈与之对应，而堆则是所有线程共享的。


栈存储的信息都是跟当前线程（或程序）相关信息的，包括：局部变量、程序运行状态、方法返回值等等。

堆只负责存储对象信息。
简单来说：堆中存的是对象，栈中存的是基本数据类型和堆中对象的引用。



2、堆模型

虚拟机中共划分为三个代：

年轻代（Young Generation）：用来存放JVM刚分配的Java对象。

年老代（Old Generation）：年轻代中经过垃圾回收没有回收掉的对象将被Copy到年老代。

持久代（PermGeneration）：存放Class、Method元信息，其大小跟项目的规模、类、方法的量有关。


如下图所示：




3、堆内存分配策略

1.对象优先在Eden分配

如果Eden区不足分配对象，会做一个minorgc，回收内存，尝试分配对象，如果依然不足分配，才分配到Old区。

2.大对象直接进入老年代

大对象是指需要大量连续内存空间的Java对象，最典型的大对象就是那种很长的字符串及数组，虚拟机提供了一个-XX:PretenureSizeThreshold参数，令大于这个设置值的对象直接在老年代中分配。这样做的目的是避免在Eden区及两个Survivor区之间发生大量的内存拷贝（新生代采用复制算法收集内存）。PretenureSizeThreshold参数只对Serial和ParNew两款收集器有效。


3.长期存活的对象将进入老年代
在经历了多次的Minor GC后仍然存活：在触发了Minor GC后，存活对象被存入Survivor区在经历了多次MinorGC之后，如果仍然存活的话，则该对象被晋升到Old区。虚拟机给每个对象定义了一个对象年龄（Age）计数器。如果对象在Eden出生并经过第一次MinorGC后仍然存活，并且能被Survivor容纳的话，将被移动到Survivor空间中，并将对象年龄设为1。对象在Survivor区中每熬过一次MinorGC，年龄就增加1岁，当它的年龄增加到一定程度（默认为15岁）时，就会被晋升到老年代中。对象晋升老年代的年龄阈值，可以通过参数-XX:MaxTenuringThreshold来设置。


4.动态对象年龄判定

为了能更好地适应不同程序的内存状况，虚拟机并不总是要求对象的年龄必须达到MaxTenuringThreshold才能晋升老年代，如果在Survivor空间中相同年龄所有对象大小的总和大于Survivor空间的一半，年龄大于或等于该年龄的对象就可以直接进入老年代，无须等到MaxTenuringThreshold中要求的年龄。

5.MinorGC后Survivor空间不足就直接放入Old区
6.空间分配担保

在发生MinorGC时，虚拟机会检测之前每次晋升到老年代的平均大小是否大于老年代的剩余空间大小，如果大于，则改为直接进行一次Full GC。如果小于，则查看HandlePromotionFailure设置是否允许担保失败；如果允许，那只会进行Minor GC；如果不允许，则也要改为进行一次Full GC。大部分情况下都还是会将HandlePromotionFailure开关打开，避免FullGC过于频繁。



4、JVM垃圾回收器简介
Java 语言的一大特点就是可以进行自动垃圾回收处理，而无需开发人员过于关注内存资源的释放情况。自动垃圾收集虽然大大减轻了开发人员的工作量，但是也增加了软件系统的负担。

垃圾回收器常用的算法：

1.引用计数法 (Reference Counting)
2.标记-清除算法 (Mark-Sweep)
3.复制算法 (Copying)
4.标记-压缩算法 (Mark-Compact)
5.增量算法 (Incremental Collecting)
6.分代 (Generational Collecting)
从不同角度分析垃圾收集器，可以将其分为不同的类型。

1.按线程数分，可以分为串行垃圾回收器和并行垃圾回收器；
2.按照工作模式分，可以分为并发式垃圾回收器和独占式垃圾回收器；
3.按碎片处理方式可分为压缩式垃圾回收器和非压缩式垃圾回收器；
4.按工作的内存区间，又可分为新生代垃圾回收器和老年代垃圾回收器。
JVM默认的垃圾回收器是Parallel GC垃圾回收器，搭配为ParallelGC + ParallelOldGC ，满足一般场景下JVM垃圾回收。

可以用以下指标评价一个垃圾处理器的好坏：

1.吞吐量；
2.垃圾回收器负载；
3.停顿时间；
4.垃圾回收频率；
5.反应时间；
6.堆分配。

5、 CMS(Concurrent Mark-Sweep)垃圾回收器简介

CMS(Concurrent Mark-Sweep)是以牺牲吞吐量为代价来获得最短回收停顿时间的垃圾回收器。对于要求服务器响应速度的应用上，这种垃圾回收器非常适合。在启动JVM参数加上-XX:+UseConcMarkSweepGC ，这个参数表示对于老年代的回收采用CMS。CMS采用的基础算法是：标记—清除。

CMS适应场景：
1. 相对较多存活时间较长的对象(老年代比较大)；

2. 服务器响应性能要求高；



6、 JVM GC组合方式

![](https://img-blog.csdn.net/20160330195621087?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQv/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


二、 JVM调优参数简介


1、 JVM参数简介

-XX 参数被称为不稳定参数，之所以这么叫是因为此类参数的设置很容易引起JVM 性能上的差异，使JVM 存在极大的不稳定性。如果此类参数设置合理将大大提高JVM 的性能及稳定性。


不稳定参数语法规则：

1.布尔类型参数值
-XX:+<option> '+'表示启用该选项
-XX:-<option> '-'表示关闭该选项

2.数字类型参数值：
-XX:<option>=<number> 给选项设置一个数字类型值，可跟随单位，例如：'m'或'M'表示兆字节;'k'或'K'千字节;'g'或'G'千兆字节。32K与32768是相同大小的。

3.字符串类型参数值：
-XX:<option>=<string> 给选项设置一个字符串类型值，通常用于指定一个文件、路径或一系列命令列表。

例如：-XX:HeapDumpPath=./dump.core


2、 JVM参数示例


配置： -Xmx4g –Xms4g –Xmn1200m –Xss512k -XX:NewRatio=4 -XX:SurvivorRatio=8 -XX:PermSize=100m

-XX:MaxPermSize=256m -XX:MaxTenuringThreshold=15


解析：
-Xmx4g：堆内存最大值为4GB。

-Xms4g：初始化堆内存大小为4GB 。

-Xmn1200m：设置年轻代大小为1200MB。增大年轻代后，将会减小年老代大小。此值对系统性能影响较大，Sun官方推荐配置为整个堆的3/8。

-Xss512k：设置每个线程的堆栈大小。JDK5.0以后每个线程堆栈大小为1MB，以前每个线程堆栈大小为256K。应根据应用线程所需内存大小进行调整。在相同物理内存下，减小这个值能生成更多的线程。但是操作系统对一个进程内的线程数还是有限制的，不能无限生成，经验值在3000~5000左右。

-XX:NewRatio=4：设置年轻代（包括Eden和两个Survivor区）与年老代的比值（除去持久代）。设置为4，则年轻代与年老代所占比值为1：4，年轻代占整个堆栈的1/5

-XX:SurvivorRatio=8：设置年轻代中Eden区与Survivor区的大小比值。设置为8，则两个Survivor区与一个Eden区的比值为2:8，一个Survivor区占整个年轻代的1/10

-XX:PermSize=100m：初始化永久代大小为100MB。

-XX:MaxPermSize=256m：设置持久代大小为256MB。

-XX:MaxTenuringThreshold=15：设置垃圾最大年龄。如果设置为0的话，则年轻代对象不经过Survivor区，直接进入年老代。对于年老代比较多的应用，可以提高效率。如果将此值设置为一个较大值，则年轻代对象会在Survivor区进行多次复制，这样可以增加对象再年轻代的存活时间，增加在年轻代即被回收的概论。



三、 JVM调优目标


1. 何时需要做jvm调优？

什么情况下需要对jvm做调优？
1. heap 内存（老年代）持续上涨达到设置的最大内存值；
2. Full GC 次数频繁；
3. GC 停顿时间过长（超过1秒）；
4. 应用出现OutOfMemory 等内存异常；
5. 应用中有使用本地缓存且占用大量内存空间；

6. 系统吞吐量与响应性能不高或下降。



2. JVM调优原则

JVM调优原则：
1、多数的Java应用不需要在服务器上进行JVM优化；

2、多数导致GC问题的Java应用，都不是因为我们参数设置错误，而是代码问题；

3、在应用上线之前，先考虑将机器的JVM参数设置到最优（最适合）；

4、减少创建对象的数量；

5、减少使用全局变量和大对象；

6、JVM优化是到最后不得已才采用的手段；

7、在实际使用中，分析GC情况优化代码比优化JVM参数更好；



3. JVM调优目标

JVM调优目标 ：
1. GC低停顿；

2. GC低频率；

3. 低内存占用；

4. 高吞吐量;

JVM调优量化目标（示例）：
1. Heap 内存使用率 <= 70%;

2. Old generation内存使用率<= 70%;

3. avgpause <= 1秒;

4. Full gc 次数0 或 avg pause interval >= 24小时 ;

注意：不同应用，其JVM调优量化目标是不一样的。



四、 JVM调优经验



1. JVM调优经验总结

JVM调优的一般步骤为：
第1步：分析GC日志及dump文件，判断是否需要优化，确定瓶颈问题点；

第2步：确定JVM调优量化目标；

第3步：确定JVM调优参数（根据历史JVM参数来调整）；

第4步：调优一台服务器，对比观察调优前后的差异；

第5步：不断的分析和调整，直到找到合适的JVM参数配置；

第6步：找到最合适的参数，将这些参数应用到所有服务器，并进行后续跟踪。



2. JVM调优重要参数解析

注意：不同应用，其JVM最佳稳定参数配置是不一样的。

配置： -server

-Xms12g -Xmx12g -XX:PermSize=500m -XX:MaxPermSize=1000m -Xmn2400m -XX:SurvivorRatio=1 -Xss512k  -XX:MaxDirectMemorySize=1G

-XX:+DisableExplicitGC -XX:CompileThreshold=8000 -XX:+UseConcMarkSweepGC  -XX:+UseParNewGC

-XX:+UseCompressedOops -XX:CMSInitiatingOccupancyFraction=60  -XX:ConcGCThreads=4

-XX:MaxTenuringThreshold=10  -XX:ParallelGCThreads=8

-XX:+ParallelRefProcEnabled  -XX:+CMSClassUnloadingEnabled  -XX:+CMSParallelRemarkEnabled

-XX:CMSMaxAbortablePrecleanTime=500 -XX:CMSFullGCsBeforeCompaction=4 -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseCMSCompactAtFullCollection

-XX:+HeapDumpOnOutOfMemoryError  -verbose:gc  -XX:+PrintGCDetails  -XX:+PrintGCDateStamps  -Xloggc:/weblogic/gc/gc_$$.log


重要参数（可调优）解析：

-Xms12g：初始化堆内存大小为12GB。

-Xmx12g：堆内存最大值为12GB 。

-Xmn2400m：新生代大小为2400MB，包括 Eden区与2个Survivor区。

-XX:SurvivorRatio=1：Eden区与一个Survivor区比值为1:1。

-XX:MaxDirectMemorySize=1G：直接内存。报java.lang.OutOfMemoryError: Direct buffer memory 异常可以上调这个值。

-XX:+DisableExplicitGC：禁止运行期显式地调用 System.gc() 来触发fulll GC。

                                           注意: Java RMI的定时GC触发机制可通过配置-Dsun.rmi.dgc.server.gcInterval=86400来控制触发的时间。

-XX:CMSInitiatingOccupancyFraction=60：老年代内存回收阈值，默认值为68。

-XX:ConcGCThreads=4：CMS垃圾回收器并行线程线，推荐值为CPU核心数。

-XX:ParallelGCThreads=8：新生代并行收集器的线程数。

-XX:MaxTenuringThreshold=10：设置垃圾最大年龄。如果设置为0的话，则年轻代对象不经过Survivor区，直接进入年老代。对于年老代比较多的应用，可以提高效率。如果将此值设置为一个较大值，则年轻代对象会在Survivor区进行多次复制，这样可以增加对象再年轻代的存活时间，增加在年轻代即被回收的概论。

-XX:CMSFullGCsBeforeCompaction=4：指定进行多少次fullGC之后，进行tenured区 内存空间压缩。

-XX:CMSMaxAbortablePrecleanTime=500：当abortable-preclean预清理阶段执行达到这个时间时就会结束。


3. 触发Full GC的场景及应对策略

年轻代空间（包括 Eden 和 Survivor 区域）回收内存被称为 Minor GC，对老年代GC称为MajorGC，而Full GC是对整个堆来说的，在最近几个版本的JDK里默认包括了对永生带即方法区的回收（JDK8中无永生带了），出现Full GC的时候经常伴随至少一次的Minor GC,但非绝对的。MajorGC的速度一般会比Minor GC慢10倍以上。

触发Full GC的场景及应对策略：

1.System.gc()方法的调用，应对策略：通过-XX:+DisableExplicitGC来禁止调用System.gc ;
2.老年代代空间不足，应对策略：让对象在Minor GC阶段被回收，让对象在新生代多存活一段时间，不要创建过大的对象及数组;
3.永生区空间不足，应对策略：增大PermGen空间
4.GC时出现promotionfailed和concurrent mode failure，应对策略：增大survivor space
5.Minor GC后晋升到旧生代的对象大小大于老年代的剩余空间，应对策略：增大Tenured space 或下调CMSInitiatingOccupancyFraction=60
6.   内存持续增涨达到上限导致Full GC  ，应对策略：通过dumpheap 分析是否存在内存泄漏



4. Gc日志分析工具

借助GCViewer日志分析工具，可以非常直观地分析出待调优点。

可从以下几方面来分析：

1. Memory, 分析 Totalheap 、 Tenuredheap 、 Youngheap 内存占用率及其他指标，理论上 内存占用率越小越好 ；
2. Pause  , 分析 Gc pause 、 Full gc pause 、 Total pause 三个大项中各指标，理论上 GC 次数越少越好， GC 时长越小越好 ；

5. MAT 堆内存分析工具

EclipseMemory Analysis Tools (MAT) 是一个分析Java堆数据的专业工具，用它可以定位内存泄漏的原因。
