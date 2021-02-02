
## 功能开关:
   
1. 打印gc日志 -XX:+PrintGCDetails 

2. -Djava.lang.Integer.IntegerCache.high=128;//控制integer缓存范围 - XX:+AggressiveOpts也会将 IntegerCache.high 调整至 20000

3. -Dsun.reflect.noInflation=true;//在反射调用一开始便会直接生成动态实现，而不会使用委派实现或者本地实现

4.  Dsun.reflect.inflationThreshold = 15;//当某个反射调用的调用次数在 15 之下时，采用本地实现；当达到 15 时，便开始动态生成字节码，并将委派实现的委派对象切换至动态实现，这个过程我们称之为 Inflation

5. -XX:-AllowUserSignalHandlers	限于Linux和Solaris,默认不启用允许为java进程安装信号处理器,信号处理参见类:sun.misc.Signal,sun.misc.SignalHandler

6. -XX:+DisableExplicitGC	默认启用	禁止在运行期显式地调用System.gc()

7. -XX:+FailOverToOldVerifier	Java6新引入选项，默认启用	如果新的Class校验器检查失败，则使用老的校验器(失败原因:因为JDK6最高向下兼容到JDK1.2，而JDK1.2的class info 与JDK6的info存在较大的差异，所以新校验器可能会出现校验失败的情况)

8. -XX:+HandlePromotionFailure	java5以前是默认不启用，java6默认启用	关闭新生代收集担保

9. -XX:+MaxFDLimit	限于Solaris,默认启用	设置java进程可用文件描述符为操作系统允许的最大值。

10. -XX:PreBlockSpin=10	-XX:+UseSpinning 必须先启用，对于java6来说已经默认启用了，这里默认自旋10次	控制多线程自旋锁优化的自旋次数（JDK1.7后，去掉此参数，由jvm控制）

11. -XX:-RelaxAccessControlCheck	默认不启用	在Class校验器中，放松对访问控制的检查,作用与reflection里的setAccessible类似

12. -XX:+ScavengeBeforeFullGC	默认启用	在Full GC前触发一次Minor GC

13. -XX:+UseAltSigs	限于Solaris，默认启用	为了防止与其他发送信号的应用程序冲突，允许使用候补信号替代 SIGUSR1和SIGUSR2

14. -XX:+UseBoundThreads	限于Solaris, 默认启用	绑定所有的用户线程到内核线程, 减少线程进入饥饿状态（得不到任何cpu time）的次数

15. -XX:-UseConcMarkSweepGC	默认不启用	启用CMS低停顿垃圾收集器,减少FGC的暂停时间

16. -XX:+UseGCOverheadLimit	默认启用	限制GC的运行时间。如果GC耗时过长，就抛OOM

17. -XX:+UseLWPSynchronization	限于solaris，默认启用	使用轻量级进程（内核线程）替换线程同步

18. -XX:-UseParallelGC	-server时启用,其他情况下，默认不启用	策略为新生代使用并行清除，年老代使用单线程Mark-Sweep-Compact的垃圾收集器

19. -XX:-UseParallelOldGC	默认不启用	策略为老年代和新生代都使用并行清除的垃圾收集器

20. -XX:-UseSerialGC	-client时启用,其他情况下，默认不启用	使用串行垃圾收集器

21. -XX:-UseSpinning	java1.4.2和1.5需要手动启用, java6默认已启用	启用多线程自旋锁优化

22. -XX:+UseTLAB	1.4.2以前和使用-client选项时，默认不启用，其余版本默认启用	启用线程本地缓存区

23. -XX:+UseSplitVerifier	java5默认不启用, java6默认启用	使用新的Class类型校验器

24. -XX:+UseThreadPriorities	默认启用	使用本地线程的优先级

25.  -XX:+UseVMInterruptibleIO	限于solaris，默认启用	在solaris中，允许运行时中断线程

## 性能参数:

1. -XX:+AggressiveOpts	JDK 5 update 6后引入，但需要手动启用, JDK6默认启用	启用JVM开发团队最新的调优成果。例如编译优化，偏向锁，并行年老代收集等

2.  -XX:CompileThreshold=10000	1000	通过JIT编译器，将方法编译成机器码的触发阀值，可以理解为调用方法的次数，例如调1000次，将方法编译为机器码

3. -XX:LargePageSizeInBytes=4m	默认4m, amd64位：2m	设置堆内存的内存页大小

4. -XX:MaxHeapFreeRatio=70	70	GC后，如果发现空闲堆内存占到整个预估上限值的70%，则收缩预估上限值

5. -XX:MaxNewSize=size	1.3.1 Sparc: 32m, 1.3.1 x86: 2.5m	新生代占整个堆内存的最大值

6. -XX:MaxPermSize=64m	5.0以后: 64 bit VMs会增大预设值的30%, 1.4 amd64: 96m, 1.3.1 -client: 32m, 其他默认 64m	Perm（俗称方法区）占整个堆内存的最大值

7. -XX:MinHeapFreeRatio=40	40	GC后，如果发现空闲堆内存占到整个预估上限值的40%，则增大上限值

8. -XX:NewRatio=2	Sparc -client: 8, x86 -server: 8, x86 -client: 12, -client: 4 (1.3),8 (1.3.1+), x86: 12, 其他默认 2	新生代和年老代的堆内存占用比例, 例如2表示新生代占年老代的1/2，占整个堆内存的1/3

9. -XX:NewSize=2.125m	5.0以后: 64 bit Vms 会增大预设值的30%, x86: 1m, x86, 5.0以后: 640k, 其他默认 2.125m	新生代预估上限的默认值

10. -XX:ReservedCodeCacheSize=32m	Solaris 64-bit, amd64, -server x86: 48m, 1.5.0_06之前, Solaris 64-bit amd64: 1024m, 其他默认 32m	设置代码缓存的最大值，编译时用

11. -XX:SurvivorRatio=8	 其他默认 8	Eden与Survivor的占用比例。例如8表示，一个survivor区占用 1/8 的Eden内存，即1/10的新生代内存，为什么不是1/9？因为我们的新生代有2个survivor，即S0和S1。所以survivor总共是占用新生代内存的 2/10，Eden与新生代的占比则为 8/10

12. -XX:TargetSurvivorRatio=50	50	实际使用的survivor空间大小占比。默认是50%，最高90%(会导致晋级)

13. -XX:ThreadStackSize=512	Sparc: 512, Solaris x86: 320 (5.0以前 256), Sparc 64 bit: 1024, Linux amd64: 1024 (5.0 以前 0), 其他默认 512.	线程堆栈大小

14. -XX:+UseBiasedLocking	JDK 5 update 6后引入，但需要手动启用, JDK6默认启用 **启用偏向锁（高并发时关闭，减少偏向锁撤销过程开销）**

15. -XX:+UseFastAccessorMethods	默认启用	优化原始类型的getter方法性能(get/set:Primitive Type)

16. -XX:-UseISM	默认启用	启用solaris的ISM

17. -XX:+UseLargePages	JDK 5 update 5后引入，但需要手动启用, JDK6默认启用	启用大内存分页

18. -XX:+UseMPSS	1.4.1 之前: 不启用, 其余版本默认启用	启用solaris的MPSS，不能与ISM同时使用

19. -XX:+UseStringCache	默认开启	启用缓存常用的字符串。

20. -XX:AllocatePrefetchLines=1	1	Number of cache lines to load after the last object allocation using prefetch instructions generated in JIT compiled code. Default values are 1 if the last allocated object was an instance and 3 if it was an array.

21. -XX:AllocatePrefetchStyle=1	1	Generated code style for prefetch instructions.

- no prefetch instructions are generate*d*,
- execute prefetch instructions after each allocation,
- use TLAB allocation watermark pointer to gate when prefetch instructions are executed.

22. -XX:+UseCompressedStrings	Java 6 update 21有一选项	其中，对于不需要16位字符的字符串，可以使用byte[] 而非char[]。对于许多应用，这可以节省内存，但速度较慢（5％-10％）

23. -XX:+UsePSAdaptiveSurvivorSizePolicy: 根据生成对象的速率，以及survivor区的使用情况动态调整Eden和Survivor的比例

24. -XX:+OptimizeStringConcat	在Java 6更新20中引入	优化字符串连接操作在可能的情况下

25. -XX:TieredStopAtLevel=1 使用C1进行编译

26. -XX:+PrintBiasedLockingStatistics 打印各个锁的个数

27. -XX:PetenureSizeThreshold : 直接进入老年代的对象大小

28. -XX:+UseAdaptiveSizePolicy : JVM 将会动态调整 Java 堆中各个区域的大小以及进入老年代的年龄，–XX:NewRatio 和 -XX:SurvivorRatio 将会失效

29. -XX:+UseHeavyMonitors  //直接设置重量级锁,不采用偏向锁和轻量级锁

## 调试参数:

1. -XX:-CITime	 	打印发费在JIT编译上的时间

2. -XX:ErrorFile=./hs_err_pid<pid>.log	JDK6中引入	错误文件

3. -XX:-ExtendedDTraceProbes	JDK6中引入仅在Solaris	启用性能的影响DTrace探测器

4. -XX:HeapDumpPath=./java_pid<pid>.hprof	1.4.2 update 12, 5.0 update 7	指定HeapDump的文件路径或目录

5. -XX:-HeapDumpOnOutOfMemoryError	1.4.2 update 12, 5.0 update 7	当抛出OOM时进行HeapDump,表示当JVM发生OOM时，自动生成DUMP文件

6. -XX:OnError=”<cmd args>;<cmd args>”	1.4.2 update 9	当发生错误时执行用户指定的命令

7. -XX:OnOutOfMemoryError=”<cmd args>; <cmd args>”	1.4.2 update 12, 6	当发生OOM时执行用户指定的命令

8. -XX:-PrintClassHistogram	1.4.2	当Ctrl+Break发生时打印Class实例信息,与jmap -histo相同

9. -XX:-PrintConcurrentLocks	6	当Ctrl+Break发生时打印java.util.concurrent的锁信息, 与jstack -l相同

10. -XX:-PrintCommandLineFlags	5	打印命令行上的标记

11. -XX:-PrintCompilation	 	当方法被编译时打印信息

12. -XX:-PrintGC	 	当GC发生时打印信息

13. -XX:-PrintGCDetails	1.4.0	打印GC详细信息

14. -XX:-PrintGCTimeStamps	1.4.0	打印GC用时;-XX:+PrintGCDateStamps 输出GC的时间戳（以日期的形式，如 2013-05-04T21:53:59.234+0800）

15. -XX:-PrintTenuringDistribution	 	打印Tenuring年龄信息

16. -XX:-TraceClassLoading	 	跟踪类加载

17. -XX:-TraceClassLoadingPreorder	1.4.2	跟踪所有加载的引用类

18. -XX:-TraceClassResolution	1.4.2	跟踪常量池的变化

19. -XX:-TraceClassUnloading	 	跟踪类的卸载

20. -XX:-TraceLoaderConstraints	6	Trace recording of loader constraints

21. -XX:+PerfSaveDataToFile	 	退出时保存jvmstat二进制文件

22. -XX:ParallelGCThreads=	 	设置新生代与老年代并行垃圾回收器的线程数

23. -XX:+UseCompressedOops	 	Enables the use of compressed pointers (object references represented as 32 bit offsets instead of 64-bit pointers) for optimized 64-bit performance with Java heap sizes less than 32gb.

24. -XX:+AlwaysPreTouch	 	Pre-touch the Java heap during JVM initialization. Every page of the heap is thus demand-zeroed during initialization rather than incrementally during application execution.

25. -XX:AllocatePrefetchDistance=	 	Sets the prefetch distance for object allocation. Memory about to be written with the value of new objects is prefetched into cache at this distance (in bytes) beyond the address of the last allocated object. Each Java thread has its own allocation point. The default value varies with the platform on which the JVM is running.

26. -XX:InlineSmallCode=	 	当编译的代码小于指定的值时,内联编译的代码

27. -XX:MaxInlineSize=35	 	内联方法的最大字节数

28. -XX:FreqInlineSize=	 	内联频繁执行的方法的最大字节码大小

29. -XX:LoopUnrollLimit=	 	Unroll loop bodies with server compiler intermediate representation node count less than this value. The limit used by the server compiler is a function of this value, not the actual value. The default value varies with the platform on which the JVM is running.

30. -XX:InitialTenuringThreshold=7	 	设置初始的对象在新生代中最大存活次数

31. -XX:MaxTenuringThreshold=15	 	设置对象在新生代中最大的存活次数,最大值15,并行回收机制默认为15,CMS默认为4(会导致晋级)

32. -XX:+PrintInlining //将内联方法打印出来

33. -XX:+UnlockDiagnosticVMOptions //解锁对JVM进行诊断的选项参数。默认是关闭的，开启后支持一些特定参数对JVM进行诊断

34. -XX:+DoEscapeAnalysis开启逃逸分析,java8默认开启

35. -XX:-DoEscapeAnalysis 关闭逃逸分析

36. -XX:+PrintHeapAtGC 在进行GC的前后打印出堆的信息

37. -Xloggc:../logs/gc.log 日志文件的输出路径


## 锁相关

1. XX:BiasedLockingBulkRebiasThreshold  某一类锁对象的总撤销数超过了一个阈值,那么 Java 虚拟机会宣布这个类的偏向锁失效,撤销偏向锁需要到安全点

2. XX:BiasedLockingBulkRevokeThreshold  Java 虚拟机会认为这个类已经不再适合偏向锁

## JVM启动参数

```

vim catalina.sh

```

JAVA_OPTS=-XX:+PrintGCDetails -Xmx512M -Xms512M -XX:+HeapDumpOnOutOfMemoryError -XX:+UseSerialGC -XX:PermSize=32M -Xloggc:d:/gc.log"

-Xms：堆初始大小；

-Xmx：堆最大值。