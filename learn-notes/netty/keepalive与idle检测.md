### keepalive

#### 设计原则

- 问题出现概率很小,没有必要频繁探测

- 判断要谨慎

#### TCP层的keepalive核心参数

- net.ipv4.tcp_keepalive_time= 7200

- net.ipv4.tcp_keepalive_intvl=75

- net.ipv4.tcp_keepalive_probes=9

**当启用keepalive时，tcp在没有数据是，通过7200秒后发送keepalive消息，当探测到没有确认时，按75秒的重试频率重发。一直发9个探测包都没有确认，就认定连接失效。**

##### tcp层的keepalive默认是关闭的，且经过路由等中转设备keepalive包可能会被丢弃

##### tcp层的keepalive时间太长

#### 应用层keepalive


### idle检测

- 配合发送keepalive:减少keepalive消息，在没有其他数据传输一定时间判定为idle时，发送keepalive

- 直接关闭连接： 拒绝服务攻击，快速释放损坏的连接，让系统保持最好的状态

### 开启keepalive

- server端开启TCP keepalive

````
//两种设置keepalive的风格
bootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);
bootstrap.childOption(NioChannelOption.of(StandardSocket.SO_KEEPALIVE),true);

````

- idle

````
ch.pipleline().addLast("idleCheckHandler",new idleStateHandler(0,20,0,TimeUnit.SECONDS));

````
