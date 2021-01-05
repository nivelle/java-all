### list

#### 1. LPUSH key value [value …]

```
返回值: 执行 LPUSH 命令后，列表的长度。
     
// 将一个或多个值 value 插入到列表 key 的表头

// 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。
  
```

#### 2. LPUSHX key value

```
返回值: 执行 LPUSH 命令后，列表的长度。
     
// 将一个或多个值 value 插入到列表 key 的表头
   
// 和 LPUSH key value [value …] 命令相反，当 key 不存在时， LPUSHX 命令什么也不做。
   
```
#### 3. RPUSH key value [value …]

```
返回值： 执行 RPUSH 操作后，表的长度。
     
// 将一个或多个值 value 插入到列表 key 的表尾(最右边)。
   
// 如果 key 不存在，一个空列表会被创建并执行 RPUSH 操作。
   


```
#### 4. RPUSHX key value

````
返回值: RPUSHX 命令执行之后，表的长度。
     

//将值 value 插入到列表 key 的表尾，当且仅当 key 存在并且是一个列表。
  
````
#### 5. LPOP key

````
返回值： 列表的头元素。 当 key 不存在时，返回 nil 。
     
// 移除并返回列表 key 的头元素。
  
````

#### 6. RPOP key

````
返回值： 列表的尾元素。 当 key 不存在时，返回 nil 。
     
// 移除并返回列表 key 的尾元素。
  
````

#### 7. RPOPLPUSH source destination

```
返回值: 被弹出的元素

//将列表 source 中的最后一个元素(尾元素)弹出，并返回给客户端。

// 将 source 弹出的元素插入到列表 destination ，作为 destination 列表的的头元素。
   
// 如果 source 不存在，值 nil 被返回，并且不执行其他动作。
   
// 如果 source 和 destination 相同，则列表中的表尾元素被移动到表头，并返回该元素，可以把这种特殊情况视作列表的旋转(rotation)操作。
   

```

#### 8. LREM key count value

```
返回值: 被移除元素的数量。 因为不存在的 key 被视作空表(empty list)，所以当 key 不存在时， LREM 命令总是返回 0 。
     
// 根据参数 count 的值，移除列表中与参数 value 相等的元素。
   
// count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。
   
// count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
   
// count = 0 : 移除表中所有与 value 相等的值。
   
```

#### 9. LLEN key

````
返回值: 返回列表 key 的长度。
     
// 如果 key 不存在，则 key 被解释为一个空列表，返回 0 .

// 如果 key 不是列表类型，返回一个错误。
   
````

#### 10. LINDEX key index

````
返回值: 列表中下标为 index 的元素。 如果 index 参数的值不在列表的区间范围内(out of range)，返回 nil 。
     
//下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。

//你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
  
````

#### 11. LINSERT key BEFORE|AFTER pivot value

````
返回值: 如果命令执行成功，返回插入操作完成之后，列表的长度。 如果没有找到 pivot ，返回 -1 。 如果 key 不存在或为空列表，返回 0 。
     
//将值 value 插入到列表 key 当中，位于值 pivot 之前或之后。
  
// 当 pivot 不存在于列表 key 时，不执行任何操作。
   
// 当 key 不存在时， key 被视为空列表，不执行任何操作。
   
````

#### 12. LSET key index value

````
返回值: 操作成功返回 ok ，否则返回错误信息。
    
//将列表 key 下标为 index 的元素的值设置为 value 。

//当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回一个错误。

````

#### 13. LRANGE key start stop

````
//返回列表 key 中指定区间内的元素，区间以偏移量 start 和 stop 指定。
  
//下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。

````

#### 14. LTRIM key start stop

````
//对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
  
//超出范围的下标值不会引起错误。
  
//如果 start 下标比列表的最大下标 end ( LLEN list 减去 1 )还要大，或者 start > stop ， LTRIM 返回一个空列表(因为 LTRIM 已经将整个列表清空)。
  
// 如果 stop 下标比 end 下标还要大，Redis将 stop 的值设置为 end 。
   
````

#### 15. BLPOP key [key …] timeout

```
// 它是 LPOP key 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BLPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
   
// 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的头元素。

```

- 非阻塞行为: 当 BLPOP 被调用时，如果给定 key 内至少有一个非空列表，那么弹出遇到的第一个非空列表的头元素，并和被弹出元素所属的列表的名字一起，组成结果返回给调用者。

- 阻塞行为: 如果所有给定 key 都不存在或包含空列表，那么 BLPOP 命令将阻塞连接，直到等待超时，或有另一个客户端对给定 key 的任意一个执行 LPUSH key value [value …] 或 RPUSH key value [value …] 命令为止。
        
#### 16. BRPOP key [key …] timeout

````
// 它是 RPOP key 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BRPOP 命令阻塞，直到等待超时或发现可弹出元素为止。
         
// 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的尾部元素。

````

#### 17. BRPOPLPUSH source destination timeout

```
返回值: 假如在指定时间内没有任何元素被弹出，则返回一个 nil 和等待时长。 反之，返回一个含有两个元素的列表，第一个元素是被弹出元素的值，第二个元素是等待时长。
     
//当列表 source 为空时， BRPOPLPUSH 命令将阻塞连接，直到等待超时，或有另一个客户端对 source 执行 LPUSH key value [value …] 或 RPUSH key value [value …] 命令为止。
  
// 超时参数 timeout 接受一个以秒为单位的数字作为值。超时参数设为 0 表示阻塞时间可以无限期延长(block indefinitely) 。
   

```