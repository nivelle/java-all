## http基础


### header

#### general 通用头部 (不属于headers，只用于收集请求url和响应的status等信息)

- Request URL: http://nivelle.me:39300/nd_admin/settlement/list?page=1&size=10&total=0&count=0&companyId=&companyName=

- Request Method: GET

- Status Code: 200 

- Remote Address: 39.107.11.210:443 //路由地址

- Referrer Policy: no-referrer-when-downgrade

- Date:创建报文的日期时间

- Keep-Alive:用来设置超时时长和最大请求数

#### Request Headers

- Accept:告诉WEB服务器自己接受什么介质类型，*/* 表示任何类型，type/* 表示该类型下的所有子类型;

- Accept-Charset:浏览器申明自己接收的字符集

- Accept-Encoding: 浏览器申明自己接收的编码方法,通常指定压缩方法,是否支持压缩,支持什么压缩方法(gzip,deflate)

- Connection: 表示是否需要持久连接。close（告诉WEB服务器或者代理服务器,在完成本次请求的响应后，断开连接,不要等待本次连接的后续请求了）。keep-alive（告诉WEB服务器或者代理服务器，在完成本次请求的响应后，保持连接，等待本次连接的后续请求）。

- Authorization: HTTP授权的授权证书	 Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==

- Content-Type: 请求参数对应的MIME信息 

- Referer: 先前网页的地址，当前请求网页紧随其后,即来路	

- Origin: 表明了请求来自于哪个站点  (eg:<scheme> "://" <host> [ ":" <port> ])

#### Response Headers

- Content-Location: 请求资源可替代的备用的另一地址	

- Location: 令客户端重定向至指定 URI;	

- Allow: 对某网络资源的有效的请求行为,不允许则返回405	

- Content-Type:返回内容的MIME类型	(Content-Type: text/html; charset=utf-8)

#### Entity Headers

- Expires:包含日期/时间,即在此时候之后,响应过期(如果在Cache-Control响应头设置了 "max-age" 或者 "s-max-age" 指令，那么 Expires 头会被忽略)

