 ### setNX实现原理

- setGenericCommand 方法实现

````C
//setGenericCommand()函数是以下命令: SET, SETEX, PSETEX, SETNX.的最底层实现
//flags 可以是NX或XX，由上面的宏提供
//expire 定义key的过期时间，格式由unit指定
//ok_reply和abort_reply保存着回复client的内容，NX和XX也会改变回复
//如果ok_reply为空，则使用 "+OK"
//如果abort_reply为空，则使用 "$-1"
void setGenericCommand(client *c, int flags, robj *key, robj *val, robj *expire, int unit, robj *ok_reply, robj *abort_reply) {
    long long milliseconds = 0; /* initialized to avoid any harmness warning */

    if (expire) { //判断过期时间是否有效
        if (getLongLongFromObjectOrReply(c, expire, &milliseconds, NULL) != C_OK)
            return;
        if (milliseconds <= 0) { //<0返回错误
            addReplyErrorFormat(c,"invalid expire time in %s",c->cmd->name);
            return;
        }
        if (unit == UNIT_SECONDS) milliseconds *= 1000; //如果单位是秒，转化为毫秒
    }

    //lookupKeyWrite函数是为执行写操作而取出key的值对象
    //如果设置了NX(不存在)，并且在数据库中 找到 该key，或者
    //设置了XX(存在)，并且在数据库中 没有找到 该key
    //回复abort_reply给client
    if ((flags & OBJ_SET_NX && lookupKeyWrite(c->db,key) != NULL) ||
        (flags & OBJ_SET_XX && lookupKeyWrite(c->db,key) == NULL))
    {
        addReply(c, abort_reply ? abort_reply : shared.nullbulk);
        return;
    }
    setKey(c->db,key,val);
    server.dirty++;
    if (expire) setExpire(c,c->db,key,mstime()+milliseconds);
    notifyKeyspaceEvent(NOTIFY_STRING,"set",key,c->db->id);
    if (expire) notifyKeyspaceEvent(NOTIFY_GENERIC,
        "expire",key,c->db->id);
    addReply(c, ok_reply ? ok_reply : shared.ok);
}

robj *lookupKeyWrite(redisDb *db, robj *key) {
    expireIfNeeded(db,key); //查看key是否过期
    return lookupKey(db,key,LOOKUP_NONE); //取出key值，核心实现
}

robj *lookupKey(redisDb *db, robj *key, int flags) {
    dictEntry *de = dictFind(db->dict,key->ptr);// 在字典中根据key查找字典对象
    if (de) {
        robj *val = dictGetVal(de);// 获取字典对象的值

        /* Update the access time for the ageing algorithm.
         * Don't do it if we have a saving child, as this will trigger
         * a copy on write madness. *//* 更新key的最新访问时间 */
        if (server.rdb_child_pid == -1 &&
            server.aof_child_pid == -1 &&
            !(flags & LOOKUP_NOTOUCH))
        {
            if (server.maxmemory_policy & MAXMEMORY_FLAG_LFU) {
                unsigned long ldt = val->lru >> 8;
                unsigned long counter = LFULogIncr(val->lru & 255);
                val->lru = (ldt << 8) | counter;
            } else {
                val->lru = LRU_CLOCK();
            }
        }
        return val;
    } else {
        return NULL;
    }
}

````

根据源码分析，setnx 命令并没有加锁，也没有必要加锁，因为redis是单线程

### 问题1：codis是如何保证setnx名字原子执行的

codis主要是执行转发操作的，一个key值只能存在一台机器上，根据hash值索引，因此可以保证原子性

### 问题2：在代码中执行加锁时，使用以下写法是否有问题

````java
SETNX key value
EXPIRE key 30
````

有问题的，对于客户端来说这是两种操作，如果执行第一步时出现了问题，就会导致key永久存储

redis 提供了 SET key value NX PX 过期值

这个命令保证原子执行

### 问题 3，释放锁时怎么保证原子操作

````lua
if redis.call("get",KEY) == val then
    return redis.call("del",KEY)
else
    return 0
end
````
redis支持lua脚本，lua是一个轻量级的保证原子性操作的。

问题4：为什么lua能保证原子性

简单的说单线程，这种表述不是很准确

实现原子性的几个选择：

- 单线程 redis

- 用一个master管理需求的分配 memcached

- 多个进程之间抢锁 nginx
