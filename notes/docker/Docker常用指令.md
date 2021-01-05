
## docker hub

```
https://hub.docker.com/

账户: nivelle
密码:nivell2

```
---

## Docker版本

```

$ docker --version

```
---

## 避免输出sudo
   
```
   
这里把当前用户加入到docker组就可以直接使用命令，而不用每次都加sudo
   
$ sudo groupadd docker
   
改完后需要重新登陆用户
   
$ sudo gpasswd -a ${USER} docker
   
```
---
## 当前Docker宿主机的信息

```

$ docker info

```

---

## 镜像

### 查看本地镜像


```

$ docker images

REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
ubuntu              15.10               9b9cb95443b5        3 years ago         137MB

```
- REPOSITORY : 表示镜像的仓库源

- TAG:镜像的标签(同一仓库源可以有多个 TAG，代表这个仓库源的不同个版本)

- IMAGE ID：镜像ID

- CREATED：镜像创建时间
  
- SIZE：镜像大小

定义仓库源: REPOSITORY:TAG 

### 删除镜像

```
docker rmi <image id>

```

### 删除未打标签的镜像


```

docker rmi $(docker images | grep "^<none>" | awk "{print $3}")

```


### 搜索 docker Image

```
$ docker search tutorial

搜索网址是：index.docker.io [国内无法访问]

其他网址是：https://hub.docker.com/

```

### 获取一个镜像

```
docker pull 镜像源:tag

```

### 构建镜像

#### 通过 Dockerfile 文件构建镜像

```
docker build -t imageName -f dockerfile 

```

- t: 指定要创建的目标镜像名

- . //Dockerfile文件所在的目录，可以指定Dockerfile绝对路径

#### 将container保存为一个image

```

docker commit [container] [image_name]

eg: docker commit -m="has update" -a="runoob" e218edb10161 runoob/ubuntu:v2

```

- -m: 提交的描述信息

- -a: 指定镜像作者

- e218edb10161：容器 ID
  
- runoob/ubuntu:v2: 指定要创建的目标镜像名
  


### 更新镜像

更新镜像前，使用镜像创建容器。在容器中使用 **apt-get update**命令进行更新，在完成操作之后，输入 exit 命令来退出这个容器。
                                               
```
docker commit -m="has update" -a="runoob" e218edb10161 runoob/ubuntu:v2
```
- m "描述信息"

- a "镜像作者"

- e218edb10161 “镜像ID”

- runoob/ubuntu:v2 "镜像名"

### 设置镜像标签

```
- docker tag imageId[镜像id] runoob/centos[镜像源名]:dev[tag名称]

```

### 从指定image里生成一个container并在其中运行一个命令

```
[root@jessy ~]# docker run ubuntu:15.10 /bin/echo "hello nivelle"

hello nivelle


```

- docker: Docker 的二进制执行文件。
  
- run: 与前面的 docker 组合来运行一个容器。
  
- ubuntu:15.10 指定要运行的镜像，Docker 首先从本地主机上查找镜像是否存在，如果不存在，Docker 就会从镜像仓库 Docker Hub 下载公共镜像

- /bin/echo "Hello world": 在启动的容器里执行的命令
  



### 将image上传到仓库

```
$ docker push [image_name]
```

### 删除images


```

$ docker rmi [image id]

```

--- 

## 容器

### 查看正在运行的容器

```

$ docker ps

```

- CONTAINER ID（container id ） ：顾名思义 ,容器ID的意思，可以通过这id找到唯一的对应容器

- IMAGE （image）：该容器所使用的镜像

- COMMAND （command）：启动容器时运行的命令

- CREATED （created）：容器的创建时间，显示格式为”**时间之前创建“

- STATUS （status）：容器现在的状态，状态有7种：created（已创建）|restarting（重启中）|running（运行中）|removing（迁移中）|paused（暂停）|exited（停止）|dead

- PORTS （ports）:容器的端口信息和使用的连接类型（tcp\udp）

- NAMES （names）:镜像自动为容器创建的名字，也唯一代表一个容器


### 删除容器

```

docker rm <docker id>

```


### 在container里运行交互式命令，比如shell

```

$ docker run -i -t [image] [cmd]

eg:
 
- [root@jessy ~]# docker run -i -t ubuntu:15.10 /bin/bash

- root@93c84c575fd7:/# 

```
- -t: 在新容器内指定一个伪终端或终端。
  
- -i: 允许你对容器内的标准输入 (STDIN) 进行交互。
  
- root@93c84c575fd7:/#  此时我们已进入一个 ubuntu15.10 系统的容器
  
- exit : 退出容器,返回当前主机

- d: 启动模式（后台启动)

- -p :内部端口:外部映射端口

- -P :是容器内部端口随机映射到主机的高端口。

- -- name:对容器进行命名



### 在container里运行后台任务

```
$ docker run -d ubuntu:15.10 /bin/sh -c "while true; do echo hello world; sleep 1; done"

4c2dfe051a9368dc58c16a33577508ebee9d9065071cbf9eb8c18bcfb107d99c

```

### 查看某个container的运行日志,查看容器内的标准输出

```
docker logs 4c2dfe051a9368dc58c16a33577508ebee9d9065071cbf9eb8c18bcfb107d99c

```

### 查看所有的容器命令m,列出所有container

```

$ docker ps -a

```


### 运行某个container

```

$ docker start [container]

```


### 查看容器的端口映射情况

```
docker top zealous_darwin

```
### 重启容器

```
$ docker restart <容器 ID>

```

### 后台启动运行某个容器

```
$ docker run -itd --name ubuntu-test ubuntu /bin/bash

```

- -i: 交互式操作

- -t: 终端。

- ubuntu: ubuntu 镜像。

- /bin/bash：放在镜像名后的是命令，这里我们希望有个交互式 Shell，因此用的是 /bin/bash。


### 在使用 -d 参数时，容器启动后会进入后台。此时想要进入容器

```

注意：切换到后台任务以后无法用Ctrl-C退出

$ docker attach [container]

eg: ocker exec -it 93c84c575fd7 /bin/bash

```


### 中止后台任务container

```

$ docker stop [container_id]

```

### 列出最近一个运行过的container

```

不加-l则只列出正在运行的container（比如后台任务）

$ docker ps -l
```


### 查看container详情

```

$ docker inspect [container]
```

### 删除某个container

```

其中container_id不需要输入完整，只要能保证唯一即可。

运行中的Docker容器是无法删除的，必须先通过docker stop或者docker kill命令停止。

$ docker rm [container]

$ docker rm `docker ps -a -q` 删除所有容器，-q表示只返回容器的ID
```
### 查看容器内部的进程信息

```
$ docker top [container]
```

### 在容器中运行后台任务，只对正在运行的容器有效。


```

$ docker exec -d [container] [cmd]

$ docker exec -d edison touch /home/haha

```

### 在容器中运行交付式任务，只对正在运行的容器有效。

```

$ docker exec -t -i edison /bin/bash

```


### 为容器指定名称，容器的名称是唯一


```

$ docker run --name edison -i -t ubuntu /bin/bash
```

### 有三种方式可以唯一指代容器


```
短UUID: 716d3c16dc65（12位）

长UUID：716d3c16dc654230ada14f555faadd036474231dfca0ca44b597574a5c618565（64位）

名称: edison

```

## 新建网络

#### docker 有一个连接系统允许将多个容器连接在一起，共享连接信息。
  
#### docker 连接会创建一个父子关系，其中父容器可以看到子容器的信息。
 
```
docker network create -d bridge test-net(网络名)

```
- -d：参数指定 Docker 网络类型，有 bridge、overlay。
  
```
docker run -itd --name test1 --network test-net ubuntu /bin/bash

docker run -itd --name test2 --network test-net ubuntu /bin/bash

运行两个容器,test1 d test2 并通过test-net来进行连接

```

```
docker run -d -p 127.0.0.1:5001:5000 training/webapp python app.py

```
可以通过访问 127.0.0.1:5001 来访问容器的 5000 端口。默认都是绑定 tcp 端口，如果要绑定 UDP 端口，可以在端口后面加上 /udp

-P :是容器内部端口随机映射到主机的高端口。

-p : 是容器内部端口绑定到指定的主机端口。

---
##  