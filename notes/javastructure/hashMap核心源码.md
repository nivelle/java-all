#### 属性

````

/**
 * 默认的初始容量为16
 */
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

/**
 * 最大的容量为2的30次方
 */
static final int MAXIMUM_CAPACITY = 1 << 30;

/**
 * 默认的装载因子
 */
static final float DEFAULT_LOAD_FACTOR = 0.75f;

/**
 * 当一个桶中的元素个数大于等于8时进行树化
 */
static final int TREEIFY_THRESHOLD = 8;

/**
 * 当一个桶中的元素个数小于等于6时把树转化为链表
 */
static final int UNTREEIFY_THRESHOLD = 6;

/**
 * 树化，当容量达到64且链表的长度达到8时进行树化，当链表的长度小于6时反树化。
 *  
 */
static final int MIN_TREEIFY_CAPACITY = 64;

/**
 * 数组，又叫作桶（bucket）
 */
transient Node<K,V>[] table;

/**
 * 作为entrySet()的缓存
 */
transient Set<Map.Entry<K,V>> entrySet;

/**
 * 元素的数量
 */
transient int size;

/**
 * 修改次数，用于在迭代的时候执行快速失败策略
 */
transient int modCount;

/**
 * 当桶的使用数量达到多少时进行扩容，threshold = capacity * loadFactor
 */
int threshold;

/**
 * 装载因子
 */
final float loadFactor;

````

#### put 方法底层逻辑

（1）计算key的hash值;

（2）如果桶（数组）数量为0，则初始化桶;

（3）如果key所在的桶没有元素,则直接插入;

（4）如果key所在的桶中的第一个元素的key与待插入的key相同，说明找到了元素，转后续流程（9）处理；

（5）如果第一个元素是树节点，则调用树节点的putTreeVal()寻找元素或插入树节点；

（6）如果不是以上三种情况，则遍历桶对应的链表查找key是否存在于链表中；

（7）如果找到了对应key的元素，则转后续流程（9）处理；

（8）如果没找到对应key的元素，则在链表最后插入一个新节点并判断是否需要树化；【尾查法】

（9）如果找到了对应key的元素，则判断是否需要替换旧值，并直接返回旧值；

（10）如果插入了元素，则数量加1并判断是否需要扩容;


```
    /*
     *
     * @param hash         hash for key
     * @param key          the key
     * @param value        the value to put
     * @param onlyIfAbsent if true, don't change existing value(如果为true则不改变存在的值,hashMap put方法默认是false)
     * @param evict        if false, the table is in creation mode.(hashMap put方法默认是true)
     * @return  如果存在指定key则返回旧值 如果不存在则返回null
     *
     */
    public V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        //数组
        Node<K, V>[] tab;
        //数组中第一个位置的元素
        Node<K, V> p;
        //数组长度
        int n;
        //数组index
        int i; 
        // 如果桶的数量为0，则初始化
        if ((tab = table) == null || (n = tab.length) == 0) {
            //调用resize()初始化数组
            n = (tab = resize()).length;
        }
        // 如果这个桶中还没有元素，则把这个元素放在桶中的第一个位置(先赋值再比较)
        // p 是桶的第一个元素
        if ((p = tab[i = (n - 1) & hash]) == null) {
            //新建一个节点放在桶中
            tab[i] = newNode(hash, key, value, null);
        } else {
            // 如果桶中已经有元素存在了
            // 暂时存指定位置某个node(最后一个)
            Node<K, V> e;
            K k;
            //如果桶中第一个元素的key与待插入元素的key相同,保存到e中用于后续修改value值
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p;
                //如果第一个元素是树节点,则调用树节点的putTreeVal插入元素
            } else if (p instanceof TreeNode) {
                e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
            } else {
                // 遍历这个桶对应的链表，binCount用于存储链表中元素的个数,直到插入尾部结束
                for (int binCount = 0; ; ++binCount) {
                    //如果链表遍历完了都没有找到相同key的元素，说明该key对应的元素不存在，则在链表最后插入一个新节点(尾插法)
                    //这个地方开始 e元素开始从首个元素开始后移
                    if ((e = p.next) == null) {
                        //创建一个节点,放在尾节点之后
                        p.next = newNode(hash, key, value, null);
                        // 如果插入新节点后链表长度大于8，则判断是否需要树化，因为第一个元素没有加到binCount中，所以这里-1
                        if (binCount >= TREEIFY_THRESHOLD - 1) {
                            //树化
                            treeifyBin(tab, hash);
                        }
                        break;
                    }
                    //如果指定key存在则跳出循环,此时的e 是 该桶位置的链表里的元素。
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                        break;
                    }
                    //p元素后移
                    p = e;
                }
            }
            if (e != null) {
                // 记录下旧值
                V oldValue = e.value;
                // 判断是否需要替换旧值
                if (!onlyIfAbsent || oldValue == null) {
                    // 替换旧值为新值
                    e.value = value;
                }
                // 在节点被访问后做点什么事，在LinkedHashMap中用到(LRU)
                afterNodeAccess(e);
                // 返回旧值
                return oldValue;
            }
        }
        // 到这里了说明没有找到元素;修改次数加1
        ++modCount;
        // 元素数量加1，判断是否需要扩容
        if (++size > threshold) {
            //扩容
            resize();
        }
        // 在节点插入后做点什么事，在LinkedHashMap中用到(LRU)，默认是TRUE,在 hashMap的 readObject方法调用中为false
        afterNodeInsertion(evict);
        return null;
    }

```

---

#### 扩容方法

（1）如果使用是默认构造方法，则第一次插入元素时初始化为默认值,容量为16，扩容门槛为12；

（2）如果使用的是非默认构造方法则第一次插入元素时初始化容量等于扩容门槛,扩容门槛在构造方法里等于传入容量向上最近的2的n次方；

（3）如果旧容量大于0，则新容量等于旧容量的2倍，但不超过最大容量:2的30次方，新扩容门槛为旧扩容门槛的2倍；

（4）创建一个新容量的桶；

（5）搬移元素，原链表分化成两个链表，低位链表存储在原来桶的位置，高位链表搬移到原来桶的位置加旧容量的位置； 


#### 优化点: 

#### 容量变为原来的二倍后,二进制位就多了一位,这一位可能是0可能是1(0就是原位置,1就是原来的位置+原来的数组长度[oldCap])  
       
#### JDK1.7 扩容采取的是头插法，数据会倒置，会产生环形链表或者丢失值;但是1.8采用了尾插法，避免了环形链表，但是还是可能丢失值
     
[![D1uz24.png](https://s3.ax1x.com/2020/11/21/D1uz24.png)](https://imgchr.com/i/D1uz24)
    
```
   final HashMap.Node<K,V>[] resize() {
        // 旧数组
        HashMap.Node<K,V>[] oldTab = table;
        // 旧容量
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        // 旧扩容门槛
        int oldThr = threshold;
        // 新容量
        int newCap;
        // 新的扩容门槛
        int newThr = 0;
        //旧容量大于0
        if (oldCap > 0) {
            //如果旧容量达到了最大容量,则不再进行扩容
            if (oldCap >= MAXIMUM_CAPACITY) {
                //扩容门槛设置为最大值
                threshold = Integer.MAX_VALUE;
                //返回旧的Node数组
                return oldTab;
            }else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY){
                // 新数组容量（就数组容量2倍）小于最大容量,而且旧的容量大于等于默认初始化容量(16)则扩容门槛也扩大为原来2倍
                newThr = oldThr << 1; // double threshold
            }
        }
       
        //使用非默认构造方法创建的map，第一次插入元素会走到这里,设置初始容量的时候初始化了扩容门槛(非默认方法没有设置容量,只设置了扩容门槛)
        else if (oldCap==0 && oldThr > 0) // initial capacity was placed in threshold                                                                      
            newCap = oldThr;// 如果旧容量为0且旧扩容门槛大于0，则把新容量赋值为旧门槛     
        else { // zero initial threshold signifies using defaults
            //默认构造方法创建的map,第一次插入会走到这里
            //如果旧容量旧扩容门槛都是0，说明还未初始化过，则初始化容量为默认容量，扩容门槛为默认容量*默认装载因子          
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // 如果新扩容门槛为0,则计算为容量*装载因子,但不能超过最大容量
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?(int)ft : Integer.MAX_VALUE);
        }
        // 赋值扩容门槛为新门槛
        threshold = newThr;
        // 新建一个新容量的数组
        @SuppressWarnings({"rawtypes","unchecked"})
        HashMap.Node<K,V>[] newTab = (HashMap.Node<K,V>[])new HashMap.Node[newCap];
        // 把桶赋值为新数组
        table = newTab;
        // 如果旧数组不为空,则迁移数据
        if (oldTab != null) {
            // 遍历旧数组
            for (int j = 0; j < oldCap; ++j) {
                HashMap.Node<K,V> e;
                //如果桶中第一个元素不为空，赋值给e
                if ((e = oldTab[j]) != null) {
                    //清空旧桶,便于GC回收  
                    oldTab[j] = null;
                    //如果这个桶中只有一个元素,则计算它在新桶中的位置并把它搬移到新桶中
                    if (e.next == null)
                        //因为每次都扩容两倍，所以这里的第一个元素搬移到新桶的时候新桶肯定还没有元素
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof HashMap.TreeNode){
                         //如果第一个元素是树节点,则把这颗树打散成两颗树插入到新桶中去(如果桶中节点小于6个则反树化)                     
                        ((HashMap.TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    }else { // preserve order
                        //如果这个链表不止一个元素且不是一颗树则分化成两个链表插入到新的桶中去
                        HashMap.Node<K,V> loHead = null;
                        HashMap.Node<K,V> loTail = null;

                        HashMap.Node<K,V> hiHead = null;
                        HasHMap.Node<L,V> hiTail = null;
                        HashMap.Node<K,V> next;
                        do {
                            next = e.next;
                            //(e.hash & oldCap) == 0 的元素放在低位链表中(扩展位位0)
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            //(e.hash & oldCap) != 0 的元素放在高位链表中（扩展位为1）
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 遍历完成分化成两个链表了
                        // 低位链表在新桶中的位置与旧桶一样（即3和11还在三号桶中）
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }         
                        // 高位链表在新桶中的位置正好是原来的位置加上旧容量
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

#### 红黑树节点扩容 // 把一颗树打成两颗树插入到新桶
```
//split(this, newTab, j, oldCap)
final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<K,V> loHead = null, loTail = null;
            TreeNode<K,V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K,V> e = b, next; e != null; e = next) {              
                next = (TreeNode<K,V>)e.next;
                e.next = null;
                //如果当前节点哈希值的最后一位等于要修剪的 bit 值
                if ((e.hash & bit) == 0) {
                   //就把当前节点放到 lXXX 树中
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    //然后 loTail 记录 e
                    loTail = e;
                    //记录 lXXX 树的节点数量
                    ++lc;
                }
                else {
                    //如果当前节点哈希值最后一位不是要修剪的 就把当前节点放到 hXXX 树中
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    //记录 hXXX 树的节点数量
                    ++hc;
                }
            }

            if (loHead != null) {
                //如果 lXXX 树的数量小于 6，就把 lXXX 树的枝枝叶叶都置为空，变成一个单节点 然后让这个桶中，要还原索引位置开始往后的结点都变成还原成链表的 lXXX 节点 这一段元素以后就是一个链表结构
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                   //否则让索引位置的结点指向 lXXX 树，这个树被修剪过，元素少了
                    tab[index] = loHead;
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
               //同理，让 指定位置 index + bit 之后的元素指向 hXXX 还原成链表或者修剪过的树
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }
```

### 树结构添加数据

（1）寻找根节点；

（2）从根节点开始查找；

（3）比较hash值及key值,如果都相同,直接返回，在putVal()方法中决定是否要替换value值；

（4）根据hash值及key值确定在树的左子树还是右子树查找,找到了直接返回;

（5）如果最后没有找到则在树的相应位置插入元素,并做平衡;

```

    /*
     * 红黑树 结构的HashMap 的put操作
     *
     * @param map      原来的hashMap结构
     * @param tab      底层的数组结构
     * @param h        当前节点的hash值
     * @param k        key
     * @param v        value 
     * @return  如果存在指定key则返回旧值 如果不存在则返回null
     *
     */

final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,int h, K k, V v) {
            //key的真实类型或者为null
            Class<?> kc = null;
            // 标记是否找到这个key的节点
            boolean searched = false;
            // 找到树的根节点
            TreeNode<K,V> root = (parent != null) ? root() : this;
            // 从树的根节点开始遍历
            for (TreeNode<K,V> p = root;;) {
                int dir;//标记是在左边还是右边,标记目标节点:direction
                int ph; //遍历的当前节点的hash只
                K pk;//遍历的当前节点的key值
                
                if ((ph = p.hash) > h)// 当前hash比目标hash大,说明目标在左边                                    
                    dir = -1;
                else if (ph < h){//当前hash比目标hash大,说明目标在右边
                    dir = 1;
                }
                //hash值相等，key相等说明找到了;回到putVal()中判断是否需要修改其value值
                else if ((pk = p.key) == k || (k != null && k.equals(pk))){
                    return p;
                }
                // 如果k是Comparable的子类则返回其真实的类,否则返回null;如果k和pk不是同样的类型则返回0,否则返回两者比较的结果
                // 这个条件表示两者hash相同但是其中一个不是Comparable类型或者两者类型不同;比如key是Object类型,这时可以传String也可以传Integer,两者hash值可能相同;
                // 在红黑树中把同样hash值的元素存储在同一颗子树,这里相当于找到了这颗子树的顶点从这个顶点分别遍历其左右子树去寻找有没有跟待插入的key相同的元素
                else if ((kc == null && (kc = comparableClassFor(k)) == null) ||(dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        // 遍历左右子树找到了直接返回
                        if (((ch = p.left) != null && (q = ch.find(h, k, kc)) != null) ||((ch = p.right) != null && (q = ch.find(h, k, kc)) != null)){
                            return q;
                        }
                    }
                    // 如果两者类型相同,再根据它们的内存地址计算hash值进行比较
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K,V> xp = p;
                // 如果最后确实没找到对应key的元素,则新建一个节点
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K,V> xpn = xp.next;
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0){
                        xp.left = x;
                    }
                    else {
                        xp.right = x;
                    }
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null){
                        ((TreeNode<K,V>)xpn).prev = x;
                    }
                    // 插入树节点后平衡
                    // 把root节点移动到链表的第一个节点
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }


```

### 链表变树方法 treeifyBin() 

```
final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n; //数组长度
        int index;
        Node<K,V> e;
        //如果数组长度桶数量小于64,直接扩容而不用树化
        //因为扩容之后,链表会分化成两个链表,达到减少元素的作用
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY){
            //扩容
            resize();
         }
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null;//遍历时被包装成的树节点，head
            TreeNode tl = null;//最后一个
            do {
                // 把所有节点换成树节点;树节点继承只 LinkedHashMap.Entry
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    //暂存的节点
                    hd = p;
                else {
                    //将所有TreeNode连接在一起此时只是链表结构。
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            //把树节点链表结构赋值给数组某个捅中,先转换成树节点，链接起来，然后放到tab[index] 桶结构
            if ((tab[index] = hd) != null){
                // 对TreeNode链表进行树化
                hd.treeify(tab);
            }
        }
    }
    
```
### TreeNode.treeify() 方法

```
final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            //this 为节点树化后的链表头
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                // 第一个元素作为根节点且为黑节点,其它元素依次插入到树中再做平衡
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                }
                else {
                    //x即为当前访问链表中的项。
                    //从根节点查找元素插入的位置                
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    //此时红黑树已经有了根节点，上面获取了当前加入红黑树的项的key和hash值进入核心循环。
                    //这里从root开始,是以一个自顶向下的方式遍历添加
                    //for循环没有控制条件,由代码内break跳出循环
                    for (TreeNode<K,V> p = root;;) {                    
                        int dir; // dir：directory,比较添加项与当前树中访问节点的hash值判断加入项的路径，-1为左子树，+1为右子树。
                        int ph; // ph：parent hash。
                        K pk = p.key;
                        if ((ph = p.hash) > h){
                            dir = -1;
                        }
                        else if (ph < h){
                            dir = 1;
                        }
                        else if ((kc == null &&(kc = comparableClassFor(k)) == null) ||(dir = compareComparables(kc, k, pk)) == 0){
                            dir = tieBreakOrder(k, pk);
                        }
                        //如果最后没找到元素，则插入  // xp：x parent。
                        TreeNode<K,V> xp = p;
                        //找到符合x添加条件的节点。
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            // 如果xp的hash值大于x的hash值，将x添加在xp的左边。
                            if (dir <= 0){
                                xp.left = x;
                            }else{ // 如果xp的hash值大于x的hash值，将x添加在xp的左边。
                                xp.right = x;
                            }
                            // 维护添加后红黑树的红黑结构。
                            root = balanceInsertion(root, x);
                            // 跳出循环当前链表中的项成功的添加到了红黑树中。
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }
        
```        
### get(Object key) 方法

（1）计算key的hash值；

（2）找到key所在的桶及其第一个元素；

（3）如果第一个元素的key等于待查找的key，直接返回；

（4）如果第一个元素是树节点就按树的方式来查找，否则按链表方式查找；

```
public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }
```

#### 真正的查找方法
```

final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; 
        Node<K,V> first;
        Node<K,V> e; 
        int n; 
        K k;
        //如果桶的数量大于0并且待查找的key所在的桶的第一个元素不为空         
        if ((tab = table) != null && (n = tab.length) > 0 && (first = tab[(n - 1) & hash]) != null) {
            //检查第一个元素是不是要查的元素，如果是直接返回 
            //hash值相等，key相等           
            if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k)))){
                return first;
            }
            //否则开始遍历该桶的链表
            if ((e = first.next) != null) {
                // 如果是树结构则走树结构的查找
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    //否则就遍历链表
                    if (e.hash == hash &&((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }    
    
```


##### getTreeNode - > find // 树类型的hashMap 查找

```

final TreeNode<K,V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }
```
##### 树节点查找
````                
final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this; // 桶的首节点｜parent节点
            do {
                int ph; // parent hash 
                int dir; //dierction 目标路径
                K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl; //目标节点在左子树
                else if (ph < h)
                    p = pr; //目标节点在右子树
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p; //若hash值相同则找到了，返回
                else if (pl == null)//左子树为空查右子树                 
                    p = pr;  
                else if (pr == null)//右子树为空查左子树
                    p = pl;
                else if ((kc != null ||(kc = comparableClassFor(k)) != null) &&(dir = compareComparables(kc, k, pk)) != 0){
                    //通过compare方法比较key值的大小决定使用左子树还是右子树  
                    p = (dir < 0) ? pl : pr; 
                }                                          
                else if ((q = pr.find(h, k, kc)) != null){
                    //如果以上条件都不通过，则尝试在右子树查找
                    return q; 
                }
                else
                    p = pl; //都没找到就在左子树查找
            } while (p != null);
            return null;
        }
````
####  remove(Object key)方法

（1）先查找元素所在的节点；

（2）如果找到的节点是树节点，则按树的移除节点处理；

（3）如果找到的节点是桶中的第一个节点，则把第二个节点移到第一的位置；

（4）否则按链表删除节点处理；

（5）修改size，调用移除节点后置处理等；

```
public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }
  
```  
  
#### 真正删除操作

````
final Node<K,V> removeNode(int hash, Object key, Object value,boolean matchValue, boolean movable) {
        Node<K,V>[] tab; 
        Node<K,V> p; 
        int n;
        index;
        // 如果桶的数量大于0且待删除的元素所在的桶的第一个元素不为空
        if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null;
            Node<K,V> node e; 
            K k;
            V v;
            if (p.hash == hash &&((k = p.key) == key || (key != null && key.equals(k)))){
              // 如果第一个元素正好就是要找的元素，赋值给node变量后续删除使用
              node = p；
            }
            //否则开始遍历查找节点
            else if ((e = p.next) != null) {
                if (p instanceof TreeNode){
                    //如果第一个元素是树节点，则以树的方式查找需要删除的节点
                     node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                }
                else {//否则遍历整个链表
                    do {
                        if (e.hash == hash &&((k = e.key) == key ||(key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            // 如果找到了元素，则看参数是否需要匹配value值, 如果不需要匹配直接删除,如果需要匹配则看value值是否与传入的value相等
            if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))) {
                if (node instanceof TreeNode){
                    //如果是树节点，调用树的删除方法（以node调用的，是删除自己）
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);                                                                              
                }
                else if (node == p){
                    // 如果待删除的元素是第一个元素,则把第二个元素移到第一的位置
                    tab[index] = node.next;                                          
                }else{
                    // 否则删除node节点 
                    p.next = node.next;                                      
                }
                ++modCount;
                --size;
                // 删除节点后置处理     
                afterNodeRemoval(node);                                  
                return node;
            }
        }
        return null;
    }

````

### TreeNode.removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,boolean movable) //树节点的删除

```
 final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,boolean movable) {
            int n;
            // 如果桶的数量为0直接返回
            if (tab == null || (n = tab.length) == 0){
                return;
            }
            // 节点在桶中的索引    
            int index = (n - 1) & hash;
            // 第一个节点，根节点，根左子节点
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
            TreeNode<K,V> root = first;
            TreeNode<K,V> rl;
            // 后继节点，前置节点
            TreeNode<K,V> succ = (TreeNode<K,V>)next;
            TreeNode<K,V> pred = prev;
            if (pred == null){
                 // 如果前置节点为空，说明当前节点是根节点，则把后继节点赋值到第一个节点的位置，相当于删除了当前节点
                 tab[index] = first = succ;
               }
            else{
                 // 否则把前置节点的下个节点设置为当前节点的后继节点，相当于删除了当前节点
                pred.next = succ;
            }
            // 如果后继节点不为空，则让后继节点的前置节点指向当前节点的前置节点，相当于删除了当前节点
            if (succ != null){
                succ.prev = pred;
            }
            // 如果第一个节点为空，说明没有后继节点了，直接返回
            if (first == null){
                return;
            }
            // 如果根节点的父节点不为空，则重新查找父节点
            if (root.parent != null){            
                root = root.root();
            }
            // 如果根节点为空，则需要反树化（将树转化为链表）            
            if (root == null || root.right == null ||(rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map);  //如果需要移动节点且树的高度比较小，则需要反树化                                                       
                return;
            }
            // 删除红黑树中的节点
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K,V> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                }
                else {
                    TreeNode<K,V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            }
            else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // detach
                TreeNode<K,V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

```
## 总结

（1）HashMap是一种散列表，采用（数组 + 链表 + 红黑树）的存储结构；

（2）HashMap的默认初始容量为16（1<<4），默认装载因子为0.75f，容量总是2的n次方；

（3）HashMap扩容时每次容量变为原来的两倍；

（4）当桶的数量小于64时不会进行树化，只会扩容；

（5）当桶的数量大于64且单个桶中元素的数量大于8时，进行树化；

（6）当单个桶中元素数量小于6时，进行反树化；

（7）HashMap是非线程安全的容器；

（8）HashMap查找添加元素的时间复杂度都为O(1)；


来自: [彤哥读源码](https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247483711&idx=3&sn=f0743c914e26695eb9c0d4cb0cab5e99&scene=21#wechat_redirect)