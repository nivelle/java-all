### ThreadLocal 核心源码

#### ThreadLocalMap

##### ThreadLocalMap提供了一种为ThreadLocal定制的高效实现,并且自带一种基于弱引用的垃圾清理机制。

````
static class Entry extends WeakReference<ThreadLocal<?>> {
            //这里是实际的保存的值
            Object value;
            
            Entry(ThreadLocal<?> k, Object v) {
                // reference : threadLocal的弱引用
                super(k);
                value = v;
            }
   }

````

#### 重点: 为什么key是threadLocal的弱饮用

````
因为如果这里使用普通的key-value形式来定义存储结构,实质上就会造成节点的生命周期与线程强绑定,只要线程没有销毁,那么节点在GC分析中一直处于可达状态,没办法被回收,而程序本身也无法判断是否可以清理节点。
弱引用是Java中四档引用的第三档，比软引用更加弱一些，如果一个对象没有强引用链可达，那么一般活不过下一次GC。当某个ThreadLocal已经没有强引用可达，则随着它被垃圾回收，在ThreadLocalMap里对应的Entry的键值会失效，这为ThreadLocalMap本身的垃圾清理提供了便利。
````
#### 成员变量

````
         /**
         * The initial capacity -- MUST be a power of two.//初始容量，必须为2的幂
         *
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * The table, resized as necessary.
         * table.length MUST always be a power of two. //Entry表，大小必须为2的幂
         */
        private Entry[] table;

        /**
         * The number of entries in the table. //表里entry的个数
         */
        private int size = 0;

        /**
         * The next size value at which to resize.//重新分配表大小的阈值，默认为0
         */
        private int threshold; // Default to 0
        
        
        /**
         * Set the resize threshold to maintain at worst a 2/3 load factor.//设置resize阈值以维持最坏2/3的装载因子
         */
        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }
        /**
         * 环形意义的下一个索引
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }
        
        /**
         * 环形意义的上一个索引
         */
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }
        
         /**
         * Construct a new map initially containing (firstKey, firstValue).
         * ThreadLocalMaps are constructed lazily, so we only create
         * one when we have at least one entry to put in it.
         * 
         * 构造函数,ThreadLocal是懒加载的,所以构造函数需要默认指定初始键值
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            //初始化table数组
            table = new Entry[INITIAL_CAPACITY];
            //用firstKey的threadLocalHashCode与初始大小16取模得到哈希值
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            //初始化该节点
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            //设定扩容阈值
            setThreshold(INITIAL_CAPACITY);
        }         

````

#### 重点: 由于ThreadLocalMap使用线性探测法来解决散列冲突，所以实际上Entry[]数组在程序逻辑上是作为一个环形存在的。

[![DVRtVe.png](https://s3.ax1x.com/2020/11/17/DVRtVe.png)](https://imgchr.com/i/DVRtVe)

- threadLocal hash运算
````

 /**
     * The difference between successively generated hash codes - turns
     * implicit sequential thread-local IDs into near-optimally spread
     * multiplicative hash values for power-of-two-sized tables.
     *
     */
    private static final int HASH_INCREMENT = 0x61c88647;

    /**
     * Returns the next hash code.
     *//这个魔数的选取与斐波那契散列有关，0x61c88647对应的十进制为1640531527。斐波那契散列的乘数可以用(long) ((1L << 31) * (Math.sqrt(5) - 1))可以得到2654435769，如果把这个值给转为带符号的int，则会得到-1640531527。
     *//换句话说(1L << 32) - (long) ((1L << 31) * (Math.sqrt(5) - 1))得到的结果就是1640531527也就是0x61c88647。
     *//通过理论与实践，当我们用0x61c88647作为魔数累加为每个ThreadLocal分配各自的ID也就是threadLocalHashCode再与2的幂取模，得到的结果分布很均匀。
     */
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }
    
    private final int threadLocalHashCode = nextHashCode();

````

### getEntry(ThreadLocal<?> key)

````
private Entry getEntry(ThreadLocal<?> key) {
            //根据key这个ThreadLocal的ID来获取索引，也即哈希值
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            //对应的entry存在且未失效且弱引用指向的ThreadLocal就是key，则命中返回
            //e.get() 是reference的方法，返回的是引用的目标对象
            if (e != null && e.get() == key)
                return e;
            else
                // 因为用的是线性探测，所以往后找还是有可能能够找到目标Entry的
                return getEntryAfterMiss(key, i, e);
        }
        
````

#### Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e)  从下一个位槽查找threadLocal

````

 private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;
            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null) //threadLocal 已经被垃圾回收;调用expungeStaleEntry来清理无效的entry
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);//线性探测下一个探测点校验是否命中
                e = tab[i];
            }
            return null;
        }


````

#### int expungeStaleEntry(int staleSlot)

**作用:**

1. 就是从staleSlot开始遍历,将无效（弱引用指向对象被回收）清理,即对应entry中的value置为null,将指向这个entry的table[i]置为null,直到扫到空entry

2. 在过程中还会对非空的entry作rehash

````

private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            // expunge[删除] entry at staleSlot
            //因为entry对应的ThreadLocal已经被回收，value设为null，显式断开强引用
            tab[staleSlot].value = null;
            //显式设置该entry为null，以便垃圾回收
            tab[staleSlot] = null;
            size--;

            // Rehash until we encounter null
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);(e = tab[i]) != null;i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                //清理对应ThreadLocal已经被回收的entry
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    //对于还没有被回收的情况，需要做一次rehash。
                    //如果对应的ThreadLocal的ID对len取模出来的索引h不为当前位置i,则从h向后线性探测到第一个空的slot，把当前的entry给挪过去
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        //原位置置为null
                        tab[i] = null;
                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        //找到第一个null的slot把rhash后的节点放进去
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            //返回staleSlot之后第一个空的slot索引
            return i;
        }

````

### private void set(ThreadLocal<?> key, Object value)

````
private void set(ThreadLocal<?> key, Object value) {

            // We don't use a fast path as with get() because it is at
            // least as common to use set() to create new entries as
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            // 线性探测
            for (Entry e = tab[i];e != null;e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();
                if (k == key) {
                    //如果已经存在，则覆盖
                    e.value = value;
                    return;
                }
                //如果存在已经失效的threadLocal
                if (k == null) {
                    //替换失效的entry
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }
            //未找到 threadLocal 也不存在失效的sloat,则在新的sloat放入新增加的 entry
            tab[i] = new Entry(key, value);
            //元素个数加1
            int sz = ++size;
            // 如果已经达到阀值，则进行rehash
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

````

#### private void replaceStaleEntry(ThreadLocal<?> key, Object value,int staleSlot) //替换失效的 sloat

````

private void replaceStaleEntry(ThreadLocal<?> key, Object value,int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            // Back up to check for prior stale entry in current run.
            // We clean out whole runs at a time to avoid continual
            // incremental rehashing due to garbage collector freeing
            // up refs in bunches (i.e., whenever the collector runs).
            int slotToExpunge = staleSlot;
            //向前扫描，查找最前的一个无效slot
            for (int i = prevIndex(staleSlot, len);(e = tab[i]) != null;i = prevIndex(i, len))
                if (e.get() == null){
                    slotToExpunge = i;
                }
            // Find either the key or trailing null slot of run, whichever
            // occurs first
            //向后遍历table
            for (int i = nextIndex(staleSlot, len);(e = tab[i]) != null;i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                // If we find key, then we need to swap it
                // with the stale entry to maintain hash table order.
                // The newly stale slot, or any other stale slot
                // encountered above it, can then be sent to expungeStaleEntry
                // to remove or rehash all of the other entries in run.
                //找到了key，将其与无效的slot交换
                if (k == key) {
                    // 更新对应slot的value值
                    e.value = value;                
                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;
                    // Start expunge at preceding stale entry if it exists
                    /*
                     * 如果在整个扫描过程中（包括函数一开始的向前扫描与i之前的向后扫描）
                     * 找到了之前的无效slot则以那个位置作为清理的起点，
                     * 否则则以当前的i作为清理起点
                     */
                    if (slotToExpunge == staleSlot){
                        slotToExpunge = i;
                    }
                    // 从slotToExpunge开始做一次连续段的清理，再做一次启发式清理
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                // If we didn't find stale entry on backward scan, the
                // first stale entry seen while scanning for key is the
                // first still present in the run.
                //如果当前的slot已经无效，并且向前扫描过程中没有无效slot，则更新slotToExpunge为当前位置
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            // If key not found, put new entry in stale slot
            //如果key在table中不存在，则在原地放一个即可
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            // If there are any other stale entries in run, expunge them
            //在探测过程中如果发现任何无效slot，则做一次清理（连续段清理+启发式清理）
            if (slotToExpunge != staleSlot){
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
            }
        }

````

#### boolean cleanSomeSlots(int i, int n)

````
/**
 * 启发式地清理slot,
 * i对应entry是非无效（指向的ThreadLocal没被回收，或者entry本身为空）
 * n是用于控制控制扫描次数的
 * 正常情况下如果log n次扫描没有发现无效slot,函数就结束了
 * 但是如果发现了无效的slot，将n置为table的长度len，做一次连续段的清理
 * 再从下一个空的slot开始继续扫描
 * 
 * 这个函数有两处地方会被调用，一处是插入的时候可能会被调用，另外个是在替换无效slot的时候可能会被调用，
 * 区别是前者传入的n为元素个数，后者为table的容量
 */
private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            //i在任何情况下自己都不会是一个无效slot，所以从下一个开始判断
            do {
                i = nextIndex(i, len);
                Entry e = tab[i];
                if (e != null && e.get() == null) {
                   //扩大扫描控制因子
                    n = len;
                    removed = true;
                    // 清理一个连续段
                    i = expungeStaleEntry(i);
                }
            } while ( (n >>>= 1) != 0);
            return removed;
        }

````

#### rehash()
````
private void rehash() {
            // 做一次全量清理
            expungeStaleEntries();

           /*
            * 因为做了一次清理，所以size很可能会变小。
            * ThreadLocalMap这里的实现是调低阈值来判断是否需要扩容，
            * threshold默认为len*2/3，所以这里的threshold - threshold / 4相当于len/2
            */
            if (size >= threshold - threshold / 4)
                resize();
        }

````

### remove(ThreadLocal<?> key)

````
private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    //显示断开弱引用
                    e.clear();
                    //进行段清理
                    expungeStaleEntry(i);
                    return;
                }
            }
        }
````

### 重点：内存泄漏

````
1. 认为ThreadLocal会引起内存泄漏的说法是因为如果一个ThreadLocal对象被回收了,
我们往里面放的value对于【当前线程->当前线程的threadLocals(ThreadLocal.ThreadLocalMap对象）->Entry数组->某个entry.value】这样一条强引用链是可达的,因此value不会被回收。

2. 认为ThreadLocal不会引起内存泄漏的说法是因为ThreadLocal.ThreadLocalMap源码实现中自带一套自我清理的机制。
   
3. 显示地调用remove,或者调用ThreadLocal的get和set方法都有很高的概率会顺便清理掉无效对象，断开value强引用，从而大对象被收集器回收
````











