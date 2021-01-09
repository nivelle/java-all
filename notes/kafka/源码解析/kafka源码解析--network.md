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
































