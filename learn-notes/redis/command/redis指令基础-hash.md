### hash

#### 1. HSET hash field value

````
返回值: 当 HSET 命令在哈希表中新创建 field 域并成功为它设置值时， 命令返回 1 ； 如果域 field 已经存在于哈希表， 并且 HSET 命令成功使用新值覆盖了它的旧值， 那么命令返回 0 。
    
//将哈希表 hash 中域 field 的值设置为 value 。
  
// 如果给定的哈希表并不存在， 那么一个新的哈希表将被创建并执行 HSET 操作。
   
// 如果域 field 已经存在于哈希表中， 那么它的旧值将被新值 value 覆盖。
   
````

#### 2. HGET hash field

````
// HGET 命令在默认情况下返回给定域的值。
  
// 如果给定域不存在于哈希表中， 又或者给定的哈希表并不存在， 那么命令返回 nil 

````

#### 3. HSETNX hash field value

```
返回值: HSETNX 命令在设置成功时返回 1 ， 在给定域已经存在而放弃执行设置操作时返回 0 。
     
// 当且仅当域 field 尚未存在于哈希表的情况下， 将它的值设置为 value 。
   
// 如果给定域已经存在于哈希表当中， 那么命令将放弃执行设置操作。
   
// 如果哈希表 hash 不存在， 那么一个新的哈希表将被创建并执行 HSETNX 命令。
   
```
#### 4. HDEL hash field [field …]
        
````
// 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。

````

#### 5. HLEN hash
        
```
返回值: 哈希表中域的数量。当 key 不存在时，返回 0 

//返回哈希表 key 中域的数量。
  
```

#### 6. HSTRLEN hash field
        
```
返回值: 返回哈希表 key 中， 与给定域 field 相关联的值的字符串长度（string length）。
     
如果给定的键或者域不存在， 那么命令返回 0 。


```

#### 7. HEXISTS hash field

```
返回值: HEXISTS 命令在给定域存在时返回 1 ， 在给定域不存在时返回 0 。
     
检查给定域 field 是否存在于哈希表 hash 当中。

```

#### 8. HINCRBY hash field increment
        
````
// 为哈希表 hash 中的域 field 的值加上增量 increment 。

// 增量也可以为负数,相当于对给定域进行减法操作。

// 如果 hash 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。

// 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0 。

// 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误

````

#### 9. HINCRBYFLOAT hash field increment
        
````
返回值: 执行加法操作之后 field 域的值。
     
//为哈希表 hash 中的域 field 加上浮点数增量 increment 。

//如果哈希表中没有域 field ，那么 HINCRBYFLOAT 会先将域 field 的值设为 0 ，然后再执行加法操作。

//如果键 hash 不存在，那么 HINCRBYFLOAT 会先创建一个哈希表，再创建域 field ，最后再执行加法操作

````

#### 10. HMSET hash field value [field value …]
   
```
返回值: 如果命令执行成功，返回 OK 。

// 同时将多个 field-value (域-值)对设置到哈希表 hash 中。

// 此命令会覆盖哈希表中已存在的域。
   
// 如果 hash 不存在，一个空哈希表被创建并执行 HMSET 操作。

```      

#### 11. HMGET hash field [field …]
         
````
返回值: 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     

// 返回哈希表 hash 中，一个或多个给定域的值。

// 如果给定的域不存在于哈希表，那么返回一个 nil 值。
   
````

#### 12. HKEYS hash
        

```
返回哈希表 hash 中的所有域 fileds

```

#### 13. HVALS hash
         
```
返回哈希表 key 中所有域的值。

```

#### 14. HGETALL hash
         
````
返回值: 返回哈希表 key 中，所有的域和值。
     

````
           








