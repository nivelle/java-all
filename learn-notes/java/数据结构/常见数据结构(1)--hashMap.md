---
layout: post
title:  "常见数据结构(1)--hashMap"
date:   2017-11-28 01:06:05
categories: 技术
tags: Interview
excerpt: 常见数据结构
---

### 数据结构

#### hashMap

##### HashMap中的两个方法

hashCode和equals

```

//jni,调用底层其他语言实现
public native int hashCode();

// 默认同==,直接比较对象
public boolean equals(object obj){
    return (this == obj);
}

```

String 类中重写了equals方法,比较字符串值,看下源码:

```

public boolean equals(Object anObject){
    if(this == anObject){
        return true;
    }
    if(anObject instanceof String){
        String anotherString =(String)anObject;
        int n = value.length; //value String 类内部标识String组成的字符数组
        if(n == anotherString.value.length){
            char v1[] = value;
            char v2[] = anotherString.value;
            int i =0;
            while(n-- !=0){
                if(v1[i]!=v2[i])
                {
                    return false;
                    i++;
                }
                return true;
            }
        }
        return false;
    }
}


```

重写equals要满足几个条件:

- 自反性:对于任何非空引用值 x，x.equals(x) 都应返回 true
- 对称性:对于任何非空引用值 x 和 y，当且仅当 y.equals(x) 返回 true 时，x.equals(y) 才应返回 true。 
- 传递性:对于任何非空引用值 x、y 和 z，如果 x.equals(y) 返回 true，并且 y.equals(z) 返回 true，那么 x.equals(z) 应返回 true。
- 一致性:对于任何非空引用值 x 和 y，多次调用 x.equals(y) 始终返回 true 或始终返回 false，前提是对象上 equals 比较中所用的信息没有被修改。
- 对于任何非空引用x,x.equals(null)都应该返回false

Object 类的equals方法实现对象上差别可能性最大的相等关系;对于任何非空引用值x和y,当且仅当x和y引用同一个对象时,此方法才返回true(x==y具有值true).当次方法被重写时,通常有必要重写hashCode方法,以维护hashCode方法的常规协定,该协定申明相等对象必须有相等的哈希码.


下面来说说hashCode方法,这个方法我们平时通常是用不到的,它是为哈希家族的集合类框架(hashMap,HashSet,HashTable)提供服务,hashCode的常规协定是:

- 在java应用程序执行期间,在同一对象上多次调用hashCode方法时,必须一致地返回相同的整数,前提是对象上equals比较中用的信息没有被修改.从某一应用程序的一次执行到同一应用程序的另一次执行,该整数无需保持一致.
- 如果根据equals(object)方法,两个对象时相等的,那么在两个对象中的每个对象上调用hashCode方法都必须生成相同的整数结果.
- 以下情况不是必需的:如果根据equals(Object)方法,两个对象不相等,那么在两个对象中任一对象上调用hashCode方法必定会生成不同的整数结果.但是,为不相等的对象生成不同整数结果可以提高哈希表的性能.


### java.util.HashMap<K,V>

java.lang.object 
    继承者
      java.util.AbstractMap<K,V>
         继承者
           java.util.HashMap<K,V>
          

所有已经实现的接口:

```
Serializable,Cloneable,Map<K,V>

```
直接已知子类:


```
LinkedHashMap,PrinterStateReasons

```

HashMap中我们最常用的就是put(K,V)和get(K).HashMap的K值是唯一的,如何保证唯一性呢?如果用equals()比较,随着内部元素的增多,put和get的效率会越来越低,这里的时间复杂度是O(n). 实际上,HashMap很少会用到equals方法,因为其内通过一个哈希表管理所有元素,哈希是通过hash单词音译过来的,也可以称为散列表,哈希算法可以快速的存取元素,当我们调用put存值时,HashMap首先会调用k的hashCode方法,获取哈希码,通过哈希码快速找到某个存放位置,这个位置可以被称为bucketIndex,通过上面所述hashCode的协定可知,如果hashCode不同,equals一定为false,如果hashCode相同,equals不一定为true.所以,hashCode可能存在冲突的情况,专业叫碰撞,当碰撞发生时,计算出的bucketIndex也是相同的,这时会取到bucketIndex位置已存储的元素,最终通过equals来比较,equals方法就是哈希冲突的时候才会执行的方法.HashMap通过hashCode和equals最终判断出K是否已存在，如果已存在，则使用新V值替换旧V值，并返回旧V值，如果不存在 ，则存放新的键值对<K, V>到bucketIndex位置。

![image](http://7xpuj1.com1.z0.glb.clouddn.com/hashMap.jpg)


由以上图可知:

- HashMap通过键的hashCode来快速的存取元素。
- 当不同的对象hashCode发生碰撞时，HashMap通过单链表来解决，将新元素加入链表表头，通过next指向原有的元素。单链表在Java中的实现就是对象的引用(复合)。

HashMap put方法源码:

```

public V put(K key, V value) {  
    // 处理key为null，HashMap允许key和value为null  
    if (key == null)  
        return putForNullKey(value);  
    // 得到key的哈希码  
    int hash = hash(key);  
    // 通过哈希码计算出bucketIndex  
    int i = indexFor(hash, table.length);  
    // 取出bucketIndex位置上的元素，并循环单链表，判断key是否已存在  
    for (Entry<K,V> e = table[i]; e != null; e = e.next) {  
        Object k;  
        // 哈希码相同并且对象相同时  
        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {  
            // 新值替换旧值，并返回旧值  
            V oldValue = e.value;  
            e.value = value;  
            e.recordAccess(this);  
            return oldValue;  
        }  
    }  
  
    // key不存在时，加入新元素  
    modCount++;  
    addEntry(hash, key, value, i);  
    return null;  
} 

```

### hashMap容量和加载因子

hashMap有两个参数影响其性能:初始容量和加载因子.默认初始容量是16,加载因子是0.75.容量是哈希表中桶(Entry数组)的数量，初始容量只是哈希表在创建时的容量。加载因子是哈希表在其容量自动增加之前可以达到多满的一种尺度。当哈希表中的条目数超出了加载因子与当前容量的乘积时，通过调用 rehash 方法将容量翻倍。


hashMap构造函数源码:

```
public HashMap(int initialCapacity, float loadFactor) {  
    // 参数判断，不合法抛出运行时异常  
    if (initialCapacity < 0)  
        throw new IllegalArgumentException("Illegal initial capacity: " +  
                                           initialCapacity);  
    if (initialCapacity > MAXIMUM_CAPACITY)  
        initialCapacity = MAXIMUM_CAPACITY;  
    if (loadFactor <= 0 || Float.isNaN(loadFactor))  
        throw new IllegalArgumentException("Illegal load factor: " +  
                                           loadFactor);  
  
    // Find a power of 2 >= initialCapacity  
    // 这里需要注意一下  
    int capacity = 1;  
    while (capacity < initialCapacity)  
        capacity <<= 1;  
  
    // 设置加载因子  
    this.loadFactor = loadFactor;  
    // 设置下次扩容临界值  
    threshold = (int)Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);  
    // 初始化哈希表  
    table = new Entry[capacity];  
    useAltHashing = sun.misc.VM.isBooted() &&  
            (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);  
    init();  
}  


```

jdk7中resize，只有当 size>=threshold并且 table中的那个槽中已经有Entry时，才会发生resize。即有可能虽然size>=threshold，但是必须等到每个槽都至少有一个Entry时，才会扩容。还有注意每次resize都会扩大一倍容量

**为什么容量为2的n次幂呢?**

HashMap中的数据结构是数组+单链表的组合，我们希望的是元素存放的更均匀，最理想的效果是，Entry数组中每个位置都只有一个元素，这样，查询的时候效率最高，不需要遍历单链表，也不需要通过equals去比较K，而且空间利用率最大。那如何计算才会分布最均匀呢？我们首先想到的就是%运算，哈希值%容量=bucketIndex，SUN的大师们是否也是如此做的呢？我们阅读一下这段源码：


```
static int indexFor(int h, int length) {  
    return h & (length-1);  //h 为hash值,length是目前容量。
}  

```

当容量一定是2^n时，h & (length - 1) == h % length，它俩是等价不等效的，位运算效率非常高，实际开发中，很多的数值运算以及逻辑判断都可以转换成位运算，但是位运算通常是难以理解的，因为其本身就是给电脑运算的，运算的是二进制，而不是给人类运算的，人类运算的是十进制，这也是位运算在普遍的开发者中间不太流行的原因(门槛太高)。



当重新调整HashMap大小的时候，确实存在条件竞争，因为如果两个线程都发现HashMap需要重新调整大小了，它们会同时试着调整大小。在调整大小的过程中，存储在链表中的元素的次序会反过来，因为移动到新的bucket位置的时候，HashMap并不会将元素放在链表的尾部，而是放在头部，这是为了避免尾部遍历(tail traversing)。直接采用队头插入,会使得的链表数据倒序,但这样直接插入效率更高.



 本文来自：高爽|Coder，原文地址：http://blog.csdn.net/ghsau/article/details/16843543
 
 
 
 ---
 
 
 ###  JDK8中的HashMap
 
 
 如果成百上千的节点在hash时发生碰撞,存储在一个链表中,那就不可避免花费o(N)的查找时间,这将是巨大的性能损失.链表的时间复杂度为O(n),而红黑树一直是O(longn),因此JDK8中采用的是位桶+链表/红黑树（有关红黑树请查看红黑树）的方式，也是非线程安全的。当某个位桶的链表的长度达到某个阀值的时候，这个链表就将转换成红黑树。
 

JDK8中HashMap源码:

JDk中Entry的名字变成了Node,原因是和红黑树的实现TreeNode相关联

```

transient Node<K,V>[] table;


```

 
当冲突节点数不小于8-1时，转换成红黑树:

```

static final int TREEIFY_THRESHOLD = 8;


```
 
 
 put方法在JDK8中有了很大的改变:

 ```

        return putVal(hash(key), key, value, false, true);
 }
 
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab;
    Node<K,V> p; 
    int n, i;
    //如果当前map中无数据，执行resize方法。并且返回n
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
     //如果要插入的键值对要存放的这个位置刚好没有元素，那么把他封装成Node对象，放在这个位置上就完事了
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
    //否则的话，说明这上面有元素
        else {
            Node<K,V> e; K k;
        //如果这个元素的key与要插入的一样，那么就替换一下，也完事。
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
        //1.如果当前节点是TreeNode类型的数据，执行putTreeVal方法
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
        //还是遍历这条链子上的数据，跟jdk7没什么区别
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
            //2.完成了操作后多做了一件事情，判断，并且可能执行treeifyBin方法
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null) //true || --
                    e.value = value;
           //3.
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
    //判断阈值，决定是否扩容
        if (++size > threshold)
            resize();
        //4.
        afterNodeInsertion(evict);
        return null;
    }



 ```
