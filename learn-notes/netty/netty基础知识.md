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
