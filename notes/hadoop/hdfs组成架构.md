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
### HDFS读写流程