### 写时复制

- 不可变对象的写操作往往是使用Copy-On-Write方法解决的

- CopyOnWriteArrayList、CopyOnWriteArraySet 的实现思想就是Copy-On-Write,这样做的好处是，读操作是无锁的，所以读性能很高

- 本质上是一种空间换时间的实践

- 函数式编程的基础是不可变性，所以函数式编程中的修改操作都是copy-on-write的，而且实现了按需复制