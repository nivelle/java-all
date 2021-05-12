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
