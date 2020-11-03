

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