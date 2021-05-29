🖕欢迎关注我的公众号“**彤哥读源码**”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

---

本章接着上一章，链接直达[请点我](https://mp.weixin.qq.com/s/rlyoQp4ngTX8mjGDJgJIRA)。

---

### 初始化桶数组

第一次放元素时，初始化桶数组。

```java
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table) == null || tab.length == 0) {
        if ((sc = sizeCtl) < 0)
            // 如果sizeCtl<0说明正在初始化或者扩容，让出CPU
            Thread.yield(); // lost initialization race; just spin
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
            // 如果把sizeCtl原子更新为-1成功，则当前线程进入初始化
            // 如果原子更新失败则说明有其它线程先一步进入初始化了，则进入下一次循环
            // 如果下一次循环时还没初始化完毕，则sizeCtl<0进入上面if的逻辑让出CPU
            // 如果下一次循环更新完毕了，则table.length!=0，退出循环
            try {
                // 再次检查table是否为空，防止ABA问题
                if ((tab = table) == null || tab.length == 0) {
                    // 如果sc为0则使用默认值16
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                    // 新建数组
                    @SuppressWarnings("unchecked")
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    // 赋值给table桶数组
                    table = tab = nt;
                    // 设置sc为数组长度的0.75倍
                    // n - (n >>> 2) = n - n/4 = 0.75n
                    // 可见这里装载因子和扩容门槛都是写死了的
                    // 这也正是没有threshold和loadFactor属性的原因
                    sc = n - (n >>> 2);
                }
            } finally {
                // 把sc赋值给sizeCtl，这时存储的是扩容门槛
                sizeCtl = sc;
            }
            break;
        }
    }
    return tab;
}
```

（1）使用CAS锁控制只有一个线程初始化桶数组；

（2）sizeCtl在初始化后存储的是扩容门槛；

（3）扩容门槛写死的是桶数组大小的0.75倍，桶数组大小即map的容量，也就是最多存储多少个元素。

### 判断是否需要扩容

每次添加元素后，元素数量加1，并判断是否达到扩容门槛，达到了则进行扩容或协助扩容。

```java
private final void addCount(long x, int check) {
    CounterCell[] as; long b, s;
    // 这里使用的思想跟LongAdder类是一模一样的（后面会讲）
    // 把数组的大小存储根据不同的线程存储到不同的段上（也是分段锁的思想）
    // 并且有一个baseCount，优先更新baseCount，如果失败了再更新不同线程对应的段
    // 这样可以保证尽量小的减少冲突

    // 先尝试把数量加到baseCount上，如果失败再加到分段的CounterCell上
    if ((as = counterCells) != null ||
            !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
        CounterCell a; long v; int m;
        boolean uncontended = true;
        // 如果as为空
        // 或者长度为0
        // 或者当前线程所在的段为null
        // 或者在当前线程的段上加数量失败
        if (as == null || (m = as.length - 1) < 0 ||
                (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
                !(uncontended =
                        U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
            // 强制增加数量（无论如何数量是一定要加上的，并不是简单地自旋）
            // 不同线程对应不同的段都更新失败了
            // 说明已经发生冲突了，那么就对counterCells进行扩容
            // 以减少多个线程hash到同一个段的概率
            fullAddCount(x, uncontended);
            return;
        }
        if (check <= 1)
            return;
        // 计算元素个数
        s = sumCount();
    }
    if (check >= 0) {
        Node<K,V>[] tab, nt; int n, sc;
        // 如果元素个数达到了扩容门槛，则进行扩容
        // 注意，正常情况下sizeCtl存储的是扩容门槛，即容量的0.75倍
        while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                (n = tab.length) < MAXIMUM_CAPACITY) {
            // rs是扩容时的一个邮戳标识
            int rs = resizeStamp(n);
            if (sc < 0) {
                // sc<0说明正在扩容中
                if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                        sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                        transferIndex <= 0)
                    // 扩容已经完成了，退出循环
                    // 正常应该只会触发nextTable==null这个条件，其它条件没看出来何时触发
                    break;

                // 扩容未完成，则当前线程加入迁移元素中
                // 并把扩容线程数加1
                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    transfer(tab, nt);
            }
            else if (U.compareAndSwapInt(this, SIZECTL, sc,
                    (rs << RESIZE_STAMP_SHIFT) + 2))
                // 这里是触发扩容的那个线程进入的地方
                // sizeCtl的高16位存储着rs这个扩容邮戳
                // sizeCtl的低16位存储着扩容线程数加1，即(1+nThreads)
                // 所以官方说的扩容时sizeCtl的值为 -(1+nThreads)是错误的

                // 进入迁移元素
                transfer(tab, null);
            // 重新计算元素个数
            s = sumCount();
        }
    }
}
```

（1）元素个数的存储方式类似于LongAdder类，存储在不同的段上，减少不同线程同时更新size时的冲突；

（2）计算元素个数时把这些段的值及baseCount相加算出总的元素个数；

（3）正常情况下sizeCtl存储着扩容门槛，扩容门槛为容量的0.75倍；

（4）扩容时sizeCtl高位存储扩容邮戳(resizeStamp)，低位存储扩容线程数加1（1+nThreads）；

（5）其它线程添加元素后如果发现存在扩容，也会加入的扩容行列中来；

### 协助扩容（迁移元素）

线程添加元素时发现正在扩容且当前元素所在的桶元素已经迁移完成了，则协助迁移其它桶的元素。

```java
final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
    Node<K,V>[] nextTab; int sc;
    // 如果桶数组不为空，并且当前桶第一个元素为ForwardingNode类型，并且nextTab不为空
    // 说明当前桶已经迁移完毕了，才去帮忙迁移其它桶的元素
    // 扩容时会把旧桶的第一个元素置为ForwardingNode，并让其nextTab指向新桶数组
    if (tab != null && (f instanceof ForwardingNode) &&
            (nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
        int rs = resizeStamp(tab.length);
        // sizeCtl<0，说明正在扩容
        while (nextTab == nextTable && table == tab &&
                (sc = sizeCtl) < 0) {
            if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    sc == rs + MAX_RESIZERS || transferIndex <= 0)
                break;
            // 扩容线程数加1
            if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                // 当前线程帮忙迁移元素
                transfer(tab, nextTab);
                break;
            }
        }
        return nextTab;
    }
    return table;
}
```

当前桶元素迁移完成了才去协助迁移其它桶元素；

### 迁移元素

扩容时容量变为两倍，并把部分元素迁移到其它桶中。

```java
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
        stride = MIN_TRANSFER_STRIDE; // subdivide range
    if (nextTab == null) {            // initiating
        // 如果nextTab为空，说明还没开始迁移
        // 就新建一个新桶数组
        try {
            // 新桶数组是原桶的两倍
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
    // 新桶数组大小
    int nextn = nextTab.length;
    // 新建一个ForwardingNode类型的节点，并把新桶数组存储在里面
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
    boolean advance = true;
    boolean finishing = false; // to ensure sweep before committing nextTab
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;
        // 整个while循环就是在算i的值，过程太复杂，不用太关心
        // i的值会从n-1依次递减，感兴趣的可以打下断点就知道了
        // 其中n是旧桶数组的大小，也就是说i从15开始一直减到1这样去迁移元素
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
            // 如果一次遍历完成了
            // 也就是整个map所有桶中的元素都迁移完成了
            int sc;
            if (finishing) {
                // 如果全部迁移完成了，则替换旧桶数组
                // 并设置下一次扩容门槛为新桶数组容量的0.75倍
                nextTable = null;
                table = nextTab;
                sizeCtl = (n << 1) - (n >>> 1);
                return;
            }
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                // 当前线程扩容完成，把扩容线程数-1
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                    // 扩容完成两边肯定相等
                    return;
                // 把finishing设置为true
                // finishing为true才会走到上面的if条件
                finishing = advance = true;
                // i重新赋值为n
                // 这样会再重新遍历一次桶数组，看看是不是都迁移完成了
                // 也就是第二次遍历都会走到下面的(fh = f.hash) == MOVED这个条件
                i = n; // recheck before commit
            }
        }
        else if ((f = tabAt(tab, i)) == null)
            // 如果桶中无数据，直接放入ForwardingNode标记该桶已迁移
            advance = casTabAt(tab, i, null, fwd);
        else if ((fh = f.hash) == MOVED)
            // 如果桶中第一个元素的hash值为MOVED
            // 说明它是ForwardingNode节点
            // 也就是该桶已迁移
            advance = true; // already processed
        else {
            // 锁定该桶并迁移元素
            synchronized (f) {
                // 再次判断当前桶第一个元素是否有修改
                // 也就是可能其它线程先一步迁移了元素
                if (tabAt(tab, i) == f) {
                    // 把一个链表分化成两个链表
                    // 规则是桶中各元素的hash与桶大小n进行与操作
                    // 等于0的放到低位链表(low)中，不等于0的放到高位链表(high)中
                    // 其中低位链表迁移到新桶中的位置相对旧桶不变
                    // 高位链表迁移到新桶中位置正好是其在旧桶的位置加n
                    // 这也正是为什么扩容时容量在变成两倍的原因
                    Node<K,V> ln, hn;
                    if (fh >= 0) {
                        // 第一个元素的hash值大于等于0
                        // 说明该桶中元素是以链表形式存储的
                        // 这里与HashMap迁移算法基本类似
                        // 唯一不同的是多了一步寻找lastRun
                        // 这里的lastRun是提取出链表后面不用处理再特殊处理的子链表
                        // 比如所有元素的hash值与桶大小n与操作后的值分别为 0 0 4 4 0 0 0
                        // 则最后后面三个0对应的元素肯定还是在同一个桶中
                        // 这时lastRun对应的就是倒数第三个节点
                        // 至于为啥要这样处理，我也没太搞明白
                        int runBit = fh & n;
                        Node<K,V> lastRun = f;
                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;
                            }
                        }
                        // 看看最后这几个元素归属于低位链表还是高位链表
                        if (runBit == 0) {
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun;
                            ln = null;
                        }
                        // 遍历链表，把hash&n为0的放在低位链表中
                        // 不为0的放在高位链表中
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            if ((ph & n) == 0)
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                hn = new Node<K,V>(ph, pk, pv, hn);
                        }
                        // 低位链表的位置不变
                        setTabAt(nextTab, i, ln);
                        // 高位链表的位置是原位置加n
                        setTabAt(nextTab, i + n, hn);
                        // 标记当前桶已迁移
                        setTabAt(tab, i, fwd);
                        // advance为true，返回上面进行--i操作
                        advance = true;
                    }
                    else if (f instanceof TreeBin) {
                        // 如果第一个元素是树节点
                        // 也是一样，分化成两颗树
                        // 也是根据hash&n为0放在低位树中
                        // 不为0放在高位树中
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null;
                        TreeNode<K,V> hi = null, hiTail = null;
                        int lc = 0, hc = 0;
                        // 遍历整颗树，根据hash&n是否为0分化成两颗树
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
                        // 如果分化的树中元素个数小于等于6，则退化成链表
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                (hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                (lc != 0) ? new TreeBin<K,V>(hi) : t;
                        // 低位树的位置不变
                        setTabAt(nextTab, i, ln);
                        // 高位树的位置是原位置加n
                        setTabAt(nextTab, i + n, hn);
                        // 标记该桶已迁移
                        setTabAt(tab, i, fwd);
                        // advance为true，返回上面进行--i操作
                        advance = true;
                    }
                }
            }
        }
    }
}
```

（1）新桶数组大小是旧桶数组的两倍；

（2）迁移元素先从靠后的桶开始；

（3）迁移完成的桶在里面放置一ForwardingNode类型的元素，标记该桶迁移完成；

（4）迁移时根据hash&n是否等于0把桶中元素分化成两个链表或树；

（5）低位链表（树）存储在原来的位置；

（6）高们链表（树）存储在原来的位置加n的位置；

（7）迁移元素时会锁住当前桶，也是分段锁的思想；

---

未完待续~~

---

现在文章没办法留言了，如果有任何建议或者意见，欢迎大家在公众号后台给我留言，谢谢~

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)

