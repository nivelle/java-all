## 内存使用

### 优化目标

#### 1.内存占用少

- 能用基本类型就不要用包装类型（包装类型多了对象头）

- 能定义成类变量的不要定义为实例变量

#### 2.应用速度快

----
### 零复制

- 使用逻辑组合，代替实际复制

- 使用包装，代替实际的复制

- 调用jdk的`zero-copy`接口

- 堆外内存

### 内存池

- Apache Commons Pool

- netty轻量级对象池实现 `io.netty.util.Recycler`

#### 内存池/非内存池的默认选择以及切换方式

````java
io.netty.channel.DefaultChannelConfig#allocator

bootstrap.childOption(ChannelOption.ALLOCATOR,PooledByteBufAllocator.DEFAULT);

````

#### 内存池的实现(PooledDirectByteBuf)

````
io.netty.buffer.PooledDierctByteBuf

````