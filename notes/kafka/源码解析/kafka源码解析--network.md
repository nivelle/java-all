## 请求
### request
### shutdownRequest

当Broker进程关闭时，请求处理器会发送ShutDownRequest到专属的请求处理线程。
该线程接收到此请求后，会触发一系列的Broker关闭逻辑

### request 请求属性

#### processor 

- processor是Processor 线程的序号，即这个请求是由哪个Processor线程接收处理的。 Broker端参数 **num.network.threads** 控制了Broker每个监听器上创建的Processor线程数
  
- 线程序号的作用：当Request 被后面的IO线程处理完毕后，还需要依靠Processor 将 response 发送给请求发送方。

- Processor 线程仅仅是网络接收线程，不会执行真正的Request 请求处理逻辑，那是I/O线程负责的事情

#### context

- 用来标示请求上下文信息的。客户端实现类 RequestContext

````

public class RequestContext implements AuthorizableRequestContext {
    public final RequestHeader header; // Request头部数据，主要是一些对用户不可见的元数据信息，如Request类型、Request API版本、clientId等
    public final String connectionId; // Request发送方的TCP连接串标识，由Kafka根据一定规则定义，主要用于表示TCP连接
    public final InetAddress clientAddress; // Request发送方IP地址
    public final KafkaPrincipal principal;  // Kafka用户认证类，用于认证授权
    public final ListenerName listenerName; // 监听器名称，可以是预定义的监听器（如PLAINTEXT），也可自行定义
    public final SecurityProtocol securityProtocol; // 安全协议类型，目前支持4种：PLAINTEXT、SSL、SASL_PLAINTEXT、SASL_SSL
    public final ClientInformation clientInformation; // 用户自定义的一些连接方信息
    // 从给定的ByteBuffer中提取出Request和对应的Size值
    public RequestAndSize parseRequest(ByteBuffer buffer) {
             ......
    }
    // 其他Getter方法
    ......
}

````

#### startTimeNanos

startTimeNanos 记录了 Request 对象被创建的时间，主要用于各种时间统计指标的计算。

#### memoryPoll

memoryPool 表示源码定义的一个非阻塞式的内存缓冲区，主要作用是避免 Request 对象无限使用内存。

#### buffer

保存Request对象内容的字节缓存区。 request发送方必须按照Kafka的RPC 协议规定的格式向该缓存区写入字节，否则抛出InvalidRequestException异常。
这个逻辑主要是由 RequestContext 的 parseRequest方法实现的。

````

public RequestAndSize parseRequest(ByteBuffer buffer) {
    if (isUnsupportedApiVersionsRequest()) {
        // 不支持的ApiVersions请求类型被视为是V0版本的请求，并且不做解析操作，直接返回
        ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest(new ApiVersionsRequestData(), (short) 0, header.apiVersion());
        return new RequestAndSize(apiVersionsRequest, 0);
    } else {
        // 从请求头部数据中获取ApiKeys信息
        ApiKeys apiKey = header.apiKey();
        try {
            // 从请求头部数据中获取版本信息
            short apiVersion = header.apiVersion();
            // 解析请求
            Struct struct = apiKey.parseRequest(apiVersion, buffer);
            AbstractRequest body = AbstractRequest.parseRequest(apiKey, apiVersion, struct);
            // 封装解析后的请求对象以及请求大小返回
            return new RequestAndSize(body, struct.sizeOf());
        } catch (Throwable ex) {
            // 解析过程中出现任何问题都视为无效请求，抛出异常
            throw new InvalidRequestException("Error getting request for apiKey: " + apiKey +
                    ", apiVersion: " + header.apiVersion() +
                    ", connectionId: " + connectionId +
                    ", listenerName: " + listenerName +
                    ", principal: " + principal, ex);
        }
    }
}


````

#### metrics

各种监控指标的一个管理类，里面构建了一个Map,封装了所有请求的JMX指标

### Response 响应

#### Response 子类作用

- 抽象基类：Response 定义 Response 的抽象基类。每个Response对象都包含了对应的Request对象。onComplete方法，用来实现Response被处理后需要执行的回调逻辑

- sendResponse: 保存返回结果的Response子类，大多数Request 处理完都要执行的回调逻辑，onCompletionCallback,即指定处理完成之后的回调逻辑。

- NoResponse: request处理完成后无需淡出执行额外的回调逻辑。

- CloseConnectionResponse: 用户出错后需要关闭TCP连接的场景，返回CloseConnectionResponse给Request发送方，显示通知它关闭连接

- StartThrottlingResponse:用于通知Broker的Socket Server组件，某个TCP连接开始限流（throttling）

- endThrottlingResponse:用于通知Broker的Socket Server组件，某个TCP连接通信通道的限流已结束

#### response 属性

- request : 每个Response 对象都要保存它对应的Request对象


### RequestChannel: 传输 Request/Response 的通道

- requestQueue : 每个 RequestChannel 对象实例创建时，会定义一个队列来保存 Broker 接收到的各类请求，这个队列被称为请求队列或 Request 队列。Kafka 使用 Java 提供的阻塞队列 ArrayBlockingQueue 实现这个请求队列，并利用它天然提供的线程安全性来保证多个线程能够并发安全高效地访问请求队列

- queueSize: 当 Broker 启动时，SocketServer 组件会创建 RequestChannel 对象，并把 Broker 端参数 **queued.max.requests**赋值给 queueSize。因此，在默认情况下，每个 RequestChannel 上的队列长度是 500

- processors: processors封装的是 RequestChannel 下辖的 Processor 线程池。每个 Processor 线程负责具体的请求处理逻辑


#### RequestChannel 的作用：Processor 管理

- 创建Processor线程池，并使用java 的 concurrentHashMap 保存， key 是 processor 序号， value 是 Processor 实例。

- 当前kafka broker 端的所有网络线程都是 requestChannel中维护的。每当 Broker 启动时，它都会调用 addProcessor 方法，向 RequestChannel 对象添加 **num.network.threads** 个 Processor 线程； num.network.threads 是可以动态修改的。

#### RequestChannel 的作用： 处理 request 和 response

````

def sendRequest(request: RequestChannel.Request): Unit = {
    requestQueue.put(request)
}
def receiveRequest(timeout: Long): RequestChannel.BaseRequest =
    requestQueue.poll(timeout, TimeUnit.MILLISECONDS)
def receiveRequest(): RequestChannel.BaseRequest =
    requestQueue.take()

````

- 收发 request : 发送 request指的是 将 request 放到 request 队列里面；接收 request 指的是从 request队列里面拉取 request

[![sMjxtx.jpg](https://s3.ax1x.com/2021/01/09/sMjxtx.jpg)](https://imgchr.com/i/sMjxtx)

- 发送 response: 将 response 发送到 Response 队列

````

def sendResponse(response: RequestChannel.Response): Unit = {
    if (isTraceEnabled) {  // 构造Trace日志输出字符串
      val requestHeader = response.request.header
      val message = response match {
        case sendResponse: SendResponse =>
          s"Sending ${requestHeader.apiKey} response to client ${requestHeader.clientId} of ${sendResponse.responseSend.size} bytes."
        case _: NoOpResponse =>
          s"Not sending ${requestHeader.apiKey} response to client ${requestHeader.clientId} as it's not required."
        case _: CloseConnectionResponse =>
          s"Closing connection for client ${requestHeader.clientId} due to error during ${requestHeader.apiKey}."
        case _: StartThrottlingResponse =>
          s"Notifying channel throttling has started for client ${requestHeader.clientId} for ${requestHeader.apiKey}"
        case _: EndThrottlingResponse =>
          s"Notifying channel throttling has ended for client ${requestHeader.clientId} for ${requestHeader.apiKey}"
      }
      trace(message)
    }
    // 找出response对应的Processor线程，即request当初是由哪个Processor线程处理的
    val processor = processors.get(response.processor)
    // 将response对象放置到对应Processor线程的Response队列中
    if (processor != null) {
      processor.enqueueResponse(response)
    }
}

````

#### RequestChannel 的作用：监控指标

````

object RequestMetrics {
  val consumerFetchMetricName = ApiKeys.FETCH.name + "Consumer"
  val followFetchMetricName = ApiKeys.FETCH.name + "Follower"
  val RequestsPerSec = "RequestsPerSec" //每秒处理的 request数目，表示broker的繁忙状态
  val RequestQueueTimeMs = "RequestQueueTimeMs" // request 在队列里面的停留时间，单位毫秒。如果太长需要增加线程。
  val LocalTimeMs = "LocalTimeMs" //计算 Request 实际被处理的时间，单位是毫秒。一旦定位到这个监控项的值很大，你就需要进一步研究 Request 被处理的逻辑了，具体分析到底是哪一步消耗了过多的时间。
  val RemoteTimeMs = "RemoteTimeMs"//Kafka 的读写请求（PRODUCE 请求和 FETCH 请求）逻辑涉及等待其他 Broker 操作完成的时间。
  val ThrottleTimeMs = "ThrottleTimeMs"
  val ResponseQueueTimeMs = "ResponseQueueTimeMs"
  val ResponseSendTimeMs = "ResponseSendTimeMs"
  val TotalTimeMs = "TotalTimeMs"//计算 Request 被处理的完整流程时间
  val RequestBytes = "RequestBytes"
  val MessageConversionsTimeMs = "MessageConversionsTimeMs"
  val TemporaryMemoryBytes = "TemporaryMemoryBytes"
  val ErrorsPerSec = "ErrorsPerSec"
}

````

### kafka 网络架构

[![s1cFOS.jpg](https://s3.ax1x.com/2021/01/10/s1cFOS.jpg)](https://imgchr.com/i/s1cFOS)

### SocketServer 组件

实现了Reactor模式，用于处理多个Clients 的并发请求，并负责将处理结果封装进Response中，返还给Clients

#### AbstractServerThread 类： 是Acceptor线程和Processor线程的抽象即类，定义了他们的共有方法，如 shutDown(关闭线程)

#### Acceptor线程类

#### 接收和创建外部TCP连接的线程。 每个SocketServer实例只会创建一个Acceptor线程。它的作用就是创建连接，并将接收到的Request传递给下游的Processor线程处理。

#### Acceptor 初始化方法定义

````

private[kafka] class Acceptor(val endPoint: EndPoint,
                              val sendBufferSize: Int,
                              val recvBufferSize: Int,
                              brokerId: Int,
                              connectionQuotas: ConnectionQuotas,
                              metricPrefix: String) extends AbstractServerThread(connectionQuotas) with KafkaMetricsGroup {
  // 创建底层的NIO Selector对象
  // Selector对象负责执行底层实际I/O操作，如监听连接创建请求、读写请求等
  private val nioSelector = NSelector.open() 
  // Broker端创建对应的ServerSocketChannel实例
  // 后续把该Channel向上一步的Selector对象注册
  val serverChannel = openServerSocket(endPoint.host, endPoint.port)
  // 创建Processor线程池，实际上是Processor线程数组
  private val processors = new ArrayBuffer[Processor]()
  private val processorsStarted = new AtomicBoolean

  private val blockedPercentMeter = newMeter(s"${metricPrefix}AcceptorBlockedPercent",
    "blocked time", TimeUnit.NANOSECONDS, Map(ListenerMetricTag -> endPoint.listenerName.value))
  ......
}

````


- endPoint: 定义kafka连接信息，包括主机名和端口信息，用于创建Server Socekt

- sendBufferSize: 设置SocketOptions的SO_SNDBUF,用于出站网络I/O 底层缓冲区大小。默认值是**socket.send.buffer.bytes** 也就是100KB

- recvBufferSize: 设置SocketOption的SO_RCVBUF ,用于入站网络I/O底层缓冲区大小，默认是**socket.receive.buffer.bytes** 也是100KB

#### 核心允许方法

````

def run(): Unit = {
  //注册OP_ACCEPT事件
  serverChannel.register(nioSelector, SelectionKey.OP_ACCEPT)
  // 等待Acceptor线程启动完成
  startupComplete()
  try {
    // 当前使用的Processor序号，从0开始，最大值是num.network.threads - 1
    var currentProcessorIndex = 0
    while (isRunning) {
      try {
        // 每500毫秒获取一次就绪I/O事件
        val ready = nioSelector.select(500)
        if (ready > 0) {
          // 如果有I/O事件准备就绪
          val keys = nioSelector.selectedKeys()
          val iter = keys.iterator()
          while (iter.hasNext && isRunning) {
            try {
              val key = iter.next
              iter.remove()
              // accept 网络连接事件监听器，一旦接收到网络连接，Accepto就会指定一个Processor线程，交给它去创建真正的网络连接。
              if (key.isAcceptable) {
                accept(key).foreach { socketChannel =>
                  var retriesLeft = synchronized(processors.length)
                  var processor: Processor = null
                  do {
                    retriesLeft -= 1
                    // 指定由哪个Processor线程进行处理
                    processor = synchronized {
                      currentProcessorIndex = currentProcessorIndex % processors.length
                      //扔给processors线程池处理创建好的Socket连接
                      processors(currentProcessorIndex)
                    }
                    // 更新Processor线程序号
                    currentProcessorIndex += 1
                  } while (!assignNewConnection(socketChannel, processor, retriesLeft == 0)) // Processor是否接受了该连接
                }
              } else
                throw new IllegalStateException("Unrecognized key state for acceptor thread.")
            } catch {
              case e: Throwable => error("Error while accepting connection", e)
            }
          }
        }
      }
      catch {
        case e: ControlThrowable => throw e
        case e: Throwable => error("Error occurred", e)
      }
    }
  } finally { // 执行各种资源关闭逻辑
    debug("Closing server socket and selector.")
    CoreUtils.swallow(serverChannel.close(), this, Level.ERROR)
    CoreUtils.swallow(nioSelector.close(), this, Level.ERROR)
    shutdownComplete()
  }
}

````

#### Processor线程类

##### 处理单个TCP连接上所有请求的线程。 每个SocketServer实例默认创建若干个（num.network.threads）Processor线程。负责将接收到的Request添加到RequestChannel的Request队列上，同时还负责将Response返回给Request发送方，真正创建连接以及分发请求的地方

##### Processor 从底层 Socket 通道不断读取已接收到的网络请求，然后转换成 Request 实例，并将其放入到 Request 队列
````

override def run(): Unit = {
    //等待Processor线程启动完成
    startupComplete() 
    try {
      while (isRunning) {
        try {
          configureNewConnections() // 创建新连接，调用Selector 的 register 注册SocketChannel，注册 SelectionKey.OP_READ 事件
          // register any new responses for writing
          processNewResponses() // 发送Response，并将Response放入到inflightResponses临时队列
          poll() // 执行NIO poll，获取对应SocketChannel上准备就绪的I/O操作; select.poll(pollTimeOut) 方法，执行准备就绪可读的事件，不管是接收request还是 发送 response
          processCompletedReceives() // 将接收到的Request放入Request队列
          processCompletedSends() // 为临时Response队列中的Response执行回调逻辑
          processDisconnected() // 处理因发送失败而导致的连接断开
          closeExcessConnections() // 关闭超过配额限制部分的连接
        } catch {
          case e: Throwable => processException("Processor got uncaught exception.", e)
        }
      }
    } finally { // 关闭底层资源
      debug(s"Closing selector - processor $id")
      CoreUtils.swallow(closeAll(), this, Level.ERROR)
      shutdownComplete()
    }
}

````

[![sGPyUf.md.jpg](https://s3.ax1x.com/2021/01/11/sGPyUf.md.jpg)](https://imgchr.com/i/sGPyUf)

#### processor创建了三个队列

- newConnections: 保存要创建的新连接信息。就是SocketChannel 对象，默认上限是20的队列，每当processor线程接收新的连接请求时，都会将对应的SocketChannel放入这个队列。后面在创建连接时（configureNewConnection时）就从该队列中取出SocketChannel,然后注册新的连接

- inflightResponse: 临时Response队列。当Processor线程将Response返还给Request发送方之后，还要将Response放入这个临时队列。有些 Response 回调逻辑要在 Response 被发送回发送方之后，才能执行，因此需要暂存在一个临时队列里面

- responseQueue: 每个Processor线程会维护自己的Response队列，response队列里保存着需要返还给发送方的所有Response对象。



### KafkaRequestHandlerPool组件

IO线程池，定义了若干的线程，用于执行真实的请求处理逻辑。


### socketServer 请求的优先级

- Data plane 和 Control plane ： 数据类请求和控制类请求；Controller 与 Broker 交互的请求类型有3种： LeaderAndIsrRequest、StopReplicaRequest、UpdateMetadataRequest 都属于控制类请求，PRODUCE 和 FETCH 属于数据类请求

- 监听器（listener）: 源码区分数据类请求和控制类请求不同处理方式的主要途径，就是通过监听器。也就是说，创建多组监听器分别来执行数据类和控制类请求的处理代码

````

case class EndPoint(host: String, port: Int, listenerName: ListenerName, securityProtocol: SecurityProtocol) {
  // 构造完整的监听器连接字符串
  // 格式为：监听器名称://主机名：端口
  // 比如：PLAINTEXT://kafka-host:9092
  def connectionString: String = {
    val hostport =
      if (host == null)
        ":"+port
      else
        Utils.formatAddress(host, port)
    listenerName.value + "://" + hostport
  }
  // clients工程下有一个Java版本的Endpoint类供clients端代码使用
  // 此方法是构造Java版本的Endpoint类实例
  def toJava: JEndpoint = {
    new JEndpoint(listenerName.value, securityProtocol, host, port)
  }
}

````

- host:broker主机名
- port: broker端口号
- listenerName:监听器名字。 目前预定义的名字包括：PLAINTEXT 、SSL 、SASL_PLAINTEXT和SASL_SSL. 
- securityProtocol:监听器使用的安全协议。支持4安全协议：PLAINTEXT、SSL、SASL_PLAINTEXT、SASL_SSL. **Broker 端参数 listener.security.protocol.map 用于指定不同名字的监听器都使用哪种安全协议**

````

class SocketServer(val config: KafkaConfig, 
  val metrics: Metrics,
  val time: Time,  
  val credentialProvider: CredentialProvider) 
  extends Logging with KafkaMetricsGroup with BrokerReconfigurable {
  // SocketServer实现BrokerReconfigurable trait表明SocketServer的一些参数配置是允许动态修改的
  // 即在Broker不停机的情况下修改它们
  // SocketServer的请求队列长度，由Broker端参数**queued.max.requests**值而定，默认值是500
  private val maxQueuedRequests = config.queuedMaxRequests
  ......
  // data-plane  // 处理数据类请求的Processor线程池
  private val dataPlaneProcessors = new ConcurrentHashMap[Int, Processor]()
  // 处理数据类请求的Acceptor线程池，每套监听器对应一个Acceptor线程
  private[network] val dataPlaneAcceptors = new ConcurrentHashMap[EndPoint, Acceptor]()
  // 处理数据类请求专属的RequestChannel对象
  val dataPlaneRequestChannel = new RequestChannel(maxQueuedRequests, DataPlaneMetricPrefix)
  // control-plane
  // 用于处理控制类请求的Processor线程
  // 注意：目前定义了专属的Processor线程而非线程池处理控制类请求
  private var controlPlaneProcessorOpt : Option[Processor] = None
  private[network] var controlPlaneAcceptorOpt : Option[Acceptor] = None
  // 处理控制类请求专属的RequestChannel对象
  val controlPlaneRequestChannelOpt: Option[RequestChannel] = config.controlPlaneListenerName.map(_ => new RequestChannel(20, ControlPlaneMetricPrefix))
  ......
}

````

- Broker 端参数**max.connections.per.ip**、**max.connection.per.ip.overrides**、**max.connections**是可以动态修改的

#### 创建 数据类请求监听器

- queued.max.requests : 定义了请求队列的最大长度

````

private def createDataPlaneAcceptorsAndProcessors(
  dataProcessorsPerListener: Int, endpoints: Seq[EndPoint]): Unit = {
  // 遍历监听器集合
  endpoints.foreach { endpoint =>
    
    // 将监听器纳入到连接配额管理之下->初始化该监听器对应的最大连接数计数器
    connectionQuotas.addListener(config, endpoint.listenerName)
    // 为监听器创建对应的Acceptor线程
    val dataPlaneAcceptor = createAcceptor(endpoint, DataPlaneMetricPrefix)
    // 为监听器创建多个Processor线程。具体数目由num.network.threads决定
    addDataPlaneProcessors(dataPlaneAcceptor, endpoint, dataProcessorsPerListener)
    // 将<监听器，Acceptor线程>对保存起来统一管理
    dataPlaneAcceptors.put(endpoint, dataPlaneAcceptor)
    info(s"Created data-plane acceptor and processors for endpoint : ${endpoint.listenerName}")
  }
}

````

#### 创建 控制类请求监听器

- 1 个 Acceptor 线程 + 1 个 Processor 线程 + 1 个深度是 20 的请求队列而已

- **control.plane.listener.name**，就是用于设置 Control plane 所用的监听器的地方

````

private def createControlPlaneAcceptorAndProcessor(
  endpointOpt: Option[EndPoint]): Unit = {
  // 如果为Control plane配置了监听器
  endpointOpt.foreach { endpoint =>
    // 将监听器纳入到连接配额管理之下
    connectionQuotas.addListener(config, endpoint.listenerName)
    // 为监听器创建对应的Acceptor线程
    val controlPlaneAcceptor = createAcceptor(endpoint, ControlPlaneMetricPrefix)
    // 为监听器创建对应的Processor线程
    val controlPlaneProcessor = newProcessor(nextProcessorId, controlPlaneRequestChannelOpt.get, connectionQuotas, endpoint.listenerName, endpoint.securityProtocol, memoryPool)
    controlPlaneAcceptorOpt = Some(controlPlaneAcceptor)
    controlPlaneProcessorOpt = Some(controlPlaneProcessor)
    val listenerProcessors = new ArrayBuffer[Processor]()
    listenerProcessors += controlPlaneProcessor
    // 将Processor线程添加到控制类请求专属RequestChannel中
    // 即添加到RequestChannel实例保存的Processor线程池中
    controlPlaneRequestChannelOpt.foreach(
      _.addProcessor(controlPlaneProcessor))
    nextProcessorId += 1
    // 把Processor对象也添加到Acceptor线程管理的Processor线程池中
    controlPlaneAcceptor.addProcessors(listenerProcessors, ControlPlaneThreadPrefix)
    info(s"Created control-plane acceptor and processor for endpoint : ${endpoint.listenerName}")
  }
}



````
























