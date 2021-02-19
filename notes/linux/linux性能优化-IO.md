### linux IO结构

#### 索引节点和目录项

- Linux 中一切皆文件。不仅普通的文件和目录，就连块设备、套接字、管道等，也都要通过统一的文件系统来管理

- 索引节点，简称为 inode，用来记录文件的元数据，比如 inode 编号、文件大小、访问权限、修改日期、数据的位置等。索引节点和文件一一对应，它跟文件内容一样，都会被持久化存储到磁盘中。所以记住，索引节点同样占用磁盘空间

- 目录项，简称为 dentry，用来记录文件的名字、索引节点指针以及与其他目录项的关联关系。多个关联的目录项，就构成了文件系统的目录结构。不过，不同于索引节点，目录项是由内核维护的一个内存数据结构，所以通常也被叫做目录项缓存

#### 扇区

磁盘读写的最小单位是扇区，然而扇区只有512B大小；文件系统把连续的扇区组成了逻辑块，然后每次以逻辑块为最小单元，来管理数据。 常见逻辑块大小为4KB,也就是由连续的8个扇区组成。

[![sjvT0J.md.jpg](https://s3.ax1x.com/2021/01/26/sjvT0J.md.jpg)](https://imgchr.com/i/sjvT0J)

### 虚拟文件系统

- 目录项、索引节点、逻辑块以及超级块，构成了Linux文件系统的四大基本要素。为了支持各种不同的文件系统，linux内核在用户进程和文件系统中间，引入了一个抽象层： VFS (virtual File System)

- VFS 定义了一组所有文件系统都支持的数据接口和标准接口。用户进程和内核中的其他子系统，只需要和VFS提供的统一接口进行交互就可以了，不必关心底层各种文件系统的实现细节

[![sjxOEj.jpg](https://s3.ax1x.com/2021/01/26/sjxOEj.jpg)](https://imgchr.com/i/sjxOEj)

#### 文件系统的分类

- 基于磁盘的文件系统，也即 把数据直接存储在计算机本地挂载的磁盘中，例如 Ext4、XFS

- 基于内存的文件系统，也即虚拟文件系统。 不会占用磁盘空间，但是占用内存，例如/proc、/sys

- 网络文件系统，用来访问其他计算机数据的文件系统，比如 NFS、SMB、iSCSI

这些文件系统，要先挂载到目录树中的某个子目录（挂载点），然后才能访问其中的文件。

#### 文件系统I/O

把文件系统挂载到挂载点之后，就能通过挂载点，访问它管理的文件了。 VFS 提供了一组标准的文件访问接口。这些接口以系统调用方式，提供给应用程序使用。

##### 根据是否利用标准库缓存，文件I/O 分为缓冲I/O 与 非缓冲I/O

- 缓冲I/O ，是指利用标准库缓存来加速文件的访问，而标准库内部再通过系统调度访问文件
- 非缓冲I/O ，直接通过系统调用来访问文件，不再进故宫标准库缓存

##### 根据是否利用操作系统的页缓存，可以把文件I/O 分为直接I/O 与非直接I/O

- 直接I/O ，指的是跳过操作系统的页缓存，直接根文件系统交互来访问文件
- 非直接I/O ，文件读写时，要先进过系统的页缓存，然后再由内核或额外的系统调用，真正写入磁盘

##### 根据应用程序是否阻塞自身运行，可以把文件 I/O 分为阻塞 I/O 和非阻塞 I/O

- 所谓阻塞 I/O，是指应用程序执行 I/O 操作后,如果没有获得响应,就会阻塞当前线程,自然就不能执行其他任务
- 所谓非阻塞 I/O，是指应用程序执行 I/O 操作后,不会阻塞当前的线程,可以继续执行其他的任务,随后再通过轮询或者事件通知的形式,获取调用的结

##### 根据是否等待响应结果，可以把文件 I/O 分为同步和异步 I/O

- 所谓同步 I/O，是指应用程序执行 I/O 操作后，要一直等到整个 I/O 完成后，才能获得 I/O 响应。
- 所谓异步 I/O，是指应用程序执行 I/O 操作后，不用等待完成和完成后的响应，而是继续执行就可以。等到这次 I/O 完成后，响应会用事件通知的方式，告诉应用程序

### 容量

#### 文件容量

````
-> df /dev
Filesystem     1K-blocks  Used Available Use% Mounted on
devtmpfs         1009488     0   1009488   0% /dev

-> df -h /dev
Filesystem      Size  Used Avail Use% Mounted on
devtmpfs        986M     0  986M   0% /dev

````

#### 索引节点

````
 df -i /dev
Filesystem     Inodes IUsed  IFree IUse% Mounted on
devtmpfs       252372   326 252046    1% /dev

````

#### 缓存

- 内核使用Slab机制，管理目录项和索引节点的缓存。

##### /proc/meminfo

````
[root@jessy ~]# cat /proc/meminfo 
MemTotal:        2040928 kB
MemFree:         1567772 kB
MemAvailable:    1678136 kB
Buffers:           23676 kB
Cached:           207288 kB
SwapCached:            0 kB
Active:           290732 kB
Inactive:         130688 kB
Active(anon):     188308 kB
Inactive(anon):      268 kB
Active(file):     102424 kB
Inactive(file):   130420 kB
Unevictable:           0 kB
Mlocked:               0 kB
SwapTotal:             0 kB
SwapFree:              0 kB
Dirty:                 0 kB
Writeback:             0 kB
AnonPages:        190476 kB
Mapped:            74280 kB
Shmem:               476 kB
Slab:              28360 kB
SReclaimable:      18396 kB
SUnreclaim:         9964 kB
KernelStack:        2188 kB
PageTables:         4396 kB
NFS_Unstable:          0 kB

````

##### /proc/slabinfo

- 查看具体到每一种Slab缓存

- inode_cache 行，表示VFS索引节点缓存，其余的则是各种文件系统的索引节点缓存

````
-> cat /proc/slabinfo | grep -E '^#|dentry|inode' 
# name            <active_objs> <num_objs> <objsize> <objperslab> <pagesperslab> : tunables <limit> <batchcount> <sharedfactor> : slabdata <active_slabs> <num_slabs> <sharedavail>
mqueue_inode_cache      8      8    960    8    2 : tunables    0    0    0 : slabdata      1      1      0
fat_inode_cache        0      0    736   11    2 : tunables    0    0    0 : slabdata      0      0      0
ext4_inode_cache    6705   6705   1088   15    4 : tunables    0    0    0 : slabdata    447    447      0
hugetlbfs_inode_cache     13     13    624   13    2 : tunables    0    0    0 : slabdata      1      1      0
sock_inode_cache     121    121    704   11    2 : tunables    0    0    0 : slabdata     11     11      0
shmem_inode_cache    759    759    704   11    2 : tunables    0    0    0 : slabdata     69     69      0
proc_inode_cache    1728   1728    672   12    2 : tunables    0    0    0 : slabdata    144    144      0
inode_cache//VFS索引节点缓存         5694   5694    600   13    2 : tunables    0    0    0 : slabdata    438    438      0
dentry             19215  19215    192   21    1 : tunables    0    0    0 : slabdata    915    915      0

````

### 磁盘

- 磁盘是可以持久化存储的设备，根据存储介质的不同，常见磁盘可以分为两类：机械磁盘和固态磁盘。

- 机械硬盘：硬盘驱动器（Hard Disk Driver），通常缩写为 HDD；

- 固态磁盘：固态磁盘（Solid State Disk），通常缩写为 SSD，不需要磁道寻址，所以不管是连续I/O 还是随机I/O的性能，都比机械磁盘的好

#### 随机I/O和连续I/O

- 对机械磁盘来说，由于随机 I/O 需要更多的磁头寻道和盘片旋转，它的性能自然要比连续 I/O 慢

- 对固态磁盘来说，虽然它的随机性能比机械硬盘好很多，但同样存在“先擦除再写入”的限制。随机读写会导致大量的垃圾回收，所以相对应的，随机 I/O 的性能比起连续 I/O 来，也还是差了很多。

- 连续 I/O 还可以通过预读的方式，来减少 I/O 请求的次数，这也是其性能优异的一个原因。很多性能优化的方案，也都会从这个角度出发，来优化 I/O 性能

- 机械磁盘的最小读写单位是扇区，一般大小为 512 字节。

- 固态磁盘的最小读写单位是页，通常大小是 4KB、8KB 等

### 通用块层

通用块层，其实是处在文件系统和磁盘驱动中间的一个块设备抽象层。它主要有两个功能 。

- 第一个功能跟虚拟文件系统的功能类似。向上，为文件系统和应用程序，提供访问块设备的标准接口；向下，把各种异构的磁盘设备抽象为统一的块设备，并提供统一框架来管理这些设备的驱动程序。

- 通用块层还会给文件系统和应用程序发来的 I/O 请求排队，并通过重新排序、请求合并等方式，提高磁盘读写的效率。

#### 对I/O 请求排序过程，就是I/O调度

- NONE：更确切来说，并不能算 I/O 调度算法。因为它完全不使用任何 I/O 调度器，对文件系统和应用程序的 I/O 其实不做任何处理，常用在虚拟机中（此时磁盘 I/O 调度完全由物理机负责）

- NOOP: 是最简单的一种 I/O 调度算法。它实际上是一个先入先出的队列，只做一些最基本的请求合并，常用于 SSD 磁盘

- CFQ（Completely Fair Scheduler），也被称为完全公平调度器，是现在很多发行版的默认 I/O 调度器，它为每个进程维护了一个 I/O 调度队列，并按照时间片来均匀分布每个进程的 I/O 请求

- DeadLine: 分别为读、写请求创建了不同的 I/O 队列，可以提高机械磁盘的吞吐量，并确保达到最终期限（deadline）的请求被优先处理。DeadLine 调度算法，多用在 I/O 压力比较重的场景，比如数据库

### I/O 栈

[![svNh8J.png](https://s3.ax1x.com/2021/01/26/svNh8J.png)](https://imgchr.com/i/svNh8J)

- 文件系统层，包括虚拟文件系统和其他各种文件系统的具体实现。它为上层的应用程序，提供标准的文件访问接口；对下会通过通用块层，来存储和管理磁盘数据

- 通用块层，包括块设备 I/O 队列和 I/O 调度器。它会对文件系统的 I/O 请求进行排队，再通过重新排序和请求合并，然后才要发送给下一级的设备层。

- 设备层，包括存储设备和相应的驱动程序，负责最终物理设备的 I/O 操作

### 磁盘性能指标

- 使用率： 指的是磁盘处理I/O 的时间百分比。 过高的使用率（超过80%），通常意味这磁盘I/O 存在性能瓶颈

- 饱和度：指的是磁盘处理I/O的繁忙程度。过高的饱和度，意味着磁盘存在严重的性能瓶颈，饱和度达到100% 时，无法接受新的I/O请求

- IOPS：Input/Output Per Second ，指的是每秒的I/O 请求数

- 吞吐量：每秒的I/O请求大小

- 响应时间：指的是I/O请求从发出到收到的响应的间隔时间

#### 磁盘 I/O 观测

#### iostat I/O 数据详情

````
[root@jessy ~]# iostat -d -x 1  //-d -x表示显示所有磁盘I/O的指
Linux 4.19.57-15.1.al7.x86_64 (jessy)   01/26/2021      _x86_64_        (1 CPU)

Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
vda               0.06     0.17    0.15  341.12     4.64     2.30     0.04     0.00    0.03    0.75    0.03   0.00   0.01

Device:         rrqm/s   wrqm/s     r/s     w/s    rkB/s    wkB/s avgrq-sz avgqu-sz   await r_await w_await  svctm  %util
vda               0.00     0.00    0.99    0.00     7.92     0.00    16.00     0.00    0.00    0.00    0.00   0.00   0.00


````

[![svw9q1.png](https://s3.ax1x.com/2021/01/26/svw9q1.png)](https://imgchr.com/i/svw9q1)

- %util ，就是磁盘 I/O 使用率

- r/s+ w/s ，就是 IOPS

- rkB/s+wkB/s ，就是吞吐量

- r_await+w_await ，就是响应时间

### 指标找工具

[![ySPAQs.md.png](https://s3.ax1x.com/2021/01/28/ySPAQs.md.png)](https://imgchr.com/i/ySPAQs)

### 工具找指标

[![ySPAQs.md.png](https://s3.ax1x.com/2021/01/28/ySPAQs.md.png)](https://imgchr.com/i/ySPAQs)

### 问题排查

[![ySPnoT.md.png](https://s3.ax1x.com/2021/01/28/ySPnoT.md.png)](https://imgchr.com/i/ySPnoT)


