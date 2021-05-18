欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

## 简介

TreeMap使用红黑树存储元素，可以保证元素按key值的大小进行遍历。

## 继承体系

![TreeMap](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/TreeMap.png)

TreeMap实现了Map、SortedMap、NavigableMap、Cloneable、Serializable等接口。

SortedMap规定了元素可以按key的大小来遍历，它定义了一些返回部分map的方法。

```java
public interface SortedMap<K,V> extends Map<K,V> {
    // key的比较器
    Comparator<? super K> comparator();
    // 返回fromKey（包含）到toKey（不包含）之间的元素组成的子map
    SortedMap<K,V> subMap(K fromKey, K toKey);
    // 返回小于toKey（不包含）的子map
    SortedMap<K,V> headMap(K toKey);
    // 返回大于等于fromKey（包含）的子map
    SortedMap<K,V> tailMap(K fromKey);
    // 返回最小的key
    K firstKey();
    // 返回最大的key
    K lastKey();
    // 返回key集合
    Set<K> keySet();
    // 返回value集合
    Collection<V> values();
    // 返回节点集合
    Set<Map.Entry<K, V>> entrySet();
}
```

NavigableMap是对SortedMap的增强，定义了一些返回离目标key最近的元素的方法。

```java
public interface NavigableMap<K,V> extends SortedMap<K,V> {
    // 小于给定key的最大节点
    Map.Entry<K,V> lowerEntry(K key);
    // 小于给定key的最大key
    K lowerKey(K key);
    // 小于等于给定key的最大节点
    Map.Entry<K,V> floorEntry(K key);
    // 小于等于给定key的最大key
    K floorKey(K key);
    // 大于等于给定key的最小节点
    Map.Entry<K,V> ceilingEntry(K key);
    // 大于等于给定key的最小key
    K ceilingKey(K key);
    // 大于给定key的最小节点
    Map.Entry<K,V> higherEntry(K key);
    // 大于给定key的最小key
    K higherKey(K key);
    // 最小的节点
    Map.Entry<K,V> firstEntry();
    // 最大的节点
    Map.Entry<K,V> lastEntry();
    // 弹出最小的节点
    Map.Entry<K,V> pollFirstEntry();
    // 弹出最大的节点
    Map.Entry<K,V> pollLastEntry();
    // 返回倒序的map
    NavigableMap<K,V> descendingMap();
    // 返回有序的key集合
    NavigableSet<K> navigableKeySet();
    // 返回倒序的key集合
    NavigableSet<K> descendingKeySet();
    // 返回从fromKey到toKey的子map，是否包含起止元素可以自己决定
    NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                             K toKey,   boolean toInclusive);
    // 返回小于toKey的子map，是否包含toKey自己决定
    NavigableMap<K,V> headMap(K toKey, boolean inclusive);
    // 返回大于fromKey的子map，是否包含fromKey自己决定
    NavigableMap<K,V> tailMap(K fromKey, boolean inclusive);
    // 等价于subMap(fromKey, true, toKey, false)
    SortedMap<K,V> subMap(K fromKey, K toKey);
    // 等价于headMap(toKey, false)
    SortedMap<K,V> headMap(K toKey);
    // 等价于tailMap(fromKey, true)
    SortedMap<K,V> tailMap(K fromKey);
}
```

## 存储结构

![TreeMap-structure](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/TreeMap-structure.png)

TreeMap只使用到了红黑树，所以它的时间复杂度为O(log n)，我们再来回顾一下红黑树的特性。

（1）每个节点或者是黑色，或者是红色。

（2）根节点是黑色。

（3）每个叶子节点（NIL）是黑色。（注意：这里叶子节点，是指为空(NIL或NULL)的叶子节点！）

（4）如果一个节点是红色的，则它的子节点必须是黑色的。

（5）从一个节点到该节点的子孙节点的所有路径上包含相同数目的黑节点。

## 源码解析

### 属性

```java
/**
 * 比较器，如果没传则key要实现Comparable接口
 */
private final Comparator<? super K> comparator;

/**
 * 根节点
 */
private transient Entry<K,V> root;

/**
 * 元素个数
 */
private transient int size = 0;

/**
 * 修改次数
 */
private transient int modCount = 0;
```

（1）comparator
 
按key的大小排序有两种方式，一种是key实现Comparable接口，一种方式通过构造方法传入比较器。
 
（2）root
 
根节点，TreeMap没有桶的概念，所有的元素都存储在一颗树中。

### Entry内部类

存储节点，典型的红黑树结构。

```java
static final class Entry<K,V> implements Map.Entry<K,V> {
    K key;
    V value;
    Entry<K,V> left;
    Entry<K,V> right;
    Entry<K,V> parent;
    boolean color = BLACK;
}
```

### 构造方法

```java
/**
 * 默认构造方法，key必须实现Comparable接口 
 */
public TreeMap() {
    comparator = null;
}

/**
 * 使用传入的comparator比较两个key的大小
 */
public TreeMap(Comparator<? super K> comparator) {
    this.comparator = comparator;
}
    
/**
 * key必须实现Comparable接口，把传入map中的所有元素保存到新的TreeMap中 
 */
public TreeMap(Map<? extends K, ? extends V> m) {
    comparator = null;
    putAll(m);
}

/**
 * 使用传入map的比较器，并把传入map中的所有元素保存到新的TreeMap中 
 */
public TreeMap(SortedMap<K, ? extends V> m) {
    comparator = m.comparator();
    try {
        buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }
}
```

构造方法主要分成两类，一类是使用comparator比较器，一类是key必须实现Comparable接口。

其实，笔者认为这两种比较方式可以合并成一种，当没有传comparator的时候，可以用以下方式来给comparator赋值，这样后续所有的比较操作都可以使用一样的逻辑处理了，而不用每次都检查comparator为空的时候又用Comparable来实现一遍逻辑。

```java
// 如果comparator为空，则key必须实现Comparable接口，所以这里肯定可以强转
// 这样在构造方法中统一替换掉，后续的逻辑就都一致了
comparator = (k1, k2) -> ((Comparable<? super K>)k1).compareTo(k2);
```

### get(Object key)方法

获取元素，典型的二叉查找树的查找方法。

```java
public V get(Object key) {
    // 根据key查找元素
    Entry<K,V> p = getEntry(key);
    // 找到了返回value值，没找到返回null
    return (p==null ? null : p.value);
}

final Entry<K,V> getEntry(Object key) {
    // 如果comparator不为空，使用comparator的版本获取元素
    if (comparator != null)
        return getEntryUsingComparator(key);
    // 如果key为空返回空指针异常
    if (key == null)
        throw new NullPointerException();
    // 将key强转为Comparable
    @SuppressWarnings("unchecked")
    Comparable<? super K> k = (Comparable<? super K>) key;
    // 从根元素开始遍历
    Entry<K,V> p = root;
    while (p != null) {
        int cmp = k.compareTo(p.key);
        if (cmp < 0)
            // 如果小于0从左子树查找
            p = p.left;
        else if (cmp > 0)
            // 如果大于0从右子树查找
            p = p.right;
        else
            // 如果相等说明找到了直接返回
            return p;
    }
    // 没找到返回null
    return null;
}
    
final Entry<K,V> getEntryUsingComparator(Object key) {
    @SuppressWarnings("unchecked")
    K k = (K) key;
    Comparator<? super K> cpr = comparator;
    if (cpr != null) {
        // 从根元素开始遍历
        Entry<K,V> p = root;
        while (p != null) {
            int cmp = cpr.compare(k, p.key);
            if (cmp < 0)
                // 如果小于0从左子树查找
                p = p.left;
            else if (cmp > 0)
                // 如果大于0从右子树查找
                p = p.right;
            else
                // 如果相等说明找到了直接返回
                return p;
        }
    }
    // 没找到返回null
    return null;
}
```

（1）从root遍历整个树；

（2）如果待查找的key比当前遍历的key小，则在其左子树中查找；

（3）如果待查找的key比当前遍历的key大，则在其右子树中查找；

（4）如果待查找的key与当前遍历的key相等，则找到了该元素，直接返回；

（5）从这里可以看出是否有comparator分化成了两个方法，但是内部逻辑一模一样，因此可见笔者`comparator = (k1, k2) -> ((Comparable<? super K>)k1).compareTo(k2);`这种改造的必要性。

---

我是一条美丽的分割线，前方高能，请做好准备。

---

### 特性再回顾

（1）每个节点或者是黑色，或者是红色。

（2）根节点是黑色。

（3）每个叶子节点（NIL）是黑色。（注意：这里叶子节点，是指为空(NIL或NULL)的叶子节点！）

（4）如果一个节点是红色的，则它的子节点必须是黑色的。

（5）从一个节点到该节点的子孙节点的所有路径上包含相同数目的黑节点。

### 左旋

左旋，就是以某个节点为支点向左旋转。

![left-rotation](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/left-rotation.jpg)

整个左旋过程如下：

（1）将 y的左节点 设为 x的右节点，即将 β 设为 x的右节点；

（2）将 x 设为 y的左节点的父节点，即将 β的父节点 设为 x；

（3）将 x的父节点 设为 y的父节点；

（4）如果 x的父节点 为空节点，则将y设置为根节点；如果x是它父节点的左（右）节点，则将y设置为x父节点的左（右）节点；

（5）将 x 设为 y的左节点；

（6）将 x的父节点 设为 y；

让我们来看看TreeMap中的实现：

```java
/**
 * 以p为支点进行左旋
 * 假设p为图中的x
 */
private void rotateLeft(Entry<K,V> p) {
    if (p != null) {
        // p的右节点，即y
        Entry<K,V> r = p.right;
        
        // （1）将 y的左节点 设为 x的右节点
        p.right = r.left;
        
        // （2）将 x 设为 y的左节点的父节点（如果y的左节点存在的话）
        if (r.left != null)
            r.left.parent = p;

        // （3）将 x的父节点 设为 y的父节点
        r.parent = p.parent;

        // （4）...
        if (p.parent == null)
            // 如果 x的父节点 为空，则将y设置为根节点
            root = r;
        else if (p.parent.left == p)
            // 如果x是它父节点的左节点，则将y设置为x父节点的左节点
            p.parent.left = r;
        else
            // 如果x是它父节点的右节点，则将y设置为x父节点的右节点
            p.parent.right = r;

        // （5）将 x 设为 y的左节点
        r.left = p;

        // （6）将 x的父节点 设为 y
        p.parent = r;
    }
}
```

### 右旋

右旋，就是以某个节点为支点向右旋转。

![right-rotation](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/right-rotation.jpg)

整个右旋过程如下：

（1）将 x的右节点 设为 y的左节点，即 将 β 设为 y的左节点；

（2）将 y 设为 x的右节点的父节点，即 将 β的父节点 设为 y；

（3）将 y的父节点 设为 x的父节点；

（4）如果 y的父节点 是 空节点，则将x设为根节点；如果y是它父节点的左（右）节点，则将x设为y的父节点的左（右）节点；

（5）将 y 设为 x的右节点；

（6）将 y的父节点 设为 x；

让我们来看看TreeMap中的实现：

```java
/**
 * 以p为支点进行右旋
 * 假设p为图中的y
 */
private void rotateRight(Entry<K,V> p) {
    if (p != null) {
        // p的左节点，即x
        Entry<K,V> l = p.left;

        // （1）将 x的右节点 设为 y的左节点
        p.left = l.right;

        // （2）将 y 设为 x的右节点的父节点（如果x有右节点的话）
        if (l.right != null) l.right.parent = p;

        // （3）将 y的父节点 设为 x的父节点
        l.parent = p.parent;

        // （4）...
        if (p.parent == null)
            // 如果 y的父节点 是 空节点，则将x设为根节点
            root = l;
        else if (p.parent.right == p)
            // 如果y是它父节点的右节点，则将x设为y的父节点的右节点
            p.parent.right = l;
        else
            // 如果y是它父节点的左节点，则将x设为y的父节点的左节点
            p.parent.left = l;

        // （5）将 y 设为 x的右节点
        l.right = p;

        // （6）将 y的父节点 设为 x
        p.parent = l;
    }
}
```

未完待续，下一节我们一起探讨红黑树插入元素的操作。

**现在公众号文章没办法留言了，如果有什么疑问或者建议请直接在公众号给我留言。**

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)