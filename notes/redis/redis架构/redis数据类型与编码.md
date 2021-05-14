![redis数据类型的底层实现.jpg](https://i.loli.net/2021/05/14/dH3zLlnAsOqcJXQ.jpg)

### 对象的类型和编码

- redis将键和值都封装成了了对象 redisObject，对象中有当前存储的数据(键或者值)的类型，编码，以及存储他们用到的数据结构的指针。

```
比如一个字符串键，redis会创建两个对象，一个是用作字符串键的键，另一个对象是用作字符串键的值。
```

#### redis对象数据结构

````
typedef struct redisObject {
    // 类型
    unsigned type:4;
    
    // 编码
    unsigned encoding:4;
    
    // 指向底层实现数据结构的指针
    void *ptr;
} robj;

````

##### type 类型

- 记录当前对象的类型:

1. REDIS_STRING //字符串对象
2. REDIS_LIST //列表对象
3. REDIS_HASH //哈希对象
4. REDIS_SET //集合对象
5. REDIS_ZSET //有序集合对象

- 对于redis保存的键值对来讲，键总是一个字符串对象，而值则可能是字符串对象，列表对象等其中的任意一个。

- 当我们称呼一个数据库键为 “字符串键”时，我们指的是“这个数据键所对应的值是字符串对象”;当称呼一个键为 “列表键”时，我们值的是“这个数据库键所对应的值为列表对象”

- 对一个数据库键执行 TYPE 命令时， 命令返回的结果为数据库键对应的值对象的类型， 而不是键对象的类型

````

127.0.0.1:6379> set test 1
OK
127.0.0.1:6379> type test
string

````

##### encoding 编码和底层实现

encoding代表了存储当前值所使用的数据结构。encoding翻译是编码，就是类似于传统编码，数据是二进制文件，看用何种编码方式打开，redis对应的编码方式就是实现他们的数据结构

- 每种类型的对象都至少使用了两种不同的编码

[![DFrbkT.png](https://i.loli.net/2021/05/14/ugYctw4N5dRzXMJ.png)](https://i.loli.net/2021/05/14/ugYctw4N5dRzXMJ.png)

- 使用 OBJECT ENCODING 命令可以查看一个数据库键的值对象的编码

````
127.0.0.1:6379> set test "1233"
OK
127.0.0.1:6379> object encoding test
"int"
127.0.0.1:6379> set test "nivelle"
OK
127.0.0.1:6379> object encoding test
"embstr"
127.0.0.1:6379> set test "dkdkdkkdkdkkdkdkkdkdkdkdkkdkdkdkkdkdkdkdkdkdkdkkdkdkdkkdkskdkksdksdededededededededeeeeeeeeeeeeeeeeejdjjjjjjdjdjjdjdjdjdjdjjsllkkfdlflsdfsdajfasdhfjasdfhsadfas"
OK
127.0.0.1:6379> object encoding test
"raw"

````

![encoding编码.png](https://i.loli.net/2021/05/14/oGLih1rTkjnOV2H.png)

----------------

### 字符串对象

- 字符串对象的编码是 **int 、 raw 或者 embstr** 。

1. 如果一个字符串对象保存的是整数值， 并且这个整数值可以用 long 类型来表示， 那么字符串对象会将整数值保存在字符串对象结构的 ptr 属性里面（将 void* 转换成 long ）， 并将字符串对象的编码设置为 int 。

2. 如果字符串对象保存的是一个字符串值， 并且这个字符串值的长度**大于 39 字节**， 那么字符串对象将使用一个简单动态字符串（SDS）来保存这个字符串值， 并将对象的编码设置为 raw 。

![string类型raw实现.png](https://i.loli.net/2021/05/14/wNXphUqTcM1fQvB.png)

3. embstr 编码是专门用于保存短字符串的一种优化编码方式， 这种编码和 raw 编码一样， 都使用 redisObject 结构和 sdshdr 结构来表示字符串对象， 但 raw 编码会调用两次内存分配函数来分别创建
   redisObject 结构和 sdshdr 结构， 而 embstr 编码则通过调用一次内存分配函数来分配一块连续的空间， 空间中依次包含 redisObject 和 sdshdr 两个结构

![string类型之embstr.png](https://i.loli.net/2021/05/14/1owU4bjmrXRn6Ge.png)

- embstr 编码的字符串对象在执行命令时,产生的效果和 raw 编码的字符串对象执行命令时产生的效果是相同的,但使用 embstr 编码的字符串对象来保存短字符串值有以下好处：

（1).embstr 编码将创建字符串对象所需的内存分配次数从 raw 编码的两次降低为一次。

（2).释放 embstr 编码的字符串对象只需要调用一次内存释放函数， 而释放 raw 编码的字符串对象需要调用两次内存释放函数

（3).因为 embstr 编码的字符串对象的所有数据都保存在一块连续的内存里面， 所以这种编码的字符串对象比起 raw 编码的字符串对象能够更好地利用缓存带来的优势

4. 可以用 long double 类型表示的浮点数在 Redis 中也是作为字符串值来保存的： 如果我们要保存一个浮点数到字符串对象里面， 那么程序会先将这个浮点数转换成字符串值， 然后再保存起转换所得的字符串

````
127.0.0.1:6379> set pi 2.0
OK
127.0.0.1:6379> object encoding pi
"embstr"

````

#### 字符串编码总结

![字符串对象保存值类型编码.png](https://i.loli.net/2021/05/14/yKBYQNlbHiEDRp8.png)

#### string 字符串命令实现

![字符串指令底层实现.png](https://i.loli.net/2021/05/14/trYBZ2cTL4359iX.png)

------------------

### 列表对象

- 列表对象的编码可以zipList或者linkedList.

1. ziplist 编码的列表对象使用压缩列表作为底层实现,每个压缩列表节点(entry)保存了一个列表元素.

2. linkedLis 编码的列表使用双端链表作为底层实现,每个双端链表节点都保存了一个字符串对象,而每个字符串对象都保存了一个列表元素.

- **list-max-ziplist-value** 和 **list-max-ziplist-entries**

#### 使用ziplist编码实现

````
127.0.0.1:6379> rpush numbers 1 "three" 5
(integer) 3

````

[![yNfq9e.md.png](https://z3.ax1x.com/2021/02/07/yNfq9e.md.png)](https://imgtu.com/i/yNfq9e)

#### 使用linkedlist编码实现

![list对象双向列表实现.png](https://i.loli.net/2021/05/14/ch1mNMTC4Gx2V3L.png)

#### 最新列表底层实现:quicklist编码实现

- 考虑到链表的附加空间相对太高，prev 和 next 指针就要占去 16 个字节 (64bit 系统的指针是 8 个字节)，另外每个节点的内存都是单独分配，会加剧内存的碎片化，影响内存管理效率。

![list列表quicklist实现.png](https://i.loli.net/2021/05/14/4RDJ6tAcXxhmPBw.png)

- 压缩深度

(1). quickList 默认的压缩深度是0,也就是不压缩,压缩的深度实际由配置参数: **list-compress-depth**决定

(2). 为了支持快速的 push/pop 操作，quicklist 的首尾两个 ziplist 不压缩，此时深度就是 1。

(3). 如果深度为 2，就表示 quicklist 的首尾第一个 ziplist 以及首尾第二个 ziplist 都不压缩

- ziplist长度

(1). quickList内部默认单个ziplist长度为**8k字节**，超出这个字节，就会新起一个ziplist ；

(2). ziplist的长度由参数配置 **list-max-ziplist-size** 决定

#### 列表命令的实现

![列表命令实现.png](https://i.loli.net/2021/02/08/pQRC7OF8vbYXzAr.png)

-----------------

### hash对象

- 哈希对象的编码可以是**ziplist或者hashtable**.

- ziplist编码的哈希对象使用压缩列表作为底层实现,每当有新的键值对要加入到哈希对象时,程序会先将保存了建的压缩列表节点推入到压缩列表表尾,然后再将保存了值的压缩列表节点推入到压缩列表表尾:

(1). 保存了同一键值对的两个节点总是紧挨在一起,保存键的节点在前,保存值的节点在后
(2). 先添加到哈希对象中的键值对会被方在压缩列表的表头方向,而后添加到哈希对象总的键值对会被放在压缩列表的表尾方向

1. 如果profile键的值对象使用的是 ziplist 编码,那么这个值对象将会是如下所示:

````
127.0.0.1:6379> hset profile name "tom"
(integer) 1
127.0.0.1:6379> hset profile age 25
(integer) 1
127.0.0.1:6379> hset profile career "javaer"
(integer) 1
127.0.0.1:6379> object encoding profile
"ziplist"
127.0.0.1:6379> 

````

[![yU9Dk8.png](https://s3.ax1x.com/2021/02/08/yU9Dk8.png)](https://imgchr.com/i/yU9Dk8)

2. hashtable编码的哈希对象使用字典作为底层实现,哈希对象中的每个键值对都使用一个字典键值对来保存:

- 字典的每个键都是一个字符串对象,对象中保存了键值对的键;
- 字典的每个值都是一个字符串对象,对象中保存了键值对的值.

[![yU9H1J.png](https://s3.ax1x.com/2021/02/08/yU9H1J.png)](https://imgchr.com/i/yU9H1J)

#### 编码转换

当哈希对象可以同时满足两个条件时,哈希对象使用ziplist编码:

- 哈希对象保存的所有键值对的键和值的字符串长度都小于64字节

- 哈希对象保存的键值对数量小于512个;不能满足这两个条件的哈希对象需要使用hashtable编码

- 这两个条件的上限值是可以修改,具体**hash-max-ziplist-value和hash-max-ziplist-entries**

-
对于使用ziplist编码的列表对象来说,当使用ziplist编码所需的两个条件的任意一个不能被满足时,对象的编码转换操作会被执行,原本保存在压缩列表里面的所有键值对都会被转义并保存到字典里面,对象的编码也会从ziplist变为hashtable.

- **若包含的键值对数量过多也会引起的编码转换**

````
127.0.0.1:6379> hset profile career "https://cn.bing.com/search?q=64%E4%B8%AA%E5%AD%97%E6%AF%8D+abcd&qs=n&form=QBRE&sp=-1&pq=64%E4%B8%AA%E5%AD%97%E6%AF%8D+abcd&sc=0-10&sk=&cvid=97CEFA7896F7414FA6A0ABC10171608A"
(integer) 0
127.0.0.1:6379> object profile
(error) ERR Unknown subcommand or wrong number of arguments for 'profile'. Try OBJECT HELP.
127.0.0.1:6379> object encoding profile
"hashtable"

````

#### hash命令实现

[![yUCijA.md.png](https://s3.ax1x.com/2021/02/08/yUCijA.md.png)](https://imgchr.com/i/yUCijA)

------------------

### 集合对象

- 集合对象的编码可以是intset或者hashtable

- intset编码的集合对象使用整数集合作为底层实现,集合对象包含的所有元素都被保存在整数集合里面.

``````

127.0.0.1:6379> sadd numbers 1 3 5 
(integer) 3
127.0.0.1:6379> 

``````

[![yUPeq1.png](https://s3.ax1x.com/2021/02/08/yUPeq1.png)](https://imgchr.com/i/yUPeq1)

- hashtable编码的集合对象使用字典作为底层实现,字典的每个键都是一个字符串对象,每个字符串对象包含了一个集合元素,而字典的值则全部被设置为null

``````
127.0.0.1:6379> sadd numbers 1 3 5 5.00
(integer) 1
127.0.0.1:6379> type numbers
set
127.0.0.1:6379> object encoding numbers
"hashtable"
127.0.0.1:6379> 

``````

### 编码的转换

当集合对象可以同时满足以下两个条件时,对象使用intset编码:

- 集合对象保存的所有元素是整数、

- 集合对象保存的元素数量不超过512个

- 其余均使用hashtable编码.对于使用intset编码的集合对象来说,当任意一个条件不能满足时,将会执行转码操作,转换为字典保存,若向只包含整数元素的集合对象添加一个字符串元素,集合短信的编码转移操作就会被执行.

#### 命令实现

[![yUih7t.png](https://s3.ax1x.com/2021/02/08/yUih7t.png)](https://imgchr.com/i/yUih7t)


-----------------

### 有序集合对象

- 有序集合的编码可以是ziplist或者skiplist

- ziplist编码的压缩列表对象使用压缩列表作为底层实现,每个集合元素使用两个紧挨在一起的压缩列表节点来保存,第一个节点保存元素的成员,第二个元素则保存元素的分值.

  压缩列表内的集合元素按照分值从小到大排序,分值小的元素放置在靠近表头的方向,而分值较大的元素则被放置在靠近表尾的方向.

```
127.0.0.1:6379> zadd price 8.5 apple 5.0 banana 6.0 cherry
(integer) 3
127.0.0.1:6379> 

```

[![yUFEH1.md.png](https://s3.ax1x.com/2021/02/08/yUFEH1.md.png)](https://imgchr.com/i/yUFEH1)

- **skiplist编码的有序集合对象使用zset结构作为底层实现,一个zset结构同时也包含一个字典和一个跳跃表"**

```
typedef struct zset{
    zskiplist *zsl;
    dict * dict;
}zset;


```

![有序集合底层数据结构.png](https://i.loli.net/2021/05/13/4j5i9ONCRDecA8o.png)

- zset结构中zsl跳跃表按照分值大小保存了所有集合元素,每个跳跃表节点都保存了一个集合元素:跳跃表节点的object属性保存了元素的成员,而跳跃表节点的score属性则保存了元素的分值. 通过这个跳跃表，
  程序可以对有序集合进行范围型操作， 比如 ZRANK 、 ZRANGE 等命令就是基于跳跃表 API 来实现的。

- zset结构中的dict字典为有序集合创建了一个从成员到分值的映射,字典中的每个键值对都保存了一个集合元素:字典的键保存了元素的成员,而字典的值则保存了元素的分值。 通过这个字典， 程序可以用 O(1) 复杂度查找给定成员的分值，
  ZSCORE 命令就是根据这一特性实现的， 而很多其他有序集合命令都在实现的内部用到了这一特性

- 有序集合的每个元素的成员都是一个字符串对象,而每个元素的分值都是一个double类型的浮点数.虽然zset结构同时使用跳跃表和字典来保存有序集合元素,但是这两种数据结构都会通过指针来共享元素的成员和分值,所以同时使用跳跃表和字典来保存集合元素不会产生任何重复成员或者分值,也不会因此浪费额外的内存.

````
为什么有序集合需要同时使用跳跃表和字典来实现？

在理论上来说， 有序集合可以单独使用字典或者跳跃表的其中一种数据结构来实现， 但无论单独使用字典还是跳跃表， 在性能上对比起同时使用字典和跳跃表都会有所降低。

举个例子， 如果我们只使用字典来实现有序集合， 那么虽然以 O(1) 复杂度查找成员的分值这一特性会被保留， 但是， 因为字典以无序的方式来保存集合元素， 所以每次在执行范围型操作 —— 比如 ZRANK 、 ZRANGE 等命令时， 程序都需要对字典保存的所有元素进行排序， 完成这种排序需要至少 O(N \log N) 时间复杂度， 以及额外的 O(N) 内存空间 （因为要创建一个数组来保存排序后的元素）。

另一方面， 如果我们只使用跳跃表来实现有序集合， 那么跳跃表执行范围型操作的所有优点都会被保留， 但因为没有了字典， 所以根据成员查找分值这一操作的复杂度将从 O(1) 上升为 O(\log N) 。

因为以上原因， 为了让有序集合的查找和范围型操作都尽可能快地执行， Redis 选择了同时使用字典和跳跃表两种数据结构来实现有序集合。


````

- 在实际中， 字典和跳跃表会共享元素的成员和分值， 所以并不会造成任何数据重复， 也不会因此而浪费任何内存。

````
127.0.0.1:6379> zadd price 8.5 apple 5.0 banana 6.0 cherry
(integer) 3
127.0.0.1:6379> object encoding price
"ziplist"
127.0.0.1:6379> zadd price 9.0 "https://cn.bing.com/search?q=%E6%9C%89%E5%BA%8F%E9%9B%86%E5%90%88%E7%BC%96%E7%A0%81%E8%BD%AC%E6%8D%A2&qs=n&form=QBLH&sp=-1&pq=%E6%9C%89%E5%BA%8F%E9%9B%86%E5%90%88%E7%BC%96%E7%A0%81&sc=0-6&sk=&cvid=1568FAD541EA4929A417BFBFED9541C1"
(integer) 1
127.0.0.1:6379> object encoding price
"skiplist"
127.0.0.1:6379> 


````

#### 编码的转换

当有序集合对象可以同时满足以下条件时,对象使用ziplist编码:

- 有序集合保存的元素数量小于128个
- 有序集合保存的所有元素成员的长度都小于64字节

#### 有序集合命令实现

[![yUAw0s.png](https://s3.ax1x.com/2021/02/08/yUAw0s.png)](https://imgchr.com/i/yUAw0s)










