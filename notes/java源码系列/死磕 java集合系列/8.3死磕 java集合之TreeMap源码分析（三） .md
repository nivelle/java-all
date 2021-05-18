欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。 

---

### 删除元素

删除元素本身比较简单，就是采用二叉树的删除规则。

（1）如果删除的位置有两个叶子节点，则从其右子树中取最小的元素放到删除的位置，然后把删除位置移到替代元素的位置，进入下一步。

（2）如果删除的位置只有一个叶子节点（有可能是经过第一步转换后的删除位置），则把那个叶子节点作为替代元素，放到删除的位置，然后把这个叶子节点删除。

（3）如果删除的位置没有叶子节点，则直接把这个删除位置的元素删除即可。

（4）针对红黑树，如果删除位置是黑色节点，还需要做再平衡。

（5）如果有替代元素，则以替代元素作为当前节点进入再平衡。

（6）如果没有替代元素，则以删除的位置的元素作为当前节点进入再平衡，平衡之后再删除这个节点。

```java
public V remove(Object key) {
    // 获取节点
    Entry<K,V> p = getEntry(key);
    if (p == null)
        return null;

    V oldValue = p.value;
    // 删除节点
    deleteEntry(p);
    // 返回删除的value
    return oldValue;
}

private void deleteEntry(Entry<K,V> p) {
    // 修改次数加1
    modCount++;
    // 元素个数减1
    size--;

    if (p.left != null && p.right != null) {
        // 如果当前节点既有左子节点，又有右子节点
        // 取其右子树中最小的节点
        Entry<K,V> s = successor(p);
        // 用右子树中最小节点的值替换当前节点的值
        p.key = s.key;
        p.value = s.value;
        // 把右子树中最小节点设为当前节点
        p = s;
        // 这种情况实际上并没有删除p节点，而是把p节点的值改了，实际删除的是p的后继节点
    }

    // 如果原来的当前节点（p）有2个子节点，则当前节点已经变成原来p的右子树中的最小节点了，也就是说其没有左子节点了
    // 到这一步，p肯定只有一个子节点了
    // 如果当前节点有子节点，则用子节点替换当前节点
    Entry<K,V> replacement = (p.left != null ? p.left : p.right);

    if (replacement != null) {
        // 把替换节点直接放到当前节点的位置上（相当于删除了p，并把替换节点移动过来了）
        replacement.parent = p.parent;
        if (p.parent == null)
            root = replacement;
        else if (p == p.parent.left)
            p.parent.left  = replacement;
        else
            p.parent.right = replacement;

        // 将p的各项属性都设为空
        p.left = p.right = p.parent = null;

        // 如果p是黑节点，则需要再平衡
        if (p.color == BLACK)
            fixAfterDeletion(replacement);
    } else if (p.parent == null) {
        // 如果当前节点就是根节点，则直接将根节点设为空即可
        root = null;
    } else {
        // 如果当前节点没有子节点且其为黑节点，则把自己当作虚拟的替换节点进行再平衡
        if (p.color == BLACK)
            fixAfterDeletion(p);

        // 平衡完成后删除当前节点（与父节点断绝关系）
        if (p.parent != null) {
            if (p == p.parent.left)
                p.parent.left = null;
            else if (p == p.parent.right)
                p.parent.right = null;
            p.parent = null;
        }
    }
}
```

### 删除再平衡

经过上面的处理，真正删除的肯定是黑色节点才会进入到再平衡阶段。

因为删除的是黑色节点，导致整颗树不平衡了，所以这里我们假设把删除的黑色赋予当前节点，这样当前节点除了它自已的颜色还多了一个黑色，那么：

（1）如果当前节点是根节点，则直接涂黑即可，不需要再平衡；

（2）如果当前节点是红+黑节点，则直接涂黑即可，不需要平衡；

（3）如果当前节点是黑+黑节点，则我们只要通过旋转把这个多出来的黑色不断的向上传递到一个红色节点即可，这又可能会出现以下四种情况：

**（假设当前节点为父节点的左子节点）**

情况|策略
---|---
1）x是黑+黑节点，x的兄弟是红节点|（1）将兄弟节点设为黑色；<br>（2）将父节点设为红色；<br>（3）以父节点为支点进行左旋；<br>（4）重新设置x的兄弟节点，进入下一步；
2）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的两个子节点都是黑色|（1）将兄弟节点设置为红色；<br>（2）将x的父节点作为新的当前节点，进入下一次循环；
3）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的右子节点为黑色，左子节点为红色|（1）将兄弟节点的左子节点设为黑色；<br>（2）将兄弟节点设为红色；<br>（3）以兄弟节点为支点进行右旋；<br>（4）重新设置x的兄弟节点，进入下一步；
3）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的右子节点为红色，左子节点任意颜色|（1）将兄弟节点的颜色设为父节点的颜色；<br>（2）将父节点设为黑色；<br>（3）将兄弟节点的右子节点设为黑色；<br>（4）以父节点为支点进行左旋；<br>（5）将root作为新的当前节点（退出循环）；

**（假设当前节点为父节点的右子节点，正好反过来）**

情况|策略
---|---
1）x是黑+黑节点，x的兄弟是红节点|（1）将兄弟节点设为黑色；<br>（2）将父节点设为红色；<br>（3）以父节点为支点进行右旋；<br>（4）重新设置x的兄弟节点，进入下一步；
2）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的两个子节点都是黑色|（1）将兄弟节点设置为红色；<br>（2）将x的父节点作为新的当前节点，进入下一次循环；
3）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的左子节点为黑色，右子节点为红色|（1）将兄弟节点的右子节点设为黑色；<br>（2）将兄弟节点设为红色；<br>（3）以兄弟节点为支点进行左旋；<br>（4）重新设置x的兄弟节点，进入下一步；
3）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的左子节点为红色，右子节点任意颜色|（1）将兄弟节点的颜色设为父节点的颜色；<br>（2）将父节点设为黑色；<br>（3）将兄弟节点的左子节点设为黑色；<br>（4）以父节点为支点进行右旋；<br>（5）将root作为新的当前节点（退出循环）；

让我们来看看TreeMap中的实现：

```java
/**
 * 删除再平衡
 *（1）每个节点或者是黑色，或者是红色。
 *（2）根节点是黑色。
 *（3）每个叶子节点（NIL）是黑色。（注意：这里叶子节点，是指为空(NIL或NULL)的叶子节点！）
 *（4）如果一个节点是红色的，则它的子节点必须是黑色的。
 *（5）从一个节点到该节点的子孙节点的所有路径上包含相同数目的黑节点。
 */
private void fixAfterDeletion(Entry<K,V> x) {
    // 只有当前节点不是根节点且当前节点是黑色时才进入循环
    while (x != root && colorOf(x) == BLACK) {
        if (x == leftOf(parentOf(x))) {
            // 如果当前节点是其父节点的左子节点
            // sib是当前节点的兄弟节点
            Entry<K,V> sib = rightOf(parentOf(x));

            // 情况1）如果兄弟节点是红色
            if (colorOf(sib) == RED) {
                // （1）将兄弟节点设为黑色
                setColor(sib, BLACK);
                // （2）将父节点设为红色
                setColor(parentOf(x), RED);
                // （3）以父节点为支点进行左旋
                rotateLeft(parentOf(x));
                // （4）重新设置x的兄弟节点，进入下一步
                sib = rightOf(parentOf(x));
            }

            if (colorOf(leftOf(sib))  == BLACK &&
                    colorOf(rightOf(sib)) == BLACK) {
                // 情况2）如果兄弟节点的两个子节点都是黑色
                // （1）将兄弟节点设置为红色
                setColor(sib, RED);
                // （2）将x的父节点作为新的当前节点，进入下一次循环
                x = parentOf(x);
            } else {
                if (colorOf(rightOf(sib)) == BLACK) {
                    // 情况3）如果兄弟节点的右子节点为黑色
                    // （1）将兄弟节点的左子节点设为黑色
                    setColor(leftOf(sib), BLACK);
                    // （2）将兄弟节点设为红色
                    setColor(sib, RED);
                    // （3）以兄弟节点为支点进行右旋
                    rotateRight(sib);
                    // （4）重新设置x的兄弟节点
                    sib = rightOf(parentOf(x));
                }
                // 情况4）
                // （1）将兄弟节点的颜色设为父节点的颜色
                setColor(sib, colorOf(parentOf(x)));
                // （2）将父节点设为黑色
                setColor(parentOf(x), BLACK);
                // （3）将兄弟节点的右子节点设为黑色
                setColor(rightOf(sib), BLACK);
                // （4）以父节点为支点进行左旋
                rotateLeft(parentOf(x));
                // （5）将root作为新的当前节点（退出循环）
                x = root;
            }
        } else { // symmetric
            // 如果当前节点是其父节点的右子节点
            // sib是当前节点的兄弟节点
            Entry<K,V> sib = leftOf(parentOf(x));

            // 情况1）如果兄弟节点是红色
            if (colorOf(sib) == RED) {
                // （1）将兄弟节点设为黑色
                setColor(sib, BLACK);
                // （2）将父节点设为红色
                setColor(parentOf(x), RED);
                // （3）以父节点为支点进行右旋
                rotateRight(parentOf(x));
                // （4）重新设置x的兄弟节点
                sib = leftOf(parentOf(x));
            }

            if (colorOf(rightOf(sib)) == BLACK &&
                    colorOf(leftOf(sib)) == BLACK) {
                // 情况2）如果兄弟节点的两个子节点都是黑色
                // （1）将兄弟节点设置为红色
                setColor(sib, RED);
                // （2）将x的父节点作为新的当前节点，进入下一次循环
                x = parentOf(x);
            } else {
                if (colorOf(leftOf(sib)) == BLACK) {
                    // 情况3）如果兄弟节点的左子节点为黑色
                    // （1）将兄弟节点的右子节点设为黑色
                    setColor(rightOf(sib), BLACK);
                    // （2）将兄弟节点设为红色
                    setColor(sib, RED);
                    // （3）以兄弟节点为支点进行左旋
                    rotateLeft(sib);
                    // （4）重新设置x的兄弟节点
                    sib = leftOf(parentOf(x));
                }
                // 情况4）
                // （1）将兄弟节点的颜色设为父节点的颜色
                setColor(sib, colorOf(parentOf(x)));
                // （2）将父节点设为黑色
                setColor(parentOf(x), BLACK);
                // （3）将兄弟节点的左子节点设为黑色
                setColor(leftOf(sib), BLACK);
                // （4）以父节点为支点进行右旋
                rotateRight(parentOf(x));
                // （5）将root作为新的当前节点（退出循环）
                x = root;
            }
        }
    }

    // 退出条件为多出来的黑色向上传递到了根节点或者红节点
    // 则将x设为黑色即可满足红黑树规则
    setColor(x, BLACK);
}
```


### 删除元素举例

假设我们有下面这样一颗红黑树。

![treemap-delete1](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap-delete1.png)

我们删除6号元素，则从右子树中找到了最小元素7，7又没有子节点了，所以把7作为当前节点进行再平衡。

我们看到7是黑节点，且其兄弟为黑节点，且其兄弟的两个子节点都是红色，满足情况4），平衡之后如下图所示。

![treemap-delete2](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap-delete2.png)

我们再删除7号元素，则从右子树中找到了最小元素8，8有子节点且为黑色，所以8的子节点9是替代节点，以9为当前节点进行再平衡。

我们发现9是红节点，则直接把它涂成黑色即满足了红黑树的特性，不需要再过多的平衡了。

![treemap-delete3](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap-delete3.png)

这次我们来个狠的，把根节点删除，从右子树中找到了最小的元素5，5没有子节点，所以把5作为当前节点进行再平衡。

我们看到5是黑节点，且其兄弟为红色，符合情况1），平衡之后如下图所示，然后进入情况2）。

![treemap-delete4](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap-delete4.png)

对情况2）进行再平衡后如下图所示。

![treemap-delete5](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap-delete5.png)

然后进入下一次循环，发现不符合循环条件了，直接把x涂为黑色即可，退出这个方法之后会把旧x删除掉（见deleteEntry()方法），最后的结果就是下面这样。

![treemap-delete6](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/treemap-delete6.png)


---

未完待续，下一节我们一起探讨红黑树遍历元素的操作。

**现在公众号文章没办法留言了，如果有什么疑问或者建议请直接在公众号给我留言。**

---

欢迎关注我的公众号“彤哥读源码”，查看更多源码系列文章, 与彤哥一起畅游源码的海洋。

![qrcode](https://gitee.com/alan-tang-tt/yuan/raw/master/死磕%20java集合系列/resource/qrcode_ss.jpg)