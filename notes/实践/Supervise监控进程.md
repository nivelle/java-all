
- Supervisor 是用Python开发的一个client/server服务，是Linux/Unix系统下的一个进程管理工具，不支持Windows系统。

- 它可以很方便的`监听、启动、停止、重启一个或多个进程`。用Supervisor管理的进程，当一个进程意外被杀死，supervisort监听到进程死后，会自动将它重新拉起，很方便的做到进程自动恢复的功能，不再需要自己写shell脚本来控制。

因为Supervisor是Python开发的，安装前先检查一下系统否安装了Python2.4以上版本。下面以CentOS7，Python2.7版本环境下，介绍Supervisor的安装与配置步聚：

### 安装Python包管理工具（easy_install）

easy_install是setuptools包里带的一个命令，使用easy_install实际上是在调用setuptools来完成安装模块的工作,所以安装setuptools即可。

wget --no-check-certificate https://bootstrap.pypa.io/ez_setup.py -O - | sudo python

### 安装supervisor

easy_install supervisor

supervisor安装完成后会生成三个执行程序：supervisortd、supervisorctl、echo_supervisord_conf，分别是supervisor的守护进程服务（用于接收进程管理命令）、客户端（用于和守护进程通信，发送管理进程的指令）、生成初始配置文件程序。

### 配置
运行supervisord服务的时候，需要指定supervisor配置文件，如果没有显示指定，默认在以下目录查找：
````shell
$CWD/supervisord.conf
$CWD/etc/supervisord.conf
/etc/supervisord.conf
/etc/supervisor/supervisord.conf (since Supervisor 3.3.0)
../etc/supervisord.conf (Relative to the executable)
../supervisord.conf (Relative to the executable)
````
- $CWD表示运行supervisord程序的目录。
- 可以通过运行echo_supervisord_conf程序生成supervisor的初始化配置文件，如下所示：

````shell
mkdir /etc/supervisor
echo_supervisord_conf > /etc/supervisor/supervisord.conf
````
### 配置文件参数说明
supervisor的配置参数较多，下面介绍一下常用的参数配置，详细的配置及说明，请参考官方文档介绍。注：分号（;）开头的配置表示注释

````shell
[root@izuf63s64m3cxyi42c8qfsz sancell-shop-server-php]#
more  /etc/supervisor/supervisord.conf

[unix_http_server]
file=/var/run/supervisor/supervisor.sock   ; (the path to the socket file)

[inet_http_server]         ; inet (TCP) server disabled by default
port=0.0.0.0:9001        ; (ip_address:port specifier, *:port for all iface)
username=sancell            ; (default is no username (open server))
password=sancell123!               ; (default is no password (open server))

[supervisord]
logfile=/var/log/supervisord/supervisord.log  ; (main log file;default $CWD/supervisord.log)
logfile_maxbytes=50MB       ; (max main logfile bytes b4 rotation;default 50MB)
logfile_backups=10          ; (num of main logfile rotation backups;default 10)
loglevel=info               ; (log level;default info; others: debug,warn,trace)
pidfile=/var/run/supervisord.pid ; (supervisord pidfile;default supervisord.pid)
nodaemon=false              ; (start in foreground if true;default false)
minfds=1024                 ; (min. avail startup file descriptors;default 1024)
minprocs=200                ; (min. avail process descriptors;default 200)

[rpcinterface:supervisor]
supervisor.rpcinterface_factory = supervisor.rpcinterface:make_main_rpcinterface

[supervisorctl]
serverurl=unix:///var/run/supervisor/supervisor.sock ; use a unix:// URL  for a unix socket

[include]
files = /etc/supervisor/dirini/*.ini
````
### 配置管理进程

进程管理配置参数，不建议全都写在supervisord.conf文件中，应该每个进程写一个配置文件放在include指定的目录下包含进，/etc/supervisor/dirini/*.ini

````shell
[root@izuf63s64m3cxyi42c8qfsz sancell-shop-server-php]# more  /etc/supervisor/dirini/queue-groupon-success.ini
[program:queue-groupon-success]
command=php yii queue-groupon-success/listen
;程序启动命令
directory=/data/product/sancell-shop-server-php/
; 执行命令的路径
autostart=true       
; 在supervisord启动的时候也自动启动
startsecs=10         
; 启动10秒后没有异常退出，就表示进程正常启动了，默认为1秒
autorestart=true     
; 程序退出后自动重启,可选值：[unexpected,true,false]，默认为unexpected，表示进程意外杀死后才重启
startretries=60       
; 启动失败自动重试次数，默认是3
user=root          
; 用哪个用户启动进程，默认是root
priority=999         
; 进程启动优先级，默认999，值小的优先启动
redirect_stderr=true
; 把stderr重定向到stdout，默认false
stdout_logfile_maxbytes=50MB  
; stdout 日志文件大小，默认50MB
stdout_logfile_backups = 10   
; stdout 日志文件备份数，默认是10
;stdout 日志文件，需要注意当指定目录不存在时无法正常启动，所以需要手动创建目录（supervisord 会自动创建日志文件）
stdout_logfile=/var/log/supervisord/queue-groupon-success-listen.log
stderr_logfile=/var/log/supervisord/queue-groupon-success-listen.log
loglevel=info  
;日志的级别
stopasgroup=false     
;默认为false,进程被杀死时，是否向这个进程组发送stop信号，包括子进程
killasgroup=false     
;默认为false，向进程组发送kill信号，包括子进程
````
### 启动Supervisor服务
supervisord -c /etc/supervisor/supervisord.conf

### 控制进程
6.1 交互终端
supervisord启动成功后，可以通过supervisorctl客户端控制进程，启动、停止、重启。运行supervisorctl命令，不加参数，会进入supervisor客户端的交互终端，并会列出当前所管理的所有进程。

6.2 bash终端

supervisorctl status
supervisorctl stop tomcat
supervisorctl start tomcat
supervisorctl restart tomcat
supervisorctl reread
supervisorctl update
