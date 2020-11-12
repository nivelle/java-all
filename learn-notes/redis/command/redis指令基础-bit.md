### 位图

#### 1. setbit key offset value

````
返回值：指定偏移量原来储存的位。
    
// 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。
   
//位的设置或清除取决于 value 参数，可以是 0 也可以是 1 。
  
//当 key 不存在时，自动生成一个新的字符串值。
  
//字符串会进行伸展(grown)以确保它可以将 value 保存在指定的偏移量上。当字符串值进行伸展时，空白位置以 0 填充。
  
//offset 参数必须大于或等于 0 ，小于 2^32 (bit 映射被限制在 512 MB 之内)。
  
````

#### 2. getbit key offset

````
返回值: 字符串值指定偏移量上的位(bit)。
     
// 对 key 所储存的字符串值，获取指定偏移量上的位(bit)。
  
// 当 offset 比字符串值的长度大，或者 key 不存在时，返回 0 。
   
````

#### 3. bitcount key [start] [end]

````
返回值: 被设置为 1 的位的数量。
     

// 计算给定字符串中，被设置为 1 的比特位的数量。
   
// 一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 start 或 end 参数，可以让计数只在特定的位上进行。
   
//start 和 end 参数的设置和 GETRANGE key start end 命令类似，都可以使用负数值： 比如 -1 表示最后一个字节， -2 表示倒数第二个字节，以此类推。
  
````

#### 4. BITPOS key bit [start] [end]
````
// 返回位图中第一个值为 bit 的二进制位的位置。

// 在默认情况下， 命令将检测整个位图， 但用户也可以通过可选的 start 参数和 end 参数指定要检测的范围。
   
````

#### 5. BITOP operation destkey key [key …]

````
返回值: 保存到 destkey 的字符串的长度，和输入 key 中最长的字符串长度相等。
     
// 对一个或多个保存二进制位的字符串 key 进行位元操作，并将结果保存到 destkey 上。
  
// 除了 NOT 操作之外，其他操作都可以接受一个或多个 key 作为输入。
   
````
#####  operation 可以是 AND 、 OR 、 NOT 、 XOR 这四种操作中的任意一种：

- BITOP AND destkey key [key ...] ，对一个或多个 key 求逻辑并，并将结果保存到 destkey 。
  
- BITOP OR destkey key [key ...] ，对一个或多个 key 求逻辑或，并将结果保存到 destkey 。
  
- BITOP XOR destkey key [key ...] ，对一个或多个 key 求逻辑异或，并将结果保存到 destkey 。
  
- BITOP NOT destkey key ，对给定 key 求逻辑非，并将结果保存到 destkey 。
  
