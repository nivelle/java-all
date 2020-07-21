package com.nivelle.base.jdk.concurrent;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 跳跃表
 *
 * @author nivelle
 * @date 2020/04/12
 */
public class ConcurrentSkipListMapDemo {
    public static void main(String[] args) {
        /**
         * 跳表是一个随机化的数据结构,实质就是一种可以进行二分查找的有序链表。
         *
         * 跳表在原有的有序链表上面增加了多级索引,通过索引来实现快速查找。
         *
         * 跳表不仅能提高搜索性能,同时也可以提高插入和删除操作的性能。
         */
        ConcurrentSkipListMap<Integer, Integer> concurrentSkipListMap = new ConcurrentSkipListMap();
        concurrentSkipListMap.put(1, 1);
        concurrentSkipListMap.put(2, 2);
        concurrentSkipListMap.putIfAbsent(3, 3);
        concurrentSkipListMap.put(1, 1);
        concurrentSkipListMap.put(4, 4);
        concurrentSkipListMap.put(5, 5);
        System.out.println(concurrentSkipListMap);
        Integer firstKey = concurrentSkipListMap.firstKey();
        System.out.println("firstKey:" + firstKey);
        ConcurrentNavigableMap concurrentNavigableMap = concurrentSkipListMap.descendingMap();
        System.out.println("逆序排序:" + concurrentNavigableMap);
        ConcurrentSkipListMap.Entry entry = concurrentSkipListMap.lastEntry();
        System.out.println("lastEntry:" + entry);
        ConcurrentNavigableMap concurrentNavigableMap1 = concurrentSkipListMap.headMap(2, true);
        System.out.println("headMap:" + concurrentNavigableMap1);
        ConcurrentNavigableMap concurrentNavigableMap2 = concurrentSkipListMap.tailMap(3, true);
        System.out.println("tailMap :" + concurrentNavigableMap2);


        ConcurrentSkipListMap.Entry pollFirstEntry = concurrentSkipListMap.pollFirstEntry();
        System.out.println("pollFirstEntry:" + pollFirstEntry);
        System.out.println("pollFirstEntry after poll:" + concurrentNavigableMap);

        ConcurrentSkipListMap.Entry pollLastEntry = concurrentSkipListMap.pollLastEntry();
        System.out.println("pollLastEntry:" + pollLastEntry);
        System.out.println("pollLastEntry after poll:" + concurrentNavigableMap);


        ConcurrentSkipListMap.Entry ceilingEntry = concurrentSkipListMap.ceilingEntry(4);
        System.out.println("ceilingEntry:" + ceilingEntry);

        Integer ceilingKey = concurrentSkipListMap.ceilingKey(4);
        System.out.println("ceilingKey:" + ceilingKey);


        concurrentSkipListMap.compute(2, (k, v) -> {
            return 10;
        });
        System.out.println("concurrentSkipListMap after compute:" + concurrentSkipListMap);


        /**
         * 数据节点:
         * ## 单链表结构:
         * static final class Node<K,V> {
         *         ## 这里value的类型是Object，而不是V;在删除元素的时候value会指向当前元素本身
         *         final K key;
         *         volatile Object value;
         *         volatile Node<K,V> next;
         *
         * }
         *
         * ## 索引节点:
         * static class Index<K,V> {
         *         ## 存储着对应的node值，及向下和向右的索引指针
         *         final Node<K,V> node;
         *         final Index<K,V> down;
         *         volatile Index<K,V> right;
         *
         *
         * Index(Node<K,V> node, Index<K,V> down, Index<K,V> right) {
         *             this.node = node;
         *             this.down = down;
         *             this.right = right;
         *         }
         * }
         *
         * ## 头索引节点
         * static final class HeadIndex<K,V> extends Index<K,V> {
         *         ##  头索引节点，继承自Index，并扩展一个level字段，用于记录索引的层级
         *         final int level;
         *         HeadIndex(Node<K,V> node, Index<K,V> down, Index<K,V> right, int level) {
         *             super(node, down, right);
         *             this.level = level;
         *         }
         *     }
         *
         * ## 构造函数
         *
         * private void initialize() {
         *         keySet = null;
         *         entrySet = null;
         *         values = null;
         *         descendingMap = null;
         *         ## 创建了一个头索引节点，里面存储着一个数据节点，这个数据节点的值是空对象，且它的层级是1。
         *         ## 初始化的时候，跳表中只有一个头索引节点，层级是1，数据节点是一个空对象，down和right都是null
         *         head = new HeadIndex<K,V>(new Node<K,V>(null, BASE_HEADER, null),null, null, 1);
         *     }
         *
         */
    }
    /**
     * 添加元素:
     *
     * public V put(K key, V value) {
     *         ## 不能存储value为null的元素，因为value为null标记该元素被删除
     *         if (value == null){
     *            throw new NullPointerException();
     *         }
     *         ## 调用doPut()方法添加元素
     *         return doPut(key, value, false);
     *     }
     *
     *
     *    private V doPut(K key, V value, boolean onlyIfAbsent) {
     *         ## 添加元素后存储在z中
     *         Node<K,V> z;
     *         ## key也不能为null
     *         if (key == null){
     *             throw new NullPointerException();
     *         }
     *         Comparator<? super K> cmp = comparator;
     *         ## Part 1:找到目标节点的位置并插入，这里的目标节点是数据节点，也就是最底层的那条链
     *         ## 自旋
     *         outer: for (;;) {
     *             ## 寻找目标节点之前最近的一个索引对应的数据节点，存储在b中，b= before 并把b的下一个数据节点存储在n中，n=next为了便于描述，
     *             ## 这里把b叫做当前节点，n叫做下一个节点
     *             for (Node<K,V> b = findPredecessor(key, cmp), n = b.next;;) {
     *                 ## 如果下一个节点不为空;就拿其key与目标节点的key比较，找到目标节点应该插入的位置
     *                 if (n != null) {
     *                     ## v=value,存储节点value值
     *                     Object v;
     *                     ## c = compare,存储两个节点比较的大小
     *                     int c;
     *                     ## n的下一个数据节点，也就是b的下一个节点的下一个节点（孙子节点）
     *                     Node<K,V> f = n.next;
     *                     ## 如果n不为b的下一个节点，说明有其他线程修改了数据，跳出内层循环，
     *                     ## 也就是回到了外层循环自旋的位置，从头来过
     *                     if (n != b.next){
     *                         break;
     *                     }
     *                     ## 如果n的value值为空，说明该节点已经删除，协助删除节点
     *                     if ((v = n.value) == null) {   // n is deleted
     *                         ## 协助删除
     *                         n.helpDelete(b, f);
     *                         break;
     *                     }
     *                     ## 如果b的值为空或者v等于n,说明b已被删除
     *                     ## 这时候n就是marker节点，那b就是被删除的那个
     *                     if (b.value == null || v == n){ // b is deleted
     *                         break;
     *                     }
     *                     ## 如果目标key比下一个节点的key大
     *                     ## 数目目标元素所在的位置还在下一个节点的后面
     *                     if ((c = cpr(cmp, key, n.key)) > 0) {
     *                         ## 就把当前节点往后移一位，同样的下一个节点也往后移一位
     *                         ## 再重新检查新n是否为空，它与目标key的关系
     *                         b = n;
     *                         n = f;
     *                         continue;
     *                     }
     *                     ## 如果比较时发现下一个节点的key与目标key相同，说明链表中本身句存在目标节点
     *                     if (c == 0) {
     *                         ## 则用新值替换旧值，并返回旧值（onlyIfAbset=false）
     *                         if (onlyIfAbsent || n.casValue(v, value)) {
     *                             @SuppressWarnings("unchecked") V vv = (V)v;
     *                             return vv;
     *                         }
     *                         ## 如果替换旧值时失败，说明其它先一步修改了值，从头来过
     *                         break; // restart if lost race to replace value
     *                     }
     *                     // else c < 0; fall through
     *                 }
     *                 ## 有两种情况会到这里:
     *                 ## 1. 到链表尾部了，也就是n为null了
     *                 ## 2. 找到了目标节点的位置，也就是上面的c<0
     *
     *                 ## 新建目标节点，并赋值给z，这里把n作为新节点的next
     *                 ## 如果到链表尾部了，n为null,如果c<0,则n的key比目标key大，相当于在b和n之间插入目标节点z
     *                 z = new Node<K,V>(key, value, n);
     *                 if (!b.casNext(n, z)){
     *                     break;         // restart if lost race to append to b
     *                 }
     *                 ## 如果更新成功，跳出自旋状态
     *                 break outer;
     *             }
     *         }
     *         ## 经过Part 1,目标节点已经插入到有序链表中了
     *         ## Part 2:随机决定是否需要建立索引及其层次，如果需要则建立自上而下的索引
     *
     *         ## 取随机数
     *         int rnd = ThreadLocalRandom.nextSecondarySeed();
     *         ## 0x80000001展开为二进制为10000000000000000000000000000001，只有两头是1，这里(rnd & 0x80000001) ==0 相当于排除了负数（负数最高位是1），排除了奇数（奇数最低位是1）
     *         ## 只有最高位最低位都不为1的数和 0x80000001做&操作才会为0；也就是正偶数
     *         if ((rnd & 0x80000001) == 0) { // test highest and lowest bits
     *             ## 默认level为1，也就是只要到这里了就会至少建立一层索引
     *             int level = 1;
     *             int max;
     *             ## 随机数从最低位的第二位开始，有几个连续的1则level就加几
     *             ## 因为最低位肯定是0
     *             while (((rnd >>>= 1) & 1) != 0){
     *                 ++level;
     *             }
     *             ## 用于记录目标节点建立的最高的那层索引节点
     *             Index<K,V> idx = null;
     *             ## 取头索引节点（这是最高层的头索引节点）
     *             HeadIndex<K,V> h = head;
     *             ## 如果生成的层数小于等于当前最高层的层级，也就是跳表的高度不会超过现有的高度
     *             if (level <= (max = h.level)) {
     *                 ## 从第一层开始建立一条竖直的索引链表，这条链表使用down指针连接起来
     *                 ## 每个索引节点里面都存储着目标节点这个数据节点;最后idx存储的是这条索引链表的最高层节点
     *                 for (int i = 1; i <= level; ++i){
     *                     idx = new Index<K,V>(z, idx, null);
     *                 }
     *             }
     *             else { // try to grow by one level
     *                 ## 如果新的层数超过了现有跳表的高度,则最多只增加一层，比如现在只要一层索引，那下一次最多增加到两层索引。
     *                 level = max + 1; // hold in array and later pick the one to use
     *                 ## idx用户存储目标节点建立的竖起索引的所有索引节点
     *                 ## 其实这里直接使用idx这个最高节点也是可以完成的，只是用一个数组存储所有节点要方便。（这里数组0号位是没有使用的）
     *                 @SuppressWarnings("unchecked")Index<K,V>[] idxs = (Index<K,V>[])new Index<?,?>[level+1];
     *                 for (int i = 1; i <= level; ++i){
     *                     ## 从第一层开始建立一条竖的索引链表（根上面一样，只是这里顺便把索引节点放到数组里面了）
     *                     idxs[i] = idx = new Index<K,V>(z, idx, null);
     *                 }
     *                 ## 自旋
     *                 for (;;) {
     *                     ## 旧的最高层头索引节点
     *                     h = head;
     *                     ## 旧的最高层级
     *                     int oldLevel = h.level;
     *                     ## 再次检查，如果旧的最高层级已经不比新层级矮了
     *                     ## 说明有其它线程先一步修改了值，从头来过
     *                     if (level <= oldLevel){// lost race to add level
     *                         break;
     *                     }
     *                     ## 新的最高层头索引节点
     *                     HeadIndex<K,V> newh = h;
     *                     ## 头节点指向的数据节点
     *                     Node<K,V> oldbase = h.node;
     *                     ## 超出的部分建立新的头索引节点
     *                     for (int j = oldLevel+1; j <= level; ++j){
     *                         newh = new HeadIndex<K,V>(oldbase, newh, idxs[j], j);
     *                     }
     *                     ## 原子更新头索引节点
     *                     if (casHead(h, newh)) {
     *                         ## h指向新的最高层头索引节点
     *                         h = newh;
     *                         ## 把level赋值为旧的最高层级的idx指向的不是最高的索引节点了而是与旧的最高层平齐的索引节点
     *                         idx = idxs[level = oldLevel];
     *                         break;
     *                     }
     *                 }
     *             }
     *             ## 经过上面的步骤，有两种情况
     *             ## 1. 没有超出高度，新建一条目标节点的索引节点链
     *             ## 2. 超出了高度，新建一条目标节点的索引节点链，同时最高层头索引节点同样往上长
     *             // find insertion points and splice in
     *             ## part 3: 将新建的索引节点(包含头索引节点)与其它索引节点通过右指针连接在一起
     *             ## 这时level是等于旧的最高层级的，自旋
     *             splice: for (int insertionLevel = level;;) {
     *                  ## h为最高头索引节点
     *                 int j = h.level;
     *                 ## 从头索引节点开始遍历
     *                 ## 这里叫q为当前节点，r为右节点，d为下节点，t为目标节点相应层级的索引
     *                 for (Index<K,V> q = h, r = q.right, t = idx;;) {
     *                     ## 如果遍历到了最右边，或者最下边，也就是遍历到头了，则退出外层循环
     *                     if (q == null || t == null){
     *                         break splice;
     *                     }
     *                     ## 如果右节点不为空
     *                     if (r != null) {
     *                         ## n是右节点的数据节点，为了方便，这里直接叫右节点的值
     *                         Node<K,V> n = r.node;
     *                         // compare before deletion check avoids needing recheck
     *                         ## 比较目标key与右节点的值
     *                         int c = cpr(cmp, key, n.key);
     *                         ## 如果右节点的值为空了，则表示此节点已删除
     *                         if (n.value == null) {
     *                             ## 则把右节点删除
     *                             if (!q.unlink(r)){
     *                                 ## 如果删除失败，说明有其它线程先一步修改了，从头来过
     *                                 break;
     *                             }
     *                             ## 删除成功后重新取右节点
     *                             r = q.right;
     *                             continue;
     *                         }
     *                         ## 如果比较c>0，表示目标节点还要往右
     *                         if (c > 0) {
     *                             ## 则把当前节点和右节点分别右移
     *                             q = r;
     *                             r = r.right;
     *                             continue;
     *                         }
     *                     }
     *                     ## 到这里说明已经到当前层级的最右边了,这里实际是会先走第二个if
     *                     ## 第一个if j与insertionLevel相等了
     *                     ## 实际是先走的第二个if，j自减后应该与insertionLevel相等
     *                     if (j == insertionLevel) {
     *                         ## 这里是真正连右指针的地方
     *                         if (!q.link(r, t)){
     *                             ## 连接失败，从头来过
     *                             break; // restart
     *                         }
     *                         ## t节点的值为空，可能是其它线程删除了这个元素
     *                         if (t.node.value == null) {
     *                             ## 这里会去协助删除元素
     *                             findNode(key);
     *                             break splice;
     *                         }
     *                         ## 当前层级右指针连接完毕，向下移一层继续连接
     *                         ## 如果移到了最下面一层，则说明都连接完成了，退出外层循环
     *                         if (--insertionLevel == 0){
     *                             break splice;
     *                         }
     *                     }
     *                     ## 第二个if；j先减1，再与两个level比较
     *                     ## j、insertionLevel和t(idx)三者是对应的，都是还未把右指针连好的那个层级
     *                     if (--j >= insertionLevel && j < level){
     *                         ## t往下移
     *                         t = t.down;
     *                     }
     *                     ## 当前层级到最右边了,那只能往下一层级去走了;当前节点下移,再取相应的右节点
     *                     q = q.down;
     *                     r = q.right;
     *                 }
     *             }
     *         }
     *         return null;
     *     }
     *
     *
     *   ## 寻找目标节点之前最近的一个索引对应的数据节点
     *   private Node<K,V> findPredecessor(Object key, Comparator<? super K> cmp) {
     *         ## key不能为空
     *         if (key == null){
     *             throw new NullPointerException(); // don't postpone errors
     *         }
     *         ## 自旋
     *         for (;;) {
     *             ## 从最高层头索引节点开始查找，先向右，再向下；直到找到目标位置之前的那个索引
     *             for (Index<K,V> q = head, r = q.right, d;;) {
     *                 ## 如果右节点不为空
     *                 if (r != null) {
     *                     ## 右节点对应的数据节点，为了方便，我们叫右节点的值
     *                     Node<K,V> n = r.node;
     *                     K k = n.key;
     *                     ## 如果右节点的value为空；说明其它线程把这个节点标记为删除了则协助删除
     *                     if (n.value == null) {
     *                         if (!q.unlink(r)){
     *                             ## 如果删除失败
     *                             ## 说明其它线程先删除了，从头来过
     *                             break;           // restart
     *                         }
     *                         ## 删除之后重新读取右节点
     *                         r = q.right;         // reread r
     *                         continue;
     *                     }
     *                     ## 如果目标key比右节点还大，继续向右寻找
     *                     if (cpr(cmp, key, k) > 0) {
     *                         ## 往右移
     *                         q = r;
     *                         ## 重新取右节点
     *                         r = r.right;
     *                         continue;
     *                     }
     *                     ## 如果c<0，说明不能再往右了
     *                 }
     *                 ## 如果c<0，说明不能再往右了；
     *                 ## 两种情况：一是r==null，二是c<0 再从下一级开始找
     *                 ## 如果没有下一级了，就返回这个索引对应的数据节点
     *                 if ((d = q.down) == null){
     *                     return q.node;
     *                 }
     *                 ## 往下移
     *                 q = d;
     *                 ## 重新取右节点
     *                 r = d.right;
     *             }
     *         }
     *     }
     *
     *   ## Node.class中的方法，协助删除元素
     *   void helpDelete(Node<K,V> b, Node<K,V> f) {
     *     ## 这里的调用者this==n，三者关系是b->n->f
     *     if (f == next && this == b.next) {
     *         ## 将n的值设置为null后，会先把n的下个节点设置为marker节点;
     *         ## 这个marker节点的值是它自己
     *         ## 这里如果不是它自己说明marker失败了，重新marker
     *         if (f == null || f.value != f) {// not already marked
     *             casNext(f, new Node<K,V>(f));
     *         }
     *         else{
     *             ## marker过了，就把b的下个节点指向marker的下个节点
     *             b.casNext(this, f.next);
     *        }
     *     }
     * }
     * ## Index.class中的方法，删除succ节点
     * final boolean unlink(Index<K,V> succ) {
     *            ## 原子更新当前节点指向下一个节点的下一个节点
     *            ## 也就是删除下一个节点
     *             return node.value != null && casRight(succ, succ.right);
     *  }
     * ##  Index.class中的方法，在当前节点与succ之间插入newSucc节点
     *  final boolean link(Index<K,V> succ, Index<K,V> newSucc) {
     *             ## 在当前节点与下一个节点中间插入一个节点
     *             Node<K,V> n = node;
     *             ## 新节点指向当前节点的下一个节点
     *             newSucc.right = succ;
     *             ## 原子更新当前节点的下一个节点指向新节点
     *             return n.value != null && casRight(succ, newSucc);
     *   }
     */

}
