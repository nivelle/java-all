---
layout: post
title:  "数据结构源码学习--HashMap"
date:   2019-05-09 01:06:05
categories: java
tags: hashMap
excerpt: hashMap
---



* content
{:toc}


Hash table based implementation of the <tt>Map</tt> interface.  This implementation provides all of the optional map operations,and permits<tt>null</tt> values and the <tt>null</tt> key.The HashMap class is roughly equivalent to HashTable, except that it is unsynchronized and permits nulls.)

**hashMap 允许 null key 和 null value ,大体上等于 hashTable, 不同点在于hashMap是非同步的以及允许空值**

This class makes no guarantees as to the order of the map; in particular,it does not guarantee that the order will remain constant over time.
  
 
**对于 get 和 put 操纵是常量时间级别的**

Iteration【迭代】 over collection views requires time proportional【成比例】 to the "capacity" of the HashMap instance (the number of buckets)
plus its size (the number of key-value mappings).it's very important not to set the initial capacity too high【初始容量不能太高】 (or the load factor too low)if iteration performance is important.
  
**影响hashMap性能的两个属性是：初始容量(initial capacity)和加载因子(load factor)**

When the number of entries in the hash table exceeds【超过】 the product of the load factor and the current capacity,the hash table is rehashed (that is, internal data structures are rebuilt) so that the hash table has approximately twice【大约两倍】 the
number of buckets.
 
**根据时间和空间的因素,默认的加载因子是 0.75**

hashMap 非同步，需要外部同步来实现同步。或者可以使用 Collections.synchronizedMap

在返回迭代器之后,除非通过迭代器的remove方法,其他改变hashMap结构的方法都有可能会在迭代期间抛出 ConcurrentModificationException 异常。
  
采用快速失败机制,而不是在一个不确定的未来时机抛出异常。同时快速失败机制并不是可靠的,仅仅是力所能及的抛出异常。不能依赖快速失败机制来
  
Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to make any hard guarantees in thepresence of unsynchronized concurrent modification.  Fail-fast iterators throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
Therefore, it would be wrong to write a program that depended on this exception for its correctness: the fail-fast behavior of iteratorsshould be used only to detect bugs.
 
treeNode 默认hashCode 排序,如果实现了Comparable 接口,则按照比较器进行排序。
  
### 静态常量

```
//初始化默认容量2的4次方,必须是2的整数倍
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
  
//最大为2的30次方
static final int MAXIMUM_CAPACITY = 1 << 30;
  
//默认加载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f;
  
//链表数超过8个则考虑进行转换为红黑树
static final int TREEIFY_THRESHOLD = 8;
  
//当执行resize操作时，当桶中bin的数量少于UNTREEIFY_THRESHOLD时使用链表来代替树。默认值是6
static final int UNTREEIFY_THRESHOLD = 6;
  
//当集合中的容量大于这个值时，表中的桶才能进行树形化 ，否则桶内元素太多时会扩容而不是树形化 \
为了避免进行扩容、树形化选择的冲突，这个值不能小于 4  TREEIFY_THRESHOLD
static final int MIN_TREEIFY_CAPACITY = 64;
  
//装载因子，是用来衡量 HashMap 满的程度，计算HashMap的实时装载因子的方法为：size/capacity，而不是占用桶的数量去除以capacity。capacity 是桶的数量，也就是 table 的长度length。
//默认的负载因子0.75 是对空间和时间效率的一个平衡选择，建议大家不要修改，除非在时间和空间比较特殊的情况下，如果内存空间很多而又对时间效率要求很高，可以降低负载因子loadFactor 的值；相反，如果内存空间紧张而对时间效率要求不高，可以增加负载因子 loadFactor 的值，这个值可以大于1。
final float loadFactory
  
```
#### 内部方法
 
- 默认加载因子:0.75
  
```

final void putMapEntries (Map < ? extends K, ? extends V > m,boolean evict){
            int s = m.size();
            if (s > 0) {
                if (table == null) { // pre-size
                    float ft = ((float) s / loadFactor) + 1.0F;
                    int t = ((ft < (float) MAXIMUM_CAPACITY) ?
                            (int) ft : MAXIMUM_CAPACITY);
                    if (t > threshold)
                         //table的容量是离t最近的2的整次幂
                        threshold = tableSizeFor(t);
                } else if (s > threshold)
                    //若table已经初始化,容量不够则需要进行扩容
                    resize();
                for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                    K key = e.getKey();
                    V value = e.getValue();
                    putVal(hash(key), key, value, false, evict);
                }
            }
        }
        
```

##### 添加元素

```

/**
 * Map.put和其他相关方法的实现需要的方法
 * 
 * @param hash 指定参数key的哈希值
 * @param key 指定参数key
 * @param value 指定参数value
 * @param onlyIfAbsent 如果为true，即使指定参数key在map中已经存在，也不会替换value
 * @param evict 如果为false，数组table在创建模式中
 * @return 如果value被替换，则返回旧的value，否则返回null。当然，可能key对应的value就是null。
 */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    //如果哈希表为空，调用resize()创建一个哈希表，并用变量n记录哈希表长度
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    //如果指定参数hash在表中没有对应的桶，即为没有碰撞
    if ((p = tab[i = (n - 1) & hash]) == null)
        //直接将键值对插入到map中即可
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        //如果碰撞了，且桶中的第一个节点就匹配了
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            //将桶中的第一个节点记录起来
            e = p;
        //如果桶中的第一个节点没有匹配上，且桶内为红黑树结构，则调用红黑树对应的方法插入键值对
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        //不是红黑树结构，那么就肯定是链式结构
        else {
            //遍历链式结构
            for (int binCount = 0; ; ++binCount) {
                //如果到了链表尾部
                if ((e = p.next) == null) {
                    //在链表尾部插入键值对
                    p.next = newNode(hash, key, value, null);
                    //如果链的长度大于TREEIFY_THRESHOLD这个临界值，则把链变为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    //跳出循环
                    break;
                }
                //如果找到了重复的key，判断链表中结点的key值与插入的元素的key值是否相等，如果相等，跳出循环
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                //用于遍历桶中的链表，与前面的e = p.next组合，可以遍历链表
                p = e;
            }
        }
        //如果key映射的节点不为null
        if (e != null) { // existing mapping for key
            //记录节点的vlaue
            V oldValue = e.value;
            //如果onlyIfAbsent为false，或者oldValue为null
            if (!onlyIfAbsent || oldValue == null)
                //替换value
                e.value = value;
            //访问后回调
            afterNodeAccess(e);
            //返回节点的旧值
            return oldValue;
        }
    }
    //结构型修改次数+1
    ++modCount;
    //判断是否需要扩容
    if (++size > threshold)
        resize();
    //插入后回调
    afterNodeInsertion(evict);
    return null;
}

```

putVal方法可以分为下面的几个步骤：

- 如果哈希表为空，调用resize()创建一个哈希表。

- 如果指定参数hash在表中没有对应的桶，即为没有碰撞，直接将键值对插入到哈希表中即可。

- 如果有碰撞，遍历桶，找到key映射的节点 
  1. 桶中的第一个节点就匹配了，将桶中的第一个节点记录起来。
  2. 如果桶中的第一个节点没有匹配，且桶中结构为红黑树，则调用红黑树对应的方法插入键值对。
  3. 如果不是红黑树，那么就肯定是链表。遍历链表，如果找到了key映射的节点，就记录这个节点，退出循环。如果没有找到，在链表尾部插入节点。插入后，如果链的长度大于TREEIFY_THRESHOLD这个临界值，则使用treeifyBin方法把链表转为红黑树。

- 如果找到了key映射的节点，且节点不为null 
  1. 记录节点的vlaue。
  2. 如果参数onlyIfAbsent为false，或者oldValue为null，替换value，否则不替换。
  3. 返回记录下来的节点的value。
  4. 如果没有找到key映射的节点（2、3步中讲了，这种情况会插入到hashMap中），插入节点后size会加1，这时要检查size是否大于临界值threshold，如果大于会使用resize方法进行扩容。


##### 扩容函数

Initializes or doubles table size.If null, allocates in accord with initial capacity target held in
field threshold.Otherwise, because we are using power -of - two expansion, the elements from each bin must either stay at
same index, or move with a power of two offset in the new table

resize方法非常巧妙，因为每次扩容都是翻倍，与原来计算（n-1）&hash的结果相比，节点要么就在原来的位置，要么就被分配到“原位置+旧容量”这个位置。

```

/**
 * 对table进行初始化或者扩容。
 * 如果table为null，则对table进行初始化
 * 如果对table扩容，因为每次扩容都是翻倍，与原来计算（n-1）&hash的结果相比，节点要么就在原来的位置，要么就被分配到“原位置+旧容量”这个位置。
 */
final Node<K,V>[] resize() {
    //新建oldTab数组保存扩容前的数组table
    Node<K,V>[] oldTab = table;
    //使用变量oldCap扩容前table的容量
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    //保存扩容前的临界值
    int oldThr = threshold;
    int newCap, newThr = 0;
    //如果扩容前的容量 > 0
    if (oldCap > 0) {
        //如果当前容量>=MAXIMUM_CAPACITY
        if (oldCap >= MAXIMUM_CAPACITY) {
            //扩容临界值提高到正无穷
            threshold = Integer.MAX_VALUE;
            //无法进行扩容，返回原来的数组
            return oldTab;
        }
        //如果现在容量的两倍小于MAXIMUM_CAPACITY且现在的容量大于DEFAULT_INITIAL_CAPACITY
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&oldCap >= DEFAULT_INITIAL_CAPACITY)
            //临界值变为原来的2倍
            newThr = oldThr << 1; 
    }//如果旧容量 <= 0，而且旧临界值 > 0
    else if (oldThr > 0) 
        //数组的新容量设置为老数组扩容的临界值
        newCap = oldThr;
    else {//如果旧容量 <= 0，且旧临界值 <= 0，新容量扩充为默认初始化容量，新临界值为DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {//在当上面的条件判断中，只有oldThr > 0成立时，newThr == 0
        //ft为临时临界值，下面会确定这个临界值是否合法，如果合法，那就是真正的临界值
        float ft = (float)newCap * loadFactor;
        //当新容量< MAXIMUM_CAPACITY且ft < (float)MAXIMUM_CAPACITY，新的临界值为ft，否则为Integer.MAX_VALUE
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    //将扩容后hashMap的临界值设置为newThr
    threshold = newThr;
    //创建新的table，初始化容量为newCap
    @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    //修改hashMap的table为新建的newTab
    table = newTab;
    //如果旧table不为空，将旧table中的元素复制到新的table中
    if (oldTab != null) {
        //遍历旧哈希表的每个桶，将旧哈希表中的桶复制到新的哈希表中
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            //如果旧桶不为null，使用e记录旧桶
            if ((e = oldTab[j]) != null) {
                //将旧桶置为null
                oldTab[j] = null;
                //如果旧桶中只有一个node
                if (e.next == null)
                    //将e也就是oldTab[j]放入newTab中e.hash & (newCap - 1)的位置
                    newTab[e.hash & (newCap - 1)] = e;
                //如果旧桶中的结构为红黑树
                else if (e instanceof TreeNode)
                    //将树中的node分离
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { //如果旧桶中的结构为链表。这段没有仔细研究
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    //遍历整个链表中的节点
                    do {
                        next = e.next;
                        //
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}

```

**resize的步骤总结为** 

1. 计算扩容后的容量，临界值。
2. 将hashMap的临界值修改为扩容后的临界值
3. 根据扩容后的容量新建数组，然后将hashMap的table的引用指向新数组。
4. 将旧数组的元素复制到table中。



  
  