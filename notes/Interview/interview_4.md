### 一面
- 1.开始是自我介绍；
- 2.HashMap的实现原理，什么是hash碰撞，怎样解决hash碰撞？
- 3.ConcurrentHashMap的原理，与HashTable的区别？
- 4.HashSet和TreeSet的区别以及底层实现原理
- 5.HashMap中存key-value，value有重复但是都是Comparable类型可比较；怎样根据value排序此集合，介绍实现方法
- 6.ReentrantLock和synchronized关键字有什么区别？
- 7.synchronized 修饰static方法，具体锁的是什么？
- 8.工作当中cpu和内存异常排查方法；详细说明分析过程及定位解决方式
- 然后就是各种基础，jvm内存模型，nio，bio，aio，高并发，sychronized和volltail，HashMap，数据结构和扩容；
- 还有一些场景题目，大并发/海量数量的情况下，怎么设计系统。从里面拿出两点来问，一个是系统解耦，一个是分库分表；
- 最后一个是编码题，HashMap里key是自定义对象的情况，排序
- jvm问的比较多，线上发版如何做到分批发的，redis命令，数据结构，数据库内部锁机制，线上问题解决，sql优化等等;

### 二面
- 1.ClassLoader的原理,举出应用场景及工作实例，介绍类加载过程及工作中的应用
- 2.HashMap的实现原理，什么是hash碰撞，怎样解决hash碰撞？
- 3.ConcurrentHashMap的原理，与HashTable的区别？
- 4.HashSet和TreeSet的区别以及底层实现原理
- 5.HashMap中存key-value，value有重复但是都是Comparable类型可比较；怎样根据value排序此集合，介绍实现方法
- 6.ReentrantLock和synchronized关键字有什么区别？
- 7.synchronized 修饰static方法，具体锁的是什么？
- 8.工作当中cpu和内存异常排查方法；详细说明分析过程及定位解决方式
- 9.一个jvm的原理及优化；
- 10.sql的优化；
- 11.现在使用的框架原理，比如使用了dubbo，会问dubbo的原理，还有h5怎么调用dubbo等；
- 12.接着是讲项目，项目里的问题比较简单；
- 13.然后就是各种基础，jvm内存模型，nio，bio，aio，高并发sychronized和volltail，HashMap，数据结构和扩容；
- 14.还有一些场景题目，大并发/海量数量的情况下，怎么设计系统。从里面拿出两点来问，一个是系统解耦，一个是分库分表；
- 15.最后一个是编码题，HashMap里key是自定义对象的情况，排序

### 三面
- 1、executor service实现的方法，可以设置的参数；
- 2、出了个算法提，找出链表中倒数第n个节点；
- 3、还问了thread和runable的区别；
- 4、聚簇索引是什么；
- 5、redis问了一个实际问题的解决办法，如果redis一个value特别大，有什么解决方案；
- 6、redis内存淘汰机制；
- 7、mysql的默认隔离级别；
- 8. 堆排序
- 9. paxos协议
- 10. 跨机房部署，遇到的问题及解决方案，全年的9999率
- 11. MMM的DB架构，主从未完全同步，master挂了，未同步的内容会造成什么影响，怎么恢复
- 12. ng和tomcat什么区别？能否将两者角色互换。即：tomcat做反向代理，ng做服务容器。说明原因。
- 13. DNS协议
- 14. volatile实现原理
- 15. NAT：公网ip和局域网ip转换
- 16.类加载委托机制，锁的应用，项目架构

### 四面：
- 0、 jit，nio，排序算法，hashmap，更多的是项目细节~
- 1、谈谈项目里主要负责了什么，负责的项目是怎样的架构，自己负责了什么等等；
- 2、JVM类加载机制； 
  
- 3、JVM内存模型，栈空间都放什么，什么情况下栈内存会溢出等；
- 4、JVM调优；
- 5、JVM垃圾收集机制；
- 6、比较关心金融方面的知识是否了解，是否有过金融项目开发的经验；
- 7、jvm栅栏问题，threadlocal的使用；
- 8、JVM
- 9、多线程
- 10、List求交集
- 11、解决项目运行时，CPU占用过高的问题
- 12、线程同步几种机制
- 13、linux检索log，匹配某一请求最多的top10