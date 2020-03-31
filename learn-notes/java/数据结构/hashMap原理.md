---
layout: post
title:  "hashMap原理"
date:   2018-08-24 01:06:05
categories: 知识点
tags: 数据结构
excerpt: 数据结构
---


* content
{:toc}

### 数据结构

- HashMap 是一个关联数组、哈希表，它是线程不安全的，允许key为null,value为null。遍历时无序。 
其底层数据结构是数组称之为哈希桶，每个桶里面放的是链表，链表中的每个节点，就是哈希表中的每个元素。 
在JDK8中，当链表长度达到8，会转化成红黑树，以提升它的查询、插入效率，它实现了Map<K,V>, Cloneable, Serializable接口。

### 常见操作

##### put方法
  
  1. 据key计算当前Node的hash值，用于定位对象在HashMap数组的哪个节点。
  2. 判断table有没有初始化，如果没有初始化，则调用resize（）方法为table初始化容量，以及threshold的值。
  3. 根据hash值定位该key对应的数组索引，如果对应的数组索引位置无值，则调用newNode（）方法，为该索引创建Node节点
  4. 如果根据hash值定位的数组索引有Node，并且Node中的key和需要新增的key相等，则将对应的value值更新
  5. 如果在已有的table中根据hash找到Node，其中Node中的hash值和新增的hash相等，但是key值不相等的，那么创建新的Node，放到当前已存在的Node的链表尾部。如果当前Node的长度大于8,则调用treeifyBin（）方法扩大table数组的容量，或者将当前索引的所有Node节点变成TreeNode节点，变成TreeNode节点的原因是由于TreeNode节点组成的链表索引元素会快很多。
  6. 将当前的key-value数量标识size自增，然后和threshold对比，如果大于threshold的值，则调用resize（）方法，扩大当前HashMap对象的存储容量。
  7. 返回oldValue或者null。

##### get方法

1. 根据key计算hash值

2. 根据hash值和key  确定所需要返回的结果，如果不存在，则返回空。

##### treeifyBin

1. 将链表转化成红黑树
2. 转化为树的时候，如果是第一次初始化table或者数组长度小余64的时候不进行转化，而是继续扩容

##### resize

1. 如果当前数组为空，则初始化当前数组(容量：16,阀值：12)

2. 如果当前table数组不为空，则将当前的table数组扩大两倍，同时将阈值（threshold）扩大两倍数组长度和阈值扩大成两倍之后，将之前table数组中的值全部放到新的table中去


```
void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
 
        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable, initHashSeedAsNeeded(newCapacity));
        table = newTable;
        threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
}
void transfer(Entry[] newTable, boolean rehash) {
        int newCapacity = newTable.length;
        for (Entry<K,V> e : table) {
            while(null != e) {
                Entry<K,V> next = e.next;
                if (rehash) {
                    e.hash = null == e.key ? 0 : hash(e.key);
                }
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
}
```



#### equals方法实现


```
public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
```



### 知识要点

- 【8】树化链表节点的阈值，当某个链表的长度大于或者等于这个长度，则扩大数组容量，或者树化链表
- 【16】初始容量，必须是2的倍数，默认是16
- 【2^30】最大所能容纳的key-value 个数
- 【0.75f】默认的加载因子
- 初始化指定初始化容量并不会严格初始化指定的容量，而是找最近的2的整数倍
- 容量为2的整数倍是为了在计算位置的时候，使得index =  HashCode（Key） &  （Length - 1） 等价与 index =  HashCode（Key） % Length ；length-1 二进制后四位全是1，这样Hash算法最终得到的index结果，完全取决于Key的Hashcode值的最后几位。
- 如果key为null,则赋值给它的哈希值是0，indexFor获得它的位置是table[0]

- rehash的时候，可能会出现链表环，所以是线程不安全的。

- 四种遍历方法

```
map.keySet()

map.entrySet().iterator()

map.entrySet()

map.values()

```
