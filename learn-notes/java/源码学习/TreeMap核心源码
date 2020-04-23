##  TreeMap 红黑树

```
public class TreeMap<K,V> extends AbstractMap<K,V> implements NavigableMap<K,V>, Cloneable, java.io.Serializable

```

### SortedMap规定了元素可以按key的大小来遍历,它定义了一些返回部分map的方法。

### NavigableMap是对SortedMap的增强,定义了一些返回离目标key最近的元素的方法。
    

## 红黑树特征

定义：含有红黑节点并能自平衡的二叉查找树；

性质1：每个节点或者是黑色,或者是红色

性质2：根节点是黑色

性质3：每个叶子节点（NIL）是黑色。（注意：这里叶子节点，是指为空(NIL或NULL)的叶子节点!）

性质4：如果一个节点是红色的，则它的子节点必须是黑色的;

性质5：任意一个节点到任意一个叶子节点的所有路径上包含相同数目的黑色节点(这里指到叶子节点的路径)

### 红黑树的平衡性

## get

```
public V get(Object key) {
    // 根据key查找元素
    Entry<K,V> p = getEntry(key);
        //找到返回值,否则返回null
        return (p==null ? null : p.value);
    }
  

//有自定义比较器使用自定义比较器，否则使用key实现的比较器接口  
final Entry<K,V> getEntry(Object key) {
        //如果比较器不为空,则使用该比较器遍历树结构来获取指定节点
        if (comparator != null){
            return getEntryUsingComparator(key);
        }
        if (key == null){
            throw new NullPointerException();
        }
        //否则获取key实现的Comparable接口
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        Entry<K,V> p = root;
        //从root节点开始遍历找到指定节点
        while (p != null) {
            int cmp = k.compareTo(p.key);//比根节点小，左子树遍历
            if (cmp < 0)
                // 如果小于0从左子树查找
                p = p.left;
            else if (cmp > 0)//比根节点大，右子树遍历
                p = p.right;
            else
                return p;
        }
        return null;
    }
   
//使用自定义比较器
final Entry<K,V> getEntryUsingComparator(Object key) {
        @SuppressWarnings("unchecked")
            K k = (K) key;
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            Entry<K,V> p = root;
            while (p != null) {
                int cmp = cpr.compare(k, p.key);
                if (cmp < 0)
                    p = p.left;
                else if (cmp > 0)
                    p = p.right;
                else
                    return p;
            }
        }
        return null;
    }
```

---

### 左旋

1. 旋转节点的圆心一定是它的子节点

2. 旋转节点围绕圆心逆时针方向转动

3. 基于最短路径来确定方向旋转【判断左选还是右旋】

![JJvsC4.png](https://s1.ax1x.com/2020/04/21/JJvsC4.png)
```
//以p为支点进行左旋，假设p为图中的x
private void rotateLeft(Entry<K,V> p) {
        if (p != null) {
            //p的右节点,即y
            Entry<K,V> r = p.right; 
            //将y的左节点设置为x的右节点           
            p.right = r.left;
            if (r.left != null){
                //x设置为y的左节点的父节点（如果y的左节点存在的话）
                r.left.parent = p;
            }  
            //设置x的父节点为y的父节点  
            r.parent = p.parent;            
            if (p.parent == null){
                root = r;//如果x的父节点为空，则将y设置为根节点
            }
            //如果x是它父亲节点的左节点，则将y设置为x父节点的左节点
            else if (p.parent.left == p){
                p.parent.left = r;
            }
            else{
                //如果x是它父亲节点的右节点,则将y设置为x父节点的右节点
                p.parent.right = r;
            }
            //将x设置为y的左节点
            r.left = p;
            //将x的父节点设置为y
            p.parent = r;
        }
    }

```

### 右旋

![JYSjVx.png](https://s1.ax1x.com/2020/04/21/JYSjVx.png)


```
// 以p为支点进行右旋，假设p为图中的y
private void rotateRight(Entry<K,V> p) {
        if (p != null) {
            //p的左节点，即x
            Entry<K,V> l = p.left;
            //将y设置为x的右节点的父节点
            p.left = l.right;
            if (l.right != null) {
              //将y设置为x的右节点的父节点
              l.right.parent = p;
            }
            //将y的父亲节点设置为x的父节点
            l.parent = p.parent;
            if (p.parent == null){
                //如果y的父节点是空节点，则将x设置为根节点
                root = l;
            }
            //如果y是它父亲节点的右节点，则将x设置为y的父节点的右节点
            else if (p.parent.right == p){
                //如果y是它父节点的右节点，则将x设为y的父节点的右节点
                p.parent.right = l;
            }
            else {
               //如果y是它父亲节点的左节点，则将x设为y的父亲节点的左节点
               p.parent.left = l;
            }
            //将y设为x的右节点
            l.right = p;
            //将y的父节点设为x
            p.parent = l;
        }
    }

```

## public V put(K key, V value)

c = currentNode;

1. c = root

2. c.parent = black

3. c.parent = red & c.uncle = red

4. c.parent = red & (c.uncle = black or c.uncle is nil)


[![JGBcYn.png](https://s1.ax1x.com/2020/04/21/JGBcYn.png)](https://imgchr.com/i/JGBcYn)

```
  public V put(K key, V value) {
         Entry<K,V> t = root;
         if (t == null) {
             // type (and possibly null) check
             compare(key, key); 
             //如果没有根节点,直接插入到根节点
             root = new Entry<>(key, value, null);
             size = 1;
             modCount++;
             //插入根接待你返回null
             return null;
         }
         //key比较结果
         int cmp;
         // 用来寻找待插入节点的父节点
         Entry<K,V> parent;
         // 根据是否有comparator使用不同的分支
         Comparator<? super K> cpr = comparator;
         //如果比较器不为空
         if (cpr != null) {
             do {
                 // 如果使用的是comparator方式，key值可以为null，只要在comparator.compare()中允许即可,从根节点开始遍历寻找
                 parent = t;
                 //插入节点和根节点比较
                 cmp = cpr.compare(key, t.key);
                 if (cmp < 0){
                     //如果小于根节点，则在左子树
                     t = t.left;
                 }else if (cmp > 0){
                     //如果大于根节点，则在右子树
                     t = t.right;
                 }else
                     // 如果等于0，说明插入的节点已经存在了，直接更换其value值并返回旧值
                     return t.setValue(value);
             } while (t != null);
         }else {
              //如果不存在比较器(comparator)则key不能为空，因为要使用key实现的Comparable
             if (key == null){
                 throw new NullPointerException();
              }
             @SuppressWarnings("unchecked")
             Comparable<? super K> k = (Comparable<? super K>) key;
             // 从根节点开始遍历寻找
             do {
                 parent = t;
                 cmp = k.compareTo(t.key);
                 if (cmp < 0){
                    //如果小于根节点，则在左子树
                     t = t.left;
                 }else if (cmp > 0){
                    //如果大于根节点，则在右子树
                     t = t.right;
                 }else
                     // 如果等于0，说明插入的节点已经存在了，直接更换其value值并返回 旧值
                     return t.setValue(value);
             } while (t != null);
         }
         // 如果没找到，那么新建一个节点，并插入到树中         
         Entry<K,V> e = new Entry<>(key, value, parent);
         if (cmp < 0){
             //如果小于0插入到左子节点
             parent.left = e;
         }else{
             //如果大于0插入到右子节点
             parent.right = e;
         }
         // 插入之后的平衡
         fixAfterInsertion(e);
         size++;
         modCount++;
         return null;
     }

```

### private void fixAfterInsertion(Entry<K,V> x) //再平衡方法

1. 默认插入元素是红色的,只违背了第四条容易调整

2. 插入的元素的父节点如果为黑色，不需要平衡；
   
3. 插入的元素的父节点如果为红色，则违背了特性4，需要平衡，平衡时又分成下面三种情况：
   
   3.1 如果父节点是祖父节点的左节点
   
   情况 | 策略
   ---|---
   1）父节点为红色，叔叔节点也为红色	 | （1）将父节点设为黑色;（2）将叔叔节点设为黑色（3）将祖父节点设为红色;（4）将祖父节点设为新的当前节点，进入下一次循环判断;
   2）父节点为红色，叔叔节点为黑色，且当前节点是其父节点的右节点	 | （1）将父节点作为新的当前节点;（2）以新当节点为支点进行左旋，进入情况3）;
   3）父节点为红色，叔叔节点为黑色，且当前节点是其父节点的左节点	| （1）将父节点设为黑色;（2）将祖父节点设为红色;（3）以祖父节点为支点进行右旋，进入下一次循环判断;

   3.2 如果父节点是祖父节点的右节点，则正好与上面反过来
   
   情况 | 策略
   ---|---
   1）父节点为红色，叔叔节点也为红色		 | （1）将父节点设为黑色；（2）将叔叔节点设为黑色（3）将祖父节点设为红色；（4）将祖父节点设为新的当前节点，进入下一次循环判断；
   2）父节点为红色，叔叔节点为黑色，且当前节点是其父节点的左节点		 | （1）将父节点作为新的当前节点；（2）以新当节点为支点进行右旋；
   3）父节点为红色，叔叔节点为黑色，且当前节点是其父节点的右节点		| （1）将父节点设为黑色；（2）将祖父节点设为红色；（3）以祖父节点为支点进行左旋，进入下一次循环判断；

```
private void fixAfterInsertion(Entry<K,V> x) {
        // 插入的节点为红节点，x为当前节点
        x.color = RED;
        // 只有当插入节点不是根节点且其父节点为红色时才需要平衡（违背了特性4）
        while (x != null && x != root && x.parent.color == RED) {
            //x节点的父节点是祖父节点的左节点
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                //x节点的叔叔节点:y
                Entry<K,V> y = rightOf(parentOf(parentOf(x)));
                //如果叔叔节点是红色
                if (colorOf(y) == RED) {
                     // 情况1）如果叔叔节点为红色               
                    //父节点设置为黑色
                    setColor(parentOf(x), BLACK);
                    //叔叔节点设置为黑色
                    setColor(y, BLACK);
                    //祖父节点设置为红色
                    setColor(parentOf(parentOf(x)), RED);
                    // 将祖父节点设置为新的当前
                    x = parentOf(parentOf(x));
                } else {
                    // 情况2）如果叔叔节点为黑色 & 当前节点为其父节点的右节点
                    if (x == rightOf(parentOf(x))) {
                        //将父亲节点设置为当前节点
                        x = parentOf(x);
                        //以新当前节点左旋
                        rotateLeft(x);
                    }
                    //情况3）如果当前节点为其父节点的左节点（如果是情况2）则左旋之后新当前节点正好为其父节点的左节点了）
                    setColor(parentOf(x), BLACK);// （1）将父节点设为黑色                                                 
                    setColor(parentOf(parentOf(x)), RED);//（2）将祖父节点设为红色                                                        
                    rotateRight(parentOf(parentOf(x)));// （3）以祖父节点为支点进行右旋                                                       
                }
            } else {
                //y节点是x的祖父节点的左节点，y是叔叔节点
                Entry<K,V> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {// 情况1）如果叔叔节点为红色                                    
                    setColor(parentOf(x), BLACK);// （1）将父节点设为黑色                                                
                    setColor(y, BLACK);//（2）将叔叔节点设为黑色                                       
                    setColor(parentOf(parentOf(x)), RED);// （3）将祖父节点设为红色                                                        
                    x = parentOf(parentOf(x));// （4）将祖父节点设为新的当前节点                                              
                } else {// 如果叔叔节点为黑色                                      
                    if (x == leftOf(parentOf(x))) {// 情况2）如果当前节点为其父节点的左节点                                                     
                        x = parentOf(x);//将父节点设置为当前节点
                        rotateRight(x);//以新当前节点进行右旋转
                    }
                    // 情况3）如果当前节点为其父节点的右节点（如果是情况2）则右旋之后新当前节点正好为其父节点的右节点了）
                    setColor(parentOf(x), BLACK);// （1）将父节点设为黑色                                                 
                    setColor(parentOf(parentOf(x)), RED);// （2）将祖父节点设为红色                                                        
                    rotateLeft(parentOf(parentOf(x)));// （3）以祖父节点为支点进行左旋                                                    
                }
            }
        }
        root.color = BLACK;// 平衡完成后将根节点设为黑色
                          
    }

```


## 删除指定节点

```
public V remove(Object key) {
        // 获取节点
        Entry<K,V> p = getEntry(key);
        if (p == null){//如果元素不存在,直接返回null
            return null;
         }
        //获取当前存在的值
        V oldValue = p.value;
        //删除元素
        deleteEntry(p);
        return oldValue;
}

```

### 具体删除方法

（1）如果删除的位置有两个叶子节点，则从其右子树中取最小的元素放到删除的位置，然后把删除位置移到替代元素的位置，进入下一步。

（2）如果删除的位置只有一个叶子节点（有可能是经过第一步转换后的删除位置），则把那个叶子节点作为替代元素，放到删除的位置，然后把这个叶子节点删除。

（3）如果删除的位置没有叶子节点，则直接把这个删除位置的元素删除即可。

（4）针对红黑树，如果删除位置是黑色节点，还需要做再平衡。

（5）如果有替代元素，则以替代元素作为当前节点进入再平衡。

（6）如果没有替代元素，则以删除的位置的元素作为当前节点进入再平衡，平衡之后再删除这个节点。

```
private void deleteEntry(Entry<K,V> p) {
        modCount++;
        size--;
        //如果既有左节点又有右节点
        if (p.left != null && p.right != null) {
            // 取其右子树中最小的节点,用来替代将要被删除的p元素(有序投影)
            Entry<K,V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;// 把右子树中最小节点设为当前节点 
            //走到这里 这种情况实际上并没有删除p节点，而是把p节点的值改了，实际删除的是p的后继节点                
        }                    
        // 如果原来的当前节点(p)有2个子节点，则当前节点已经变成原来p的右子树中的最小节点了,也就是说其没有左子节点了
        // 到这一步，p肯定只有一个子节点了,如果当前节点有子节点,则用子节点替换当前节点
        Entry<K,V> replacement = (p.left != null ? p.left : p.right);
        if (replacement != null) {
            //把替换节点直接放到当前节点的位置上（相当于删除了p，并把替换节点移动过来了）
            replacement.parent = p.parent;
            //用replacement替换p节点
            if (p.parent == null){
                //如果p节点父节点为空，则其为root节点
                root = replacement;
            }
            else if (p == p.parent.left){
                p.parent.left  = replacement;
            }else{
                p.parent.right = replacement;
            }
            // 将p的各项属性都设为空
            p.left = p.right = p.parent = null;
            //如果p节点是黑节点,则需要再平衡
            if (p.color == BLACK){
                fixAfterDeletion(replacement);
            }
        } else if (p.parent == null) { //如果要删除的是根节点，则直接设置为null
            root = null;
        } else { 
            // 如果当前节点没有子节点且其为黑节点，则把自己当作虚拟的替换节点进行再平衡
            if (p.color == BLACK){
                fixAfterDeletion(p);
            }
            //平衡完成后删除当前节点（与父节点断绝关系）
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

### 删除后再平衡的方法

因为删除的是黑色节点，导致整颗树不平衡了，所以这里我们假设把删除的黑色赋予当前节点，这样当前节点除了它自已的颜色还多了一个黑色，那么：


（1）如果当前节点是根节点,则直接涂黑即可,不需要再平衡；

（2）如果当前节点是红+黑节点,则直接涂黑即可,不需要平衡；

（3）如果当前节点是黑+黑节点,则我们只要通过旋转把这个多出来的黑色不断的向上传递到一个红色节点即可,这又可能会出现以下四种情况：


#### 当前节点为父节点的左子节点

  情况	 | 策略
     ---|---
     1）x是黑+黑节点，x的兄弟是红节点	 | （1）将兄弟节点设为黑色；（2）将父节点设为红色；（3）以父节点为支点进行左旋；（4）重新设置x的兄弟节点，进入下一步；
     2）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的两个子节点都是黑色	 | （1）将兄弟节点设置为红色；（2）将x的父节点作为新的当前节点，进入下一次循环；
     3）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的右子节点为黑色，左子节点为红色	|（1）将兄弟节点的左子节点设为黑色；（2）将兄弟节点设为红色；（3）以兄弟节点为支点进行右旋；（4）重新设置x的兄弟节点，进入下一步；
     3）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的右子节点为红色，左子节点任意颜色 | （1）将兄弟节点的颜色设为父节点的颜色；（2）将父节点设为黑色；（3）将兄弟节点的右子节点设为黑色；（4）以父节点为支点进行左旋；（5）将root作为新的当前节点（退出循环）；	

#### 当前节点为父节点的右子节点
     
   情况	 | 策略
     ---|---
     1）x是黑+黑节点，x的兄弟是红节点	| （1）将兄弟节点设为黑色；（2）将父节点设为红色；（3）以父节点为支点进行右旋；（4）重新设置x的兄弟节点，进入下一步；	  
     2）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的两个子节点都是黑色	| （1）将兄弟节点设置为红色；（2）将x的父节点作为新的当前节点，进入下一次循环；
     3）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的左子节点为黑色，右子节点为红色	| （1）将兄弟节点的右子节点设为黑色；（2）将兄弟节点设为红色；（3）以兄弟节点为支点进行左旋；（4）重新设置x的兄弟节点，进入下一步；
     3）x是黑+黑节点，x的兄弟是黑节点，且兄弟节点的左子节点为红色，右子节点任意颜色	|  （1）将兄弟节点的颜色设为父节点的颜色；（2）将父节点设为黑色；（3）将兄弟节点的左子节点设为黑色；（4）以父节点为支点进行右旋；（5）将root作为新的当前节点（退出循环）；

```

 private void fixAfterDeletion(Entry<K,V> x) {
       // 只有当前节点不是根节点且当前节点是黑色时才进入循环
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) { // 如果当前节点是其父节点的左子节点;sib是当前节点的兄弟节点                                
                Entry<K,V> sib = rightOf(parentOf(x));// sib是当前节点的兄弟节点                                                                    
                if (colorOf(sib) == RED) {// 情况1）如果兄弟节点是红色                                         
                    setColor(sib, BLACK);// （1）将兄弟节点设为黑色                                         
                    setColor(parentOf(x), RED);// （2）将父节点设为红色                                               
                    rotateLeft(parentOf(x));// （3）以父节点为支点进行左旋                                            
                    sib = rightOf(parentOf(x));// （4）重新设置x的兄弟节点，进入下一步                                               
                }
                // 情况2）如果兄弟节点的两个子节点都是黑色               
                if (colorOf(leftOf(sib))  == BLACK &&colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);// （1）将兄弟节点设置为红色                                       
                    x = parentOf(x);// （2）将x的父节点作为新的当前节点，进入下一次循环                                    
                } else {
                
                    // 情况3）如果兄弟节点的右子节点为黑色                                      
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK); // （1）将兄弟节点的左子节点设为黑色
                        setColor(sib, RED);// （2）将兄弟节点设为红色                                           
                        rotateRight(sib);// （3）以兄弟节点为支点进行右旋                                         
                        sib = rightOf(parentOf(x));// （4）重新设置x的兄弟节点                                                   
                    }                   
                    // 情况4       
                    // （1）将兄弟节点的颜色设为父节点的颜色
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);// （2）将父节点设为黑色                                                
                    setColor(rightOf(sib), BLACK);// （3）将兄弟节点的右子节点设为黑色                                                  
                    rotateLeft(parentOf(x));// （4）以父节点为支点进行左旋                                            
                    x = root;// （5）将root作为新的当前节点（退出循环）                             
                }
            } else {
                // 如果当前节点是其父节点的右子节点           
                Entry<K,V> sib = leftOf(parentOf(x));// sib是当前节点的兄弟节点                                                     
                if (colorOf(sib) == RED) {// 情况1）如果兄弟节点是红色                                          
                    setColor(sib, BLACK);// （1）将兄弟节点设为黑色                                        
                    setColor(parentOf(x), RED);//// （2）将父节点设为红色
                    rotateRight(parentOf(x));// （3）以父节点为支点进行右旋                                            
                    sib = leftOf(parentOf(x));// （4）重新设置x的兄弟节点                                             
                }
                if (colorOf(rightOf(sib)) == BLACK &&
                    colorOf(leftOf(sib)) == BLACK) {// 情况2）如果兄弟节点的两个子节点都是黑色                                                    
                    setColor(sib, RED);// （1）将兄弟节点设置为红色                                       
                    x = parentOf(x);// （2）将x的父节点作为新的当前节点，进入下一次循环                                    
                } else {
                // 情况3）如果兄弟节点的左子节点为黑色                                    
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);// （1）将兄弟节点的右子节点设为黑色
                        setColor(sib, RED);// （2）将兄弟节点设为红色                                           
                        rotateLeft(sib);// （3）以兄弟节点为支点进行左旋                                        
                        sib = leftOf(parentOf(x));// （4）重新设置x的兄弟节点                                                 
                    }
                     
                  // 情况4）                
                  //（1）将兄弟节点的颜色设为父节点的颜色
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);// （2）将父节点设为黑色                                                 
                    setColor(leftOf(sib), BLACK);// （3）将兄弟节点的左子节点设为黑色                                               
                    rotateRight(parentOf(x));// （4）以父节点为支点进行右旋                                            
                    x = root;// （5）将root作为新的当前节点（退出循环）                             
                }
            }
        }

        setColor(x, BLACK);// 退出条件为多出来的黑色向上传递到了根节点或者红节点// 则将x设为黑色即可满足红黑树规则
                                                                             
    }


```

来自:[彤哥读源码](https://mp.weixin.qq.com/s?__biz=Mzg2ODA0ODM0Nw==&mid=2247483734&idx=4&sn=d2a8b4ae9d80d6381986de44338c1776&scene=21#wechat_redirect)










































