---
layout: post
title:  "常见数据结构(3)--ArrayList"
date:   2017-11-28 01:06:05
categories: 技术
tags: Interview
excerpt: 常见数据结构
---

### 线性表的定义

线性表是由n(n>=0)个类型相同的数据元素a0,a1,…,an-1组成的有限的序列，在数学中记作(a0,a1,…,an-1)，其中ai的数据类型可以是基本数据类型(int,float等)、字符或类。n代表线性表的元素个数，也称其为长度(Length)。若n=0，则为空表；若n > 0，则ai(0 < i < n-1)有且仅有一个前驱(Predecessor)元素ai-1和一个后继(Successor)元素ai+1，a0没有前驱元素，ai没有后继元素。

### 顺序存储结构的实现分析

![image](http://7xpuj1.com1.z0.glb.clouddn.com/%E7%BA%BF%E6%80%A7%E8%A1%A8)


为了访问一个数组元素，该元素的内存地址需要计算其距离数组基地址的偏移量，即用一个乘法计算偏移量然后加上基地址，就可以获得数组中某个元素的内存地址。其中c代表的是元素数据类型的存储空间大小，而序号则为数组的下标索引。整个过程需要一次乘法和一次加法运算，因为这两个操作的执行时间是常数时间，所以我们可以认为数组访问操作能再常数时间内完成，即时间复杂度为O(1)，这种存取任何一个元素的时间复杂度为O(1)的数据结构称之为随机存取结构。而顺序表的存储原理正如上图所示，因此顺序表的定义如下（引用）：


```

线性表的顺序存储结构称之为顺序表(Sequential List),它使用一维数组依次存放从a0到an-1的数据元素(a0,a1,…,an-1)，将ai(0< i <> n-1)存放在数组的第i个元素，使得ai与其前驱ai-1及后继ai+1的存储位置相邻，因此数据元素在内存的物理存储次序反映了线性表数据元素之间的逻辑次序。

```

### 线性表中常用方法实现(ArrayList数据结构分析)

ArrayList定义

```
public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable

```


实际上,ArrayList底层是个个对象数组:

```
transient Object[] elementData;//没有被私有化是为了简化内部类访问

private static final int DEFAULT_CAPACITY = 10;//默认数组长度是10
private static final Object[] EMPTY_ELEMENTDATA = {};//默认初始化数组是个空对象数组
private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
private int size;

```

- ArrayList的构造器方法有三个:

 1. 如果传入参数，则代表指定ArrayList的初始数组长度，传入参数如果是大于等于0，则使用用户的参数初始化，如果用户传入的参数小于0，则抛出异常，


```

public ArrayList(int initCapacity){
        if(initCapacity > 0) 
            this.elementData = new Object[initCapacity];
        else if(initCapacity == 0) 
            this.elementData = EMPTY_ELEMENTDATA;
        else 
            throw new IllegalArgumentException("cant init ArrayList for this initCapacity:"+initCapacity);
    }
    
```
    
  2. 如果不传入参数,此时我们创建的ArrayList对象中的elementData中的长度是1，size是0,当进行第一次add的时候，elementData将会变成默认的长度：10.
    
    

```

public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
    
```

    
  
   3. 传入集合
   
   (1)将collection对象转换成数组，然后将数组的地址的赋给elementData。

   (2)更新size的值，同时判断size的大小，如果是size等于0，直接将空对象EMPTY_ELEMENTDATA的地址赋给elementData 
   
   (3)如果size的值大于0，则执行Arrays.copy方法，把collection对象的内容（可以理解为深拷贝）copy到elementData中。
   
```
public ArrayList(Collection<? extends T> c) {
        elementData = c.toArray();
        if((size = elementData.length) != 0){
            if(elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size,Object[].class);
        } else {
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
```



**默认构造器中:**

```

this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;//特就是一个默认的空数组

```

#### add方法

add的方法有两个，一个是带一个参数的，一个是带两个参数的

- add(E e)方法:

1. 确保数组已使用长度（size）加1之后足够存下 下一个数据
2. 修改次数modCount 标识自增1，如果当前数组已使用长度（size）加1后的大于当前的数组长度，则调用grow方法，增长数组，grow方法会将当前数组的长度变为原来容量的1.5倍。
3. 确保新增的数据有地方存储之后，则将新元素添加到位于size的位置上。
4. 返回添加成功布尔值。

```

public boolean add(E e) {  
    ensureCapacityInternal(size + 1);  // Increments modCount!!  
    elementData[size++] = e;  
    return true;  
}  


```

确保添加的元素有地方存储,当第一次添加元素的时候this.size +1 的值是1,所以第一次添加的时候会将当前elementData数组长度变为10;

ensureExplicitCapacity


```
   //确认内部容量
    public void ensureCapacityInternal(int minCapacity) {
        if(elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) 
            //取值 10 - 传入最小值之间最大
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
            
        ensureExplicitCapacity(minCapacity);
    }



```

将修改次数（modCount）自增1，判断是否需要扩充数组长度,判断条件就是用当前所需的数组最小长度与数组的长度对比，如果大于0，则增长数组长度。

```
private void ensureExplicitCapacity(int minCapacity) {  
    modCount++;  
  
    // overflow-conscious code  
    if (minCapacity - elementData.length > 0)  
        grow(minCapacity);  
}  


```
如果当前的数组已使用空间（size）加1之后 大于数组长度，则增大数组容量，扩大为原来的1.5倍。

```
 private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);//1.5倍
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }



   /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */

 private static int hugeCapacity(int minCapacity) {
            if (minCapacity < 0) // overflow 超出int整数范围变负数
                throw new OutOfMemoryError();
            return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE :  MAX_ARRAY_SIZE;
     }

```

- add(int index,E element)

![image](http://7xpuj1.com1.z0.glb.clouddn.com/20170312000644695.jpg)


这个方法其实和上面的add类似，该方法可以按照元素的位置，指定位置插入元素，具体的执行逻辑如下：

1. 确保数插入的位置小于等于当前数组长度，并且不小于0，否则抛出异常
2. 确保数组已使用长度（size）加1之后足够存下 下一个数据
3. 修改次数（modCount）标识自增1，如果当前数组已使用长度（size）加1后的大于当前的数组长度，则调用grow方法，增长数组
4. grow方法会将当前数组的长度变为原来容量的1.5倍。
5. 确保有足够的容量之后，使用System.arraycopy 将需要插入的位置（index）后面的元素统统往后移动一位。
6. 将新的数据内容存放到数组的指定位置（index）上

```
public void add(int index, E element) {  
    rangeCheckForAdd(index);  
  
    ensureCapacityInternal(size + 1);  // Increments modCount!!  
    System.arraycopy(elementData, index, elementData, index + 1,  
                     size - index);  
    elementData[index] = element;  
    size++;  
}  

```

#### get方法

返回指定位置上的元素:

```
public E get(int index) {  
    rangeCheck(index);  
    checkForComodification();  
    return ArrayList.this.elementData(offset + index);  
}  


```

#### set方法

确保set的位置小于当前数组的长度（size）并且大于0，获取指定位置（index）元素，然后放到oldValue存放，将需要设置的元素放到指定的位置（index）上，然后将原来位置上的元素oldValue返回给用户。


```
public E set(int index, E element) {  
    rangeCheck(index);  
  
    E oldValue = elementData(index);  
    elementData[index] = element;  
    return oldValue;  
}
```

#### contains方法

调用indexOf方法，遍历数组中的每一个元素作对比，如果找到对于的元素，则返回true，没有找到则返回false。

```
public boolean contains(Object o) {  
    return indexOf(o) >= 0;  
}  


public int indexOf(Object o) {  
    if (o == null) {  
        for (int i = 0; i < size; i++)  
            if (elementData[i]==null)  
                return i;  
    } else {  
        for (int i = 0; i < size; i++)  
            if (o.equals(elementData[i]))  
                return i;  
    }  
    return -1;  
}  

```

#### remove方法

根据索引remove

1. 判断索引有没有越界
2. 自增修改次数
3. 将指定位置(index)上的元素保存到oldValue
4. 将指定位置(index)上的元素都往前移动一位
5. 将最后面的一个元素置空,好让垃圾回收器回收
6. 将原来的值oldValue返回



```
public E remove(int index) {  
    rangeCheck(index);  
  
    modCount++;  
    E oldValue = elementData(index);  
  
    int numMoved = size - index - 1;  
    if (numMoved > 0)  
        System.arraycopy(elementData, index+1, elementData, index,  
                         numMoved);  
    elementData[--size] = null; // clear to let GC do its work  
  
    return oldValue;  
}
```

注意：调用这个方法不会缩减数组的长度，只是将最后一个数组元素置空而已。

根据对象remove

循环遍历所有对象,得到对象所在索引的位置,然后调用fastRemove方法,执行remove操作


```
public boolean remove(Object o) {  
    if (o == null) {  
        for (int index = 0; index < size; index++)  
            if (elementData[index] == null) {  
                fastRemove(index);  
                return true;  
            }  
    } else {  
        for (int index = 0; index < size; index++)  
            if (o.equals(elementData[index])) {  
                fastRemove(index);  
                return true;  
            }  
    }  
    return false;  
}
```
定位到需要remove的元素索引,先将index后面的元素往前移动一位(System.arraycooy实现),然后将最后一个元素置空.

```

private void fastRemove(int index) {  
    modCount++;  
    int numMoved = size - index - 1;  
    if (numMoved > 0)  
        System.arraycopy(elementData, index+1, elementData, index,  
                         numMoved);  
    elementData[--size] = null; // clear to let GC do its work  
}  


```

#### clear方法



```
public void clear() {  
    modCount++;  
  
    // clear to let GC do its work  
    for (int i = 0; i < size; i++)  
        elementData[i] = null;  
  
    size = 0;  
}
```

#### sublist方法

我们看到代码中是创建了一个ArrayList 类里面的一个内部类SubList对象，传入的值中第一个参数是this参数，其实可以理解为返回当前list的部分视图，真实指向的存放数据内容的地方还是同一个地方，如果修改了sublist返回的内容的话，那么原来的list也会变动。


```
public List<E> subList(int arg0, int arg1) {  
    subListRangeCheck(arg0, arg1, this.size);  
    return new ArrayList.SubList(this, 0, arg0, arg1);  
}  
```

#### trimToSize方法

1. 修改次数加1
2. 将elementData中空余的空间(包括null)去除,例如:数组长度为10,其中只有前三个元素有值,其他为空,那么调用该 方法后,数组长度为3;
3. 


```
public void trimToSize() {  
    modCount++;  
    if (size < elementData.length) {  
        elementData = (size == 0)  
          ? EMPTY_ELEMENTDATA  
          : Arrays.copyOf(elementData, size);  
    }  
}
```
  
####  iterator方法

interator方法返回的是一个内部类，由于内部类的创建默认含有外部的this指针，所以这个内部类可以调用到外部类的属性。

```
public Iterator<E> iterator() {  
    return new Itr();  
}  


```

一般的话，调用完iterator之后，我们会使用iterator做遍历，这里使用next做遍历的时候有个需要注意的地方，就是调用next的时候，可能会引发ConcurrentModificationException，当修改次数，与期望的修改次数（调用iterator方法时候的修改次数）不一致的时候，会发生该异常，详细我们看一下代码实现：



```
@SuppressWarnings("unchecked")  
public E next() {  
    checkForComodification();  
    int i = cursor;  
    if (i >= size)  
        throw new NoSuchElementException();  
    Object[] elementData = ArrayList.this.elementData;  
    if (i >= elementData.length)  
        throw new ConcurrentModificationException();  
    cursor = i + 1;  
    return (E) elementData[lastRet = i];  
}
```
expectedModCount这个值是在用户调用ArrayList的iterator方法时候确定的，但是在这之后用户add，或者remove了ArrayList的元素，那么modCount就会改变，那么这个值就会不相等，将会引发ConcurrentModificationException异常，这个是在多线程使用情况下，比较常见的一个异常。


```
final void checkForComodification() {  
    if (modCount != expectedModCount)  
        throw new ConcurrentModificationException();  
}
```

#### Arrays.copyOf方法

```
/**
 * @Description 复制指定的数组, 如有必要用 null 截取或填充，以使副本具有指定的长度
 * 对于所有在原数组和副本中都有效的索引，这两个数组相同索引处将包含相同的值
 * 对于在副本中有效而在原数组无效的所有索引，副本将填充 null，当且仅当指定长度大于原数组的长度时，这些索引存在
 * 返回的数组属于 newType 类

 * @param original 要复制的数组
 * @param 副本的长度
 * @param 副本的类
 * 
 * @return 原数组的副本，截取或用 null 填充以获得指定的长度
 * @throws NegativeArraySizeException 如果 newLength 为负
 * @throws NullPointerException 如果 original 为 null
 * @throws ArrayStoreException 如果从 original 中复制的元素不属于存储在 newType 类数组中的运行时类型

 * @since 1.6
 */
 
public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
    T[] copy = ((Object)newType == (Object)Object[].class)
        ? (T[]) new Object[newLength]
        : (T[]) Array.newInstance(newType.getComponentType(), newLength);
    System.arraycopy(original, 0, copy, 0,
                     Math.min(original.length, newLength));
    return copy;
}

从代码可知，数组拷贝时调用的是本地方法 System.arraycopy() ； 
Arrays.copyOf()方法返回的数组是新的数组对象，原数组对象仍是原数组对象，不变，该拷贝不会影响原来的数组。

System提供的静态方法arraycopy(),我们可以使用它来实现数组之间的复制。其函数原型是：
public static void arraycopy(Object src,int srcPos,Object dest,int destPos,int length)
src:源数组；	srcPos:源数组要复制的起始位置；dest:目的数组；	destPos:目的数组放置的起始位置；	length:复制的长度。

```

**注意：src and dest都必须是同类型或者可以进行转换类型的数组．**

有趣的是这个函数可以实现自己到自己复制，比如：
int[] fun ={0,1,2,3,4,5,6}; 
System.arraycopy(fun,0,fun,3,3);
则结果为：{0,1,2,0,1,2,6};
实现过程是这样的，先生成一个长度为length的临时数组,将fun数组中srcPos 
到srcPos+length-1之间的数据拷贝到临时数组中，再执行System.arraycopy(临时数组,0,fun,3,3).

```

#### 查看数组容量


```


    // 获取ArrayList数组的容量
    public static int getArrayListCapacity(ArrayList<?> arrayList){
        Class<ArrayList> arrayListClass = ArrayList.class;

        try {
            Field field = arrayListClass.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] objects = (Object[])field.get(arrayList);//当前对象属性值

            return objects.length;

        }catch (NoSuchFieldException e){
            e.getMessage();
            return  -1;
        }catch (IllegalAccessException e){
            e.printStackTrace();
            return -1;
        }
    }


```


测试代码:

```
package com.jianlc.mgmt.util.selfLearn.Collection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class ArrayListTest {


    public static void main(String args[]) {
        ArrayList<Integer> originalArray = new ArrayList();
        originalArray.add(1);
        int a1 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量a1:" + a1);

        originalArray.add(2);
        int a2 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量a2:" + a2);

        originalArray.add(3);
        int a3 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量a3:" + a3);

        originalArray.add(4);
        int a4 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量a4:" + a4);

        originalArray.add(5);
        int a5 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a5:" + a5);


        originalArray.add(6);
        int a6 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a6:" + a6);


        originalArray.add(7);
        int a7 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a7:" + a7);


        originalArray.add(8);

        int a8 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a8:" + a8);


        originalArray.add(9);

        int a9 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a9:" + a9);


        originalArray.add(10);

        int a10 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a10:" + a10);


        originalArray.add(11);

        int a11 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a11:" + a11);


        originalArray.add(12);

        int a12 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a12:" + a12);


        originalArray.add(13);

        int a13 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a13:" + a13);


        originalArray.add(14);

        int a14 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a14:" + a14);


        originalArray.add(15);

        int a15 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a15:" + a15);


        originalArray.add(16);

        int a16 = getArrayListCapacity(originalArray);
        System.out.println("arrayList容量 a16:" + a16);


        System.out.println(originalArray.size());
        System.out.println(originalArray.lastIndexOf(16));

        Object[] realArray = originalArray.toArray();

        for (int i = 0; i < realArray.length; i++) {
            System.out.print("对象数组:" + realArray[i]);
        }
        System.out.println();

        int[] arr1 = {1, 2, 3, 4, 5};

        int[] arr2 = Arrays.copyOf(arr1, 2);
        System.out.println(arr2.length);
        for (int i = 0; i < arr2.length; i++) {
            System.out.print("副本数组" + arr2[i]);
        }

        System.out.println();

        for (int i = 0; i < arr1.length; i++) {
            System.out.print("原数组:" + arr1[i]);
        }

        System.out.println();


    }


    // 获取ArrayList数组的容量
    public static int getArrayListCapacity(ArrayList<?> arrayList) {
        Class<ArrayList> arrayListClass = ArrayList.class;

        try {
            Field field = arrayListClass.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] objects = (Object[]) field.get(arrayList);//当前对象属性值

            return objects.length;

        } catch (NoSuchFieldException e) {
            e.getMessage();
            return -1;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }


}

结果:

arrayList容量a1:10
arrayList容量a2:10
arrayList容量a3:10
arrayList容量a4:10
arrayList容量 a5:10
arrayList容量 a6:10
arrayList容量 a7:10
arrayList容量 a8:10
arrayList容量 a9:10
arrayList容量 a10:10
arrayList容量 a11:15
arrayList容量 a12:15
arrayList容量 a13:15
arrayList容量 a14:15
arrayList容量 a15:15
arrayList容量 a16:22
16
15
对象数组:1对象数组:2对象数组:3对象数组:4对象数组:5对象数组:6对象数组:7对象数组:8对象数组:9对象数组:10对象数组:11对象数组:12对象数组:13对象数组:14对象数组:15对象数组:16
2
副本数组1副本数组2
原数组:1原数组:2原数组:3原数组:4原数组:5


```













([转载至Fighter168](https://blog.csdn.net/fighterandknight/article/details/61240861))
