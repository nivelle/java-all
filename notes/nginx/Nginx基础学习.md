
### nginx 基础指令

#### 启动
```
 nginx -s start;

```
#### 重新启动，热启动，修改配置重启不影响线上

```
nginx -s reload;

```
#### 关闭

```
nginx -s stop;

```
#### 修改配置后，可以通过下面的命令测试是否有语法错误
```
nginx -t;

```

### web服务器的 nginx.conf 配置文件解读

#### 指定执行 nginx的 worker process的用户
```
user  nobody;

```

#### 工作进程数,通常将其设成CPU的个数或者内核数

```
worker_processes  1;

```

#### 定义 nginx 在哪里打日志

```
error_log(关键字)    <FILE>(日志文件)    <LEVEL>(错误日志级别);

1. 关键字：其中关键字error_log不能改变

2. 日志文件：可以指定任意存放日志的目录

3. 错误日志级别：常见的错误日志级别有[debug | info | notice | warn | error | crit | alert | emerg]，级别越高记录的信息越少。

生产场景一般是 warn | error | crit 这三个级别之一

```

#### pid  logs/nginx.pid; //nginx 进程ID（PID）;定nginx进程运行文件存放地址

#### 配置影响 nginx 服务器或与用户的网络连接。有每个进程的最大连接数,选取哪种事件驱动模型处理连接请求,是否允许同时接受多个网路连接,开启多个网络连接序列化等。

```
events { 
    accept_mutex on;   // 设置网路连接序列化，防止惊群现象发生，默认为on
    multi_accept on;  //设置一个进程是否同时接受多个网络连接，默认为off
    use epoll;      //事件驱动模型，select|poll|kqueue|epoll|resig|/dev/poll|eventport
    worker_connections  1024; //通过worker_connections和worker_processes计算maxclients。单个工作进程可以允许同时建立外部连接的数量
    max_clients = worker_processes * worker_connections 
}

```
##### worker_connections,connections不是随便设置的，而是与两个指标有重要关联，一是内存，二是操作系统级别的“进程最大可打开文件数”

- 内存:每个连接数分别对应一个read_event、一个write_event事件，一个连接数大概占用232字节，2个事件总占用96字节，那么一个连接总共占用328字节，通过数学公式可以算出100000个连接数大概会占用 31M = 100000 * 328 / 1024 / 1024，当然这只是nginx启动时，connections连接数所占用的nginx。
    
- 进程最大可打开文件数：进程最大可打开文件数受限于操作系统，可通过 ulimit -n 命令查询，以前是1024，现在是65535,nginx提供了worker_rlimit_nofile指令，这是除了ulimit的一种设置可用的描述符的方式。 该指令与使用ulimit对用户的设置是同样的效果。此指令的值将覆盖ulimit的值，如：worker_rlimit_nofile 20960;
 
  设置ulimits：ulimit -SHn 65535
           


```
http {
    include       mime.types;  // 在/opt/nginx/conf/mime.types写的配置将在http模块中解析,文件扩展名与文件类型映射表 
    
    default_type  application/octet-stream; // 默认文件类型，默认为text/plain

    // lof_format参数的标签段位置: http
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" ' '$status $body_bytes_sent "$http_referer" ' '"$http_user_agent" "$http_x_forwarded_for"';//用来定义记录日志的格式（可以定义多种日志格式，取不同名字即可）;日志格式,别名是main

    // access_log参数的标签段位置:http, server, location, if in location, limit_except                   
    access_log  logs/access.log  main;  // 用来指定日志文件的路径及使用的何种日志格式记录日志， main 是日志格式的别名

    sendfile        on; //如果是为了获取本地存储的静态化文件，sendfile可以加速服务端，但是如果是反向代理，那么该功能就失效了。允许sendfile方式传输文件，默认为off，可以在http块，server块，location块
    
    tcp_nopush     on; //在 nginx 中，tcp_nopush 配置和 tcp_nodelay "互斥"。它可以配置一次发送数据的包大小。也就是说，它不是按时间累计  0.2 秒后发送包，而是当包累计到一定大小后就发送。在 nginx 中，tcp_nopush 必须和sendfile 搭配使用。
    
    keepalive_timeout  65; //设置保持客户端连接时间,连接超时时间，默认为75s，可以在http，server，location块。
    
    sendfile_max_chunk 100k; // 每个进程每次调用传输数量不能大于设定的值，默认为0，即不设上限。
    
    gzip  on; //告诉服务端用gzip压缩
    
    //负载均衡
    upstream mysvr {   
      server 127.0.0.1:7878;
      server 192.168.10.121:3333 backup;  //热备
    }
    
    //如果你想对虚拟主机进行配置，可以在单独的文件中配置server模块，然后include进来
    server { 
        keepalive_requests 120; //单连接请求上限次数。
        listen       8080;  //告诉Nginx TCP端口，监听HTTP连接。listen 80; 和 listen *:80;是一样的
        server_name  localhost;
        
        charset koi8-r;//定义虚拟主机的名字
        access_log  logs/host.access.log  main;

        location / { //location模块可以配置nginx如何反应资源请求  #请求的url过滤，正则匹配，~为区分大小写，~*为不区分大小写。
           root   html;
           index  index.html index.htm;
           proxy_pass  http://mysvr;  //请求转向mysvr 定义的服务器列表;http:// + upstream名称
           deny 127.0.0.1;  //拒绝的ip
           allow 172.18.5.54; //允许的ip         
        }

        error_page  404              //404.html;

        //redirect server error pages to the static page /50x.html
        //error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        location ~ /\.ht {
            deny  all;
        }
    }

    # HTTPS server
    #
    #server {
    #    listen       443 ssl;
    #    server_name  localhost;

    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;

    #    ssl_session_cache    shared:SSL:1m;
    #    ssl_session_timeout  5m;

    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers  on;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}
    include servers/*;
}

```


#### 主要关键词

- location 


```

完全匹配  =;大小写敏感 ~;忽略大小写 ~*
    
根目录设置:

 location / {
    root /home/barret/test/;
    
    主页设置:
    index /html/index.html /php/index.php;   
}

```

- Rewrite 重定向

```
location /users/ {
    rewrite ^/users/(.*)$ /show?user=$1 break;
}

```
重写类型：

last ：相当于Apache里德(L)标记，表示完成rewrite，浏览器地址栏URL地址不变

break；本条规则匹配完成后，终止匹配，不再匹配后面的规则，浏览器地址栏URL地址不变

redirect：返回302临时重定向，浏览器地址会显示跳转后的URL地址

permanent：返回301永久重定向，浏览器地址栏会显示跳转后的URL地址

---


#### 反向代理服务器配置

```
upstream baidunode {
server 172.25.0.105:8081 weight=10 max_fails=3     fail_timeout=30s;
}
location / {

    add_header Cache-Control no-cache; ## nginx配置文件通过使用add_header指令来设置response header
    
    //HTTP header 中的 Host 含义为所请求的目的主机名。当 nginx 作为反向代理使用,而后端真实 web 服务器设置有类似 防盗链功能,或者根据 HTTP header 中的 Host 字段来进行 路由 或 过滤 功能的话，若作为反向代理的 nginx 不重写请求头中的 Host 字段，将会导致请求失败。
    proxy_set_header   Host local.baidu.com;

    //HTTP header 中的 X_Forward_For 表示该条 http 请求是由谁发起的。如果反向代理服务器不重写该请求头的话，那么后端真实 web 服务器在处理时会认为所有的请求都来自反向代理服务器。如果后端 web 服务器有防攻击策略的话，那么反向代理服务器对应的 ip 地址就会被封掉。
    //配置的意思是增加一个 $proxy_add_x_forwarded_for 到 X-Forwarded-For里去,注意是增加,而不是覆盖。当然由于默认的 X-Forwarded-For 值是空的，所以我们总感觉 X-Forwarded-For 的值就等于 $proxy_add_x_forwarded_for 的值。
    //X-Forwarded-For的格式为X-Forwarded-For:real client ip, proxy ip 1, proxy ip N，每经过一个反向代理就在请求头X-Forwarded-For后追加反向代理IP。

    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    proxy_set_header   X-Real-IP        $remote_addr;
  
    proxy_pass         http://baidunode;
    proxy_connect_timeout 30s;
 }

```

#### upstream(负载均衡)

##### Nginx提供了两种负载均衡策略:内置策略和扩展策略。大家可以通过upstream这个配置，写一组被代理的服务器地址，然后配置负载均衡的算法。

- 热备

```
upstream mysvr { 
      server 127.0.0.1:7878; 
      server 192.168.10.121:3333 backup;  //热备  当一台服务器发生事故时，才启用第二台服务器给提供服务。比如127.0.0.1 挂了，就启动192.168.10.121。
    }

```
- 轮询

```
upstream mysvr { 
      server 127.0.0.1:7878; 
      server 192.168.10.121:3333; //Nginx 轮询的默认权重是1。 所以请求顺序就是ABABAB....交替
     
    }
```


- 加权轮询

```
upstream mysvr { 
      server 127.0.0.1:7878 weight=1; //根据权重大小，分发给不同服务器不同数量请求。如下配置的请求顺序为：ABBABBABBABB.....。可以针对不同服务器的性能，配置不同的权重。
      server 192.168.10.121:3333 weight=2;
}

```

- ip_hash

```
upstream mysvr { 
      server 127.0.0.1:7878;  //让相同客户端ip请求相同的服务器。对客户端请求的ip进行hash操作，然后根据hash结果将同一个客户端ip的请求分发给同一台服务器进行处理，可以解决session不共享的问题。
      server 192.168.10.121:3333;
      ip_hash;
    }

```

- fair

```
upstream mysvr { 
      server 127.0.0.1:7878;  // 按后端服务器的响应时间来分配请求，响应时间短的优先分配。与weight分配策略类似。                              
      server 192.168.10.121:3333;
      fair;
    }
    
```

- url_hash

```
upstream mysvr{ 
      server 127.0.0.1:7878;  // 按访问url的hash结果来分配请求，使每个url定向到同一个后端服务器，后端服务器为缓存时比较有效。                                                              
      server 192.168.10.121:3333;
      hash $request_uri; 
      hash_method crc32; 
}

```

##### upstream还可以为每个设备设置状态值，这些状态值的含义分别如下：

- down 表示单前的server暂时不参与负载.
  
- weight 默认为1.weight越大，负载的权重就越大。
  
- max_fails ：允许请求失败的次数默认为1.当超过最大次数时，返回proxy_next_upstream 模块定义的错误.
  
- fail_timeout : max_fails次失败后，暂停的时间。
  
- backup： 其它所有的非backup机器down或者忙的时候，请求backup机器。所以这台机器压力会最轻。
  
