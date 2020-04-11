

### 定义

用来提供线程内部的局部变量。这种变量在多线程环境下访问（通过get 和 set 方法访问）时能保证各个线程的变量相对独立于其他线程内的变量。ThreadLocal实例通常来说都是private static 类型的，用于关联线程和线程上下文。


实现：ThreadLocal的实现是这样的：每个Thread维护一个ThreadLocalMap映射表，这个表的key是ThreadLocal实例本身，value是真正需要存储的Object,ThreadLocal本身并不存储值，它只是作为一个key来让线程从ThreadLocalMap获取value. ThreadLocalMap是使用ThreadLocal的弱引用作为key的，弱引用的对象在GC时会被回收。当垃圾收集器工作时，无论当前内存是否足够，都会回收掉只被弱引用关联的对象

**四种类型的引用**：

引用类型 | 被垃圾回收时间 | 用途 | 生存时间
---|--- | --- | ---
强引用 | 从来不会 | 对象的一般状态 | JVM停止运行时终止
软引用 | 在内存不足时 | 对象缓存 | 内存不足时
弱引用 | 垃圾回收时 | 对象缓存| gc运行后终止
虚引用 | unknown | unknown | unknown

### 常规操作

- get()

(1)获取当前线程Thread对象，进而获取此线程对象中维护的ThreadLocalMap对象。
(2)判断当前的ThreadLocalMap是否存在
   
1. 如果存在，则以当前的ThreadLocal为key，调用ThreadLocalMap中的getEntry方法获取对应的存储实体e。找到对应的存储实体e,获取存储实体e对应的value值，即为我们想要的当前线程对应此ThreadLocal的值，返回结果值。
2. 如果不存在，则证明此线程没有维护的ThreadLocalMap对象，调用setInitialValue方法进行初始化。返回 setInitialValue 初始化的值。

###### setInitialValue

- 调用initialValue获取初始化的值。
- 获取当前线程Thread对象，进而获取此线程对象中维护的ThreadLocalMap对象。
- 判断当前的ThreadLocalMap是否存在：

1.   如果存在，则调用map.set设置此实体entry

2.   如果不存在，则调用createMap进行ThreadLocalMap对象的初始化，并将此实体entry作为第一个值存放至ThreadLocalMap中。

- set()

(1) 获取当前线程Thread对象，进而获取此线程对象中维护的ThreadLocalMap对象。

(2) 判断当前的ThreadLocalMap是否存在：

  1. 如果存在，则调用map.set设置此实体entry。
  2. 如果不存在，则调用createMap进行ThreadLocalMap对象的初始化，并将此实体entry作为第一个值存放至ThreadLocalMap中。

- remove()

(1) 获取当前线程Thread对象，进而获取此线程对象中维护的ThreadLocalMap对象。

(2) 判断当前的ThreadLocalMap是否存在，如果存在，则调用map.remove，以当前ThreadLocal为key删除对应的实体entry。

####  数据结构

- ThreadLocalMap 实现自定义hashMap

(1) Entry[] table:底层哈希表 table, 必要时需要进行扩容，底层哈希表 table.length 长度必须是2的n次方。

(2) int size: 实际存储键值对元素个数 entries

(3) int threshold:下一次扩容时的阈值，阈值 threshold = 底层哈希表table的长度 len * 2 / 3。当size >=threshold时，遍历table并删除key为null的元素，如果删除后size >= threshold*3/4时，需要对table进行扩容（详情请查看set(ThreadLocal<?> key, Object value)方法说明）
  
##### 其中Entry[] table;哈希表存储的核心元素是Entry，Entry包含：

1. ThreadLocal<?> k；：当前存储的ThreadLocal实例对象
2. Object value;：当前 ThreadLocal 对应储存的值value

需要注意的是，此Entry继承了弱引用WeakReference，所以在使用ThreadLocalMap时，发现key == null，则意味着此keyThreadLocal不在被引用，需要将其从ThreadLocalMap哈希表中移除。

##### ThreadLocalMap的构造方法是延迟加载的，也就是说，只有当线程需要存储对应的ThreadLocal的值时，才初始化创建一次（仅初始化一次）。初始化步骤如下
1. 初始化底层数组table的初始容量为 16。
2. 获取ThreadLocal中的threadLocalHashCode，通过threadLocalHashCode & (INITIAL_CAPACITY - 1)，即ThreadLocal 的 hash 值 threadLocalHashCode  % 哈希表的长度 length 的方式计算该实体的存储位置。
3. 存储当前的实体，key 为 : 当前ThreadLocal value：真正要存储的值
4. 设置当前实际存储元素个数 size 为 1
5. 设置阈值setThreshold(INITIAL_CAPACITY)，为初始化容量 16 的 2/3。

### 内存泄露问题分析

![image](https://ws1.sinaimg.cn/large/b1eb59d9ly1fwwk2975muj20ju0b777j.jpg)

ThreadLocal内存泄漏的根源是：由于ThreadLocalMap的生命周期跟Thread一样长，如果没有手动删除对应key就会导致内存泄漏，而不是因为弱引用。

每个thread中都存在一个map, map的类型是ThreadLocal.ThreadLocalMap. Map中的key为一个threadlocal实例.这个Map的确使用了弱引用,不过弱引用只是针对key. 每个key都弱引用指向threadlocal.当把threadlocal实例置为null以后,没有任何强引用指向threadlocal实例,所以threadlocal将会被gc回收.但是,我们的value却不能回收,因为存在一条从current thread连接过来的强引用. 只有当前thread结束以后, current thread就不会存在栈中,强引用断开, Current Thread, Map, value将全部被GC回收.

ThreadLocalMap使用ThreadLocal的弱引用作为key，如果一个ThreadLocal没有外部强引用来引用它，那么系统GC的时候，这个ThreadLocal势必会被回收，这样一来，ThreadLocalMap中就会出现key为null的Entry，就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value永远无法回收，造成内存泄漏

但是这些被动的预防措施并不能保证不会内存泄漏：

- 用static的ThreadLocal，延长了ThreadLocal的生命周期，可能导致的内存泄漏
- 分配使用了ThreadLocal又不再调用get(),set(),remove()方法，那么就会导致内存泄漏。

##### 为什么使用弱引用

从表面上看内存泄漏的根源在于使用了弱引用。网上的文章大多着重分析ThreadLocal使用了弱引用会导致内存泄漏，但是另一个问题也同样值得思考：为什么使用弱引用而不是强引用？

**为了应对非常大和长时间的用途，哈希表使用弱引用的 key。**

- key 使用强引用：引用的ThreadLocal的对象被回收了，但是ThreadLocalMap还持有ThreadLocal的强引用，如果没有手动删除，ThreadLocal不会被回收，导致Entry内存泄漏。
- key 使用弱引用：引用的ThreadLocal的对象被回收了，由于ThreadLocalMap持有ThreadLocal的弱引用，即使没有手动删除，ThreadLocal也会被回收。value在下一次ThreadLocalMap调用set,get，remove的时候会被清除。

比较两种情况，我们可以发现：由于ThreadLocalMap的生命周期跟Thread一样长，如果都没有手动删除对应key，都会导致内存泄漏，但是使用弱引用可以多一层保障：弱引用ThreadLocal不会内存泄漏，对应的value在下一次ThreadLocalMap调用set,get,remove的时候会被清除。但是如果用的是线程池，那么的话线程就不会结束，只会放在线程池中等待下一个任务，但是这个线程的 map 还是没有被回收，它里面存在value的强引用，所以会导致内存溢出。

### 应用场景

- 解决并发问题：使用ThreadLocal代替synchronized来保证线程安全。同步机制采用了“以时间换空间”的方式，而ThreadLocal采用了“以空间换时间”的方式。前者仅提供一份变量，让不同的线程排队访问，而后者为每一个线程都提供了一份变量，因此可以同时访问而互不影响。
- 解决数据存储问题：ThreadLocal为变量在每个线程中都创建了一个副本，所以每个线程可以访问自己内部的副本变量，不同线程之间不会互相干扰。如一个Parameter对象的数据需要在多个模块中使用，如果采用参数传递的方式，显然会增加模块之间的耦合性。此时我们可以使用ThreadLocal解决。

**具体应用场景：**

我们知道在一般情况下，只有无状态的Bean才可以在多线程环境下共享，在Spring中，绝大部分Bean都可以声明为singleton作用域。就是因为Spring对一些Bean（如RequestContextHolder、TransactionSynchronizationManager、LocaleContextHolder等）中非线程安全状态采用ThreadLocal进行处理，让它们也成为线程安全的状态，因为有状态的Bean就可以在多线程中共享了。

一般的Web应用划分为展现层、服务层和持久层三个层次，在不同的层中编写对应的逻辑，下层通过接口向上层开放功能调用。在一般情况下，从接收请求到返回响应所经过的所有程序调用都同属于一个线程ThreadLocal是解决线程安全问题一个很好的思路，它通过为每个线程提供一个独立的变量副本解决了变量并发访问的冲突问题。在很多情况下，ThreadLocal比直接使用synchronized同步机制解决线程安全问题更简单，更方便，且结果程序拥有更高的并发性。
