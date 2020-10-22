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
  


```
