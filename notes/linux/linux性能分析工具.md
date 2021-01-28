#### 优化点思维导图

[![sIjA91.jpg](https://s3.ax1x.com/2021/01/22/sIjA91.jpg)](https://imgchr.com/i/sIjA91)



#### 性能分析工具

##### sysbench //基准测试

1. 模拟多线程切换

````
# 以10个线程运行5分钟的基准测试，模拟多线程切换的问题
$ sysbench --threads=10 --max-time=300 threads run

````
##### stress //压力测试

1. CPU 使用率100%

````

$ stress --cpu 1 --timeout 600

````

2. I/O 压力模拟

````

$ stress -i 1 --timeout 600

````

##### perf //指定应用层序性能问题

1. perf top //基于事件的，CPU 使用率过高分析工具

````
Samples【采样数】: 1K of event【事件类型】 'cpu-clock', 4000 Hz, Event count【事件总数量】 (approx.): 68944906
Overhead  Shared      Object            Symbol
  18.38%  [kernel]    [k]         finish_task_switch
   9.04%  [kernel]    [k]         _raw_spin_unlock_irqrestore
10.78%  [unknown]     [.]        0x000055c6a5541dc1


````
- Overhead： 是该符号的性能事件在所有采样中的比例，用百分比来表示

- Shared： 是该函数或指令所在的动态共享对象（Dynamic Shared Object），如内核、进程名、动态链接库名、内核模块名等

- Object:是动态共享对象的类型。比如 [.] 表示用户空间的可执行程序、或者动态链接库，而 [k] 则表示内核空间

- Symbol: ，也就是函数名。当函数名未知时，用十六进制的地址来表示。


2. perf record -g //生成文件

````

# 记录性能事件，等待大约15秒后按 Ctrl+C 退出
$ perf record -g

# 查看报告
$ perf report

````

##### IO性能基准测试

- fio io性能基础测试工具
````

# 随机读
fio -name=randread -direct=1 -iodepth=64 -rw=randread -ioengine=libaio -bs=4k -size=1G -numjobs=1 -runtime=1000 -group_reporting -filename=/dev/sdb

# 随机写
fio -name=randwrite -direct=1 -iodepth=64 -rw=randwrite -ioengine=libaio -bs=4k -size=1G -numjobs=1 -runtime=1000 -group_reporting -filename=/dev/sdb

# 顺序读
fio -name=read -direct=1 -iodepth=64 -rw=read -ioengine=libaio -bs=4k -size=1G -numjobs=1 -runtime=1000 -group_reporting -filename=/dev/sdb

# 顺序写
fio -name=write -direct=1 -iodepth=64 -rw=write -ioengine=libaio -bs=4k -size=1G -numjobs=1 -runtime=1000 -group_reporting -filename=/dev/sdb 

````

- direct，表示是否跳过系统缓存。上面示例中，我设置的 1 ，就表示跳过系统缓存

- iodepth，表示使用异步 I/O（asynchronous I/O，简称 AIO）时，同时发出的 I/O 请求上限。在上面的示例中，我设置的是 64

- 表示 I/O 模式。我的示例中， read/write 分别表示顺序读 / 写，而 randread/randwrite 则分别表示随机读 / 写

- ioengine，表示 I/O 引擎，它支持同步（sync）、异步（libaio）、内存映射（mmap）、网络（net）等各种 I/O 引擎。上面示例中，我设置的 libaio 表示使用异步 I/O

- bs，表示 I/O 的大小。

- filename，表示文件路径，当然，它可以是磁盘路径（测试磁盘性能），也可以是文件路径（测试文件系统性能）


##### dstat //可以同时查看 CPU 和 I/O 这两种资源的使用情况

##### ps aux | grep 6082 //检查进程的监控状态

##### strace -p pid //跟踪进程系统调用的工具

- 线程 28014 正在读取大量数据，且读取文件的描述符编号为 38

- 系统调用的执行情况

````

$ strace -f -p 27458
[pid 28014] read(38, "934EiwT363aak7VtqF1mHGa4LL4Dhbks"..., 131072) = 131072
[pid 28014] read(38, "hSs7KBDepBqA6m4ce6i6iUfFTeG9Ot9z"..., 20480) = 20480
[pid 28014] read(38, "NRhRjCSsLLBjTfdqiBRLvN9K6FRfqqLm"..., 131072) = 131072
[pid 28014] read(38, "AKgsik4BilLb7y6OkwQUjjqGeCTQTaRl"..., 24576) = 24576
[pid 28014] read(38, "hFMHx7FzUSqfFI22fQxWCpSnDmRjamaW"..., 131072) = 131072
[pid 28014] read(38, "ajUzLmKqivcDJSkiw7QWf2ETLgvQIpfC"..., 20480) = 20480

````

##### watch -d cat /proc/softirqs //观察命令输出的变化情况

````
$ watch -d cat /proc/softirqs
                    CPU0       CPU1
          HI:          0          0
       TIMER:    1083906    2368646
      NET_TX:         53          9
      NET_RX:    1550643    1916776
       BLOCK:          0          0
    IRQ_POLL:          0          0
     TASKLET:     333637       3930
       SCHED:     963675    2293171
     HRTIMER:          0          0
         RCU:    1542111    1590625

````

##### sar //观察网络收发的吞吐量（BPS，每秒收发的字节数）网络收发的 PPS，即每秒收发的网络帧数

````
[root@jessy ~]# sar -n DEV 1
Linux 4.19.57-15.1.al7.x86_64 (jessy)   01/24/2021      _x86_64_        (1 CPU)

12:38:33 AM     IFACE【网卡】  rxpck/s【每秒接受帧】   txpck/s【每秒发送帧】    rxkB/s【每秒接收千字节数】    txkB/s【每秒发送千字节数】   rxcmp/s   txcmp/s  rxmcst/s
12:38:34 AM        lo      0.00      0.00      0.00      0.00      0.00      0.00      0.00
12:38:34 AM   docker0      0.00      0.00      0.00      0.00      0.00      0.00      0.00
12:38:34 AM      eth0      7.07      5.05      4.44      2.06      0.00      0.00      0.00


````

##### tcpdump

````

# -i eth0 只抓取eth0网卡，-n不解析协议名和主机名
# tcp port 80表示只抓取tcp协议并且端口号为80的网络帧
$ tcpdump -i eth0 -n tcp port 80
15:11:32.678966 IP 192.168.0.2.18238 > 192.168.0.30.80: Flags [S], seq 458303614, win 512, length 0
...
````

##### hping // hping3 是一个可以构造 TCP/IP 协议数据包的工具，可以对系统进行安全审计、防火墙测试等

````

# -S参数表示设置TCP协议的SYN（同步序列号），-p表示目的端口为80
# -i u100表示每隔100微秒发送一个网络帧
# 注：如果你在实践过程中现象不明显，可以尝试把100调小，比如调成10甚至1
$ hping3 -S -p 80 -i u100 192.168.0.30

````

##### mpstat

````
 mpstat
Linux 4.19.57-15.1.al7.x86_64 (jessy)   01/24/2021      _x86_64_        (1 CPU)

12:47:30 PM  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest  %gnice   %idle
12:47:30 PM  all    0.27    0.00    0.23    0.02    0.00    0.05    0.00    0.00    0.00   99.42


`````

##### vmstat 是一款指定采样周期和次数的功能性监测工具，我们可以看到，它不仅可以统计内存的使用情况，还可以观测到 CPU 的使用率、swap 的使用情况。
 
**vmstat 1 10 命令行代表每秒收集一次性能指标，总共获取10次。**

- r(running or Runnable)：就绪队列的长度，也就是正在运行和等待CPU 的进程数

- b(blocked)：处于不可种群睡眠状态的进程数

- swpd：虚拟内存使用情况；

- free：空闲的内存；

- buff：用来作为缓冲的内存数；

- si：从磁盘交换到内存的交换页数量；

- so：从内存交换到磁盘的交换页数量；

- bi：发送到块设备的块数；

- bo：从块设备接收到的块数；

- in(interrupt)：每秒中断数；

- cs(context switch)：每秒上下文切换次数；

- us：用户 CPU 使用时间；

- sy：内核 CPU 系统使用时间；

- id：空闲时间；

- wa：等待 I/O 时间；

- st：运行虚拟机窃取的时间。

##### pidstat

###### 查看每个进程的详细情况
```
 pidstat 的参数 -p 用于指定进程 ID，-r 表示监控内存的使用情况，1 表示每秒的意思，3 则表示采样次数。
```

###### pidstat 命令则是深入到线程级别

-u：默认的参数，显示各个进程的 cpu 使用情况；

-r：显示各个进程的内存使用情况；

````
10:52:22 AM   UID       PID  minflt/s  majflt/s     VSZ    RSS   %MEM  Command
10:52:22 AM     0         1      0.11      0.00   43392   5080   0.25  systemd
10:52:22 AM     0       361      0.80      0.00   39476   8724   0.43  systemd-journal
10:52:22 AM     0       386      0.00      0.00   43312   3560   0.17  systemd-udevd

````

-d：显示各个进程的 I/O 使用情况；

````

10:55:15 AM   UID       PID   kB_rd/s【每秒对的KB数】   kB_wr/s【每秒写的KB数】 kB_ccwr/s【每秒取消的写请求数据大小】  Command
10:55:15 AM     0         1      0.03      0.14      0.03  systemd
10:55:15 AM     0       290      0.00      0.31      0.00  jbd2/vda1-8
10:55:15 AM     0       361      0.00      0.64      0.00  systemd-journal


````
-w：显示每个进程的上下文切换情况；

````

12:18:50 AM   UID       PID    %usr %system  %guest    %CPU   CPU  Command
12:18:51 AM     0       776    1.02    0.00    0.00    1.02     0  AliYunDun
12:18:51 AM     0     25933   21.43   80.61    0.00  100.00     0  sysbench
12:18:51 AM     0     25948    1.02    0.00    0.00    1.02     0  pidstat

12:18:50 AM   UID       PID   cswch/s nvcswch/s  Command
12:18:51 AM     0         9      2.04      0.00  ksoftirqd/0
12:18:51 AM     0        10     15.31      0.00  rcu_sched
12:18:51 AM     0       454      1.02      0.00  rngd
12:18:51 AM     0       776     10.20      0.00  AliYunDun


1. cswch: 每秒自愿上下文切换(voluntary context switches): 指的是进程无法获取所需资源，导致的上下文切换，比如I/O、内存资源不足

2. nvcswch:每秒非自愿上下文切换（non voluntary context switches）: 指的是进程由于时间片已到，被系统强制调度，进而发生上下文切换，比如大量进程在抢CPU时，就容易发生非自愿上下文切换
````
-p：指定进程号；

````
10:56:19 AM   UID       PID    %usr %system  %guest    %CPU   CPU  Command
10:56:19 AM    27       613    0.01    0.01    0.00    0.03     0  mysqld

````

-t：显示进程中线程的统计信息。

````
10:57:03 AM   UID      TGID       TID    %usr %system  %guest    %CPU   CPU  Command
10:57:03 AM     0         1         -    0.00    0.00    0.00    0.00     0  systemd
10:57:03 AM     0         -         1    0.00    0.00    0.00    0.00     0  |__systemd
10:57:03 AM     0         2         -    0.00    0.00    0.00    0.00     0  kthreadd
10:57:03 AM     0         -         2    0.00    0.00    0.00    0.00     0  |__kthreadd
10:57:03 AM     0         9         -    0.00    0.00    0.00    0.00     0  ksoftirqd/0
10:57:03 AM     0         -         9    0.00    0.00    0.00    0.00     0  |__ksoftirqd/0

````

#### bcc 内存检测工具
````
git clone https://github.com/iovisor/bcc.git
mkdir bcc/build; cd bcc/build
cmake .. -DCMAKE_INSTALL_PREFIX=/usr
make
sudo make install
````
##### cachestat 提供了整个操作系统缓存的读写命中情况。
````
cachestat 1 3
HITS   MISSES  DIRTIES HITRATIO   BUFFERS_MB  CACHED_MB
736        0        0  100.00%           97       1181
0        0        0    0.00%           97       1181
0        0        0    0.00%           97       1181
````

- MISSES:缓存未命中次数
- HITS：缓存命中次数
- DIRTIES：新增到缓存页的脏页数
- BUFFERS_MB: buffers的大小，以MB为单位
- CACHED_MB：表示Cache的大小，以MB为单位



##### cachetop 提供了每个进程的缓存命中情况

````

PID      UID      CMD HITS     MISSES   DIRTIES  READ_HIT%  WRITE_HIT%
    6991 root     cachetop                3        0        0     100.0%       0.0%
    1211 root     AliYunDun             586        0        0     100.0%       0.0%


````
##### memleak

````
memleak 可以跟踪系统或指定进程的内存分配、释放请求，然后定期输出一个未释放内存和相应调用栈的汇总情况（默认 5 秒）。

````

##### lsof

-  mysqld 进程打开了大量文件，而根据文件描述符（FD）的编号，我们知道，描述符为 38 的是一个路径为 /var/lib/mysql/test/products.MYD 的文件

- 系统调用的操作对象

````

$ lsof -p 27458
COMMAND  PID USER   FD   TYPE DEVICE SIZE/OFF NODE NAME

mysqld  27458      999   38u   REG    8,1 512440000 2601895 /var/lib/mysql/test/products.MYD

````