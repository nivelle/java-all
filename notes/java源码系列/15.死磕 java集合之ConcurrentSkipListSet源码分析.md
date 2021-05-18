🖕欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

（手机横屏看源码更方便）

---

## 问题

（1）ConcurrentSkipListSet的底层是ConcurrentSkipListMap吗？

（2）ConcurrentSkipListSet是线程安全的吗？

（3）ConcurrentSkipListSet是有序的吗？

（4）ConcurrentSkipListSet和之前讲的Set有何不同？

## 简介

ConcurrentSkipListSet底层是通过ConcurrentNavigableMap来实现的，它是一个有序的线程安全的集合。

## 源码分析

它的源码比较简单，跟通过Map实现的Set基本是一致，只是多了一些取最近的元素的方法。

为了保持专栏的完整性，我还是贴一下源码，最后会对Set的整个家族作一个对比，有兴趣的可以直接拉到最下面。

```java
// 实现了NavigableSet接口，并没有所谓的ConcurrentNavigableSet接口
public class ConcurrentSkipListSet<E>
    extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -2479143111061671589L;

    // 存储使用的map
    private final ConcurrentNavigableMap<E,Object> m;

    // 初始化
    public ConcurrentSkipListSet() {
        m = new ConcurrentSkipListMap<E,Object>();
    }

    // 传入比较器
    public ConcurrentSkipListSet(Comparator<? super E> comparator) {
        m = new ConcurrentSkipListMap<E,Object>(comparator);
    }
    
    // 使用ConcurrentSkipListMap初始化map
    // 并将集合c中所有元素放入到map中
    public ConcurrentSkipListSet(Collection<? extends E> c) {
        m = new ConcurrentSkipListMap<E,Object>();
        addAll(c);
    }
    
    // 使用ConcurrentSkipListMap初始化map
    // 并将有序Set中所有元素放入到map中
    public ConcurrentSkipListSet(SortedSet<E> s) {
        m = new ConcurrentSkipListMap<E,Object>(s.comparator());
        addAll(s);
    }
    
    // ConcurrentSkipListSet类内部返回子set时使用的
    ConcurrentSkipListSet(ConcurrentNavigableMap<E,Object> m) {
        this.m = m;
    }
    
    // 克隆方法
    public ConcurrentSkipListSet<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            ConcurrentSkipListSet<E> clone =
                (ConcurrentSkipListSet<E>) super.clone();
            clone.setMap(new ConcurrentSkipListMap<E,Object>(m));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /* ---------------- Set operations -------------- */
    // 返回元素个数
    public int size() {
        return m.size();
    }

    // 检查是否为空
    public boolean isEmpty() {
        return m.isEmpty();
    }
    
    // 检查是否包含某个元素
    public boolean contains(Object o) {
        return m.containsKey(o);
    }
    
    // 添加一个元素
    // 调用map的putIfAbsent()方法
    public boolean add(E e) {
        return m.putIfAbsent(e, Boolean.TRUE) == null;
    }
    
    // 移除一个元素
    public boolean remove(Object o) {
        return m.remove(o, Boolean.TRUE);
    }

    // 清空所有元素
    public void clear() {
        m.clear();
    }
    
    // 迭代器
    public Iterator<E> iterator() {
        return m.navigableKeySet().iterator();
    }

    // 降序迭代器
    public Iterator<E> descendingIterator() {
        return m.descendingKeySet().iterator();
    }


    /* ---------------- AbstractSet Overrides -------------- */
    // 比较相等方法
    public boolean equals(Object o) {
        // Override AbstractSet version to avoid calling size()
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Collection<?> c = (Collection<?>) o;
        try {
            // 这里是通过两次两层for循环来比较
            // 这里是有很大优化空间的，参考上篇文章CopyOnWriteArraySet中的彩蛋
            return containsAll(c) && c.containsAll(this);
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }
    
    // 移除集合c中所有元素
    public boolean removeAll(Collection<?> c) {
        // Override AbstractSet version to avoid unnecessary call to size()
        boolean modified = false;
        for (Object e : c)
            if (remove(e))
                modified = true;
        return modified;
    }

    /* ---------------- Relational operations -------------- */
    
    // 小于e的最大元素
    public E lower(E e) {
        return m.lowerKey(e);
    }

    // 小于等于e的最大元素
    public E floor(E e) {
        return m.floorKey(e);
    }
    
    // 大于等于e的最小元素
    public E ceiling(E e) {
        return m.ceilingKey(e);
    }

    // 大于e的最小元素
    public E higher(E e) {
        return m.higherKey(e);
    }

    // 弹出最小的元素
    public E pollFirst() {
        Map.Entry<E,Object> e = m.pollFirstEntry();
        return (e == null) ? null : e.getKey();
    }

    // 弹出最大的元素
    public E pollLast() {
        Map.Entry<E,Object> e = m.pollLastEntry();
        return (e == null) ? null : e.getKey();
    }


    /* ---------------- SortedSet operations -------------- */

    // 取比较器
    public Comparator<? super E> comparator() {
        return m.comparator();
    }

    // 最小的元素
    public E first() {
        return m.firstKey();
    }

    // 最大的元素
    public E last() {
        return m.lastKey();
    }
    
    // 取两个元素之间的子set
    public NavigableSet<E> subSet(E fromElement,
                                  boolean fromInclusive,
                                  E toElement,
                                  boolean toInclusive) {
        return new ConcurrentSkipListSet<E>
            (m.subMap(fromElement, fromInclusive,
                      toElement,   toInclusive));
    }
    
    // 取头子set
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ConcurrentSkipListSet<E>(m.headMap(toElement, inclusive));
    }

    // 取尾子set
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ConcurrentSkipListSet<E>(m.tailMap(fromElement, inclusive));
    }

    // 取子set，包含from，不包含to
    public NavigableSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }
    
    // 取头子set，不包含to
    public NavigableSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }
    
    // 取尾子set，包含from
    public NavigableSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }
    
    // 降序set
    public NavigableSet<E> descendingSet() {
        return new ConcurrentSkipListSet<E>(m.descendingMap());
    }

    // 可分割的迭代器
    @SuppressWarnings("unchecked")
    public Spliterator<E> spliterator() {
        if (m instanceof ConcurrentSkipListMap)
            return ((ConcurrentSkipListMap<E,?>)m).keySpliterator();
        else
            return (Spliterator<E>)((ConcurrentSkipListMap.SubMap<E,?>)m).keyIterator();
    }

    // 原子更新map，给clone方法使用
    private void setMap(ConcurrentNavigableMap<E,Object> map) {
        UNSAFE.putObjectVolatile(this, mapOffset, map);
    }

    // 原子操作相关内容
    private static final sun.misc.Unsafe UNSAFE;
    private static final long mapOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentSkipListSet.class;
            mapOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("m"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
```

可以看到，ConcurrentSkipListSet基本上都是使用ConcurrentSkipListMap实现的，虽然取子set部分是使用ConcurrentSkipListMap中的内部类，但是这些内部类其实也是和ConcurrentSkipListMap相关的，它们返回ConcurrentSkipListMap的一部分数据。

另外，这里的equals()方法实现的相当敷衍，有很大的优化空间，作者这样实现，应该也是知道几乎没有人来调用equals()方法吧。

## 总结

（1）ConcurrentSkipListSet底层是使用ConcurrentNavigableMap实现的；

（2）ConcurrentSkipListSet有序的，基于元素的自然排序或者通过比较器确定的顺序；

（3）ConcurrentSkipListSet是线程安全的；

## 彩蛋

Set大汇总：

Set|有序性|线程安全|底层实现|关键接口|特点
---|---|---|---|---|---
HashSet|无|否|HashMap|无|简单
LinkedHashSet|有|否|LinkedHashMap|无|插入顺序
TreeSet|有|否|NavigableMap|NavigableSet|自然顺序
CopyOnWriteArraySet|有|是|CopyOnWriteArrayList|无|插入顺序，读写分离
ConcurrentSkipListSet|有|是|ConcurrentNavigableMap|NavigableSet|自然顺序

从中我们可以发现一些规律：

（1）除了HashSet其它Set都是有序的；

（2）实现了NavigableSet或者SortedSet接口的都是自然顺序的；

（3）使用并发安全的集合实现的Set也是并发安全的；

（4）TreeSet虽然不是全部都是使用的TreeMap实现的，但其实都是跟TreeMap相关的（TreeMap的子Map中组合了TreeMap）；

（5）ConcurrentSkipListSet虽然不是全部都是使用的ConcurrentSkipListMap实现的，但其实都是跟ConcurrentSkipListMap相关的（ConcurrentSkipListeMap的子Map中组合了ConcurrentSkipListMap）；

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)

