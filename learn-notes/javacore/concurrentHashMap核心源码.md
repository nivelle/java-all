## 构造函数

private transient volatile int sizeCtl;

**sizeCtl的高16位存储着rs这个扩容邮戳 sizeCtl的低16位存储着扩容线程数加1，即(1+nThreads)**

1. -1,表示有线程正在进行初始化操作

2. - (1+ nThreads),表示有n个线程正在一起扩容

3. 0 , 默认值,后续在真正初始化的时候使用默认容量

4. > 0 初始化或者扩容完成后下一次扩容门槛


```

private static final int MAXIMUM_CAPACITY = 1 << 30;##最大容量

static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;##数组最大容量

public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0){ ##如果初始化容量小于0则报参数异常
            throw new IllegalArgumentException();
        } 
        ## 初始化值大于最大值的一半,则设置为最大容量,(初始容量+ 初始容量[无符号右移1位]>>>1+1) 否则1.5倍+1
        int cap = ((initialCapacity >= (MAXIMU_CAPACITY >>> 1)) ?MAXIMUM_CAPACITY :(initialCapacity + (initialCapacity >>> 1) + 1));
        this.sizeCtl = cap;
    }

```

## 添加元素

（1）如果桶数组未初始化，则初始化；

（2）如果待插入的元素所在的桶为空，则尝试把此元素直接插入到桶的第一个位置；

（3）如果正在扩容，则当前线程一起加入到扩容的过程中；

（4）如果待插入的元素所在的桶不为空且不在迁移元素，则锁住这个桶（分段锁）；

（5）如果当前桶中元素以链表方式存储，则在链表中寻找该元素或者插入元素；

（6）如果当前桶中元素以红黑树方式存储，则在红黑树中寻找该元素或者插入元素；

（7）如果元素存在，则返回旧值；

（8）如果元素不存在，整个Map的元素个数加1，并检查是否需要扩容；

```
final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) {
           throw new NullPointerException();##key 和 value 都不能为null
        }
        ##计算hash值
        int hash = spread(key.hashCode());
        ##要插入的元素所在桶的元素个数
        int binCount = 0;
        ##死循环,结合CAS使用(如果CAS失败,则会重新取整个桶进行下面的流程)
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; 
            int n;
            int i; 
            int fh;##数组桶第一个元素的hash值
            if (tab == null || (n = tab.length) == 0){
                ##如果桶未初始化或者桶个数为0，则初始化桶
                tab = initTable();
             ##如果要插入的元素所在的桶还没有元素，则把这个元素插入到这个桶中
            } else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {            
                if (casTabAt(tab, i, null,new Node<K,V>(hash, key, value, null))){
                       ##如果使用CAS插入元素时,发现已经有元素了,则进入下一次循环,重新操作
                       ##如果使用CAS插入元素成功,则break跳出循环,流程结束
                       break;  
                }                 
            }
            else if ((fh = f.hash) == MOVED){
                ## 如果要插入的元素所在的桶的第一个元素的hash是MOVED，则当前线程帮忙一起迁移元素
                tab = helpTransfer(tab, f);
            } else {
                ##如果桶不为空且不再迁移元素,则锁住这个桶(分段锁),查找要插入的元素是否在这个桶中，存在则替换值(onlyIfAbsent=false),不存在则插入链表结尾或插入树中
                V oldVal = null;
                synchronized (f) {
                   ##再次检测第一个元素是否有变化，如果有变化则进入下一次循环，从头来过
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {## 如果第一个元素的hash值大于等于0（说明不是在迁移，也不是树）那就是桶中的元素使用的是链表方式存储                                      
                            binCount = 1;## 桶中元素个数赋值为1                                        
                            for (Node<K,V> e = f;; ++binCount) {##遍历桶元素,遍历一次binCount+1
                                K ek;
                                if (e.hash == hash &&((ek = e.key) == key ||(ek != null && key.equals(ek)))) {## 如果找到了这个元素，则赋值了新值（onlyIfAbsent=false）                                                                                                              
                                    oldVal = e.val;
                                    if (!onlyIfAbsent){
                                        e.val = value;##设置新值
                                    }
                                    break;
                                }
                                ##若对应的key不存在,则把它插入到链表结尾并退出循环
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,value, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) {## 如果第一个元素是树节点                                                         
                            Node<K,V> p;
                            binCount = 2;##如果为树,那么赋值桶中元素个数赋值为2
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,value)) != null) {##调用红黑树的插入方法插入元素,如果成功插入则返回null,否则返回寻找到的节点
                                oldVal = p.val;
                                if (!onlyIfAbsent){
                                    p.val = value;
                                }
                            }
                        }
                    }
                }
                ## 如果binCount不为0，说明成功插入了元素或者寻找到了元素                
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD){## 如果链表元素个数达到了8，则尝试树化,因为上面把元素插入到树中时，binCount只赋值了2，并没有计算整个树中元素的个数,所以不会树化                                                       
                        treeifyBin(tab, i);
                    }
                    if (oldVal != null){##如果要插入的元素已经存在，则返回旧值
                        return oldVal;
                    }
                    break;
                }
            }
        }
        ##成功插入元素，元素个数加1（是否要扩容在这个里面）       
        addCount(1L, binCount);
        return null;
    }

```

## addCount(long x,int check);##增加桶中元素个数,每次添加元素后，元素数量加1，并判断是否达到扩容门槛，达到了则进行扩容或协助扩容。

（1）元素个数的存储方式类似于LongAdder类，存储在不同的段上，减少不同线程同时更新size时的冲突；

（2）计算元素个数时把这些段的值及baseCount相加算出总的元素个数；

（3）正常情况下sizeCtl存储着扩容门槛，扩容门槛为容量的0.75倍；

（4）扩容时sizeCtl高位存储扩容邮戳(resizeStamp)，低位存储扩容线程数加1（1+nThreads）；

（5）其它线程添加元素后如果发现存在扩容，也会加入的扩容行列中来；
                                         
```
@param check if <0, don't check resize, if <= 1 only check if uncontended
private final void addCount(long x, int check) {
        CounterCell[] as;
        long b;
        long s;
        ##把数组的大小存储根据不同的线程存储到不同的段,并且有一个baseCount，优先更新baseCount，如果失败了再更新不同线程对应的段,这样可以保证尽量小的减少冲突
        if ((as = counterCells) != null ||!U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            CounterCell a; 
            long v; 
            int m;
            boolean uncontended = true;
            # 条件1:as为空
            # 条件2:或者数组长度为0
            # 条件3:当前线程所在cell为空
            # 条件4:前线程的段上加数量失败
            if (as == null || (m = as.length - 1) < 0 ||(a = as[ThreadLocalRandom.getProbe() & m]) == null ||!(uncontended =U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
                ## 强制增加数量;不同线程对应不同的段都更新失败了;说明已经发生冲突了,那么就对counterCells进行扩容,以减少多个线程hash到同一个段的概率
                fullAddCount(x, uncontended);##就是一个LongAdder的实现
                return;
            }
            if (check <= 1){
                return;
            }
            ## 计算元素个数
            s = sumCount();
                           
        }
        if (check >= 0) {
            Node<K,V>[] tab;
            Node<K,V>[] nt; 
            int n; 
            int sc;
            ##如果元素个数达到了扩容门槛,则进行扩容;正常情况下sizeCtl存储的是扩容门槛,即容量的0.75倍
            while (s >= (long)(sc = sizeCtl) && (tab = table) != null && (n = tab.length) < MAXIMUM_CAPACITY) {
                ##rs是扩容时的一个邮戳标识
                int rs = resizeStamp(n);
                if (sc < 0) {## sc<0说明正在扩容中
                    ## 扩容已经完成了，退出循环
                    ## 正常应该只会触发nextTable==null这个条件                   
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||transferIndex <= 0){
                        break;
                    }
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)){##扩容未完成，则当前线程加入迁移元素中并把扩容线程数加1
                        transfer(tab, nt);
                    }
                }
                ##这里是触发扩容的那个线程进入的地方;sizeCtl的高16位存储着rs这个扩容邮戳sizeCtl的低16位存储着扩容线程数加1，即(1+nThreads)
                else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2)){
                    transfer(tab, null);## 进入迁移元素                                        
                }
                s = sumCount();## 重新计算元素个数                               
            }
        }
    }

```

## 初始化桶数组


（1）使用CAS锁控制只有**一个线程**初始化桶数组；

（2）sizeCtl 在初始化后存储的是扩容门槛；

（3）扩容门槛写死的是桶数组大小的0.75倍，桶数组大小即map的容量，也就是最多存储多少个元素。


```

private final Node<K,V>[] initTable() {
        Node<K,V>[] tab; ##基础数组
        int sc;##容量状态值
        while ((tab = table) == null || tab.length == 0) {
            if ((sc = sizeCtl) < 0){
                Thread.yield(); ## 如果sizeCtl<0说明正在初始化或则扩容,让出CPU
            } else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) { ##compareAndSwapInt(node,waitStatusOffset,expect,update)
                ##如果把sizeCtl原子更新为-1成功,则当前线程进入初始化;如果原子更新失败则说明有其他线程先一步进入了初始化，则进入下一次循环             
                ##如果下一次循环时还没初始化完毕，则sizeCtl<0进入上面if的逻辑让出CPU;如果下一次循环更新完毕了,则table.length!=0,退出循环               
                try {
                    ## 再次检查table是否为空，防止ABA问题                     
                    if ((tab = table) == null || tab.length == 0) {
                        ##如果sc为0则使用默认值16
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        ##元素类型为Node的数组
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        table = tab = nt;
                        ##设置sc为数组长度的0.75倍
                        ##n-(n>>>2)=n-n/4=0.75
                        ##这里装载因子和扩容门槛都写死了,这也是没有threshold和loadFactory属性的原因
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;##扩容门槛
                }
                break;
            }
        }
        return tab;
    }


```

## 协助扩容（迁移元素）

```
### 协助扩容
final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
        Node<K,V>[] nextTab; ##迁移用的新数组
        int sc;
        ## 如果桶数组不为空，并且当前桶第一个元素为ForwardingNode类型，并且nextTab不为空说明当前桶已经迁移完毕了，才去帮忙迁移其它桶的元素
        ## 扩容时会把旧桶的第一个元素置为ForwardingNode，并让其nextTab指向新桶数组
        if (tab != null && (f instanceof ForwardingNode) &&(nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
            int rs = resizeStamp(tab.length);
            ## sizeCtl<0,说明正在扩容       
            while (nextTab == nextTable && table == tab && (sc = sizeCtl) < 0) {
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||sc == rs + MAX_RESIZERS || transferIndex <= 0){
                    break;
                }
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {##扩容线程数加1                                                                     
                    transfer(tab, nextTab);##当前线程帮忙迁移元素                                           
                    break;
                }
            }
            return nextTab;
        }
        return table;
    }

### 真正的扩容方法
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        int n = tab.length;
        int stride;
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE){
            stride = MIN_TRANSFER_STRIDE; 
        }
        if (nextTab == null) { ## 如果nextTab为空，说明还没开始迁移;就新建一个新桶数组                                       
            try {
                @SuppressWarnings("unchecked")
                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];##新桶容量是旧桶的两倍
                nextTab = nt;
            } catch (Throwable ex) {      ## try to cope with OOME
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            transferIndex = n;
        }
        ## 新桶的容量大小
        int nextn = nextTab.length;
        ## 新建一个ForwardingNode类型的节点，并把新桶数组存储在里面
        ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
                
        boolean advance = true;
        boolean finishing = false; ## to ensure sweep before committing nextTab
        for (int i = 0, bound = 0;;) {
            Node<K,V> f;
            int fh;
            ## 整个while循环就是在算i的值,i的值会从n-1依次递减,其中n是旧桶数组的大小
            while (advance) {
                int nextIndex;
                int nextBound;
                if (--i >= bound || finishing){
                    advance = false;
                }else if ((nextIndex = transferIndex) <= 0) {
                    i = -1;
                    advance = false;
                }
                else if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex,nextBound = (nextIndex > stride ?nextIndex - stride : 0))) {
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            ## 如果一次遍历完成了,也就是整个map所有桶中的元素都迁移完成了
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                if (finishing) {
                    ## 如果全部迁移完成了，则替换旧桶数组;并设置下一次扩容门槛为新桶数组容量的0.75倍
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
                ## 当前线程扩容完成，把扩容线程数-1                
                if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT){
                        ##扩容完成两边肯定相等
                        return;
                    }
                    ## 把finishing设置为true
                    ## finishing为true才会走到上面的if条件                      
                    finishing = advance = true;
                    ## i重新赋值为n
                    ## 这样会再重新遍历一次桶数组，看看是不是都迁移完成了;也就是第二次遍历都会走到下面的(fh = f.hash) == MOVED这个条件
                    i = n;
                }
            }
            ## 如果桶中无数据，直接放入ForwardingNode标记该桶已迁移
            else if ((f = tabAt(tab, i)) == null){
                advance = casTabAt(tab, i, null, fwd);
            }else if ((fh = f.hash) == MOVED){## 如果桶中第一个元素的hash值为MOVED;说明它是ForwardingNode节点也就是该桶已经迁移
                advance = true; ## already processed
            }else {
                ## 锁定该桶并迁移元素
                synchronized (f) {
                    ## 再次判断当前桶第一个元素是否有修改,也就是可能其它线程先一步迁移了元素                   
                    if (tabAt(tab, i) == f) {
                        ## 把一个链表分化成两个链表规则是桶中各元素的hash与桶大小n进行与操作等于0的放到低位链表(low)中，不等于0的放到高位链表(high)中
                        ## 其中低位链表迁移到新桶中的位置相对旧桶不变，高位链表迁移到新桶中位置正好是其在旧桶的位置加n这也正是为什么扩容时容量在变成两倍的原因
                        Node<K,V> ln;
                        Node<K,V> hn;
                        if (fh >= 0) {
                            ## 第一个元素的hash值大于等于0;说明该桶中元素是以链表形式存储的与HashMap迁移算法基本类似与HashMap迁移算法基本类似
                            ## 唯一不同的是多了一步寻找lastRun 这里的lastRun是提取出链表后面不用处理再特殊处理的子链表
                            int runBit = fh & n;
                            Node<K,V> lastRun = f;
                            
                            for (Node<K,V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            }
                            else {
                                hn = lastRun;
                                ln = null;
                            }
                            ## 遍历链表，把hash&n为0的放在低位链表中;不为0的放在高位链表中
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                if ((ph & n) == 0)
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            ## 低位链表的位置不变
                            ##                             
                            setTabAt(nextTab, i, ln);
                            ## 高位链表的位置是原位置加n
                            setTabAt(nextTab, i + n, hn);
                            ## 标记当前桶已迁移
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                        else if (f instanceof TreeBin) {
                            ## 如果第一个元素是树节点
                            ## 分化成两颗树:也是根据hash&n为0放在低位树中,不为0放在高位树中                                                 
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> lo = null, loTail = null;
                            TreeNode<K,V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
                            ## 遍历整颗树，根据hash&n是否为0分化成两颗树                               
                            for (Node<K,V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K,V> p = new TreeNode<K,V>
                                    (h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null)
                                        lo = p;
                                    else
                                        loTail.next = p;
                                    loTail = p;
                                    ++lc;
                                }
                                else {
                                    if ((p.prev = hiTail) == null)
                                        hi = p;
                                    else
                                        hiTail.next = p;
                                    hiTail = p;
                                    ++hc;
                                }
                            }
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                (hc != 0) ? new TreeBin<K,V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                (lc != 0) ? new TreeBin<K,V>(hi) : t;
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                    }
                }
            }
        }
    }


```

## 删除元素


```
final V replaceNode(Object key, V value, Object cv) {
        int hash = spread(key.hashCode());
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0 ||
                (f = tabAt(tab, i = (n - 1) & hash)) == null)
                break;
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                boolean validated = false;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            validated = true;
                            for (Node<K,V> e = f, pred = null;;) {
                                K ek;
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    V ev = e.val;
                                    if (cv == null || cv == ev ||
                                        (ev != null && cv.equals(ev))) {
                                        oldVal = ev;
                                        if (value != null)
                                            e.val = value;
                                        else if (pred != null)
                                            pred.next = e.next;
                                        else
                                            setTabAt(tab, i, e.next);
                                    }
                                    break;
                                }
                                pred = e;
                                if ((e = e.next) == null)
                                    break;
                            }
                        }
                        else if (f instanceof TreeBin) {
                            validated = true;
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> r, p;
                            if ((r = t.root) != null &&
                                (p = r.findTreeNode(hash, key, null)) != null) {
                                V pv = p.val;
                                if (cv == null || cv == pv ||
                                    (pv != null && cv.equals(pv))) {
                                    oldVal = pv;
                                    if (value != null)
                                        p.val = value;
                                    else if (t.removeTreeNode(p))
                                        setTabAt(tab, i, untreeify(t.first));
                                }
                            }
                        }
                    }
                }
                if (validated) {
                    if (oldVal != null) {
                        if (value == null)
                            addCount(-1L, -1);
                        return oldVal;
                    }
                    break;
                }
            }
        }
        return null;
    }
```

## 获取元素

```
public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
        int h = spread(key.hashCode());
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
            if ((eh = e.hash) == h) {
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            }
            else if (eh < 0)
                return (p = e.find(h, key)) != null ? p.val : null;
            while ((e = e.next) != null) {
                if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }

```


来自: [彤哥读源码](https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247483746&idx=1&sn=a6b5bea0cb52f23e93dd223970b2f6f9&scene=21#wechat_redirect)