### reactor

### IO模式对应的开发模式
BIO | NIO |AIO
---|---|---
Thread-Per-Connection | Reactor | Proactor


#### Reactor开发模式的核心流程

- 注册感兴趣的事件

- 扫描是否有感兴趣的事件发生

- 事件发生后做出相应的处理

client/Server | SocketChannel/ServerSocketChannel |OP_ACCETP | OP_CONNECT | OP_WRITE | OP_READ
---|---|---|---|---|---
client | SocketChannel| |Y|Y|Y
server | ServerSocketChannel|Y|||
client | SocketChannel| | |Y|Y

#### 单线程模式
[![rra2SP.png](https://s3.ax1x.com/2020/12/22/rra2SP.png)](https://imgchr.com/i/rra2SP)
````
EventLoopGroup boss = new NioEventLoopGroup(1);
ServerBootstrap b = new ServerBootstrap();
b.group(boss);
````
#### 多线程模式

[![rr0dyV.png](https://s3.ax1x.com/2020/12/22/rr0dyV.png)](https://imgchr.com/i/rr0dyV)
````
EventLoopGroup boss = new NioEventLoopGroup();//根据CPU核数计算一个最优的线程数

ServerBootstrap b = new ServerBootstrap();
b.group(boss);
````

#### 主从Reactor模式
[![rr0x0S.png](https://s3.ax1x.com/2020/12/22/rr0x0S.png)](https://imgchr.com/i/rr0x0S)
````
EventLoopGroup boss = new NioEventLoopGroup();
EventLoopGroup worler = new NioEventLoopGroup();

ServerBootstrap b = new ServerBootstrap();
b.group(boss,worker);
````
