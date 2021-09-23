java集合中，list列表应该是我们最常使用的，它有两种常见的实现类：ArrayList和LinkedList。ArrayList底层是数组，查找比较方便；LinkedList底层是链表，更适合做新增和删除。但实际开发中，我们也会遇到使用ArrayList需要删除列表元素的时候。虽然ArrayList类已经提供了remove方法，不过其中有潜在的坑，下面将介绍remove方法的三种错误用法以及六种正确用法。

## 错误用法

### 错误用法一: for循环中使用remove(int index)，列表从前往后遍历

- 首先看一下ArrayList.remove(int index)的源码，读代码前先看方法注释：移除列表指定位置的一个元素，将该元素后面的元素们往左移动一位。返回被移除的元素。

  - 源代码也比较好理解，ArrayList底层是数组，size是数组长度大小，index是数组索引坐标，modCount是被修改次数的计数器，oldValue就是被移除索引的元素对象，numMoved是需要移动的元素数量，如果numMoved大于0，则执行一个数组拷贝（实质是被移除元素后面的元素都向前移动一位）。然后数组长度size减少1，列表最后一位元素置为空。最后将被移除的元素对象返回。

````java
    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        rangeCheck(index);
 
        modCount++;
        E oldValue = elementData(index);
 
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
 
        return oldValue;
    }
    
````

- 如果在for循环中调用了多次ArrayList.remove()，那代码执行结果是不准确的，因为每次每次调用remove函数，ArrayList列表都会改变数组长度，被移除元素后面的元素位置都会发生变化。比如下面这个例子，本来是想把列表中奇数位置的元素都移除，但最终得到的结果是[2,3,5]。

````java
        List<Long> list = new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        for (int i = 0; i < list.size(); i++) {
            if (i % 2 == 0) {
                list.remove(i);
            }
        }
        //最终得到[2,3,5]
 ````      
### 错误用法二:直接使用list.remove(Object o)

- ArrayList.remove(Object o)源码的逻辑和ArrayList.remove(int index)大致相同：列表索引坐标从小到大循环遍历，若列表中存在与入参对象相等的元素，则把该元素移除，后面的元素都往左移动一位，返回true，若不存在与入参相等的元素，返回false。
````java
    public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }
 
    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }
    
````
- 如果直接对list调用了该方法，代码结果可能会不准确。例子如下：这段代码本想移除列表中全部值为2的元素，结果并没有成功。

````java

List<Long> list = new ArrayList<>(Arrays.asList(1L, 2L, 2L, 4L, 5L));
list.remove(2L);//最终得到[1,2,4,5]
        
````   

### 错误用法三:Arrays.asList()之后使用remove()

- 为啥使用了Arrays.asList()之后使用remove是错误用法，我们看一下asList()的源码就能知道了。Arrays.asList()返回的是一个指定数组长度的列表，所以不能做Add、Remove等操作。至于为啥是返回的是固定长度的，看下面源码，asList()函数中调用的new ArrayList<>()并不是我们常用的ArrayList类，而是一个Arrays的内部类，也叫ArrayList，而且这个内部类也是基于数组实现的，但它有一个明显的关键字修饰，那就是final。都用final修饰了，那是肯定不能再对它进行add/remove操作的。如果非要在Arrays.asList之后使用remove，正确用法参见2.5。

````java
    public static <T> List<T> asList(T... a) {
        return new ArrayList<>(a);
    }
 
    private static class ArrayList<E> extends AbstractList<E>
        implements RandomAccess, java.io.Serializable
     {
        private static final long serialVersionUID = -2764017481108945198L;
        private final E[] a;
 
        ArrayList(E[] array) {
            a = Objects.requireNonNull(array);
        }
    }

````

--- 
## 正确用法

### 正确用法一： 直接使用removeIf()

- 使用removeIf()这个方法前，我是有点害怕的，毕竟前面两个remove方法都不能直接使用。于是小心翼翼的看了removeIf函数的方法。确认过源码，是我想要的方法！

- 源码如下：removeIf()的入参是一个过滤条件，用来判断需要移除的元素是否满足条件。方法中设置了一个removeSet，把满足条件的元素索引坐标都放入removeSet，然后统一对removeSet中的索引进行移除。源码相对复杂的是BitSet模型，源码这里不再贴了。

````java
 public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // figure out which elements are to be removed
        // any exception thrown from the filter predicate at this stage
        // will leave the collection unmodified
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i=0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // shift surviving elements left over the spaces left by removed elements
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i=0, j=0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k=newSize; k < size; k++) {
                elementData[k] = null;  // Let gc do its work
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }
````
- removeIf()的使用方法如下所示（jdk8），结果满足预期。
````java
    List<Long> list = new ArrayList<>(Arrays.asList(1L, 2L, 2L, 4L, 5L));
    list.removeIf(val -> val == 2L);
    //结果得到[1L,4L,5L]
    
````
### 正确用法二：在for循环之后使用removeAll(Collection<?> c)

- 这种方法思路是for循环内使用一个集合存放所有满足移除条件的元素，for循环结束后直接使用removeAll方法进行移除。removeAll源码如下，还是比较好理解的：定义了两个数组指针r和w，初始都指向列表第一个元素。循环遍历列表，r指向当前元素，若当前元素没有满足移除条件，将数组[r]元素赋值给数组[w]，w指针向后移动一位。这样就完成了整个数组中，没有被移除的元素向前移动。遍历完列表后，将w后面的元素都置空，并减少数组长度。至此完成removeAll移除操作。

````java
 private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            if (r != size) {
                System.arraycopy(elementData, r,
                                 elementData, w,
                                 size - r);
                w += size - r;
            }
            if (w != size) {
                // clear to let GC do its work
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }
    
````
### 正确用法三：list转为迭代器Iterator的方式

- 迭代器就是一个链表，直接使用remove操作不会出现问题。

````java
Iterator<Integer> it = list.iterator();
while (it.hasNext()) {
if (it.next() % 2 == 0)
it.remove();
}

````
### 正确用法四：for循环中使用remove(int index), 列表从后往前遍历

- 前面1.1也是for循环，为啥从后往前遍历就是正确的呢。因为每次调用remove(int index)，index后面的元素会往前移动，如果是从后往前遍历，index后面的元素发生移动，跟index前面的元素无关，我们循环只去和前面的元素做判断，因此就没有影响。
````java
for (int i = list.size() - 1; i >= 0; i--) {
if (list.get(i).longValue() == 2) {
list.remove(i);
}
}

````
### 正确用法五：Arrays.asList()之后使用remove()

- Arrays.asList()之后需要进行add/remove操作，可以使用下面这种方式：

````java
String[] arr = new String[3];
List list = new ArrayList(Arrays.asList(arr));

````
### 正确用法六：使用while循环

- 使用while循环，删除了元素，索引便不+1，在没删除元素时索引+1

``````java
int i=0;
while (i<list.size()) {
if (i % 2 == 0) {
list.remove(i);
}else {
i++;
}
``````
