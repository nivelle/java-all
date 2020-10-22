### String

#### 1. SET key value [EX seconds] [PX milliseconds] [NX|XX]


````
SET key value [EX seconds] [PX milliseconds] [NX|XX]

````

- EX seconds ： 将键的过期时间设置为 seconds 秒。 执行 SET key value EX seconds 的效果等同于执行 SETEX key seconds value

- PX milliseconds ： 将键的过期时间设置为 milliseconds 毫秒。 执行 SET key value PX milliseconds 的效果等同于执行 PSETEX key milliseconds value 。
  
- NX ： 只在键不存在时， 才对键进行设置操作。 执行 SET key value NX 的效果等同于执行 SETNX key value 。
  
- XX ： 只在键已经存在时， 才对键进行设置操作。
  
#### 2. SETNX key value 

```
SETNX key value

```

#### 3. SETEX key seconds value  

````
SETEX key seconds value //将键 key 的值设置为 value ， 并将键 key 的生存时间设置为 seconds 秒钟。

与 SET key value -》 EXPIRE key seconds 等效，但 setex 是原子操作
                          
````

#### 4. PSETEX key milliseconds value


````
PSETEX key milliseconds value

````

#### 5. get

```
如果键 key 不存在， 那么返回特殊值 nil ； 否则， 返回键 key 的值。

如果键 key 的值并非字符串类型， 那么返回一个错误， 因为 GET 命令只能用于字符串值。

```

#### 6. GETSET key value


````

//将键 key 的值设为 value ， 并返回键 key 在被设置之前的旧值。
  

````

#### 7. STRLEN key


```
//返回键 key 储存的字符串值的长度。
  
1. 当键 key 不存在时， 命令返回 0 。

2. 当 key 储存的不是字符串值时， 返回一个错误。
```

#### 8. APPEND key value

````
//返回值：追加之后的value长度

//如果键 key 已经存在并且它的值是一个字符串， APPEND 命令将把 value 追加到键 key 现有值的末尾。
  
//如果 key 不存在， APPEND 就简单地将键 key 的值设为 value ， 就像执行 SET key value 一样。
  

````

#### 9. SETRANGE key offset value


````

// 从偏移量 offset 开始， 用 value 参数覆写(overwrite)键 key 储存的字符串值。
   
// 不存在的键 key 当作空白字符串处理。
   
//SETRANGE 命令会确保字符串足够长以便将 value 设置到指定的偏移量上， 如果键 key 原来储存的字符串长度比偏移量小(比如字符串只有 5 个字符长，但你设置的 offset 是 10 )， 那么原字符和偏移量之间的空白将用零字节(zerobytes, "\x00" )进行填充。
  

````

#### 10. GETRANGE key start end


```

// 返回键 key 储存的字符串值的指定部分， 字符串的截取范围由 start 和 end 两个偏移量决定 (包括 start 和 end 在内)。
   
// 负数偏移量表示从字符串的末尾开始计数， -1 表示最后一个字符， -2 表示倒数第二个字符， 以此类推。
   

```

#### 11. INCR key

```
// 返回值: INCR 命令会返回键 key 在执行加一操作之后的值。
        

//为键 key 储存的数字值加上一。

//如果键 key 不存在， 那么它的值会先被初始化为 0 ， 然后再执行 INCR 命令。

//如果键 key 储存的值不能被解释为数字， 那么 INCR 命令将返回一个错误

//本操作的值限制在 64 位(bit)有符号数字表示之内。


```

#### 12. INCRBY key increment

````

//返回值: 在加上增量 increment 之后， 键 key 当前的值。
       
// 为键 key 储存的数字值加上增量 increment 。
   
// 如果键 key 不存在， 那么键 key 的值会先被初始化为 0 ， 然后再执行 INCRBY 命令。
   
// 如果键 key 储存的值不能被解释为数字， 那么 INCRBY 命令将返回一个错误。
 
// 本操作的值限制在 64 位(bit)有符号数字表示之内。
   

````
#### 13. INCRBYFLOAT key increment

````

//无论加法计算所得的浮点数的实际精度有多长， INCRBYFLOAT 命令的计算结果最多只保留小数点的后十七位

````

#### 13. DECR key

````

返回值: DECR 命令会返回键 key 在执行减一操作之后的值。
     
//为键 key 储存的数字值减去一。

// key 不存在， 那么键 key 的值会先被初始化为 0 ， 然后再执行 DECR 操作。

//如果键 key 储存的值不能被解释为数字， 那么 DECR 命令将返回一个错误。

````

#### 14. DECRBY key decrement

````
返回值：DECRBY 命令会返回键在执行减法操作之后的值。
    

//将键 key 储存的整数值减去减量 decrement 。

//如果键 key 不存在， 那么键 key 的值会先被初始化为 0 ， 然后再执行 DECRBY 命令。

//如果键 key 储存的值不能被解释为数字， 那么 DECRBY 命令将返回一个错误

````


#### 15. MSET key value [key value …]

````
返回值： ok
// 同时设置 多个 key value 

// 如果某个给定键已经存在， 那么 MSET 将使用新值去覆盖旧值

// MSET 是一个原子性(atomic)操作， 所有给定键都会在同一时间内被设置， 不会出现某些键被设置了但是另一些键没有被设置的情况。
      
````

#### 16. MSETNX key value [key value …]

````
返回值: 当所有给定键都设置成功时， 命令返回 1 ； 如果因为某个给定键已经存在而导致设置未能成功执行， 那么命令返回 0 。
     
//当且仅当所有给定键都不存在时， 为所有给定键设置值。
  
// 即使只有一个给定键已经存在， MSETNX 命令也会拒绝执行对所有键的设置操作。
   
// MSETNX 是一个原子性(atomic)操作， 所有给定键要么就全部都被设置， 要么就全部都不设置， 不可能出现第三种状态。
   
````

#### 17. MGET key [key …]

````
返回值: 返回给定的一个或多个字符串键的值。

//如果给定的字符串键里面， 有某个键不存在， 那么这个键的值将以特殊值 nil 表示。
  
````


































