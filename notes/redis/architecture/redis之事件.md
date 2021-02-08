### 发布与订阅

一般来说,发布与订阅(pub/sub)的特点是订阅者(listener)负责订阅频道(channel),发送者(publisher)负责向频道发送二进制字符串消息(Binary string message). 每当有消息被发送给频道时,频道的所有订阅者都会收到消息.

### 发布与订阅命令

命令 | 用例和描述
--- | --- |
SUBSCRIBE | SUBSCRIBE channel [channel ...]:订阅给定的一个或多个频道
UNSUBSCRIBE | UNSUBSCRIBE [channel [channel ...]]:退订给定的一个或多个频道,如果执行时没有给定任何频道,那么退订所有频道
PUBLISH | PUBLISH channel message :向给定频道发送消息
PSUBSCRIBE |PSUBSCRIBE pattern [pattern...]:订阅与给定模式相匹配的所有频道
PUNSUBSCRIBE | PUNSUBSCRIBE [pattern [pattern...]]:退订给定的模式,如果执行时没有给定任何模式,那么退订所有模式.

### 排序

redis可以根据某种比较规则对一系列元素进行有序的排列,负责排序的操作SORT命令可以根据字符串,列表,集合,有序集合,散列这5种键里面存储着的数据,对列表,集合以及有序集合进行排序.

命令 | 用例和描述
--- | --- |
SORT | SORT source-key [BY pattern] [LIMIT offset count] [GET pattern [GET pattern ...]] [ASC|DESC] [ALPHA] [STORE dest-key]:根据给定的选项,对输入列表,集合或者有序集合进行排序,然后返回或者存储排序的结果.

### REDIS基本事务

redis的基本事务(basic transaction)需要用到MULTI命令和EXEC命令,这种事务可以让一个客户端在不被其他客户端打断的情况下执行多条命令.redis里面,被multi命令和exec命令包围的所有命令会一个接一个地执行,直到所有命令都执行完毕为止.当一个事务执行完毕之后,redis才会处理其他客户端命令.

### 键的过期时间

redis可以通过过期时间(expiration)特性来让一个键在给定的时限(timeout)之后被自动删除.当我们说一个键"带有生存时间"或者一个键"会在特定时间之后过期(expire)"时,我们指的是Redis会在这个键的过期时间到达时自动删除该键.

命令 | 用例和描述
--- | --- |
PERSIST | PERSIST key-name:移除键的过期时间
TTL | TTL key -name:查看给定键距离过期还有多少秒
EXPIRE | EXPIRE key - name seconds : 让给定键在指定的秒数之后过期
EXPIREAT | EXPIREAT key - name timestamp:将给定键的过期时间设置为给定的unix时间戳
PTTL | PTTL key - name :查看给定键距离过期时间换有多少毫秒.
PEXPIRE | PEXPIRE key - name milliseconds :让给定键在指定的毫秒数之后过期.
PEXPIREAT | PEXPIREAT key - name timestamp-milliseconds :将一个毫秒级精度的unix时间戳设置为给定键的过期时间.

---------
### 事件

Redis服务器是一个事件驱动程序,服务器需要处理一下两类事件

- 文件事件:redis服务器通过套接字与客户端进行连接,而文件事件就是服务器对套接字操作的抽象.服务器与客户端的通信会产生响应的文件事件,而服务器则通过监听并处理这些事件来完成一些列网络通信操作


- 时间事件:redis服务器中的一些操作需要在给定时间点执行,而时间事件就是服务器对这类定时操作的抽象


### 文件事件

redis基于Reactor开发了自己的网络事件处理器:这个处理器被称为文件事件处理器:

- 文件事件处理器使用I/O多路复用程序来同时监听多个套接字,并根据套接字目前执行的任务来为套接字关联不同的事件处理器

- 当被监听的套接字准备好执行连接应答accept,读取read,写入write,关闭操作时,与操作相对应的文件事件就会产生,这时文件事件处理器就会调用套接字之前关联好的事件处理这些事件


### 文件事件处理器
