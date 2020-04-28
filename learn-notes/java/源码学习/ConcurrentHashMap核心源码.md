## 构造函数

private transient volatile int sizeCtl;

1. -1,表示有线程正在进行初始化操作

2. - (1+ nThreads),表示有n个线程正在一起扩容

3. 0 , 默认值,后续在真正初始化的时候使用默认容量

4. > 0 初始化或者扩容完成后下一次扩容门槛


```

private static final int MAXIMUM_CAPACITY = 1 << 30;//最大容量

static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;//数组最大容量

public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0){ //如果初始化容量小于0则报参数异常
            throw new IllegalArgumentException();
        } 
        // 初始化值大于最大值的一半,则设置为最大容量,(初始容量+ 初始容量[无符号右移1位]>>>1+1) 否则1.5倍+1
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
           throw new NullPointerException();//key 和 value 都不能为null
        }
        //计算hash值
        int hash = spread(key.hashCode());
        //要插入的元素所在桶的元素个数
        int binCount = 0;
        //死循环,结合CAS使用(如果CAS失败,则会重新取整个桶进行下面的流程)
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; 
            int n;
            int i; 
            int fh;//数组桶第一个元素的hash值
            if (tab == null || (n = tab.length) == 0){
                //如果桶未初始化或者桶个数为0，则初始化桶
                tab = initTable();
             //如果要插入的元素所在的桶还没有元素，则把这个元素插入到这个桶中
            } else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {            
                if (casTabAt(tab, i, null,new Node<K,V>(hash, key, value, null))){
                       //如果使用CAS插入元素时,发现已经有元素了,则进入下一次循环,重新操作
                       //如果使用CAS插入元素成功,则break跳出循环,流程结束
                       break;  
                }                 
            }
            else if ((fh = f.hash) == MOVED){
                // 如果要插入的元素所在的桶的第一个元素的hash是MOVED，则当前线程帮忙一起迁移元素
                tab = helpTransfer(tab, f);
            } else {
                //如果桶不为空且不再迁移元素,则锁住这个桶(分段锁),查找要插入的元素是否在这个桶中，存在则替换值(onlyIfAbsent=false),不存在则插入链表结尾或插入树中
                V oldVal = null;
                synchronized (f) {
                   //再次检测第一个元素是否有变化，如果有变化则进入下一次循环，从头来过
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {// 如果第一个元素的hash值大于等于0（说明不是在迁移，也不是树）那就是桶中的元素使用的是链表方式存储                                      
                            binCount = 1;// 桶中元素个数赋值为1                                        
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                if (e.hash == hash &&((ek = e.key) == key ||(ek != null && key.equals(ek)))) {// 如果找到了这个元素，则赋值了新值（onlyIfAbsent=false）                                                                                                              
                                    oldVal = e.val;
                                    if (!onlyIfAbsent){
                                        e.val = value;//设置新值
                                    }
                                    break;
                                }
                                //若对应的key不存在,则把它插入到链表结尾并退出循环
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,value, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) {// 如果第一个元素是树节点                                                         
                            Node<K,V> p;
                            binCount = 2;//桶中元素个数赋值为2
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,value)) != null) {//调用红黑树的插入方法插入元素,如果成功插入则返回null,否则返回寻找到的节点
                                oldVal = p.val;
                                if (!onlyIfAbsent){
                                    p.val = value;
                                }
                            }
                        }
                    }
                }
                // 如果binCount不为0，说明成功插入了元素或者寻找到了元素                
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD){// 如果链表元素个数达到了8，则尝试树化,因为上面把元素插入到树中时，binCount只赋值了2，并没有计算整个树中元素的个数,所以不会树化                                                       
                        treeifyBin(tab, i);
                    }
                    if (oldVal != null){//如果要插入的元素已经存在，则返回旧值
                        return oldVal;
                    }
                    break;
                }
            }
        }
        //成功插入元素，元素个数加1（是否要扩容在这个里面）       
        addCount(1L, binCount);
        return null;
    }

```

## addCount(long x,int check);//增加桶中元素个数,每次添加元素后，元素数量加1，并判断是否达到扩容门槛，达到了则进行扩容或协助扩容。
                                         
```
private final void addCount(long x, int check) {
        CounterCell[] as;
        long b;
        long s;
        //把数组的大小存储根据不同的线程存储到不同的段,并且有一个baseCount，优先更新baseCount，如果失败了再更新不同线程对应的段,这样可以保证尽量小的减少冲突
        if ((as = counterCells) != null ||!U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
            CounterCell a; long v; int m;
            boolean uncontended = true;
            if (as == null || (m = as.length - 1) < 0 ||
                (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
                !(uncontended =
                  U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
                fullAddCount(x, uncontended);
                return;
            }
            if (check <= 1)
                return;
            s = sumCount();
        }
        if (check >= 0) {
            Node<K,V>[] tab, nt; int n, sc;
            while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                   (n = tab.length) < MAXIMUM_CAPACITY) {
                int rs = resizeStamp(n);
                if (sc < 0) {
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                        sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                        transferIndex <= 0)
                        break;
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }
                else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                             (rs << RESIZE_STAMP_SHIFT) + 2))
                    transfer(tab, null);
                s = sumCount();
            }
        }
    }

```

## 初始化桶数组


（1）使用CAS锁控制只有**一个线程**初始化桶数组；

（2）sizeCtl在初始化后存储的是扩容门槛；

（3）扩容门槛写死的是桶数组大小的0.75倍，桶数组大小即map的容量，也就是最多存储多少个元素。


```

private final Node<K,V>[] initTable() {
        Node<K,V>[] tab; //基础数组
        int sc;//容量状态值
        while ((tab = table) == null || tab.length == 0) {
            if ((sc = sizeCtl) < 0){
                Thread.yield(); // 如果sizeCtl<0说明正在初始化或则扩容,让出CPU
            } else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) { //compareAndSwapInt(node,waitStatusOffset,expect,update)
                //如果把sizeCtl原子更新为-1成功,则当前线程进入初始化;如果原子更新失败则说明有其他线程先一步进入了初始化，则进入下一次循环             
                //如果下一次循环时还没初始化完毕，则sizeCtl<0进入上面if的逻辑让出CPU;如果下一次循环更新完毕了,则table.length!=0,退出循环               
                try {
                    // 再次检查table是否为空，防止ABA问题                     
                    if ((tab = table) == null || tab.length == 0) {
                        //如果sc为0则使用默认值16
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        //元素类型为Node的数组
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        table = tab = nt;
                        //设置sc为数组长度的0.75倍
                        //n-(n>>>2)=n-n/4=0.75
                        //这里装载因子和扩容门槛都写死了,这也是没有threshold和loadFactory属性的原因
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;//扩容门槛
                }
                break;
            }
        }
        return tab;
    }


```

## 协助扩容（迁移元素）

```

final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
        Node<K,V>[] nextTab; int sc;
        if (tab != null && (f instanceof ForwardingNode) &&
            (nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
            int rs = resizeStamp(tab.length);
            while (nextTab == nextTable && table == tab &&
                   (sc = sizeCtl) < 0) {
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || transferIndex <= 0)
                    break;
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                    transfer(tab, nextTab);
                    break;
                }
            }
            return nextTab;
        }
        return table;
    }


private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        int n = tab.length, stride;
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            stride = MIN_TRANSFER_STRIDE; // subdivide range
        if (nextTab == null) {            // initiating
            try {
                @SuppressWarnings("unchecked")
                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
                nextTab = nt;
            } catch (Throwable ex) {      // try to cope with OOME
                sizeCtl = Integer.MAX_VALUE;
                return;
            }
            nextTable = nextTab;
            transferIndex = n;
        }
        int nextn = nextTab.length;
        ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
        boolean advance = true;
        boolean finishing = false; // to ensure sweep before committing nextTab
        for (int i = 0, bound = 0;;) {
            Node<K,V> f; int fh;
            while (advance) {
                int nextIndex, nextBound;
                if (--i >= bound || finishing)
                    advance = false;
                else if ((nextIndex = transferIndex) <= 0) {
                    i = -1;
                    advance = false;
                }
                else if (U.compareAndSwapInt
                         (this, TRANSFERINDEX, nextIndex,
                          nextBound = (nextIndex > stride ?
                                       nextIndex - stride : 0))) {
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                if (finishing) {
                    nextTable = null;
                    table = nextTab;
                    sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
                if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                        return;
                    finishing = advance = true;
                    i = n; // recheck before commit
                }
            }
            else if ((f = tabAt(tab, i)) == null)
                advance = casTabAt(tab, i, null, fwd);
            else if ((fh = f.hash) == MOVED)
                advance = true; // already processed
            else {
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K,V> ln, hn;
                        if (fh >= 0) {
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
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                if ((ph & n) == 0)
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                        else if (f instanceof TreeBin) {
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> lo = null, loTail = null;
                            TreeNode<K,V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
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