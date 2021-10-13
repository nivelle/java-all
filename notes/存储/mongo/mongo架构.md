
### MongoDB数据文件内部结构

[![gYcUnU.jpg](https://z3.ax1x.com/2021/05/09/gYcUnU.jpg)](https://imgtu.com/i/gYcUnU)

- MongoDB在数据存储上按命名空间来划分,一个Collection是一个命名空间,一个索引也是一个命名空间。

- 同一个命名空间的数据被分成很多个`Extent`，`Extent[程度]`之间使用 __双向链表连接__。

- 在每一个Extent中,保存了具体每一行的数据，这些数据也是通过双向链接来连接的。

- 每一行数据存储空间不仅包括数据占用空间，还可能包含一部分附加空间，这使得在数据Update变大后可以不移动位置。 索引以BTree结构实现。

### 在MongoDB中实现事务

[![gYgn81.jpg](https://z3.ax1x.com/2021/05/09/gYgn81.jpg)](https://imgtu.com/i/gYgn81)

#### 众所周知， MongoDB只支持对单行记录的原子性修改，并不支持对多行数据的原子操作。但是通过上图中的不可思议的操作步骤，实际上你也可以自己实现该事务。 其步骤如下：

- 第1步：先记录一条事务记录，将要修改的多行记录的修改值写到里面，并设置其状态为init（如果这时候操作中断，那么在重新启动时，会判断到它处于init状态，从而将其保存的多行修改操作应用到具体的行上）。

- 第2步：然后更新具体要修改的行，将刚才写的事务记录的标识写到它的**tran**字段中。
  
- 第3步：将事务记录的状态从init变成pending（如果在这时候操作中断，那么在重新启动时，会判断到它的状态是pending，这时查看其所有对应的多条要修改的记录，如果其tran值不为空，那么就进行第4步；如果值为空，说明第4步已经执行过了，直接将其状态从 pending 变成 `committed` 就行）。

- 第4步：将需要修改的多条记录的相应值加以修改，并且`unset`掉之前的`tran字段`。

- 第5步：将事务记录那一条的状态从`pending`变成 `committed`，事务至此完成。 其实上面的步骤并不罕见，在支持事务的DBMS中，其事务原子性提交的保证大多都与上面类似。**而事务记录的tran那条记录，就类似于这些DBMS中的 redolog**。

### MongoDB数据同步

[![gYgIZF.jpg](https://z3.ax1x.com/2021/05/09/gYgIZF.jpg)](https://imgtu.com/i/gYgIZF)

MongoDB采用Replica Sets模式的同步流程

#### 本流程可简要描述如下：

- 红色箭头表示写操作可以写到Primary上，然后异步同步到多个Secondary上。
- 蓝色箭头表示读操作可以从Primary或Secondary任意一个中读取。
- 各个Primary与Secondary之间一直保持心跳同步检测，用于判断Replica Sets的状态。

---------
### 分片机制

[![gYgbGR.jpg](https://z3.ax1x.com/2021/05/09/gYgbGR.jpg)](https://imgtu.com/i/gYgbGR)

- MongoDB的分片是指定一个分片key来进行，数据按范围分成不同的chunk，每个chunk的大小有限制。
- 有多个分片节点保存这些chunk[大块]，每个节点保存一部分的chunk。
- 每一个分片节点都是一个Replica Sets，这样保证数据的安全性。
- 当一个chunk超过其限制的最大体积时，会分裂成两个小的chunk。
- 当chunk在分片节点中分布不均衡时，会引发chunk迁移操作。

### 服务器角色

[![gYfaFg.jpg](https://z3.ax1x.com/2021/05/09/gYfaFg.jpg)](https://imgtu.com/i/gYfaFg)

- 前面讲了分片的机制，下面是具体在分片时几种节点的角色：

- 客户端访问路由节点`mongos`来进行数据读写。

- config服务器保存了两个映射关系，一个是key值的区间对应哪一个chunk的映射关系，另一个是chunk存在哪一个分片节点的映射关系。

- 路由节点通过config服务器获取数据信息，通过这些信息，找到真正存放数据的分片节点进行对应操作。
  
- 路由节点还会在写操作时判断当前chunk是否超出限定大小。如果超出，就分列成两个chunk。
  
- 对于按分片key进行的查询和update操作来说，路由节点会查到具体的chunk然后再进行相关的工作。
  
- 对于不按分片key进行的查询和update操作来说，mongos会对所有下属节点发送请求然后再对返回结果进行合并。
