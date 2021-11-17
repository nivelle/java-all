- ### netty

#### 特点

- 支持常用应用层协议

- 解决传输问题：粘包、半包现象

- 支持流程整形

- 完善断连接、idle等异常处理等

#### 使用领域

- 数据库：cassandra

- 大数据长距离：spark、hadoop

- message Queue : rocketMQ

- 检索： es

- 框架：gRPC、dubbo

- 分布式协调：zookeeper

- 工具类： async-http-client

### IO模式

#### 同步阻塞BIO

特定场景：链接数目少，并发度低，BIO性能不输NIO

#### 同步非阻塞NIO

#### 异步非阻塞模式AIO

### 不支持AIO

- windows 实现成熟，但是很少用来做服务器
- linux 常用来做服务器，但是AIO实现不够成熟
- linux 下AIO相比较NIO的性能提升不够明显

### 模式切换

- 范型+反射+工厂实现IO模式切换

- NioEventLoopGroup 切换

- NioServerSocketChannel 切换

````
public class ReflectiveChannelFactory<T extends Channel> implements ChannelFactory<T> {

    private final Constructor<? extends T> constructor;

    public ReflectiveChannelFactory(Class<? extends T> clazz) {
        ObjectUtil.checkNotNull(clazz, "clazz");
        try {
           //获取无参构造器
            this.constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + StringUtil.simpleClassName(clazz) + " does not have a public non-arg constructor", e);
        }
    }

    @Override
    public T newChannel() {
        try {
            //范性方法获取指定类型的Channel
            return constructor.newInstance();
        } catch (Throwable t) {
            throw new ChannelException("Unable to create Channel from class " + constructor.getDeclaringClass(), t);
        }
    }
}

````

### netty高性能

#### 异步非阻塞模型

- 服务端线程模型

[![cXe66s.png](https://z3.ax1x.com/2021/04/23/cXe66s.png)](https://imgtu.com/i/cXe66s)

- 客户端线程模型

[![cXmCjA.png](https://z3.ax1x.com/2021/04/23/cXmCjA.png)](https://imgtu.com/i/cXmCjA)

#### 零拷贝

1. Netty接收和发送ByteBuffer采用DirectBuffer,使用堆外内存进行Socket读写，不需要进行字节缓冲区的二次拷贝。

2. 提供了组合Buffer对象，聚合多个ByteBuffer对象，用户可以像操作一个Buffer那样方便地对组合Buffer进行操作

3. Netty文件传输采用了transferTo()方法，它可以直接将文件缓冲区的数据发送到目标channel,避免了传统通过循环write()方式导致的内存拷贝问题

#### 内存池

netty设计了一套基于内存池的缓冲区重用机制。

#### reactor线程模型

#### netty锁使用

- 在意锁的对象和范围（减少粒度）

- 注意锁的对象本书大小（减少空间占用）

- 注意锁的速度

- 不同场景选择不同的并发类

- 衡量好锁的价值