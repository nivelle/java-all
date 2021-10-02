### 什么是supervise
supervise 是Daemontools里的一个核心工具，Daemontools是一个包含了很多管理Unix服务的工具的软件包。而其中最核心的工具就是supervise，它的功能是监控一个指定的服务，当该服务进程消亡，则重新启动该进程。
最简单的，我们利用supervise，可以监控一个进程，比如我们在服务器上部署的nginx服务，nginx服务如果挂掉了我们如何知道，可以通过监控，如何重启服务，登录机器，重新start，如果不能实时看到监控，或者说想省略这个繁琐的过程，我们可以利用supervise，supervise可以实时监控nginx的进程，一旦检测到进程挂掉，会立即重新启动进程，无需人工干预。本文以监控一个jar包为例来具体说
### 相关环境
- （1）操作系统：CentOS 7.3
- （2）Daemontools：0.76

### 三、安装配置Daemontools

- （1）解压创建daemontools目录
````shell
tar -xvf daemontools-0.76.tar.gz
mv admin/daemontools-0.76/ /usr/local/
````
- （2）编辑conf-cc文件，不然安装时报错
````shell
vim /usr/local/daemontools-0.76/src/conf-cc

在最后添加“ -include /usr/include/errno.h”
````
- （3）安装daemontools
````shell
cd /usr/local/daemontools-0.76/
./package/install
````
- 安装完毕会在目录下创建command及compile目录

- 删除配置文件/etc/inittab中的最后一行“SV:123456:respawn:/command/svscanboot”，CentOS7以上不起作用，删除即可

- （4）将daemontools做成服务

````shell
vim /etc/systemd/system/daemontools.service

[Unit]
Description=daemontools Start supervise
After=getty.target

[Service]
Type=simple
User=root
Group=root
Restart=always
ExecStart=/command/svscanboot /dev/ttyS0
TimeoutSec=0

[Install]
WantedBy=multi-user.target

systemctl daemon-reload
systemctl start daemontools		# 启动daemontools
systemctl status daemontools		# 查看daemontools状态
systemctl enable daemontools 		# 设置daemontools开机自启

````
- 四、利用supervise监控一个java进程

比如我们在/root/JavaTag目录下有一个名字为“tagServer.jar”的jar包，正常启动这个jar包的命令如下
````shell
java -jar /root/JavaTag/tagServer.jar --server.port=8888
````

#### 现在我们想要利用supervise监控我们启动的这个java进程并在监控到java进程退出之后自动拉起，我们可以这么做：

- （1）创建服务目录

创建一个supervise的服务目录，这个目录里集中存放我们需要监控的进程的脚本

````shell
mkdir -p /root/service
````
针对这个jar包的服务我们再在supervise的服务目录里创建一个单独存放目录
````shell
mkdir -p /root/service/Javatag
````
- （2）编写服务脚本（名字必须为run，所以需要监控什么服务，就给什么服务创建单独的存放目录来区分）
````shell
vim /root/service/Javatag/run
````
脚本里需要添加的命令就是我们期望supervise在监控到进程挂掉之后需要做的操作，比如是启动jar包，启动nginx，启动mysql，达到进程挂掉通过supervise自动拉起的目的

````shell
#!/bin/sh
exec java -jar /root/JavaTag/tagServer.jar --server.port=8888
````
给脚本添加755权限

````shell
chmod 755 run
````
-b（3）创建软连接
````shell
ln -s /root/service/Javatag/ /service/Javatag
````
创建完毕之后，因为我们事先没有启动过这个jar包，但我们通过“ps -ef | grep java”发现，supervise已经将java进程拉起

我们手动去kill掉这个java进程，会发现supervise会马上将服务重新拉起来

- （4）服务的启动关闭

终止这个进程

``````shell
svc -d /service/Javatag
``````
启动这个进程

````shell
svc -u /service/Javatag
````
查看进程状态

```shell
svstat /service/Javatag
````

如需要彻底删除这个服务，停掉服务之后，删除掉软链接即可
````shell
svc -d /service/Javatag
rm -rf /service/Javatag
````