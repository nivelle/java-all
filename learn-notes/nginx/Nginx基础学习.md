
### nginx初步指令

```
# 启动
nginx -s start;
# 重新启动，热启动，修改配置重启不影响线上
nginx -s reload;
# 关闭
nginx -s stop;
# 修改配置后，可以通过下面的命令测试是否有语法错误
nginx -t;


```

### web服务器的 nginx.conf配置文件解读

```
#user  nobody;
##定义拥有和运行Nginx服务的Linux系统用户

worker_processes  1;
##定义单进程。通常将其设成CPU的个数或者内核数

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;
##定义Nginx在哪里打日志


#pid        logs/nginx.pid;
##Nginx写入主进程ID（PID）

events { ## 配置影响nginx服务器或与用户的网络连接。有每个进程的最大连接数，选取哪种事件驱动模型处理连接请求，是否允许同时接受多个网路连接，开启多个网络连接序列化等。
    accept_mutex on;   #设置网路连接序列化，防止惊群现象发生，默认为on
    multi_accept on;  #设置一个进程是否同时接受多个网络连接，默认为off
    #use epoll;      #事件驱动模型，select|poll|kqueue|epoll|resig|/dev/poll|eventport
    worker_connections  1024;
    ##通过worker_connections和worker_processes计算maxclients。
    ##max_clients = worker_processes * worker_connections
}


http {
    include       mime.types;
    ##在/opt/nginx/conf/mime.types写的配置将在http模块中解析,文件扩展名与文件类型映射表 
    
    default_type  application/octet-stream; #默认文件类型，默认为text/plain

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    ##如果是为了获取本地存储的静态化文件，sendfile可以加速服务端，但是如果是反向代理，那么该功能就失效了。允许sendfile方式传输文件，默认为off，可以在http块，server块，location块
    #tcp_nopush     on; ##在 nginx 中，tcp_nopush 配置和 tcp_nodelay "互斥"。它可以配置一次发送数据的包大小。也就是说，它不是按时间累计  0.2 秒后发送包，而是当包累计到一定大小后就发送。在 nginx 中，tcp_nopush 必须和sendfile 搭配使用。
    #keepalive_timeout  0;
    keepalive_timeout  65;
    ##设置保持客户端连接时间,#连接超时时间，默认为75s，可以在http，server，location块。
    sendfile_max_chunk 100k;  #每个进程每次调用传输数量不能大于设定的值，默认为0，即不设上限。
    #gzip  on; ##告诉服务端用gzip压缩
    
    upstream mysvr {   
      server 127.0.0.1:7878;
      server 192.168.10.121:3333 backup;  #热备
    }
    
    
    server { ##如果你想对虚拟主机进行配置，可以在单独的文件中配置server模块，然后include进来
        keepalive_requests 120; #单连接请求上限次数。
        listen       8080;  ##告诉Nginx TCP端口，监听HTTP连接。listen 80; 和 listen *:80;是一样的
        server_name  localhost;
        ##定义虚拟主机的名字
        #charset koi8-r;
        #access_log  logs/host.access.log  main;

        location / { ##location模块可以配置nginx如何反应资源请求  #请求的url过滤，正则匹配，~为区分大小写，~*为不区分大小写。
            ## root   html;
            ## index  index.html index.htm;
           proxy_pass  http://mysvr;  #请求转向mysvr 定义的服务器列表
           deny 127.0.0.1;  #拒绝的ip
           allow 172.18.5.54; #允许的ip       
            
        }

        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


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
    # 完全匹配  =
    # 大小写敏感 ~
    # 忽略大小写 ~*
    
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
    
    /**
    HTTP header 中的 Host 含义为所请求的目的主机名。当 nginx 作为反向代理使用，而后端真实 web 服务器设置有类似 防盗链功能 ，或者根据 HTTP header 中的 Host 字段来进行 路由 或 过滤 功能的话，若作为反向代理的 nginx 不重写请求头中的 Host 字段，将会导致请求失败。
    **/
    proxy_set_header   Host local.baidu.com;
    /**
    HTTP header 中的 X_Forward_For 表示该条 http 请求是由谁发起的。如果反向代理服务器不重写该请求头的话，那么后端真实 web 服务器在处理时会认为所有的请求都来自反向代理服务器。如果后端 web 服务器有防攻击策略的话，那么反向代理服务器对应的 ip 地址就会被封掉。
    配置的意思是增加一个 $proxy_add_x_forwarded_for 到 X-Forwarded-For里去，注意是增加，而不是覆盖。当然由于默认的 X-Forwarded-For 值是空的，所以我们总感觉 X-Forwarded-For 的值就等于 $proxy_add_x_forwarded_for 的值。
    X-Forwarded-For的格式为X-Forwarded-For:real client ip, proxy ip 1, proxy ip N，每经过一个反向代理就在请求头X-Forwarded-For后追加反向代理IP。

    /**
    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    proxy_set_header   X-Real-IP        $remote_addr;
  
    proxy_pass         http://baidunode;
    proxy_connect_timeout 30s;
 }



```

#### 负载均衡

Nginx提供了两种负载均衡策略：内置策略和扩展策略。内置策略为轮询，加权轮询，Ip hash。扩展策略，就是自己实现一套策略。
大家可以通过upstream这个配置，写一组被代理的服务器地址，然后配置负载均衡的算法。

- 热备

当一台服务器发生事故时，才启用第二台服务器给提供服务。
比如127.0.0.1 挂了，就启动192.168.10.121。

```
upstream mysvr { 
      server 127.0.0.1:7878; 
      server 192.168.10.121:3333 backup;  #热备     
    }

```
- 轮询
```
upstream mysvr { 
      server 127.0.0.1:7878;
      server 192.168.10.121:3333;       
    }
```
Nginx 轮询的默认权重是1。 所以请求顺序就是ABABAB....交替


- 加权轮询

根据权重大小，分发给不同服务器不同数量请求。如下配置的请求顺序为：ABBABBABBABB.....。可以针对不同服务器的性能，配置不同的权重。

```
upstream mysvr { 
      server 127.0.0.1:7878 weight=1;
      server 192.168.10.121:3333 weight=2;
}

```

- ip_hash

让相同客户端ip请求相同的服务器。对客户端请求的ip进行hash操作，然后根据hash结果将同一个客户端ip的请求分发给同一台服务器进行处理，可以解决session不共享的问题。

```
upstream mysvr { 
      server 127.0.0.1:7878; 
      server 192.168.10.121:3333;
      ip_hash;
    }

```

**代码配置来之:**([樂浩beyond](https://www.jianshu.com/p/734ef8e5a712))
