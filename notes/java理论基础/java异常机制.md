### java异常机制

- Exception 和 Error 都是继承了 Throwable 类，在 Java 中只有 Throwable 类型的实例才可以被抛出（throw）或者捕获（catch），它是异常处理机制的基本组成类型

- Error 是指在正常情况下，不大可能出现的情况，绝大部分的 Error 都会导致程序（比如 JVM 自身）处于非正常的、不可恢复状态

- Exception 又分为可检查（checked）异常和不检查（unchecked）异常，可检查异常在源代码里必须显式地进行捕获处理，这是编译期检查的一部分

#### 常见受检异常

- ClassNotFoundException

- IOException

- SQLException

- FileNotFoundException

- InterruptedException

#### 常见非受检异常

- NullPointerException
  
- ArrayIndexOutBoundException

````
1. Thrown to indicate that an array has been accessed with an illegal index. The index is either negative or greater than or equal to the size of the array.
2. 是IndexOutOfBoundsException的子类
````

- ClassCastException
  
- IndexOutOfBoundsException

````
Thrown to indicate that an index of some sort (such as to an array, to a string, or to a vector) is out of range.
````

- ArrayStoreException //数组类型元素错误

````
Thrown to indicate that an attempt has been made to store the wrong type of object into an array of objects.
````

---------

### java异常

[![6lXKeO.md.png](https://s3.ax1x.com/2021/03/08/6lXKeO.md.png)](https://imgtu.com/i/6lXKeO)

- 所有的异常都是 Throwable 类或其子类的实例; Throwable 有两大子类

- 第一个是Error ，涵盖程序不应捕获的异常；当程序触发Error时，它的执行状态已经无法恢复，需要中止线程甚至中止虚拟机

- 第二个是Exception,涵盖程序可能需要捕获并且处理的异常；Exception有一个特殊的子类 RuntimeException ，表示"程序虽然无法继续执行，但是还能抢救一下"

- RuntimeException和Error属于java非检查异常(unchecked exception);其他异常属于检查异常(checked exception)

- 异常实例的构造十分昂贵。这是由于在构造异常实例时，Java 虚拟机便需要生成该异常的栈轨迹（stack trace）。

### jvm虚拟机处理异常

- 在编译生成的字节码中，每个方法都附带一个异常表。异常表中的每一个条目代表一个异常处理器，并且由 from 指针、to 指针、target 指针以及所捕获的异常类型构成。这些指针的值是字节码索引（bytecode
  index，bci），用以定位字节码。

- 当程序触发异常时，Java 虚拟机会从上至下遍历异常表中的所有条目。当触发异常的字节码的索引值在某个异常表条目的监控范围内，Java 虚拟机会判断所抛出的异常和该条目想要捕获的异常是否匹配。如果匹配，Java
  虚拟机会将控制流转移至该条目 target 指针指向的字节码。如果遍历完所有异常表条目，Java 虚拟机仍未匹配到异常处理器，那么它会弹出当前方法对应的 Java 栈帧，并且在调用者（caller）中重复上述操作。在最坏情况下，Java
  虚拟机需要遍历当前线程 Java 栈上所有方法的异常表。

- from 指针和 to 指针标示了该异常处理器所监控的范围

- target 指针则指向异常处理器的起始位置

- Java 字节码中，每个方法对应一个异常表。当程序触发异常时，Java 虚拟机将查找异常表，并依此决定需要将控制流转移至哪个异常处理器之中。Java 代码中的 catch 代码块和 finally 代码块都会生成异常表条目。

--------