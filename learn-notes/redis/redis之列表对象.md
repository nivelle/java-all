
### 列表对象

列表对象的编码可以zipList或者linkedList.

1. ziplist编码的列表对象使用压缩列表作为底层实现,每个压缩列表节点(entry)保存了一个列表元素.

![image](http://7xpuj1.com1.z0.glb.clouddn.com/zip%E7%BC%96%E7%A0%81%E7%9A%84list.png)

2. linkedLis编码的列表使用双端链表作为底层实现,每个双端链表节点都保存了一个字符串对象,而每个字符串对象都保存了一个列表元素.

![image](http://7xpuj1.com1.z0.glb.clouddn.com/linkedList%E7%BC%96%E7%A0%81%E7%9A%84list.png)

![image](http://7xpuj1.com1.z0.glb.clouddn.com/%E5%AE%8C%E6%95%B4%E5%AD%97%E7%AC%A6%E4%B8%B2%E8%A1%A8%E7%A4%BA.png)

#### 编码转换

当列表对象同时满足以下两个条件时,使用ziplist编码

- 列表对象保存所有字符串长度都小于64字节
- 列表对象保存的元素数量小于512个;

其余列表对象使用linkedList编码.



![image](http://7xpuj1.com1.z0.glb.clouddn.com/%E5%88%97%E8%A1%A8%E5%AF%B9%E8%B1%A1%E5%91%BD%E4%BB%A4%E5%AE%9E%E7%8E%B0.png)