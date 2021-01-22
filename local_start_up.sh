### Redis


#### 启动server:

```

cd /usr/local/redis-5.0.4

redis-server

```
#### 远程启动

```
$ redis-cli -h host -p port -a password

```


- 查看状态
```
是否启动: ps aux | grep redis

```

- 通过redis-cli命令可以启动redis客户端


```
cd /usr/local/redis-5.0.4

nivellefu$ redis-cli;

```

- 客户端退出


```
redis-cli shutdown

```

- 服务端退出

```
ps -u jim(替换成你的用户名) -o pid,rss,command | grep redis-server

```
### mysql

- 启动


```
  mysql.server start或者service mysqld start或者/etc/init.d/mysqld start
  
  mysql -h ip -userName -p pasword

```
- 关闭

```
service mysqld stop

```

#### 链接本地数据库

```
mysql -uroot -p

然后输入密码

```


### rabbitMQ


```
cd /usr/local/Cellar/rabbitmq/3.7.15/sbin
```

- 前台启动 

```
sudo ./rabbitmq-server    或


sudo su
/usr/local/Cellar/rabbitmq/3.7.8/sbin/rabbitmq-server -detacted

 

```

- 后台启动

```

 sudo ./rabbitmq-server -detached

```

- 查看状态


```
// 查看状态
  rabbitmqctl status 
```


- 关闭


```
后台关闭 sudo ./rabbitmqctl stop

```



### zk


```
# 启动3个zookeeper服务
zkServer start /usr/local/etc/zookeeper/zoo1.cfg
zkServer start /usr/local/etc/zookeeper/zoo2.cfg
zkServer start /usr/local/etc/zookeeper/zoo3.cfg

# 查看每个zookeeper对应的角色
zkServer status /usr/local/etc/zookeeper/zoo1.cfg
zkServer status /usr/local/etc/zookeeper/zoo2.cfg
zkServer status /usr/local/etc/zookeeper/zoo3.cfg

# 停止zookeeper服务
zkServer stop /usr/local/etc/zookeeper/zoo1.cfg
zkServer stop /usr/local/etc/zookeeper/zoo2.cfg
zkServer stop /usr/local/etc/zookeeper/zoo3.cfg


```

### nginx 

- 启动:

```
cd /usr/local

sudo nginx

```

- 配置文件


```
/usr/local/etc/nginx/nginx.conf

```

### kafka


- 启动

```
cd /usr/local/Cellar/kafka/2.2.1/bin

kafka-server-start /usr/local/etc/kafka/server.properties

```

- 停止

````
kafka-server-stop /usr/local/etc/kafka/server.properties


````
#### hbase

````
cd /usr/local/Cellar/hbase/1.3.5/libexec/bin
````
- 启动:  start-hbase.sh

- 停止:  stop-hbase.sh


### 启动 JAR服务


```
java -jar whk-wechat-0.0.1-SNAPSHOT.jar &
```

- 查看进程


```
ps - ef|grep whk-wechat-0.0.1-SNAPSHOT.jar

```

### mongo server启动

````
mongod --dbpath '/Users/nivellefu/data/db' #更改指定运行路径
````
#### mongo客户端启动
````
mongo
或者
./mongod --dbpath '/Users/nivellefu/data/db' --logpath=/Users/nivellefu/logs --logappend --port=27017 --fork

````
#### mongo server关闭在客户端窗口
````
use admin;

db.shutdownServer();

或者

cd /usr/local/mongodb/bin

mongod --shutdown --dbpath '/Users/nivellefu/data/db'
````

#### elasticSearch

- 运行

```
brew services start elasticsearch

默认端口  http://localhost:9200

```

- 查看集群状态

```

localhost:9200/_cat/health?v

```

#### Kibana

```
brew services start kibana

默认端口：http://localhost:5601

```