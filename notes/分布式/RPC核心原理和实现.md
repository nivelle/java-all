### 一、什么是RPC

RPC 的全称是 Remote Procedure Call 是一种进程间通信方式。它允许程序调用另一个地址空间（通常是共享网络的另一台机器上）的过程或函数，而不用程序员显式编码这个远程调用的细节。即无论是调用本地接口/服务的还是远程的接口/服务，本质上编写的调用代码基本相同。

比如两台服务器A，B，一个应用部署在A服务器上，想要调用B服务器上应用提供的函数或者方法，由于不在一个内存空间，不能直接调用，这时候需要通过就可以应用RPC框架的实现来解决。

- RPC 会隐藏底层的通讯细节（不需要直接处理Socket通讯或Http通讯）

- RPC 是一个请求响应模型。客户端发起请求，服务器返回响应（类似于Http的工作方式）

- RPC 在使用形式上像调用本地函数（或方法）一样去调用远程的函数（或方法）。

### 二、常见RPC框架

几种比较典型的RPC的实现和调用框架。

-  RMI实现，利用java.rmi包实现，基于Java远程方法协议(Java Remote Method Protocol)和java的原生序列化。

- Hessian,是一个轻量级的remoting onhttp工具，使用简单的方法提供了RMI的功能。 基于HTTP协议，采用二进制编解码。

- THRIFT是一种可伸缩的跨语言服务的软件框架。thrift允许你定义一个描述文件，描述数据类型和服务接口。依据该文件，编译器方便地生成RPC客户端和服务器通信代码。

### 二、RPC框架实现原理

#### 在RPC框架中主要有三个角色：Provider、Consumer和Registry。如下图所示：

![](http://p1.pstatp.com/large/pgc-image/1540190693339a1cd496dff)

#### 节点角色说明：

* Server: 暴露服务的服务提供方。

* Client: 调用远程服务的服务消费方。

* Registry: 服务注册与发现的注册中心。

### 三、RPC调用流程

RPC基本流程图,RPC框架面试总结-RPC原理及实现,一次完整的RPC调用流程（同步调用，异步另说）如下：

![](http://p99.pstatp.com/large/pgc-image/1540190765251e9d1633416)

- 1）服务消费方（client）调用以本地调用方式调用服务；

- 2）client stub接收到调用后负责将方法、参数等组装成能够进行网络传输的消息体；

- 3）client stub找到服务地址，并将消息发送到服务端；

- 4）server stub收到消息后进行解码；

- 5）server stub根据解码结果调用本地的服务；

- 6）本地服务执行并将结果返回给server stub；

- 7）server stub将返回结果打包成消息并发送至消费方；

- 8）client stub接收到消息，并进行解码；

- 9）服务消费方得到最终结果。

RPC框架的目标就是要2~8这些步骤都封装起来，让用户对这些细节透明。

### 四、服务注册&发现

- RPC框架面试总结-RPC原理及实现 ,服务提供者启动后主动向注册中心注册机器ip、port以及提供的服务列表；

- 服务消费者启动时向注册中心获取服务提供方地址列表，可实现`软负载均衡`和`Failover[故障转移]`；

### 五、使用到的技术

1、动态代理

生成 client stub和server stub需要用到 Java 动态代理技术 ，我们可以使用JDK原生的动态代理机制，可以使用一些开源字节码工具框架 如：CgLib、Javassist等。

2、序列化

为了能在网络上传输和接收 Java对象，我们需要对它进行 序列化和反序列化操作。

* 序列化：将Java对象转换成byte[]的过程，也就是编码的过程；

* 反序列化：将byte[]转换成Java对象的过程；

可以使用Java原生的序列化机制，但是效率非常低，推荐使用一些开源的、成熟的序列化技术，例如：protobuf、Thrift、hessian、Kryo、Msgpack

关于序列化工具性能比较可以参考：jvm-serializers

3、NIO

当前很多RPC框架都直接基于netty这一IO通信框架，比如阿里巴巴的HSF、dubbo，Hadoop Avro，推荐使用Netty 作为底层通信框架。

4、服务注册中心

可选技术：

* Redis

* Zookeeper

* Consul

* Etcd