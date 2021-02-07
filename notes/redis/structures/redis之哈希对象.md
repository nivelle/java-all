

### 哈希对象

哈希对象的编码可以是ziplist或者hashtable.

ziplist编码的哈希对象使用压缩列表作为底层实现,每当有新的键值对要加入到哈希对象时,程序会先将保存了建的压缩列表节点推入到压缩列表表尾,然后再将保存了值的压缩列表节点推入到压缩列表表尾:

- 保存了同一键值对的两个节点总是紧挨在一起,保存键的节点在前,保存值的节点在后
- 先添加到哈希对象中的键值对会被方在压缩列表的表头方向,而后添加到哈希对象总的键值对会被放在压缩列表的表尾方向


1. 如果profile键的值对象使用的是ziplust编码,那么这个值对象将会是如下所示:

![image](http://7xpuj1.com1.z0.glb.clouddn.com/zip%E7%BC%96%E7%A0%81%E7%9A%84%E5%93%88%E5%B8%8C%E5%AF%B9%E8%B1%A1.png)

![image](http://7xpuj1.com1.z0.glb.clouddn.com/hash%E5%AF%B9%E8%B1%A1%E5%8E%8B%E7%BC%A9%E5%88%97%E8%A1%A8%E7%9A%84%E5%BA%95%E5%B1%82%E5%AE%9E%E7%8E%B0.png)

2. hashtable编码的哈希对象使用字典作为底层实现,哈希对象中的每个键值对都使用一个字典键值对来保存:

- 字典的每个键都是一个字符串对象,对象中保存了键值对的键;
- 字典的每个值都是一个字符串对象,对象中保存了键值对的值.

![image](http://7xpuj1.com1.z0.glb.clouddn.com/hash%E5%AF%B9%E8%B1%A1%E5%BA%95%E5%B1%82%E7%BB%93%E6%9E%84.png)

#### 编码转换

当哈希对象可以同时满足两个条件时,哈希对象使用ziplist编码:

- 哈希对象保存的所有键值对的键和值的字符串长度都小于64字节
- 哈希对象保存的键值对数量小于512个;不能满足这两个条件的哈希对象需要使用hashtable编码

**这两个条件的上限值是可以修改,具体hash-max-ziplist-value和hash-max-ziplist-entries**

对于使用ziplist编码的列表对象来说,当使用ziplist编码所需的两个条件的任意一个不能被满足时,对象的编码转换操作会被执行,原本保存在压缩列表里面的所有键值对都会被转义并保存到字典里面,对象的编码也会从ziplist变为hashtable.


**若包含的键值对数量过多也会引起的编码转换**

![image](http://7xpuj1.com1.z0.glb.clouddn.com/hash%E5%91%BD%E4%BB%A4%E7%9A%84%E5%AE%9E%E7%8E%B0.png)