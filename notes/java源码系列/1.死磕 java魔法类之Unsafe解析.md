🖕欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

（手机横屏看源码更方便）

---

## 问题

（1）Unsafe是什么？

（2）Unsafe具有哪些功能？

（3）Unsafe为什么是不安全的？

（4）怎么使用Unsafe？

## 简介

本章是java并发包专题的第一章，但是第一篇写的却不是java并发包中类，而是java中的魔法类sun.misc.Unsafe。

Unsafe为我们提供了访问低层的机制，这种机制仅供java核心类库使用，而不应该被普通用户使用。

但是，为了更好地了解java的生态体系，我们应该去学习它，去了解它，不求深入到底层的C/C++代码，但求能了解它的基本功能。

## 获取Unsafe的实例

查看Unsafe的源码我们会发现它提供了一个getUnsafe()的静态方法。

```java
@CallerSensitive
public static Unsafe getUnsafe() {
    Class var0 = Reflection.getCallerClass();
    if (!VM.isSystemDomainLoader(var0.getClassLoader())) {
        throw new SecurityException("Unsafe");
    } else {
        return theUnsafe;
    }
}
```

但是，如果直接调用这个方法会抛出一个SecurityException异常，这是因为Unsafe仅供java内部类使用，外部类不应该使用它。

那么，我们就没有方法了吗？

当然不是，我们有反射啊！查看源码，我们发现它有一个属性叫theUnsafe，我们直接通过反射拿到它即可。

```java
public class UnsafeTest {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);
    }
}
```

## 使用Unsafe实例化一个类

假如我们有一个简单的类如下：

```java
class User {
    int age;

    public User() {
        this.age = 10;
    }
}
```

如果我们通过构造方法实例化这个类，age属性将会返回10。

```java
User user1 = new User();
// 打印10
System.out.println(user1.age);
```

如果我们调用Unsafe来实例化呢？

```java
User user2 = (User) unsafe.allocateInstance(User.class);
// 打印0
System.out.println(user2.age);
```

age将返回0，因为`Unsafe.allocateInstance()`只会给对象分配内存，并不会调用构造方法，所以这里只会返回int类型的默认值0。

## 修改私有字段的值

使用Unsafe的putXXX()方法，我们可以修改任意私有字段的值。

```java
public class UnsafeTest {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Unsafe unsafe = (Unsafe) f.get(null);

        User user = new User();
        Field age = user.getClass().getDeclaredField("age");
        unsafe.putInt(user, unsafe.objectFieldOffset(age), 20);

        // 打印20
        System.out.println(user.getAge());
    }
}

class User {
    private int age;

    public User() {
        this.age = 10;
    }

    public int getAge() {
        return age;
    }
}
```

一旦我们通过反射调用得到字段age，我们就可以使用Unsafe将其值更改为任何其他int值。（当然，这里也可以通过反射直接修改）

## 抛出checked异常

我们知道如果代码抛出了checked异常，要不就使用try...catch捕获它，要不就在方法签名上定义这个异常，但是，通过Unsafe我们可以抛出一个checked异常，同时却不用捕获或在方法签名上定义它。

```java
// 使用正常方式抛出IOException需要定义在方法签名上往外抛
public static void readFile() throws IOException {
    throw new IOException();
}
// 使用Unsafe抛出异常不需要定义在方法签名上往外抛
public static void readFileUnsafe() {
    unsafe.throwException(new IOException());
}
```

## 使用堆外内存

如果进程在运行过程中JVM上的内存不足了，会导致频繁的进行GC。理想情况下，我们可以考虑使用堆外内存，这是一块不受JVM管理的内存。

使用Unsafe的allocateMemory()我们可以直接在堆外分配内存，这可能非常有用，但我们要记住，这个内存不受JVM管理，因此我们要调用freeMemory()方法手动释放它。

假设我们要在堆外创建一个巨大的int数组，我们可以使用allocateMemory()方法来实现：

```java
class OffHeapArray {
    // 一个int等于4个字节
    private static final int INT = 4;
    private long size;
    private long address;

    private static Unsafe unsafe;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    // 构造方法，分配内存
    public OffHeapArray(long size) {
        this.size = size;
        // 参数字节数
        address = unsafe.allocateMemory(size * INT);
    }
    
    // 获取指定索引处的元素
    public int get(long i) {
        return unsafe.getInt(address + i * INT);
    }
    // 设置指定索引处的元素
    public void set(long i, int value) {
        unsafe.putInt(address + i * INT, value);
    }
    // 元素个数
    public long size() {
        return size;
    }
    // 释放堆外内存
    public void freeMemory() {
        unsafe.freeMemory(address);
    }
}
```

在构造方法中调用allocateMemory()分配内存，在使用完成后调用freeMemory()释放内存。

使用方式如下：

```java
OffHeapArray offHeapArray = new OffHeapArray(4);
offHeapArray.set(0, 1);
offHeapArray.set(1, 2);
offHeapArray.set(2, 3);
offHeapArray.set(3, 4);
offHeapArray.set(2, 5); // 在索引2的位置重复放入元素

int sum = 0;
for (int i = 0; i < offHeapArray.size(); i++) {
    sum += offHeapArray.get(i);
}
// 打印12
System.out.println(sum);

offHeapArray.freeMemory();
```

最后，一定要记得调用freeMemory()将内存释放回操作系统。

## CompareAndSwap操作

JUC下面大量使用了CAS操作，它们的底层是调用的Unsafe的CompareAndSwapXXX()方法。这种方式广泛运用于无锁算法，与java中标准的悲观锁机制相比，它可以利用CAS处理器指令提供极大的加速。

比如，我们可以基于Unsafe的compareAndSwapInt()方法构建线程安全的计数器。

```java
class Counter {
    private volatile int count = 0;

    private static long offset;
    private static Unsafe unsafe;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            offset = unsafe.objectFieldOffset(Counter.class.getDeclaredField("count"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void increment() {
        int before = count;
        // 失败了就重试直到成功为止
        while (!unsafe.compareAndSwapInt(this, offset, before, before + 1)) {
            before = count;
        }
    }

    public int getCount() {
        return count;
    }
}
```

我们定义了一个volatile的字段count，以便对它的修改所有线程都可见，并在类加载的时候获取count在类中的偏移地址。

在increment()方法中，我们通过调用Unsafe的compareAndSwapInt()方法来尝试更新之前获取到的count的值，如果它没有被其它线程更新过，则更新成功，否则不断重试直到成功为止。

我们可以通过使用多个线程来测试我们的代码：

```java
Counter counter = new Counter();
ExecutorService threadPool = Executors.newFixedThreadPool(100);

// 起100个线程，每个线程自增10000次
IntStream.range(0, 100)
    .forEach(i->threadPool.submit(()->IntStream.range(0, 10000)
        .forEach(j->counter.increment())));

threadPool.shutdown();

Thread.sleep(2000);

// 打印1000000
System.out.println(counter.getCount());
```

## park/unpark

JVM在上下文切换的时候使用了Unsafe中的两个非常牛逼的方法park()和unpark()。

当一个线程正在等待某个操作时，JVM调用Unsafe的park()方法来阻塞此线程。

当阻塞中的线程需要再次运行时，JVM调用Unsafe的unpark()方法来唤醒此线程。

我们之前在分析java中的集合时看到了大量的LockSupport.park()/unpark()，它们底层都是调用的Unsafe的这两个方法。

## 总结

使用Unsafe几乎可以操作一切：

（1）实例化一个类；

（2）修改私有字段的值；

（3）抛出checked异常；

（4）使用堆外内存；

（5）CAS操作；

（6）阻塞/唤醒线程；

## 彩蛋

论实例化一个类的方式？

（1）通过构造方法实例化一个类；

（2）通过Class实例化一个类；

（3）通过反射实例化一个类；

（4）通过克隆实例化一个类；

（5）通过反序列化实例化一个类；

（6）通过Unsafe实例化一个类；

```java
public class InstantialTest {

    private static Unsafe unsafe;
    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws Exception {
        // 1. 构造方法
        User user1 = new User();
        // 2. Class，里面实际也是反射
        User user2 = User.class.newInstance();
        // 3. 反射
        User user3 = User.class.getConstructor().newInstance();
        // 4. 克隆
        User user4 = (User) user1.clone();
        // 5. 反序列化
        User user5 = unserialize(user1);
        // 6. Unsafe
        User user6 = (User) unsafe.allocateInstance(User.class);

        System.out.println(user1.age);
        System.out.println(user2.age);
        System.out.println(user3.age);
        System.out.println(user4.age);
        System.out.println(user5.age);
        System.out.println(user6.age);
    }

    private static User unserialize(User user1) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D://object.txt"));
        oos.writeObject(user1);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("D://object.txt"));
        // 反序列化
        User user5 = (User) ois.readObject();
        ois.close();
        return user5;
    }

    static class User implements Cloneable, Serializable {
        private int age;

        public User() {
            this.age = 10;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}
```


---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)
