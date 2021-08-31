🖕欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

（手机横屏看源码更方便）

---

## 问题

（1）TreeSet真的是使用TreeMap来存储元素的吗？

（2）TreeSet是有序的吗？

（3）TreeSet和LinkedHashSet有何不同？

## 简介

TreeSet底层是采用TreeMap实现的一种Set，所以它是有序的，同样也是非线程安全的。

## 源码分析

经过前面我们学习HashSet和LinkedHashSet，基本上已经掌握了Set实现的套路了。

所以，也不废话了，直接上源码：

```java
package java.util;

// TreeSet实现了NavigableSet接口，所以它是有序的
public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable
{
    // 元素存储在NavigableMap中
    // 注意它不一定就是TreeMap
    private transient NavigableMap<E,Object> m;

    // 虚拟元素, 用来作为value存储在map中
    private static final Object PRESENT = new Object();

    // 直接使用传进来的NavigableMap存储元素
    // 这里不是深拷贝,如果外面的map有增删元素也会反映到这里
    // 而且, 这个方法不是public的, 说明只能给同包使用
    TreeSet(NavigableMap<E,Object> m) {
        this.m = m;
    }

    // 使用TreeMap初始化
    public TreeSet() {
        this(new TreeMap<E,Object>());
    }

    // 使用带comparator的TreeMap初始化
    public TreeSet(Comparator<? super E> comparator) {
        this(new TreeMap<>(comparator));
    }

    // 将集合c中的所有元素添加的TreeSet中
    public TreeSet(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    // 将SortedSet中的所有元素添加到TreeSet中
    public TreeSet(SortedSet<E> s) {
        this(s.comparator());
        addAll(s);
    }

    // 迭代器
    public Iterator<E> iterator() {
        return m.navigableKeySet().iterator();
    }

    // 逆序迭代器
    public Iterator<E> descendingIterator() {
        return m.descendingKeySet().iterator();
    }

    // 以逆序返回一个新的TreeSet
    public NavigableSet<E> descendingSet() {
        return new TreeSet<>(m.descendingMap());
    }

    // 元素个数
    public int size() {
        return m.size();
    }

    // 判断是否为空
    public boolean isEmpty() {
        return m.isEmpty();
    }

    // 判断是否包含某元素
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    // 添加元素, 调用map的put()方法, value为PRESENT
    public boolean add(E e) {
        return m.put(e, PRESENT)==null;
    }
    
    // 删除元素
    public boolean remove(Object o) {
        return m.remove(o)==PRESENT;
    }

    // 清空所有元素
    public void clear() {
        m.clear();
    }

    // 添加集合c中的所有元素
    public  boolean addAll(Collection<? extends E> c) {
        // 满足一定条件时直接调用TreeMap的addAllForTreeSet()方法添加元素
        if (m.size()==0 && c.size() > 0 &&
            c instanceof SortedSet &&
            m instanceof TreeMap) {
            SortedSet<? extends E> set = (SortedSet<? extends E>) c;
            TreeMap<E,Object> map = (TreeMap<E, Object>) m;
            Comparator<?> cc = set.comparator();
            Comparator<? super E> mc = map.comparator();
            if (cc==mc || (cc != null && cc.equals(mc))) {
                map.addAllForTreeSet(set, PRESENT);
                return true;
            }
        }
        // 不满足上述条件, 调用父类的addAll()通过遍历的方式一个一个地添加元素
        return super.addAll(c);
    }

    // 子set（NavigableSet中的方法）
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                  E toElement,   boolean toInclusive) {
        return new TreeSet<>(m.subMap(fromElement, fromInclusive,
                                       toElement,   toInclusive));
    }
    
    // 头set（NavigableSet中的方法）
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new TreeSet<>(m.headMap(toElement, inclusive));
    }

    // 尾set（NavigableSet中的方法）
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new TreeSet<>(m.tailMap(fromElement, inclusive));
    }

    // 子set（SortedSet接口中的方法）
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    // 头set（SortedSet接口中的方法）
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }
    
    // 尾set（SortedSet接口中的方法）
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    // 比较器
    public Comparator<? super E> comparator() {
        return m.comparator();
    }

    // 返回最小的元素
    public E first() {
        return m.firstKey();
    }
    
    // 返回最大的元素
    public E last() {
        return m.lastKey();
    }

    // 返回小于e的最大的元素
    public E lower(E e) {
        return m.lowerKey(e);
    }

    // 返回小于等于e的最大的元素
    public E floor(E e) {
        return m.floorKey(e);
    }
    
    // 返回大于等于e的最小的元素
    public E ceiling(E e) {
        return m.ceilingKey(e);
    }
    
    // 返回大于e的最小的元素
    public E higher(E e) {
        return m.higherKey(e);
    }
    
    // 弹出最小的元素
    public E pollFirst() {
        Map.Entry<E,?> e = m.pollFirstEntry();
        return (e == null) ? null : e.getKey();
    }

    public E pollLast() {
        Map.Entry<E,?> e = m.pollLastEntry();
        return (e == null) ? null : e.getKey();
    }

    // 克隆方法
    @SuppressWarnings("unchecked")
    public Object clone() {
        TreeSet<E> clone;
        try {
            clone = (TreeSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        clone.m = new TreeMap<>(m);
        return clone;
    }

    // 序列化写出方法
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out any hidden stuff
        s.defaultWriteObject();

        // Write out Comparator
        s.writeObject(m.comparator());

        // Write out size
        s.writeInt(m.size());

        // Write out all elements in the proper order.
        for (E e : m.keySet())
            s.writeObject(e);
    }

    // 序列化写入方法
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in any hidden stuff
        s.defaultReadObject();

        // Read in Comparator
        @SuppressWarnings("unchecked")
            Comparator<? super E> c = (Comparator<? super E>) s.readObject();

        // Create backing TreeMap
        TreeMap<E,Object> tm = new TreeMap<>(c);
        m = tm;

        // Read in size
        int size = s.readInt();

        tm.readTreeSet(size, s, PRESENT);
    }

    // 可分割的迭代器
    public Spliterator<E> spliterator() {
        return TreeMap.keySpliteratorFor(m);
    }

    // 序列化id
    private static final long serialVersionUID = -2479143000061671589L;
}
```

源码比较简单，基本都是调用map相应的方法。

## 总结

（1）TreeSet底层使用NavigableMap存储元素；

（2）TreeSet是有序的；

（3）TreeSet是非线程安全的；

（4）TreeSet实现了NavigableSet接口，而NavigableSet继承自SortedSet接口；

（5）TreeSet实现了SortedSet接口；（彤哥年轻的时候面试被问过TreeSet和SortedSet的区别^^）

## 彩蛋

（1）通过之前的学习，我们知道TreeSet和LinkedHashSet都是有序的，那它们有何不同？

LinkedHashSet并没有实现SortedSet接口，它的有序性主要依赖于LinkedHashMap的有序性，所以它的有序性是指按照插入顺序保证的有序性；

而TreeSet实现了SortedSet接口，它的有序性主要依赖于NavigableMap的有序性，而NavigableMap又继承自SortedMap，这个接口的有序性是指按照key的自然排序保证的有序性，而key的自然排序又有两种实现方式，一种是key实现Comparable接口，一种是构造方法传入Comparator比较器。

（2）TreeSet里面真的是使用TreeMap来存储元素的吗？

通过源码分析我们知道TreeSet里面实际上是使用的NavigableMap来存储元素，虽然大部分时候这个map确实是TreeMap，但不是所有时候都是TreeMap。

因为有一个构造方法是`TreeSet(NavigableMap<E,Object> m)`，而且这是一个非public方法，通过调用关系我们可以发现这个构造方法都是在自己类中使用的，比如下面这个：

```java
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new TreeSet<>(m.tailMap(fromElement, inclusive));
    }
```

而这个m我们姑且认为它是TreeMap，也就是调用TreeMap的tailMap()方法：

```java
    public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
        return new AscendingSubMap<>(this,
                                     false, fromKey, inclusive,
                                     true,  null,    true);
    }
```

可以看到，返回的是AscendingSubMap对象，这个类的继承链是怎么样的呢？

![AscendingSubMap](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/AscendingSubMap.png)

可以看到，这个类并没有继承TreeMap，不过通过源码分析也可以看出来这个类是组合了TreeMap，也算和TreeMap有点关系，只是不是继承关系。

所以，TreeSet的底层不完全是使用TreeMap来实现的，更准确地说，应该是NavigableMap。

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)

