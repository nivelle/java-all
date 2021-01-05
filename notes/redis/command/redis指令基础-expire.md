### expire


#### 1. expire key seconds

````
返回值: 设置成功返回 1 。 当 key 不存在或者不能为 key 设置生存时间时返回 0

//为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
  
// 生存时间可以通过使用 DEL 命令来删除整个 key 来移除,或者被 SET 和 GETSET 命令覆写(overwrite),
这意味着,如果一个命令只是修改(alter)一个带生存时间的 key 的值而不是用一个新的 key 值来代替(replace)它的话，那么生存时间不会被改变。
   
// RENAME 命令的另一种可能是，尝试将一个带生存时间的 key 改名成另一个带生存时间的 another_key ，这时旧的 another_key (以及它的生存时间)会被删除，然后旧的 key 会改名为 another_key ，因此，新的 another_key 的生存时间也和原本的 key 一样。
   
// 使用 PERSIST 命令可以在不删除 key 的情况下，移除 key 的生存时间，让 key 重新成为一个『持久的』(persistent) key 。

// 可以对一个已经带有生存时间的 key 执行 EXPIRE 命令，新指定的生存时间会取代旧的生存时间。
   

````

#### 2. expireat key timestamp

````
返回值：如果生存时间设置成功，返回 1 ； 当 key 不存在或没办法设置生存时间，返回 0 。
    
// EXPIREAT 的作用和 EXPIRE 类似，都用于为 key 设置生存时间。
   
// 不同在于 EXPIREAT 命令接受的时间参数是 UNIX 时间戳(unix timestamp)。
   
````

####  3. ttl key

````
返回值： 当 key 不存在时，返回 -2 。 

当 key 存在但没有设置剩余生存时间时，返回 -1 。 否则，以秒为单位，返回 key 的剩余生存时间。
     
//以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
  
````

#### 4. persist key

````
// 移除给定 key 的生存时间，将这个 key 从“易失的”(带生存时间 key )转换成“持久的”(一个不带生存时间、永不过期的 key )。

//当生存时间移除成功时，返回 1 . 如果 key 不存在或 key 没有设置生存时间，返回 0 。
  
````

#### 5. pexpire key milliseconds 

````
返回值:设置成功，返回 1 key 不存在或设置失败，返回 0
    
// 这个命令和 EXPIRE 命令的作用类似，但是它以毫秒为单位设置 key 的生存时间，而不像 EXPIRE 命令那样，以秒为单位。

````

#### 6. pexpireat key milliseconds-timestamp 

````
//这个命令和 expireat 命令类似，但它以毫秒为单位设置 key 的过期 unix 时间戳，而不是像 expireat 那样，以秒为单位。
  
//如果生存时间设置成功，返回 1 。 当 key 不存在或没办法设置生存时间时，返回 0 。(查看 EXPIRE key seconds 命令获取更多信息)

````

#### 7. PTTL key

````

返回值：

1. 当 key 不存在时，返回 -2 。

2. 当 key 存在但没有设置剩余生存时间时，返回 -1

3. 否则，以毫秒为单位，返回 key 的剩余生存时间。
   

这个命令类似于 TTL 命令，但它以毫秒为单位返回 key 的剩余生存时间，而不是像 TTL 命令那样，以秒为单位。

````
 

