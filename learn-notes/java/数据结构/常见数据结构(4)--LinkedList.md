---
layout: post
title:  "常见数据结构(4)--LinkedList"
date:   2017-11-28 01:06:05
categories: 技术
tags: Interview
excerpt: 常见数据结构
---

### 定义

LinkedList 是一个继承于AbstractSequentialList的双向链表。它也可以被当作堆栈、队列或双端队列进行操作。
LinkedList 实现 List 接口，能对它进行队列操作。
LinkedList 实现 Deque 接口，即能将LinkedList当作双端队列使用。
LinkedList 实现了Cloneable接口，即覆盖了函数clone()，能克隆。
LinkedList 实现java.io.Serializable接口，这意味着LinkedList支持序列化，能通过序列化去传输。
LinkedList 是非同步的。
LinkedList相对于ArrayList来说，是可以快速添加，删除元素，ArrayList添加删除元素的话需移动数组元素，可能还需要考虑到扩容数组长度。

### 属性

LinkedList本身的 的属性比较少，主要有三个，一个是size，表名当前有多少个节点；一个是first代表第一个节点；一个是last代表最后一个节点。

```
public class LinkedList<E>  
    extends AbstractSequentialList<E>  
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable{     
    //当前有多少个节点  
    transient int size = 0;  
    //第一个节点  
    transient Node<E> first;  
    //最后一个节点  
    transient Node<E> last;  
    
    //静态内部类
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }  
}  

public AbstractSequentialList<E> extend AbstractList<E>


```

### LinkedList构造方法

默认构造方法是空的,什么都没做,表示初始化的时候size为0,first和last的节点都为空:

```
public LinkedList();

```

另一个构造方法带Collection值的对象作为入参的构造函数,逻辑如下:

1. 使用this()调用默认的无参构造函数
2. 调用addAll()方法,传入当前的节点个数size,此时size为0,并将collection对象传递进去
3. 检查index有没有越界
4. 将Collection转换成数组兑现a
5. 循环遍历a数组,然后将a数组里面的元素创建成拥有前后链接的节点,然后一个个按照顺序连起来
6. 修改当前的节点个数size的值
7. 操作次数modCount自增加1


```
public LinkedList(Collection<? extends E> c) {  
    this();  
    addAll(c);  
}
```

将collection对象转换成数组链表:


```
public boolean addAll(int index, Collection<? extends E> c) {  
    checkPositionIndex(index);  
  
    Object[] a = c.toArray();  
    int numNew = a.length;  
    if (numNew == 0)  
        return false;  
  
    Node<E> pred, succ;  
    if (index == size) {  
        succ = null;  
        pred = last;  
    } else {  
        succ = node(index);  //返回指定位置的Node
        pred = succ.prev;  
    }  
  
    for (Object o : a) {  
        @SuppressWarnings("unchecked") E e = (E) o;  
        Node<E> newNode = new Node<>(pred, e, null);  
        if (pred == null)  
            first = newNode;  
        else  
            pred.next = newNode;  
        pred = newNode;  
    }  
  
    if (succ == null) {  
        last = pred;  
    } else {  
        pred.next = succ;  
        succ.prev = pred;  
    }  
  
    size += numNew;  
    modCount++;  
    return true;  
}

```

### add方法

add(E e)方法

该方法直接将新增的元素放置链表的最后面,然后链表的长度(size)加1,修改的次数(modCount)加1.


```
public boolean add(E e) {  
    linkLast(e);  
    return true;  
}


void linkLast(E e) {  
    final Node<E> l = last;  
    final Node<E> newNode = new Node<>(l, e, null);  
    last = newNode;  
    if (l == null)  
        first = newNode;  
    else  
        l.next = newNode;  
    size++;  
    modCount++;  
} 
```

#### add(int index,E element)方法

指定位置往数组链表中添加元素

1. 检查添加的位置index有没有小于等于当前长度链表size,并且要求大于0
2. 如果是index是等于size,那么直接往链表的最后面添加元素,相当于调用add(E e)
3. 如果index不等于size,则先是索引到处于index位置的元素,然后在index的位置前面添加新增的元素.


```
public void add(int index, E element) {  
    checkPositionIndex(index);  
  
    if (index == size)  
        linkLast(element);  
    else  
        linkBefore(element, node(index));  
}

把索引到的元素添加到新增的元素之后:

void linkBefore(E e, Node<E> succ) {  
    // assert succ != null;  
    final Node<E> pred = succ.prev;  
    final Node<E> newNode = new Node<>(pred, e, succ);  
    succ.prev = newNode;  
    if (pred == null)  
        first = newNode;  
    else  
        pred.next = newNode;  
    size++;  
    modCount++;  
}  


```

#### get方法

首先判断索引位置有没有越界,确定完成之后开始遍历链表的元素,那么从头开始遍历还是从结尾开始遍历,需要索引的位置当前链表长度的一半对比,如果索引位置小于当前链表长度的一半,否则从结尾开始遍历.


```
public E get(int index) {  
    checkElementIndex(index);  
    return node(index).item;  
}
```
遍历链表元素:


```
Node<E> node(int index) {  
    // assert isElementIndex(index);  
  
    if (index < (size >> 1)) {  
        Node<E> x = first;  
        for (int i = 0; i < index; i++)  
            x = x.next;  
        return x;  
    } else {  
        Node<E> x = last;  
        for (int i = size - 1; i > index; i--)  
            x = x.prev;  
        return x;  
    }  
}  
```


####  getFirst方法

直接将第一个元素返回:


```
public E getFirst() {  
    final Node<E> f = first;  
    if (f == null)  
        throw new NoSuchElementException();  
    return f.item;  
}
```

#### getLast方法

直接将最后一个元素返回:

```
public E getLast() {  
    final Node<E> l = last;  
    if (l == null)  
        throw new NoSuchElementException();  
    return l.item;  
}
```

#### remove

remove方法本质调用的还是removeFirst方法


```
public E remove() {  
    return removeFirst();  
}
```

#### removeFirst()方法

移除第一个节点,将第一个节点,让下一个节点成为第一个节点,链表长度减一,修改次数加1,返回移除的第一个检点.



```
public E removeFirst() {  
    final Node<E> f = first;  
    if (f == null)  
        throw new NoSuchElementException();  
    return unlinkFirst(f);  
}


private E unlinkFirst(Node<E> f) {  
    // assert f == first && f != null;  
    final E element = f.item;  
    final Node<E> next = f.next;  
    f.item = null;  
    f.next = null; // help GC  
    first = next;  
    if (next == null)  
        last = null;  
    else  
        next.prev = null;  
    size--;  
    modCount++;  
    return element;  
}  
```

#### removeLast（）方法

移除最后一个节点，将最后一个节点置空，最后一个节点的上一个节点变成last节点，链表长度减1，修改次数加1，返回移除的最后一个节点。


```
public E removeLast() {  
    final Node<E> l = last;  
    if (l == null)  
        throw new NoSuchElementException();  
    return unlinkLast(l);  
}


private E unlinkLast(Node<E> l) {  
    // assert l == last && l != null;  
    final E element = l.item;  
    final Node<E> prev = l.prev;  
    l.item = null;  
    l.prev = null; // help GC  
    last = prev;  
    if (prev == null)  
        first = null;  
    else  
        prev.next = null;  
    size--;  
    modCount++;  
    return element;  
}  
```

#### remove（int index）方法

先是检查移除位置是否在越界 ,如果不在则抛出异常,根据索引index获取需要移除的节点,将移除的节点置空,让其上一个节点和下一个节点对接起来.


```
public E remove(int index) {  
    checkElementIndex(index);  
    return unlink(node(index));  
}
```

#### set方法

检查设置元素位然后置是否越界,如果没有,则索引到index位置的节点,将index位置的节点内容替换成新的内容element,同时返回旧值.


```
public E set(int index, E element) {  
    checkElementIndex(index);  
    Node<E> x = node(index);  
    E oldVal = x.item;  
    x.item = element;  
    return oldVal;  
}
```

#### clean方法

将所有链表元素置空,然后将链表长度修改成0,修改次数加1.



```
public void clear() {  
    // Clearing all of the links between nodes is "unnecessary", but:  
    // - helps a generational GC if the discarded nodes inhabit  
    //   more than one generation  
    // - is sure to free memory even if there is a reachable Iterator  
    for (Node<E> x = first; x != null; ) {  
        Node<E> next = x.next;  
        x.item = null;  
        x.next = null;  
        x.prev = null;  
        x = next;  
    }  
    first = last = null;  
    size = 0;  
    modCount++;  
}
```

#### push和pop方法

push其实就是调用addFirst(e)方法，pop调用的就是removeFirst()方法。

#### toArray

创建一个Object的数组对象，然后将所有的节点都添加到Object对象中，返回Object数组对象。


```
public Object[] toArray() {  
    Object[] result = new Object[size];  
    int i = 0;  
    for (Node<E> x = first; x != null; x = x.next)  
        result[i++] = x.item;  
    return result;  
}
```

#### ListIterator方法

这个方法返回的是一个内部类ListIterator,用户可以使用这个内部类遍历当前的链表元素,但是由于LinkedList也是非线程安全的类,多线程情况下回产生修改异常.


```
public ListIterator<E> listIterator(int index) {  
    checkPositionIndex(index);  
    return new ListItr(index);  
}
```

---
 
 [转载总结至http://blog.csdn.net/fighterandknight](http://blog.csdn.net/fighterandknight) 
