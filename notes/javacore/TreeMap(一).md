欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

---

### 插入元素

插入元素，如果元素在树中存在，则替换value；如果元素不存在，则插入到对应的位置，再平衡树。

```java
public V put(K key, V value) {
    Entry<K,V> t = root;
    if (t == null) {
        // 如果没有根节点，直接插入到根节点
        compare(key, key); // type (and possibly null) check
        root = new Entry<>(key, value, null);
        size = 1;
        modCount++;
        return null;
    }
    // key比较的结果
    int cmp;
    // 用来寻找待插入节点的父节点
    Entry<K,V> parent;
    // 根据是否有comparator使用不同的分支
    Comparator<? super K> cpr = comparator;
    if (cpr != null) {
        // 如果使用的是comparator方式，key值可以为null，只要在comparator.compare()中允许即可
        // 从根节点开始遍历寻找
        do {
            parent = t;
            cmp = cpr.compare(key, t.key);
            if (cmp < 0)
                // 如果小于0从左子树寻找
                t = t.left;
            else if (cmp > 0)
                // 如果大于0从右子树寻找
                t = t.right;
            else
                // 如果等于0，说明插入的节点已经存在了，直接更换其value值并返回旧值
                return t.setValue(value);
        } while (t != null);
    }
    else {
        // 如果使用的是Comparable方式，key不能为null
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        // 从根节点开始遍历寻找
        do {
            parent = t;
            cmp = k.compareTo(t.key);
            if (cmp < 0)
                // 如果小于0从左子树寻找
                t = t.left;
            else if (cmp > 0)
                // 如果大于0从右子树寻找
                t = t.right;
            else
                // 如果等于0，说明插入的节点已经存在了，直接更换其value值并返回旧值
                return t.setValue(value);
        } while (t != null);
    }
    // 如果没找到，那么新建一个节点，并插入到树中
    Entry<K,V> e = new Entry<>(key, value, parent);
    if (cmp < 0)
        // 如果小于0插入到左子节点
        parent.left = e;
    else
        // 如果大于0插入到右子节点
        parent.right = e;

    // 插入之后的平衡
    fixAfterInsertion(e);
    // 元素个数加1（不需要扩容）
    size++;
    // 修改次数加1
    modCount++;
    // 如果插入了新节点返回空
    return null;
}
```

### 插入再平衡

插入的元素默认都是红色，因为插入红色元素只违背了第4条特性，那么我们只要根据这个特性来平衡就容易多了。

根据不同的情况有以下几种处理方式：

1. 插入的元素如果是根节点，则直接涂成黑色即可，不用平衡；

2. 插入的元素的父节点如果为黑色，不需要平衡；

3. 插入的元素的父节点如果为红色，则违背了特性4，需要平衡，平衡时又分成下面三种情况：

**（如果父节点是祖父节点的左节点）**

情况|策略
---|---
1）父节点为红色，叔叔节点也为红色|（1）将父节点设为黑色；<br>（2）将叔叔节点设为黑色；<br>（3）将祖父节点设为红色；<br>（4）将祖父节点设为新的当前节点，进入下一次循环判断；
2）父节点为红色，叔叔节点为黑色，且当前节点是其父节点的右节点|（1）将父节点作为新的当前节点；<br>（2）以新当节点为支点进行左旋，进入情况3）；
3）父节点为红色，叔叔节点为黑色，且当前节点是其父节点的左节点|（1）将父节点设为黑色；<br>（2）将祖父节点设为红色；<br>（3）以祖父节点为支点进行右旋，进入下一次循环判断；

**（如果父节点是祖父节点的右节点，则正好与上面反过来）**

情况|策略
---|---
1）父节点为红色，叔叔节点也为红色|（1）将父节点设为黑色；<br>（2）将叔叔节点设为黑色；<br>（3）将祖父节点设为红色；<br>（4）将祖父节点设为新的当前节点，进入下一次循环判断；
2）父节点为红色，叔叔节点为黑色，且当前节点是其父节点的左节点|（1）将父节点作为新的当前节点；<br>（2）以新当节点为支点进行右旋；
3）父节点为红色，叔叔节点为黑色，且当前节点是其父节点的右节点|（1）将父节点设为黑色；<br>（2）将祖父节点设为红色；<br>（3）以祖父节点为支点进行左旋，进入下一次循环判断；

让我们来看看TreeMap中的实现：

```java
/**
 * 插入再平衡
 *（1）每个节点或者是黑色，或者是红色。
 *（2）根节点是黑色。
 *（3）每个叶子节点（NIL）是黑色。（注意：这里叶子节点，是指为空(NIL或NULL)的叶子节点！）
 *（4）如果一个节点是红色的，则它的子节点必须是黑色的。
 *（5）从一个节点到该节点的子孙节点的所有路径上包含相同数目的黑节点。
 */
private void fixAfterInsertion(Entry<K,V> x) {
    // 插入的节点为红节点，x为当前节点
    x.color = RED;

    // 只有当插入节点不是根节点且其父节点为红色时才需要平衡（违背了特性4）
    while (x != null && x != root && x.parent.color == RED) {
        if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
            // a）如果父节点是祖父节点的左节点
            // y为叔叔节点
            Entry<K,V> y = rightOf(parentOf(parentOf(x)));
            if (colorOf(y) == RED) {
                // 情况1）如果叔叔节点为红色
                // （1）将父节点设为黑色
                setColor(parentOf(x), BLACK);
                // （2）将叔叔节点设为黑色
                setColor(y, BLACK);
                // （3）将祖父节点设为红色
                setColor(parentOf(parentOf(x)), RED);
                // （4）将祖父节点设为新的当前节点
                x = parentOf(parentOf(x));
            } else {
                // 如果叔叔节点为黑色
                // 情况2）如果当前节点为其父节点的右节点
                if (x == rightOf(parentOf(x))) {
                    // （1）将父节点设为当前节点
                    x = parentOf(x);
                    // （2）以新当前节点左旋
                    rotateLeft(x);
                }
                // 情况3）如果当前节点为其父节点的左节点（如果是情况2）则左旋之后新当前节点正好为其父节点的左节点了）
                // （1）将父节点设为黑色
                setColor(parentOf(x), BLACK);
                // （2）将祖父节点设为红色
                setColor(parentOf(parentOf(x)), RED);
                // （3）以祖父节点为支点进行右旋
                rotateRight(parentOf(parentOf(x)));
            }
        } else {
            // b）如果父节点是祖父节点的右节点
            // y是叔叔节点
            Entry<K,V> y = leftOf(parentOf(parentOf(x)));
            if (colorOf(y) == RED) {
                // 情况1）如果叔叔节点为红色
                // （1）将父节点设为黑色
                setColor(parentOf(x), BLACK);
                // （2）将叔叔节点设为黑色
                setColor(y, BLACK);
                // （3）将祖父节点设为红色
                setColor(parentOf(parentOf(x)), RED);
                // （4）将祖父节点设为新的当前节点
                x = parentOf(parentOf(x));
            } else {
                // 如果叔叔节点为黑色
                // 情况2）如果当前节点为其父节点的左节点
                if (x == leftOf(parentOf(x))) {
                    // （1）将父节点设为当前节点
                    x = parentOf(x);
                    // （2）以新当前节点右旋
                    rotateRight(x);
                }
                // 情况3）如果当前节点为其父节点的右节点（如果是情况2）则右旋之后新当前节点正好为其父节点的右节点了）
                // （1）将父节点设为黑色
                setColor(parentOf(x), BLACK);
                // （2）将祖父节点设为红色
                setColor(parentOf(parentOf(x)), RED);
                // （3）以祖父节点为支点进行左旋
                rotateLeft(parentOf(parentOf(x)));
            }
        }
    }
    // 平衡完成后将根节点设为黑色
    root.color = BLACK;
}
```

### 插入元素举例

我们依次向红黑树中插入 4、2、3 三个元素，来一起看看整个红黑树平衡的过程。

三个元素都插入完成后，符合父节点是祖父节点的左节点，叔叔节点为黑色，且当前节点是其父节点的右节点，即情况2）。

![1](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap1.png)

情况2）需要做以下两步处理：

（1）将父节点作为新的当前节点；

（2）以新当节点为支点进行左旋，进入情况3）；

![2](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap2.png)

情况3）需要做以下三步处理：

（1）将父节点设为黑色；

（2）将祖父节点设为红色；

（3）以祖父节点为支点进行右旋，进入下一次循环判断；

![3](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap3.png)

下一次循环不符合父节点为红色了，退出循环，插入再平衡完成。

---

未完待续，下一节我们一起探讨红黑树删除元素的操作。

**现在公众号文章没办法留言了，如果有什么疑问或者建议请直接在公众号给我留言。**

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)