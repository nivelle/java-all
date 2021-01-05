## http基础


### header

#### general 通用头部 (同时适用于请求和响应消息，但与最终消息传输的数据无关的消息头 不属于headers，只用于收集请求url和响应的status等信息)

- Request URL: http://nivelle.me:39300/nd_admin/settlement/list?page=1&size=10&total=0&count=0&companyId=&companyName=

- Request Method: GET

- Status Code: 200 

- Remote Address: 39.107.11.210:443 //路由地址

- Referrer Policy: no-referrer-when-downgrade

- Date:创建报文的日期时间

- Keep-Alive:用来设置超时时长和最大请求数

- Cache-Control: //缓存控制

  1. no-store 规定不能对请求或响应的任何一部分进行缓存(no-store 规定不能对请求或响应的任何一部分进行缓存) 
  
  2. no-cache (no-cache 指令规定缓存服务器需要先向源服务器验证缓存资源的有效性，只有当缓存资源有效才将能使用该缓存对客户端的请求进行响应。)
   
  3. max-age 指令出现在请求报文中，并且缓存资源的缓存时间小于该指令指定的时间，那么就能接受该缓存; max-age 指令出现在响应报文中，表示缓存资源在缓存服务器中保存的时间。或者使用Expires

- If-None-Match: //缓存验证

  可以将缓存资源的值放入request header的 If-None-Match 首部，服务器收到该请求后，判断缓存资源的 值和资源的最新 值是否一致，如果一致则表示缓存资源有效，返回 304 Not Modified。并在response header中戴上 ETag 值
  
```
//请求
If-None-Match: W/"646-RJRVaOjMluW+SOAL0EThThA2lnM"

//响应
ETag: W/"646-RJRVaOjMluW+SOAL0EThThA2lnM"
```
                                                                                            
                                                 
#### Request Headers(包含更多有关要获取的资源或客户端本身信息的消息头)

- Accept:告诉WEB服务器自己接受什么介质类型，*/* 表示任何类型，type/* 表示该类型下的所有子类型;

- Accept-Charset:浏览器申明自己接收的字符集

- Accept-Encoding: 浏览器申明自己接收的编码方法,通常指定压缩方法,是否支持压缩,支持什么压缩方法(gzip,deflate)

- Connection: 表示是否需要持久连接。close（告诉WEB服务器或者代理服务器,在完成本次请求的响应后，断开连接,不要等待本次连接的后续请求了）。keep-alive（告诉WEB服务器或者代理服务器，在完成本次请求的响应后，保持连接，等待本次连接的后续请求）。

- Authorization: HTTP授权的授权证书	 Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==

- Content-Type: 请求参数对应的MIME信息 

- Referer: 先前网页的地址，当前请求网页紧随其后,即来路	

- Origin: 表明了请求来自于哪个站点  (eg:<scheme> "://" <host> [ ":" <port> ])

#### Response Headers(包含有关响应的补充信息，如其位置或服务器本身（名称和版本等）的消息头)

- Content-Location: 请求资源可替代的备用的另一地址	

- Location: 令客户端重定向至指定 URI;	

- Allow: 对某网络资源的有效的请求行为,不允许则返回405	

- Content-Type:返回内容的MIME类型	(Content-Type: text/html; charset=utf-8)

#### Entity Headers(包含有关实体主体的更多信息，比如主体长(Content-Length)度或其MIME类型)

- Expires:包含日期/时间,即在此时候之后,响应过期(如果在Cache-Control响应头设置了 "max-age" 或者 "s-max-age" 指令，那么 Expires 头会被忽略)

- Last-Modified: 资源的最后修改日期时间

- Content-Length: 发送给接收方的消息主体的大小；

- Content-Type:告诉客户端实际返回的内容的内容类型；详情


### mime

![常见mime类型](https://s1.ax1x.com/2020/07/03/NviMWj.png)

### 状态码

- 100(continue) 表明到目前为止都很正常，客户端可以继续发送请求或者忽略这个响应。

- 200(OK) 表示从客户端发来的请求在服务器端被正常处理了。

- 204(No Content) 该状态码代表服务器接收的请求已成功处理，但在返回的响应报文中不含实体的主体部分。

- 206(Partial Content) 该状态码表示客户端进行了范围请求，而服务器成功执行了这部分的 GET 请求。响应报文中包含由 Content-Range 指定范围的实体内容。

- 301(Moved Permanently) 永久性重定向。该状态码表示请求的资源已被分配了新的 URI，以后应使用资源现在所指的 URI。

- 302(Found) 临时性重定向。比如在没有登录情况下访问网站"个人中心"，会重定向到登录页，但是你登录后，访问个人中心时，它又不会重定向到其他地方了。

- 303(See Other) 和 302 有着相同的功能，但是 303 明确要求客户端应该采用 GET 方法获取资源。

- 304(Not Modified) 如果请求报文首部包含一些条件，例如：If-Match，If-Modified-Since，If-None-Match，If-Range，If-Unmodified-Since，如果不满足条件，则服务器会返回 304 状态码。

- 400(Bad Request) 该状态码表示请求报文中存在语法错误。当错误发生时，需修改请求的内容后再次发送请求。

- 401 Unauthorized 该状态码表示发送的请求需要有认证信息。返回含有 401 的响应必须包含一个适用于被请求资源的 WWW-Authenticate 首部用以询问用户信息。当浏览器初次接收到 401 响应，会弹出认证用的对话窗口。第二次接收到，则不弹出，直接表示认证失败。

- 403(Forbidden) 对请求资源的访问被服务器拒绝了，一般是未获得文件系统的访问授权，问权限出现某些问题。

- 404(Not Found) 浏览器地址错误。服务器找不到对应资源。

- 429(Too Many Requests) 如果你希望限制客户端对服务的请求数，可使用 429 状态码，同时包含一个 Retry-After 响应头用于告诉客户端多长时间后可以再次请求服务

- 431(Request Header Fields Too Large) 请求头字段太大

- 500(Internal Server Error) 服务器在执行时报错。

- 503(Service Unavailable) 服务器暂时处于超负载或正在进行停机维护，无响应。一般需要重启服务器即可。

### CORS

是一种机制，它使用额外的 HTTP 头来告诉浏览器 让运行在一个 origin (domain) 上的Web应用被准许访问来自不同源服务器上的指定的资源。当一个资源从与该资源本身所在的服务器不同的域、协议或端口请求一个资源时，资源会发起一个跨域 HTTP 请求。

1. 简单请求

```
需要满足下列所有条件：

第一条: 请求方式必须为 GET | HEAD

第二条: Content-Type 的值必须属于下列之一 (application/x-www-form-urlencoded | multipart/form-data | text/plain)

```

例如: 在http://foo.exmaple上要访问 http://bar.other上的资源。则

```
//request headers上添加
Origin: http://foo.example


//response headers返回
Access-Control-Allow-Origin: *

```
如果返回

```
Access-Control-Allow-Origin: http://foo.example
```

2. 非“简单请求”（触发CORS预检请求）

```
需要满足下列所有条件：

第一条： http请求方式为下列
POST | PUT | DELETE | CONNECT | OPTIONS | TRACE | PATCH

第二条： Content-Type 的值不属于下列之一:
application/x-www-form-urlencoded | multipart/form-data | text/plain
```

如果在 http://foo.exmaple 上要访问 http://bar.other/resources/po... 上的资源。且 request headers 中 Content-Type为application/xml，请求method为post。
那么此请求是个“非简单请求”。首先浏览器会自动发送带有options选项的预检请求，然后发送实际请求:

```
//预检请求request headers
OPTIONS /resources/post-here/ HTTP/1.1（自动，不需要设置）

//设置预检请求的response headers
Access-Control-Request-Method: POST
Access-Control-Request-Headers: Content-Type
```

