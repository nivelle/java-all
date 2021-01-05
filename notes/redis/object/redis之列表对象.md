#### 列表对象

列表对象的编码可以zipList或者linkedList.

1. ziplist 编码的列表对象使用压缩列表作为底层实现,每个压缩列表节点(entry)保存了一个列表元素.

2. linkedLis 编码的列表使用双端链表作为底层实现,每个双端链表节点都保存了一个字符串对象,而每个字符串对象都保存了一个列表元素.

#### 编码转换

##### 当列表对象同时满足以下两个条件时,使用 ziplist 编码:

- 列表对象保存所有字符串长度都小于64字节

- 列表对象保存的元素数量小于512个;

其余列表对象使用linkedList编码.但是这些限制值是可以配置的：

#### list-max-ziplist-value 和 list-max-ziplist-entries 


### 考虑到链表的附加空间相对太高，prev 和 next 指针就要占去 16 个字节 (64bit 系统的指针是 8 个字节)，另外每个节点的内存都是单独分配，会加剧内存的碎片化，影响内存管理效率。

[![DFx8G8.png](https://s3.ax1x.com/2020/11/16/DFx8G8.png)](https://imgchr.com/i/DFx8G8)

- 压缩深度

quickList 默认的压缩深度是0，也就是不压缩，压缩的深度实际由配置参数：list-compress-depth决定

- ziplist长度

quickList内部默认单个ziplist长度为8k字节，超出这个字节，就会新起一个ziplist

ziplist的长度由参数配置 list-max-ziplist-size 决定


























