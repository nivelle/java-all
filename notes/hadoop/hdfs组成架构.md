### hdfs组成架构

HDFS(Hadoop Distributed File System) 分布式文件系统，HDFS是一个高度容错性的系统，适合部署在廉价的机器上。HDFS能提供高吞吐量的数据访问，非常适合大规模数据集上的应用.由NameNode，若干DataNode，以及Secondary NameNode组成。

[![y4eR61.md.png](https://s3.ax1x.com/2021/02/19/y4eR61.md.png)](https://imgchr.com/i/y4eR61)

1. NameNode :master，它是一个管理者

- 管理HDFS的名称空间
- 配置副本策略
- 管理数据块（block）的映射信息
- 处理客户端读写请求

2. DataNode:Slave. NameNode下达命令，DataNode执行实际的操作

- 存储实际的数据块
- 执行数据块的读写操作

3. Client :客户端

- 文件切分，文件上传HDFS的时候，Client将文件切分成一个一个的Block,然后进行上传
- 与NameNode交互，获取文件的位置信息
- 与DataNode交互，读取或者写入数据
- Client提供一些命令来管理HDFS,比如NameNode格式化
- Client可以通过一些命令来访问HDFS，比如HDFS增删改查操作

4. Secondary NameNode: 并非NameNode热备，当NameNode挂掉的时候，它并不能马上替换NameNode并提供服务

- 辅助NameNode,分担其工作量，比如定期合并Fsimage和Edits，并推送给NameNode;
- 在紧急情况下，可辅助恢复NameNode

### HDFS 文件块大小

- hdfs中文件在物理上是分块存储，块的大小可以通过配置参数**dfs.blocksize**来规定，默认大小在hadoop2.x版本中是128M

- 寻址时间是传输时间的1%时，则为最佳状态

- hdfs块的大小设置主要取决于磁盘传输速率

### HDFS客户端shell操作

````
常用命令实操
（0）启动Hadoop集群（方便后续的测试）
[atguigu@hadoop102 hadoop-2.7.2]$ sbin/start-dfs.sh
[atguigu@hadoop103 hadoop-2.7.2]$ sbin/start-yarn.sh
（1）-help：输出这个命令参数
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -help rm
（2）-ls: 显示目录信息
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -ls /
（3）-mkdir：在HDFS上创建目录
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -mkdir -p /sanguo/shuguo
（4）-moveFromLocal：从本地剪切粘贴到HDFS
[atguigu@hadoop102 hadoop-2.7.2]$ touch kongming.txt
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs  -moveFromLocal  ./kongming.txt  /sanguo/shuguo
（5）-appendToFile：追加一个文件到已经存在的文件末尾
[atguigu@hadoop102 hadoop-2.7.2]$ touch liubei.txt
[atguigu@hadoop102 hadoop-2.7.2]$ vi liubei.txt
输入
san gu mao lu
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -appendToFile liubei.txt /sanguo/shuguo/kongming.txt
（6）-cat：显示文件内容
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -cat /sanguo/shuguo/kongming.txt
（7）-chgrp 、-chmod、-chown：Linux文件系统中的用法一样，修改文件所属权限
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs  -chmod  666  /sanguo/shuguo/kongming.txt
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs  -chown  atguigu:atguigu   /sanguo/shuguo/kongming.txt
（8）-copyFromLocal：从本地文件系统中拷贝文件到HDFS路径去
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -copyFromLocal README.txt /
（9）-copyToLocal：从HDFS拷贝到本地
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -copyToLocal /sanguo/shuguo/kongming.txt ./
（10）-cp ：从HDFS的一个路径拷贝到HDFS的另一个路径
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -cp /sanguo/shuguo/kongming.txt /zhuge.txt
（11）-mv：在HDFS目录中移动文件
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -mv /zhuge.txt /sanguo/shuguo/
（12）-get：等同于copyToLocal，就是从HDFS下载文件到本地
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -get /sanguo/shuguo/kongming.txt ./
（13）-getmerge：合并下载多个文件，比如HDFS的目录 /user/atguigu/test下有多个文件:log.1, log.2,log.3,...
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -getmerge /user/atguigu/test/* ./zaiyiqi.txt
（14）-put：等同于copyFromLocal
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -put ./zaiyiqi.txt /user/atguigu/test/
（15）-tail：显示一个文件的末尾
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -tail /sanguo/shuguo/kongming.txt
（16）-rm：删除文件或文件夹
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -rm /user/atguigu/test/jinlian2.txt
（17）-rmdir：删除空目录
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -mkdir /test
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -rmdir /test
（18）-du统计文件夹的大小信息
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -du -s -h /user/atguigu/test
2.7 K  /user/atguigu/test
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -du  -h /user/atguigu/test
1.3 K  /user/atguigu/test/README.txt
15     /user/atguigu/test/jinlian.txt
1.4 K  /user/atguigu/test/zaiyiqi.txt
（19）-setrep：设置HDFS中文件的副本数量
[atguigu@hadoop102 hadoop-2.7.2]$ hadoop fs -setrep 10 /sanguo/shuguo/kongming.txt
                                              图3-3  HDFS副本数量
这里设置的副本数只是记录在NameNode的元数据中，是否真的会有这么多副本，还得看DataNode的数量。因为目前只有3台设备，最多也就3个副本，只有节点数的增加到10台时，副本数才能达到10。

````
### HDFS读写流程(上传文件)

[![y53PDH.md.png](https://s3.ax1x.com/2021/02/20/y53PDH.md.png)](https://imgchr.com/i/y53PDH)

1. 客户端通过 Distributed FileSystem 模块向NameNode请求上传文件，NameNode检查目标文件是否存在，父目录是否存在

2. NameNode 返回是否可以上传
3. 客户端请求第一个Block上传到那几个DataNode服务器上
4. NameNode返回3个DataNode节点，分别 dn1、dn2、dn3
5. 客户端通过FSDataOutputStream模块请求dn1上传数据、dn1收到请求会继续调用dn2,r然后dn2调用dn3,通信管道建立完成
6. dn1、dn2、dn3 逐级应答客户端
7. 客户端开始往dn1上传第一个Block(先从磁盘读取数据放到一个本地内存缓存)，以packet为单位，dn1收到一个Packet就会传给dn2,dn2传给dn3;dn1每传一个packet会放入一个应答队列等待应答
8. 当一个Block 传输完成之后，客户端请求NameNode上传第二个Block的服务器(重复执行3-7步)

- 在HDFS写数据的过程中，NameNode会选择距离待上传数据距离最近的DataNode接收数据。节点距离：两个节点到达最近的共同祖先的距离总和

### HDFS读数据流程(下载)

[![y5aPAg.md.png](https://s3.ax1x.com/2021/02/20/y5aPAg.md.png)](https://imgchr.com/i/y5aPAg)

1. 客户端通过Distributed FileSystem向NameNode请求下载文件，NameNode通过查询元数据，找到文件所在DataNode地址
2. 挑选一台DataNode（就近原则，然后随机）服务器，请求读取数据
3. DataNode开始传输数据给客户端（从磁盘里面读取数据输入流，以Packet为单位来做校验）
4. 客户端以Packet为单位接收，现在本地缓存，然后写入目标文件

### NameNode 和 SecondaryNameNode

[![y5Rvm4.md.png](https://s3.ax1x.com/2021/02/20/y5Rvm4.md.png)](https://imgchr.com/i/y5Rvm4)

NameNode节点元数据存于磁盘中，因为需要经常进行随机访问，且还响应客户端的请求，效率底下。因此，要将元数据放在内存中，但是如果断电，内存中的数据就会丢失，集群无法工作了。因此在磁盘中备份元数据的FsImage

但是这样会带来新的问题就是，在更新内存中的数据同时，还要同时更新FsImage,这样效率底下，因此，引入Edits文件（只进行追加，效率高），每当元数据有更新或者添加元数据时，修改内存中的数据并追加到Edits中。这样，一旦NameNode节点断电，可以通过FsImage和Edits的合并，合成元数据

但是如果长时间添加数据到Edits中，导致文件太大，某天断电，那么恢复元数据时间很长，因此，需要定期合并FsImage 和 Edits 文件，但是这个操作由NameNode节点来完成，效率底下，因此引入新的节点SecondaryNameNode,专门用于FsImage和Edits定期合并

- Fsimage:NameNode内存中元数据序列化后形成的文件。包含HDFS文件系统的所有目录和文件inode的序列化信息；是HDFS文件系统元数据的永久性检查点；
- Edits:记录客户端更新--增删改元数据的每一步操作
- NameNode启动时，先滚动Edits并生成一个空的edits.inprogress，然后加载Edits和Fsimage到内存中，此时NameNode内存就持有最新的元数据信息

#### 第一阶段：NameNode启动

1. 第一次启动NameNode格式化后，创建Fsimage和Edits文件。如果不是第一次启动，直接加载编辑日志和镜像文件到内存
2. 客户端对元数据进行增删改的请求
3. NameNode记录操作日志，更新滚动日志
4. NameNode在内存中对元数据进行增删改

#### 第二阶段：Secondary NameNode工作

1. Secondary NameNode询问NameNode是否需要CheckPoint. 直接带回NameNode是否检查结果
2. Secondary NameNode请求执行CheckPoint
3. NameNode滚动正在写的Edits日志
4. 将滚动前的编辑日志和镜像文件拷贝到Secondary NameNode
5. Secondary NameNode加载编辑日志和镜像文件到内存，并合并
6. 生成新的镜像文件fsimage.chkpoint
7. 拷贝fsimage.chkpoint到NameNode
8. NameNode将fsimage.chkpoint重新命名成fsimage

### Fsimage和Edits解析

````
root@namenode:/opt/hdfs/name/current# ls
VERSION                                        edits_0000000000000000035-0000000000000000036  edits_0000000000000000063-0000000000000000064
edits_0000000000000000001-0000000000000000003  edits_0000000000000000037-0000000000000000038  edits_0000000000000000065-0000000000000000072
edits_0000000000000000004-0000000000000000012  edits_0000000000000000039-0000000000000000040  edits_0000000000000000073-0000000000000000074
edits_0000000000000000013-0000000000000000014  edits_0000000000000000041-0000000000000000042  edits_inprogress_0000000000000000075
edits_0000000000000000015-0000000000000000017  edits_0000000000000000043-0000000000000000044  fsimage_0000000000000000072
edits_0000000000000000018-0000000000000000026  edits_0000000000000000045-0000000000000000046  fsimage_0000000000000000072.md5
edits_0000000000000000027-0000000000000000028  edits_0000000000000000047-0000000000000000048  fsimage_0000000000000000074
edits_0000000000000000029-0000000000000000030  edits_0000000000000000049-0000000000000000050  fsimage_0000000000000000074.md5
edits_0000000000000000031-0000000000000000032  edits_0000000000000000051-0000000000000000060  seen_txid
edits_0000000000000000033-0000000000000000034  edits_0000000000000000061-0000000000000000062
root@namenode:/opt/hdfs/name/current# vi seen_txid 
bash: vi: command not found
root@namenode:/opt/hdfs/name/current# cat seen_txid 
75
root@namenode:/opt/hdfs/name/current# pwd
/opt/hdfs/name/current

````

- Fsimage文件：HDFS文件系统元数据的一个永久检查点，其中包含HDFS文件系统的所有目录和文件inode的序列化信息
- Edits文件：存放HEFS文件系统的所有更新操作的路径，文件系统客户端执行的所有写操作首先会被记录到Edits文件中
- seen_txid文件保存的是一个数字，就是最后一个edits_的数字
- 每次nameNode启动的时候都会将Fsimage文件读入内存，加载Edits里面的更新操作，保证内存中的元数据信息是最新的，同步的，可以看成NameNode启动的时候将Fsimage和Edits文件进行了合并
