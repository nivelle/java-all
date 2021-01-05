### SpringBoot 之 SpringMVC请求过程

##### Tomcat监听端口生成HttpServletRequest

####### 请求处理 : socket => coyote.Request

- public abstract class AbstractProtocol<S> implements ProtocolHandler,MBeanRegistration

  - protected static class ConnectionHandler<S> implements AbstractEndpoint.Handler<S>
  
    - public SocketState process(SocketWrapperBase<S> wrapper, SocketEvent status)
    
      - S socket = wrapper.getSocket();
    
      - Processor processor = connections.get(socket);
      
  ```
   1. 创建一个Processor Http11Processor ： processor = getProtocol().createProcessor() Http11Processor 对象创建 :

     （1). 新建 coyote request,response 对象 , 这是 Tomcat 使用的对请求/响应的建模类型 ;
     
     （2). 新建 Http11InputBuffer/Http11OutputBuffer 对象,用于作为一对儿请求/响应 IO操作的数据缓冲区;
     
   2. processor.service(SocketWrapperBase<?> socketWrapper)
   
  ```
  
####### 请求处理 : coyote.Request => HttpServletRequest

- Http11Processor#service(SocketWrapperBase<?> socketWrapper);//处理某个指定的请求socket socketWrapper，还没有任何请求数据读取;
                                                                
  ```
  1. 使用一个Http11InputBuffer inputBuffer 绑定到 socketWrapper，用于处理请求数据;
  
  2. 使用一个Http11OutputBuffer outputBuffer 绑定到 socketWrapper,用于处理响应数据;
  
  3. 使用inputBuffer分析Request Line, 头部,相应的分析结果会填充到request/response对象中;
  
  4. 使用 adapter#service(request,response)进一步处理请求和做出响应;

  ```
- CoyoteAdapter#service(coyote.Request req,coyote.Response res)
  
  ```
  1. 创建 connector.Request 对象，这是一个 HttpServletRequest,包装请求参数 req;
  
  2. 创建 connector.Response 对象，这是一个 HttpServletResponse,包装响应参数 rep;
  
  3. 指定 queryString charset : 缺省 UTF-8;
  
  4. postParseRequest(req,request,res,response)
  
  5. connector.getService().getContainer().getPipeline().getFirst().invoke(request, response)
  
  ```
  
- connector.getService().getContainer().getPipeline().getFirst().invoke(request, response)
  
  ```
  1. 这里的参数 request,response 是 HttpServletRequest/HttpServletResponse ; 这里的参数 request,response 已经包装携带了原始的 coyote request,response 对象 ;

  2. 这里的 connector 在应用启动阶段已经创建，是一个 org.apache.catalina.connector.Connector;
  
  3. 这里的 connector.getService().getContainer() 对应的是一个 tomcat StandardEngine 对象;它的 pipeline 中其实只有一个 basic valve : StandardEngineValve

  4. 该调用其实继续传递 ： host.getPipeline().getFirst().invoke(request, response);这里的host来自 request.getHost(), 它在 Adapter 中 postParseRequest 过程中完成
                                                                        
  ```
  
- host.getPipeline().getFirst().invoke(request, response)
  
  - 这里 host 的 pipeline 中有两个 Valve : ErrorReportValve 和basic valve StandardHostValve
  
  - 这里的 getFirst() 对应的是 ErrorReportValve
  
- ErrorReportValve#invoke(Request request, Response response)

  - getNext().invoke(request, response) : 其实是调用 StandardHostValve#invoke;这里是先调用下一个valve，也就是真正的servlet处理，后report错误的做法

  - report(Request request, Response response, Throwable throwable);
  
    ```
    此方法在响应状态1xx,2xx,3xx时不执行 根据异常throwable和响应状态码response.getStatus()尝试跳转到错误处理页面如果找不到匹配的错误处理页面，展示一个缺省的错误处理页面（HTML内容在此拼装生成）
                                                                              
    ```
- StandardHostValve#invoke(Request request, Response response)

  ```
  该调用其实继续传递: context.getPipeline().getFirst().invoke(request, response);这里的context来自 request.getContext(), 它在 Adapter 中 postParseRequest 过程中完成这里的 context.Pipeline()有两个Valve : NonLoginAuthenticator,StandardContextValve
  
  这里的 getFirst() 对应 NonLoginAuthenticator， 缺省情况下它没做任何限制，NonLoginAuthenticator valve 中继续传递调用 : getNext().invoke(request, response)
                                                        

  ```
  
- StandardContextValve#invoke(Request request, Response response)
  
  ```
  拒绝访问/META-INF,/WEB-INF 等目录：response.sendError(404);如果找不到合适的 Wrapper, 也就是最终处理者 Servlet , 报告错误 : response.sendError(404)
  
  继续传递请求 : wrapper.getPipeline().getFirst().invoke(request, response)

  ```

- wrapper.getPipeline().getFirst().invoke(request, response)
  
  ```
  1. 这里 wrapper 的 pipeline 其实只有一个 basic valve : StandardWrapperValve 获取 servlet 实例: wrapper.allocate()，这里会对应 Spring Web 的 DispatcherServlet

  2. 构建ApplicationFilterChain filterChain = ApplicationFilterFactory.createFilterChain(request, wrapper, servlet)
  
  3. 相应 context 上的 filterMaps 属性是关于 url/filter 映射关系定义的，这里用来获取所需要应用的 Filter,然后加上request,response,servlet共同构成filterChain 调用 filterChain.doFilter(request.getRequest(), response.getResponse())
   
     注意这里getRequest()/getResponse()对应的是 RequestFacade/ResponseFacade。这里使用Facade 的目的是可以避免servlet开发环境中访问到一些tomcat底层无关的部分，从而仅仅关注必须要关注的部分。
                                                                                

  ```

##### doDispatch

[DispatcherServlet](../springmvc/SpringMVC源码解析之dispatcherServlet.md)



