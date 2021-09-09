## reactor

### IO模式对应的开发模式

BIO | NIO |AIO
---|---|---
Thread-Per-Connection | Reactor | Proactor

### Reactor开发模式的核心流程

- 注册感兴趣的事件

- 扫描是否有感兴趣的事件发生

- 事件发生后做出相应的处理

client/Server | SocketChannel/ServerSocketChannel |OP_ACCETP | OP_CONNECT | OP_WRITE | OP_READ
---|---|---|---|---|---
client | SocketChannel| |Y|Y|Y
server | ServerSocketChannel|Y|||
client | SocketChannel| | |Y|Y

### reactor 单线程模式

![netty 单线程模式.png](https://i.loli.net/2021/05/15/42PErxiKT3qOIso.png)

````java
EventLoopGroup boss = new NioEventLoopGroup(1);
ServerBootstrap b = new ServerBootstrap();
b.group(boss);
````

### reactor 多线程模式

![reactor多线程模式.png](https://i.loli.net/2021/05/15/zV87DbXl6BA2Jax.png)

````java
EventLoopGroup boss = new NioEventLoopGroup();//根据CPU核数计算一个最优的线程数

ServerBootstrap b = new ServerBootstrap();
b.group(boss);
````

### 主从Reactor模式

![netty reactor模式.png](https://i.loli.net/2021/05/15/q4luJ6zVEg2sWmo.png)
````java
EventLoopGroup boss = new NioEventLoopGroup();
EventLoopGroup worler = new NioEventLoopGroup();

ServerBootstrap b = new ServerBootstrap();
b.group(boss,worker);
````
