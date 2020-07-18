
## tomcat 自身配置

### 连接器(Connector)

1.redirectPort:当用户用http请求某个资源，而该资源本身又被设置了必须要https方式访问，此时Tomcat会自动重定向到这个redirectPort设置的https端口

2.maxThreads:默认200，最大工作线程，同时处理请求。并发数（determines the maximum number of simultaneous[同时] requests that can be handled）

  ```
  请求处理线程的最大数量。默认值是200（Tomcat7和8都是的）。如果该Connector绑定了Executor，这个值会被忽略，因为该Connector将使用绑定的Executor，而不是内置的线程池来执行任务。
  
  maxThreads规定的是最大的线程数目，并不是实际running的CPU数量；实际上，maxThreads的大小比CPU核心数量要大得多。这是因为，处理请求的线程真正用于计算的时间可能很少，大多数时间可能在阻塞，如等待数据库返回数据、等待硬盘读写数据等。因此，在某一时刻，只有少数的线程真正的在使用物理CPU，大多数线程都在等待；因此线程数远大于物理核心数才是合理的。
  
  换句话说，Tomcat通过使用比CPU核心数量多得多的线程数，可以使CPU忙碌起来，大大提高CPU的利用率。
  
  每一次HTTP请求到达Web服务，tomcat都会创建一个线程来处理该请求，那么最大线程数决定了Web服务容器可以同时处理多少个请求。maxThreads默认200，肯定建议增加。
  但是，增加线程是有成本的，更多的线程，不仅仅会带来更多的线程上下文切换成本，而且意味着带来更多的内存消耗。
  JVM中默认情况下在创建新线程时会分配大小为1M的线程栈，所以，更多的线程异味着需要更多的内存。线程数的经验值为：1核2g内存为200，线程数经验值200；4核8g内存，线程数经验值800。
  
  ```

3.acceptCount:默认100,连接超过 maxThreads后,依然可以接收请求，到达acceptCount后 connection refused;(The maximum queue length for incoming connection requests when all possible request processing threads are in use)

```
maxThreads：tomcat起动的最大线程数，即同时处理的任务个数，默认值为200
acceptCount：当调用HTTP请求数达到tomcat的最大线程数时，还有新的HTTP请求到来，这时tomcat会将该请求放在等待队列中，这个acceptCount就是指能够接受的最大等待数，默认100。如果等待队列也被放满了，这个时候再来新的请求就会被tomcat拒绝（connection refused）

这两个值如何起作用，请看下面三种情况
情况1：接受一个请求，此时tomcat起动的线程数没有到达maxThreads，tomcat会起动一个线程来处理此请求。
情况2：接受一个请求，此时tomcat起动的线程数已经到达maxThreads，tomcat会把此请求放入等待队列，等待空闲线程。
情况3：接受一个请求，此时tomcat起动的线程数已经到达maxThreads，等待队列中的请求个数也达到了acceptCount，此时tomcat会直接拒绝此次请求，返回connection refused

```

4.executor:指定执行器

5.enableLookups:如果为true，则可以通过调用request.getRemoteHost()进行DNS查询来得到远程客户端的实际主机名，若为false则不进行DNS查询，而是返回其ip地址

6.connectionTimeout:Connector接受一个连接后等待的时间(milliseconds)，默认值是60000，-1表示不限制。 Tomcat附带的标准server.xml将此值设置为20000（即20秒）。 除非disableUploadTimeout设置为false ，超时也将读取请求体（如果有的话）时使用

7.protocol:默认 Http

   - AJP(Apache JServ Protocol)协议:连接器监听8009端口，负责和其他的HTTP服务器建立连接。在把Tomcat与其他HTTP服务器集成时，就需要用到这个连接器。支持在apache和tomcat之间的连接的重用，不适用于与nginx通信；
     
       ```
         因为性能原因，使用二进制格式来传输可读性文本。WEB服务器通过TCP连接和SERVLET容器连接。WEB服务器一般维持和Web容器的多个TCP Connecions，即TCP连接池，多个request/respons循环重用同一个Connection 但是当Connection被分配（Assigned）到某个请求时，该请求完成之前，其他请求不得使用该连接。
                                                                                 
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

8. maxPostSize: http-post请求中数据(body)的最大尺寸,单位:byte,默认值为2M.这对一些表单提交(较多文本域)有影响.可以适度调整此值,大文件上传一般会在client拆分成小文件,而不是直接发送

9. useBodyEncodingForUacceptCountRI: 是否使用"Content-type"中指定的编码方式对http-get请求中查询字符串进行编码.如果为"true",将会忽略"URIEncoding"配置项,转而使用header中"content-Type"指定的编码方式

10. keepAliveTimeout:  当无实际数据交互时，链接被保持的时间，单位：毫秒。在未指定此属性时，将使用connectionTimeout作为keepAliveTimeout。

11. allowTrace:如果需要服务器能够处理用户的HAED/TRACE请求，这个值应该设置为true，默认值是false

12. asyncTimeout:异步请求的超时时间,如果没有设置则设置Servlet指定的默认值30秒

13. maxHeaderCount:默认100，-1则代表不限制。请求头的最大个数,如果请求中的header个数超过此限定值,请求将会被拒绝.

14. maxParameterCount:默认值10000,负数代表没限制。Parameter and value pairs beyond this limit will be ignored.

15. port: 指定服务器端要创建的端口号，并在这个断口监听来自客户端的请求

16. acceptorThreadCount	: 用于接受连接的线程数。 增加了多CPU的机器上这个值，虽然你永远不会真正需要超过2 。 此外，有很多非保持活动连接，你可能想增加这个值。 默认值是1 。
                          
17. allowedTrailerHeaders: 默认情况下，Tomcat在处理分块输入时将忽略所有的尾部头。 对于要处理的标头，必须将其添加到此逗号分隔的标头名称列表中。
                           
18. bindOnInit:(套接字何时绑定)Controls when the socket used by the connector is bound. By default it is bound when the connector is initiated and unbound when the connector is destroyed. If set to false, the socket will be bound when the connector is started and unbound when it is stopped.
               
19. connectionUploadTimeout:指定在数据上传正在进行时使用的超时（以毫秒为单位）。 如果disableUploadTimeout设置为这只生效false 。
         
20. maxConnections

    ```
    Tomcat在任意时刻接收和处理的最大连接数。当Tomcat接收的连接数达到maxConnections时,Acceptor线程不会读取accept队列中的连接;这时accept队列中的线程会一直阻塞着，直到Tomcat接收的连接数小于maxConnections。如果设置为-1，则连接数不受限制。
    
    默认值与连接器使用的协议有关：NIO的默认值是10000，APR/native的默认值是(1024*8) 8192，而BIO的默认值为maxThreads(200)（如果配置了Executor，则默认值是Executor的maxThreads）。

    在windows下，APR/native 的maxConnections值会自动调整为设置值以下最大的1024的整数倍;如设置为2000,则最大值实际是1024。
    
    如果设置为-1，则禁用maxconnections功能，表示不限制tomcat容器的连接数。maxConnections和accept-count的关系为：当连接数达到最大值maxConnections后，系统会继续接收连接，但不会超过acceptCount的值。
    
    ```   
#### 参数设置
                        
（1）maxThreads 的设置既与应用的特点有关，也与服务器的CPU核心数量有关。通过前面介绍可以知道，maxThreads数量应该远大于CPU核心数量；而且CPU核心数越大，maxThreads应该越大；应用中CPU越不密集（IO越密集），maxThreads应该越大，以便能够充分利用CPU。当然，maxThreads的值并不是越大越好，如果maxThreads过大，那么CPU会花费大量的时间用于线程的切换，整体效率会降低。
    
（2）maxConnections 的设置与Tomcat的运行模式有关。如果tomcat使用的是BIO，那么maxConnections的值应该与maxThreads一致；如果tomcat使用的是NIO，maxConnections值应该远大于maxThreads。

（3）通过前面的介绍可以知道，虽然tomcat同时可以处理的连接数目是maxConnections，但服务器中可以同时接收的连接数为maxConnections+acceptCount 。acceptCount的设置，与应用在连接过高情况下希望做出什么反应有关系。如果设置过大，后面进入的请求等待时间会很长；如果设置过小，后面进入的请求立马返回connection refused。

### 执行器（Executor）线程池

1.maxThreads: 优先级低于connector的该属性配置

2.minSpareThreads:默认10，最少的保持运行的线程数

3.daemon :默认为true

4.maxIdleTime:默认为1分钟，非最小保持运行线程的最大存活时间，超过则shutdown

5.maxQueueSize: 最大的等待执行的可运行任务数((int) The maximum number of runnable[就绪] tasks that can queue up[排队] awaiting execution before we reject them. Default value is Integer.MAX_VALUE)

6.prestartminSpareThreads: 默认false, 是否在启动 Executor 的时候启动 minSpareThreads 指定的线程数

7.threadRenewalDelay[重建线程的时间间隔]: 如果配置了 ThreadLocalLeakPreventionListener[防止ThreadLocal内存泄漏监听器]如果配置了,它回通知当前Executor停止上下文。上下文停止后，线程池里面的线程将会被重启。避免了重启所有线程，它设置两个线程重建的时间间隔，默认值是1000 ms，如果是负值则不会重建。

8. threadPriority (int)线程的线程优先级执行程序,默认是5(NORM_PRIORITY常数)

## tomcat 运行时jvm配置

1. -Xms【初始化内存大小】
 
2. -Xmx【可以使用的最大内存】