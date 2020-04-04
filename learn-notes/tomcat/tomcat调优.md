
## tomcat 自身配置

### 连接器

1.redirectPort:当用户用http请求某个资源，而该资源本身又被设置了必须要https方式访问，此时Tomcat会自动重定向到这个redirectPort设置的https端口

2.maxThreads:默认200，最大并发数（determines the maximum number of simultaneous requests that can be handled）

3.acceptCount:默认100,连接超过 maxThreads后,依然可以接收请求，到达acceptCount后 connection refused;(The maximum queue length for incoming connection requests when all possible request processing threads are in use)

4.executor:指定执行器

5.enableLookups:是否反查域名，取值为：true或false。为了提高处理能力，应设置为false

6.minProcessors:最小空闲连接线程数，用于提高系统处理性能，默认值为10

7.maxProcessors:最大连接线程数，即：并发处理的最大请求数，默认值为75

8.connectionTimeout:网络连接超时，单位：毫秒。设置为0表示永不超时，这样设置有隐患的。通常可设置为30000毫秒。

9.protocol:默认 Http, 

   - AJP(Apache JServ Protocol)协议:连接器监听8009端口，负责和其他的HTTP服务器建立连接。在把Tomcat与其他HTTP服务器集成时，就需要用到这个连接器。支持在apache和tomcat之间的连接的重用，不适用于与nginx通信；
     
```
     因为性能原因，使用二进制格式来传输可读性文本。WEB服务器通过TCP连接和SERVLET容器连接。
     
     WEB服务器一般维持和Web容器的多个TCP Connecions，即TCP连接池，多个request/respons循环重用同一个Connection 但是当Connection被分配（Assigned）到某个请求时，该请求完成之前，其他请求不得使用该连接。
                                                                                  
     
     (1). org.apache.coyote.ajp.AjpProtocol - blocking Java connector
     
     (2). org.apache.coyote.ajp.AjpNioProtocol - non blocking Java NIO connector.
     
     (3). org.apache.coyote.ajp.AjpNio2Protocol - non blocking Java NIO2 connector.
     
     (4). org.apache.coyote.ajp.AjpAprProtocol - the APR/native connector.

```
    
   - HTTP协议:连接器监听8080端口，负责建立HTTP连接。在通过浏览器访问Tomcat服务器的Web应用时，使用的就是这个连接器。　　

```

   1. org.apache.coyote.http11.Http11Protocol - blocking Java connector
   
   2. org.apache.coyote.http11.Http11NioProtocol - non blocking Java NIO connector
   
   3. org.apache.coyote.http11.Http11Nio2Protocol - non blocking Java NIO2 connector
   
   4. org.apache.coyote.http11.Http11AprProtocol - the APR/native connector.

```

10. maxPostSize: http-post请求中数据(body)的最大尺寸,单位:byte,默认值为2M.这对一些表单提交(较多文本域)有影响.可以适度调整此值,大文件上传一般会在client拆分成小文件,而不是直接发送

11. useBodyEncodingForURI: 是否使用"Content-type"中指定的编码方式对http-get请求中查询字符串进行编码.如果为"true",将会忽略"URIEncoding"配置项,转而使用header中"content-Type"指定的编码方式

12. keepAliveTimeout:  当无实际数据交互时，链接被保持的时间，单位：毫秒。在未指定此属性时，将使用connectionTimeout作为keepAliveTimeout。

### 执行器（Executor）

1.maxThreads: 优先级低于connector的该属性配置

2.minSpareThreads:默认10，最少的保持运行的线程数，


## tomcat 运行时jvm配置

1. -Xms【初始化内存大小】
 
2. -Xmx【可以使用的最大内存】