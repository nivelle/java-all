#### 1. exit 退出(root账号前面是#不受限制)


#### 2.目录结构

```
1. / : 根目录位于目录结构的最顶层，用斜线（/）表示，类似于Windows操作系统的“C:\“，包含Fedora操作系统中所有的目录和文件。根目录是所有用户共享的目录

2. /root: 超级用户root的主目录

3. /home/username 普通用户的家目录

4. /etc : 目录是整个Linux系统的中心，其中包含所有系统管理和维护方面的配置文件 f

5. /bin : 目录又称为二进制目录，包含了那些供系统管理员和普通用户使用的重要linux命令的二进制映像。该目录存放的内容包括各种可执行文件，还有某些可执行文件的符号连接。常用的命令有：cp、dmesg、kill、login、rm、ping、chomd、bash、cat、echo、ls、 mail、vi等
          /bin:是系统的一些指令.
          /usr/bin:是你在后期安装的一些软件的运行脚本.


6. /sbin : 目录/sbin、/usr/sbin和/usr/local/sbin 存放了该目录启动系统时需执行的程序，如管理工具、应用软件和通用的根用户权限命令等内容
           /sbin:一般是指超级用户指令.
          

7. /usr/bin/usr/sbin 系统预装的其他命令

8. /boot:目录存放系统核心文件以及启动时必须读取的文件，包括Linux内核的二进制映像。
   
9. /dev:目录保存着外部设备代码的文件,这些文件比较特殊,实际上它们都指向所代表的外围设备,如终端、磁盘驱动器、光驱、打印机等

10. /lib:目录下存放必要的运行库，主要是编程语言的库。

11. /lost+found:该目录存放所有和其它目录都没有关联的文件。系统出现错误或发生问题时，Fedora会自动扫描磁盘驱动器，修正错误，如果找到遗失或错误的片段，将这些片段转化成文件存放于此，等待管理员的进一步处理。

12. /mnt: 该目录是默认的文件系统临时装载点，这是一个通用的安装点，可以临时安装任何文件系统或远程资源。

13. /proc: 进程文件系统proc的根目录，其中的部分文件分别对应正在运行的进程，可用于访问当前进程的地址空间。它是一个非常特殊的虚拟文件系统，其中并不包含“实际的”文件，而是可用以引用当前运行系统的系统信息，如CPU、内存、运行时间、软件配置以及硬件配置的信息，这些信息是在内存中由系统自己产生的。

14. /opt:目录用来安装附加软件包，用户调用软件包程序放在目录/opt/package_name/bin下，package_name是安装软件包的名称

15. /usr:这是个最庞大的目录,我们要用到的很多应用程序和文件几乎都存放在这个目录了

16. /var: 用于存放很多不断变化的文件，例如日志文件等。包含了日志文件、计划性任务和邮件等内容
```

#### 2.ls / + 回车 展示目录结构

```
root 用户下的目录结构:

bin  boot  data  dev  etc  home  lib  lib64  lost+found  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var


```

#### 3.ls /bin + 回车 展示所有命令


#### 4. 帮助命令

- man + 命令

- help +空格+ 命令 或者 命令 + --help

- info + 命令

### 5. 文件

#### (1) pwd 所在目录

#### (2) cd命令

这是一个非常基本，也是大家经常需要使用的命令，它用于切换当前目录，它的参数是要切换到的目录的路径，可以是绝对路径，也可以是相对路径。

-  (/) 代表根目录 
 
- (./ )代表当前目录 同时 (../) 代表上级目录

- cd /root/Docements # 切换到目录/root/Docements,绝对路径 

- cd ./path          # 切换到当前目录下的path目录中，“.”表示当前目录

- cd ../path         # 切换到上层目录中的path目录中，“..”表示上一层目录 

- cd ~：回到用户家目录。 ## root用户，cd ~ 相当于 cd /root ; 普通用户，cd ~ 相当于cd /home/当前用户名

- cd # :来回切换

- cd - :返回进入此目录之前所在目

- cd /home 相当于查看有多少普通用户的家目录

这得看你是用什么用户登录了，如果是以root身份登录，执行此命令后，回到/root/目录下，如果是以其他用户等录得话，则是回到/home/目录下


#### (3) ls命令

这是一个非常有用的查看文件与目录的命令,list之意，它的参数非常多，下面就列出一些我常用的参数吧，如下：


- l : 列出长数据串，包含文件的属性与权限数据等(子文件数量,权限)  
     
     demo: drwxr-xr-x 3 root root 4096 1月  13 23:32 tomcat //文件类型,文件权限,文件个数,创建者,所属用户组,文件大小,文件时间,文件名称

- r : 和-l搭配使用,逆向展示 和 -t 搭配使用 时间逆向展示


- a ：列出全部的文件，连同隐藏文件（开头为.的文件）一起列出来（常用）

- d ：仅列出目录本身，而不是列出目录的文件数据  

- h ：将文件容量以较易读的方式（GB，kB等）列出来 

- R ：连同子目录的内容一起列出（递归列出），等于该目录下的所有文件都会显示出来 


#### (4) mkdir 创建空目录

- mkdir a b c : 创建多个目录(a,b,c),如果已经存在则报错

- mkdir -p  /a/b: 创建多级目录 a/b

- rmdir /a: 删除空目录

- rm -r -f+多个目录 :删除多级目录,-f不用挨个确认

#### (5) 复制命令

- cp -r 源目录 目标目录: 复制目录

- cp 源文件 目标文件

- cp -v :显示复制详情

- cp -a ：将文件的特性一起复制

- cp -p ：连同文件的属性一起复制，而非使用默认方式，与-a相似，常用于备份  

- cp -i ：若目标文件已经存在时，在覆盖时会先询问操作的进行  

- cp -r ：递归持续复制，用于目录的复制行为  

- cp -u ：目标文件与源文件有差异时才会复制  


#### (6) 移动重命名

- mv 源目录 目标目录(新名字)


#### (7) 文件查看命令

- cat 文本内容显示到终端
- head 查看文件开头; head -n //查看头部多少行
- tail 查看文件结尾; tail -f //文件内容更新后,显示信息同步更新; tail -n //查看尾部多少行
- wc 统计文件内容信息; wc -l 文件 //查看文件有多少行

#### (8) 打包压缩

**tar (1) c 打包 (2) x 解包 (3) f 指定操作类型为文件 (4) c 表示压缩**

-  打包压缩: tar czf(cjf) /压缩后文件名路径 源文件目录 (z:表示压缩zgip压缩格式,j:表示bzip2压缩格式)

-  解压: tar xf 指定要解压文件 -C 目标路径

-  压缩命令: gzip 和 bzip2


#### (9) vim命令

- :q 退出

- i 输入 -I 输入光标到首行

- :wq 保存

#### (10) 给可执行文件赋权限

```
chmod u+x shellfile.sh

``` 

#### (11) 返回上一个目录

- cd - 


### 6. 用户管理和组管理

#### 用户
- useradd userName //创建用户

- userdel userName //删除用户

- id + 用户名 //查看存在的用户; id+回车 显示当前的用户

- root 用户的家目录 /root  其他用户的家目录 /home/用户名 //用户的家目录

- passwd 用户名;//设置新的用户密码

- usermod -d +“新的家目录”+用户名称

#### 组

useradd -g group1 user2;//将user2加入到group1组

#### su & sudo 用户切换

- su - user1 //临时切换到user1 , - 的意义是切换上下文环境;切换到root需要密码
- 
- sudo 以其他用户身份执行命令;以普通用户身份执行root用户授权的命令,而且不用密码
  
  visudo 配置roor授权给普通用户的指令集合


### 7. 网路

- ipconfig
- route // route -n 查看路由
- **==netstat==**

```
//netstat命令用于显示与IP、TCP、UDP和ICMP协议相关的统计数据，一般用于检验本机各端口的网络连接情况

  ```
  
  - netstat –i//网卡
  - netstat -s //网路统计
  - netstat -lntup  
  - netstat –e //以太网
  - netstat –r //路由
  - netstat -an | awk '/^tcp/ {++S[$NF]}  END {for (a in S) print a,S[a]} ' //网路链接个数
  - netstat -ap | grep 程序 //程序运行的端口
  - netstat -pt //在 netstat 输出中显示 TCP连接信息

#####  套接口类型:
 
 -t ：TCP

 -u ：UDP

-raw ：RAW类型

--unix ：UNIX域类型

--ax25 ：AX25类型

--ipx ：ipx类型

--netrom ：netrom类型

##### 状态说明：


LISTEN：侦听来自远方的TCP端口的连接请求

SYN-SENT：再发送连接请求后等待匹配的连接请求（如果有大量这样的状态包，检查是否中招了）

SYN-RECEIVED：再收到和发送一个连接请求后等待对方对连接请求的确认（如有大量此状态，估计被flood攻击了）

ESTABLISHED：代表一个打开的连接

FIN-WAIT-1：等待远程TCP连接中断请求，或先前的连接中断请求的确认

FIN-WAIT-2：从远程TCP等待连接中断请求

CLOSE-WAIT：等待从本地用户发来的连接中断请求

CLOSING：等待远程TCP对连接中断的确认

LAST-ACK：等待原来的发向远程TCP的连接中断请求的确认（不是什么好东西，此项出现，检查是否被攻击）

TIME-WAIT：等待足够的时间以确保远程TCP接收到连接中断请求的确认

CLOSED：没有任何连接状态
  


### 8. 进程

#### ps

```
显示信息：
PID (进程唯一id,名字收可以重复) TTY (终端,当前为虚拟终端) TIME CMD
```

- ps -e | more //分也输出

- ps -ef | grep "关键词   //显示有效用户信息


- ps -eLf //LWP :轻量级进程-线程
```
 uid:有效用户
 ppid:父进程
 cmd:启动命令
 lwp:线程id 
```


- pstree //进程树

```
systemd─┬─AliYunDun───23*[{AliYunDun}]
        ├─AliYunDunUpdate───3*[{AliYunDunUpdate}]
        ├─2*[agetty]
        ├─aliyun-service───2*[{aliyun-service}]
        ├─chronyd
        ├─crond
        ├─dbus-daemon
        ├─java───40*[{java}]
        ├─rngd
        ├─rsyslogd───2*[{rsyslogd}]
        ├─sshd───sshd───bash─┬─passwd
        │                    └─su───bash───su───bash─┬─bash─┬─more
        │                                            │      └─pstree
        │                                            └─visudo───vi
        ├─systemd-journal
        ├─systemd-logind
        ├─systemd-network
        ├─systemd-resolve
        └─systemd-udevd

```

#### top 

```
top - 12:00:35 up 54 days(开机54天), 13:13,  1 user(两个用户登陆),  load average: 0.00, 0.00, 0.00(平均负载,繁忙程度,1是满负载)
```


```
任务: Tasks(进程数):  68 total,   1 running,  38 sleeping,   5 stopped,   0 zombie
```

```
CPU: %Cpu(s(多cup,按1,2分别显示)):  0.7 us(参与用户状态计算),  0.3 sy(进程状态交互),  0.0 ni, 99.0 id(空闲),  0.0 wa(等待),  0.0 hi,  0.0 si,  0.0 st  //加起来等于100
```

```
内存:KiB Mem :  2041072 total(内存),   117932 free,   858800 used,  1064340 buff/cache(读写缓存)
```

```
交换分区:KiB Swap:        0 total,        0 free,        0 used.  1007652 avail Mem
```
```
PID USER      PR(优先级)  NI(nice值,占用资源)    VIRT    RES    SHR S %CPU %MEM     TIME+ COMMAND (运行时间)                                                                                                            
13478 root      10 -10  127152  18140  14168 S  0.7  0.9 228:47.08 AliYunDun                                                                                                           
    1 root      20   0   43216   4924   3872 S  0.0  0.2   0:35.24 systemd                                                                                                             
    2 root      20   0       0      0      0 S  0.0  0.0   0:00.10 kthreadd                                                                                                            
    3 root       0 -20       0      0      0 I  0.0  0.0   0:00.00 rcu_gp                                                                                                              
```

#### top -Hp pid 查看具体线程占用系统资源情况。

#### nice

- nice //越小优先级越高
- renice //调整优先级

### vmstat

vmstat 是一款指定采样周期和次数的功能性监测工具，我们可以看到，它不仅可以统计内存的使用情况，还可以观测到 CPU 的使用率、swap 的使用情况。

vmstat 1 10 命令行代表每秒收集一次性能指标，总共获取10次。


- r：等待运行的进程数；

- b：处于非中断睡眠状态的进程数；

- swpd：虚拟内存使用情况；

- free：空闲的内存；

- buff：用来作为缓冲的内存数；

- si：从磁盘交换到内存的交换页数量；

- so：从内存交换到磁盘的交换页数量；

- bi：发送到块设备的块数；

- bo：从块设备接收到的块数；

- in：每秒中断数；

- cs：每秒上下文切换次数；

- us：用户 CPU 使用时间；

- sy：内核 CPU 系统使用时间；

- id：空闲时间；

- wa：等待 I/O 时间；

- st：运行虚拟机窃取的时间。

### pidstat

```
 pidstat 的参数 -p 用于指定进程 ID，-r 表示监控内存的使用情况，1 表示每秒的意思，3 则表示采样次数。
```

pidstat 命令则是深入到线程级别

-u：默认的参数，显示各个进程的 cpu 使用情况；

-r：显示各个进程的内存使用情况；

-d：显示各个进程的 I/O 使用情况；

-w：显示每个进程的上下文切换情况；

-p：指定进程号；

-t：显示进程中线程的统计信息。

### jstat 

```
jstat - option pid

```

#### 可以监测 Java 应用程序的实时运行情况，包括堆内存信息以及垃圾回收信息。

- class：显示 ClassLoad 的相关信息;

- compiler：显示 JIT 编译的相关信息;

- gc：显示和 gc 相关的堆信息;

- gccapacity：显示各个代的容量以及使用情况;

- gcmetacapacity：显示 Metaspace 的大小;

- gcnew：显示新生代信息;

- gcnewcapacity：显示新生代大小和使用情况;

- gcold：显示老年代和永久代的信息;

- gcoldcapacity:显示老年代的大小;

- gcutil：显示垃圾收集信息;

- gccause：显示垃圾回收的相关信息（通 -gcutil），同时显示最后一次或当前正在发生的垃圾回收的诱因; 
 
```
S0C：年轻代中 To Survivor 的容量（单位 KB）；

S1C：年轻代中 From Survivor 的容量（单位 KB）；

S0U：年轻代中 To Survivor 目前已使用空间（单位 KB）；

S1U：年轻代中 From Survivor 目前已使用空间（单位 KB）；

EC：年轻代中 Eden 的容量（单位 KB）；

EU：年轻代中 Eden 目前已使用空间（单位 KB）；

OC：Old 代的容量（单位 KB）；

OU：Old 代目前已使用空间（单位 KB）；

MC：Metaspace 的容量（单位 KB）；

MU：Metaspace 目前已使用空间（单位 KB）；

YGC：从应用程序启动到采样时年轻代中 gc 次数；

YGCT：从应用程序启动到采样时年轻代中 gc 所用时间 (s)；

FGC：从应用程序启动到采样时 old 代（全 gc）gc 次数；

FGCT：从应用程序启动到采样时 old 代（全 gc）gc 所用时间 (s)；

GCT：从应用程序启动到采样时 gc 用的总时间 (s)。

```

#### jps 

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

"Attach Listener" #56 daemon prio=9 os_prio=31 tid=0x00007f9240b37000 nid=0x6803 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"DestroyJavaVM" #41 prio=5 os_prio=31 tid=0x00007f923fbc6000 nid=0xc03 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"http-nio-8090-Acceptor" #39 daemon prio=5 os_prio=31 tid=0x00007f9241c20000 nid=0x6203 runnable [0x000070000c158000]
   java.lang.Thread.State: RUNNABLE
        at sun.nio.ch.ServerSocketChannelImpl.accept0(Native Method)
        at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:422)
        at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:250)
        - locked <0x000000079d6fdb28> (a java.lang.Object)
        at org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept(NioEndpoint.java:469)
        at org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept(NioEndpoint.java:71)
        at org.apache.tomcat.util.net.Acceptor.run(Acceptor.java:95)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"http-nio-8090-ClientPoller" #38 daemon prio=5 os_prio=31 tid=0x00007f9241c1d000 nid=0x6003 runnable [0x000070000c055000]
   java.lang.Thread.State: RUNNABLE
        at sun.nio.ch.KQueueArrayWrapper.kevent0(Native Method)
        at sun.nio.ch.KQueueArrayWrapper.poll(KQueueArrayWrapper.java:198)
        at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:117)
        at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
        - locked <0x00000007988c2e70> (a sun.nio.ch.Util$3)
        - locked <0x00000007988c2e60> (a java.util.Collections$UnmodifiableSet)
        - locked <0x00000007988c2d40> (a sun.nio.ch.KQueueSelectorImpl)
        at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
        at org.apache.tomcat.util.net.NioEndpoint$Poller.run(NioEndpoint.java:709)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

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

"http-nio-8080-Acceptor" #36 daemon prio=5 os_prio=31 tid=0x00007f9240b3e000 nid=0x5e03 runnable [0x000070000be4f000]
   java.lang.Thread.State: RUNNABLE
        at sun.nio.ch.ServerSocketChannelImpl.accept0(Native Method)
        at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:422)
        at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:250)
        - locked <0x0000000798808ea8> (a java.lang.Object)
        at org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept(NioEndpoint.java:469)
        at org.apache.tomcat.util.net.NioEndpoint.serverSocketAccept(NioEndpoint.java:71)
        at org.apache.tomcat.util.net.Acceptor.run(Acceptor.java:95)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"http-nio-8080-ClientPoller" #35 daemon prio=5 os_prio=31 tid=0x00007f9240b3d000 nid=0xa003 runnable [0x000070000bd4c000]
   java.lang.Thread.State: RUNNABLE
        at sun.nio.ch.KQueueArrayWrapper.kevent0(Native Method)
        at sun.nio.ch.KQueueArrayWrapper.poll(KQueueArrayWrapper.java:198)
        at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:117)
        at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
        - locked <0x00000007988a2628> (a sun.nio.ch.Util$3)
        - locked <0x00000007988a2618> (a java.util.Collections$UnmodifiableSet)
        - locked <0x00000007988a24f8> (a sun.nio.ch.KQueueSelectorImpl)
        at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
        at org.apache.tomcat.util.net.NioEndpoint$Poller.run(NioEndpoint.java:709)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"http-nio-8080-exec-1" #34 daemon prio=5 os_prio=31 tid=0x00007f9240408000 nid=0xa203 waiting on condition [0x000070000bc49000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x00000007988738a0> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
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

"http-nio-8080-BlockPoller" #33 daemon prio=5 os_prio=31 tid=0x00007f9240b3f000 nid=0xa30b runnable [0x000070000bb46000]
   java.lang.Thread.State: RUNNABLE
        at sun.nio.ch.KQueueArrayWrapper.kevent0(Native Method)
        at sun.nio.ch.KQueueArrayWrapper.poll(KQueueArrayWrapper.java:198)
        at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:117)
        at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
        - locked <0x0000000798809200> (a sun.nio.ch.Util$3)
        - locked <0x00000007988091f0> (a java.util.Collections$UnmodifiableSet)
        - locked <0x00000007988090d0> (a sun.nio.ch.KQueueSelectorImpl)
        at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
        at org.apache.tomcat.util.net.NioBlockingSelector$BlockPoller.run(NioBlockingSelector.java:313)

   Locked ownable synchronizers:
        - None

"container-0" #30 prio=5 os_prio=31 tid=0x00007f9241c00000 nid=0x5b03 waiting on condition [0x000070000ba43000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
        at java.lang.Thread.sleep(Native Method)
        at org.apache.catalina.core.StandardServer.await(StandardServer.java:570)
        at org.springframework.boot.web.embedded.tomcat.TomcatWebServer$1.run(TomcatWebServer.java:179)

   Locked ownable synchronizers:
        - None

"Catalina-utility-2" #29 prio=1 os_prio=31 tid=0x00007f9240401800 nid=0xa503 waiting on condition [0x000070000b940000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x000000079d6fcad8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
        at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
        at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
        at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
        at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"Catalina-utility-1" #28 prio=1 os_prio=31 tid=0x00007f923fb9d000 nid=0x5a0b waiting on condition [0x000070000b83d000]
   java.lang.Thread.State: TIMED_WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x000000079d6fcad8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
        at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
        at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
        at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
        at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"http-nio-8090-BlockPoller" #26 daemon prio=5 os_prio=31 tid=0x00007f9241bca800 nid=0x571b runnable [0x000070000b637000]
   java.lang.Thread.State: RUNNABLE
        at sun.nio.ch.KQueueArrayWrapper.kevent0(Native Method)
        at sun.nio.ch.KQueueArrayWrapper.poll(KQueueArrayWrapper.java:198)
        at sun.nio.ch.KQueueSelectorImpl.doSelect(KQueueSelectorImpl.java:117)
        at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
        - locked <0x000000079d6fd4f0> (a sun.nio.ch.Util$3)
        - locked <0x000000079d6fd4e0> (a java.util.Collections$UnmodifiableSet)
        - locked <0x000000079d6fd3a0> (a sun.nio.ch.KQueueSelectorImpl)
        at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
        at org.apache.tomcat.util.net.NioBlockingSelector$BlockPoller.run(NioBlockingSelector.java:313)

   Locked ownable synchronizers:
        - None

"logback-1" #20 daemon prio=5 os_prio=31 tid=0x00007f9240303800 nid=0x5807 waiting on condition [0x000070000b73a000]
   java.lang.Thread.State: WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x0000000740013e08> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
        at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
        at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1088)
        at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
        at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"RMI Scheduler(0)" #15 daemon prio=5 os_prio=31 tid=0x00007f9240920800 nid=0x5503 waiting on condition [0x000070000b431000]
   java.lang.Thread.State: TIMED_WAITING (parking)
        at sun.misc.Unsafe.park(Native Method)
        - parking to wait for  <0x0000000740042de8> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
        at java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
        at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
        at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
        at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1074)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1134)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"RMI TCP Accept-0" #13 daemon prio=5 os_prio=31 tid=0x00007f924027c800 nid=0x3b03 runnable [0x000070000b128000]
   java.lang.Thread.State: RUNNABLE
        at java.net.PlainSocketImpl.socketAccept(Native Method)
        at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:409)
        at java.net.ServerSocket.implAccept(ServerSocket.java:545)
        at java.net.ServerSocket.accept(ServerSocket.java:513)
        at sun.management.jmxremote.LocalRMIServerSocketFactory$1.accept(LocalRMIServerSocketFactory.java:52)
        at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:405)
        at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.run(TCPTransport.java:377)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"RMI TCP Accept-50084" #12 daemon prio=5 os_prio=31 tid=0x00007f9240273000 nid=0x4103 runnable [0x000070000b025000]
   java.lang.Thread.State: RUNNABLE
        at java.net.PlainSocketImpl.socketAccept(Native Method)
        at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:409)
        at java.net.ServerSocket.implAccept(ServerSocket.java:545)
        at java.net.ServerSocket.accept(ServerSocket.java:513)
        at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:405)
        at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.run(TCPTransport.java:377)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"RMI TCP Accept-0" #11 daemon prio=5 os_prio=31 tid=0x00007f9241961800 nid=0x3903 runnable [0x000070000af22000]
   java.lang.Thread.State: RUNNABLE
        at java.net.PlainSocketImpl.socketAccept(Native Method)
        at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:409)
        at java.net.ServerSocket.implAccept(ServerSocket.java:545)
        at java.net.ServerSocket.accept(ServerSocket.java:513)
        at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:405)
        at sun.rmi.transport.tcp.TCPTransport$AcceptLoop.run(TCPTransport.java:377)
        at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
        - None

"Service Thread" #9 daemon prio=9 os_prio=31 tid=0x00007f9241919000 nid=0x4403 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"C1 CompilerThread2" #8 daemon prio=9 os_prio=31 tid=0x00007f9240166800 nid=0x4503 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"C2 CompilerThread1" #7 daemon prio=9 os_prio=31 tid=0x00007f923f8ed000 nid=0x3703 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"C2 CompilerThread0" #6 daemon prio=9 os_prio=31 tid=0x00007f924011e000 nid=0x4703 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"Monitor Ctrl-Break" #5 daemon prio=5 os_prio=31 tid=0x00007f924011b000 nid=0x3403 runnable [0x000070000aa13000]
   java.lang.Thread.State: RUNNABLE
        at java.net.SocketInputStream.socketRead0(Native Method)
        at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
        at java.net.SocketInputStream.read(SocketInputStream.java:171)
        at java.net.SocketInputStream.read(SocketInputStream.java:141)
        at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:284)
        at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:326)
        at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:178)
        - locked <0x00000007400cdc20> (a java.io.InputStreamReader)
        at java.io.InputStreamReader.read(InputStreamReader.java:184)
        at java.io.BufferedReader.fill(BufferedReader.java:161)
        at java.io.BufferedReader.readLine(BufferedReader.java:324)
        - locked <0x00000007400cdc20> (a java.io.InputStreamReader)
        at java.io.BufferedReader.readLine(BufferedReader.java:389)
        at com.intellij.rt.execution.application.AppMainV2$1.run(AppMainV2.java:64)

   Locked ownable synchronizers:
        - None

"Signal Dispatcher" #4 daemon prio=9 os_prio=31 tid=0x00007f923f856000 nid=0x4903 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
        - None

"Finalizer" #3 daemon prio=8 os_prio=31 tid=0x00007f9240818800 nid=0x5203 in Object.wait() [0x000070000a707000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x00000007400cebd8> (a java.lang.ref.ReferenceQueue$Lock)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
        - locked <0x00000007400cebd8> (a java.lang.ref.ReferenceQueue$Lock)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)
        at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)

   Locked ownable synchronizers:
        - None

"Reference Handler" #2 daemon prio=10 os_prio=31 tid=0x00007f923f827800 nid=0x2d03 in Object.wait() [0x000070000a604000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x00000007400cede8> (a java.lang.ref.Reference$Lock)
        at java.lang.Object.wait(Object.java:502)
        at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
        - locked <0x00000007400cede8> (a java.lang.ref.Reference$Lock)
        at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)

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

### netstat

- netstat -an |grep tcp ;// 查看tcp连接

### jstack 参数

- prio : 表示线程优先级，就是Thread中定义的这个。

- os_prio : 表示操作系统级别的优先级
  
- tid : 表示Java内的线程ID,同样在Thread类中

- nid：表示操作系统级别的线程ID的16进制形()
  
