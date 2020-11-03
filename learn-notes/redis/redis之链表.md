

链表提供了高效的节点重排能力,以及顺序性的节点访问方式,并且可以通过增删节点来灵活地调整链表的长度.

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

使用adlist.h/list来持有链表,会使用操作更方便:

```
typedef struct list{
    //表头节点
    ListNode * head;
    //表尾节点
    ListNode * tail;
    //链表所包含的节点数量
    unsigned long len;
    //节点值复制函数
    void *(*dup)(void *ptr);
    //节点值释放函数
    void (*free)(void *ptr);
    //节点值对比函数
    int (*match)(void *ptr,void *key)
}list;

```

![image](https://ws1.sinaimg.cn/large/b1eb59d9ly1fwxnc6u8q0j20hq06v0tq.jpg)

redis链表结构特性如下:

- 双端:链表节点带有prev和next指针,获取某个节点的前置和后置节点复杂度都是O(1)
- 无环:表头节点的prev指针和表尾节点的next指针都指向NULL,对链表的访问以NULL为终点
- 带表头指针和表尾指针:通过list结构的head指针和tail指针,程序获取链表的表头节点和表尾节点的复杂度O(1)
- 带链表长度计数器:程序使用list结构的len属性来对list持有的链表节点进行计数,程序获取链表中节点数量的复杂度为O(1)
- 多态:链表节点使用void*指针来保存节点值,并且可以通过list结构的dup\free\match三个属性为节点值设置类型特定函数,所以链表可以用于保存各种不同类型的值.

函数 | 作用 | 时间复杂度
---|--- |---
listSetDupMethod |	将给定的函数设置为链表的节点值复制函数。|	O(1) 。
listGetDupMethod |	返回链表当前正在使用的节点值复制函数。	复制函数可以通过链表的 dup 属性直接获得， |O(1)
listSetFreeMethod	|将给定的函数设置为链表的节点值释放函数。|	O(1) 。
listGetFree	|返回链表当前正在使用的节点值释放函数。	释放函数可以通过链表的 free 属性直接获得，| O(1)
listSetMatchMethod	|将给定的函数设置为链表的节点值对比函数。|	O(1)
listGetMatchMethod	|返回链表当前正在使用的节点值对比函数。	对比函数可以通过链表的 match 属性直接获得， |O(1)
listLength|	返回链表的长度（包含了多少个节点）。	链表长度可以通过链表的 len 属性直接获得， |O(1) 。
listFirst|	返回链表的表头节点。	表头节点可以通过链表的 head 属性直接获得，| O(1) 。
listLast|	返回链表的表尾节点。	表尾节点可以通过链表的 tail 属性直接获得，| O(1) 。
listPrevNode|	返回给定节点的前置节点。	前置节点可以通过节点的 prev 属性直接获得， |O(1) 。
listNextNode|	返回给定节点的后置节点。	后置节点可以通过节点的 next 属性直接获得，| O(1) 。
listNodeValue|	返回给定节点目前正在保存的值。	节点值可以通过节点的 value 属性直接获得， |O(1) 。
listCreate	|创建一个不包含任何节点的新链表。	|O(1)
listAddNodeHead	|将一个包含给定值的新节点添加到给定链表的表头。|	O(1)
listAddNodeTail	|将一个包含给定值的新节点添加到给定链表的表尾。|	O(1)
listInsertNode	|将一个包含给定值的新节点添加到给定节点的之前或者之后。|	O(1)
listSearchKey	|查找并返回链表中包含给定值的节点。|	O(N) ， N 为链表长度。
listIndex|	返回链表在给定索引上的节点。	|O(N) ， N 为链表长度。
listDelNode	|从链表中删除给定节点。|	O(1) 。
listRotate	|将链表的表尾节点弹出，然后将被弹出的节点插入到链表的表头， 成为新的表头节点。|	O(1)
listDup	|复制一个给定链表的副本。|	O(N) ， N 为链表长度。
listRelease	|释放给定链表，以及链表中的所有节点。	|O(N) ， N 为链表长度。
