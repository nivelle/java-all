
#### 链表提供了高效的节点重排能力,以及顺序性的节点访问方式,并且可以通过增删节点来灵活地调整链表的长度.

##### 链表在redis中的应用：

- 当一个列表键包含了数量比较多的元素，又或者列表中包含的元素都是比较长的字符串时，redis就会使用链表键的底层实现

- 发布与订阅，慢查询，监视器

- 使用链表来构建客户端输出缓冲区

##### 链表节点

```
typedef struct listNode{
    //前置节点
    struct listNode * prev;
    //后置节点
    struct listNode * next;
    //节点的值
    voi * value;
}listNode;

```

#####list来持有链表,会使用操作更方便:

```
typedef struct list{
    //表头节点
    ListNode * head;
    //表尾节点
    ListNode * tail;
    //链表所包含的节点数量
    unsigned long len;
    //节点值复制函数，用于复制链表节点所保存的值
    void *(*dup)(void *ptr);
    //节点值释放函数，用于释放链表节点所保存的值
    void (*free)(void *ptr);
    //节点值对比函数，用于对比链表节点所保存的值和另一个输入值是否相等
    int (*match)(void *ptr,void *key)
}list;

```

[![list](https://s3.ax1x.com/2020/11/12/DSAi4K.png)](https://imgchr.com/i/DSAi4K)

#### redis链表结构特性如下:

- 双端:链表节点带有prev和next指针,获取某个节点的前置和后置节点复杂度都是O(1)

- 无环:表头节点的prev指针和表尾节点的next指针都指向NULL,对链表的访问以NULL为终点

- 带表头指针和表尾指针:通过list结构的head指针和tail指针,程序获取链表的表头节点和表尾节点的复杂度O(1)

- 带链表长度计数器:程序使用list结构的len属性来对list持有的链表节点进行计数,程序获取链表中节点数量的复杂度为O(1)

- 多态:链表节点使用void*指针来保存节点值,并且可以通过list结构的dup\free\match三个属性为节点值设置类型特定函数,所以链表可以用于保存各种不同类型的值.


