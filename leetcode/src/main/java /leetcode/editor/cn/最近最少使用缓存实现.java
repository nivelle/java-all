package leetcode.editor.cn;//运用你所掌握的数据结构，设计和实现一个 LRU (最近最少使用) 缓存机制 。
//
// 
// 
// 实现 LRUCache 类： 
//
// 
// LRUCache(int capacity) 以正整数作为容量 capacity 初始化 LRU 缓存 
// int get(int key) 如果关键字 key 存在于缓存中，则返回关键字的值，否则返回 -1 。 
// void put(int key, int value) 如果关键字已经存在，则变更其数据值；如果关键字不存在，则插入该组「关键字-值」。当缓存容量达到上
//限时，它应该在写入新数据之前删除最久未使用的数据值，从而为新的数据值留出空间。 
// 
//
// 
// 
// 
//
// 进阶：你是否可以在 O(1) 时间复杂度内完成这两种操作？ 
//
// 
//
// 示例： 
//
// 
//输入
//["LRUCache", "put", "put", "get", "put", "get", "put", "get", "get", "get"]
//[[2], [1, 1], [2, 2], [1], [3, 3], [2], [4, 4], [1], [3], [4]]
//输出
//[null, null, null, 1, null, -1, null, -1, 3, 4]
//
//解释
//LRUCache lRUCache = new LRUCache(2);
//lRUCache.put(1, 1); // 缓存是 {1=1}
//lRUCache.put(2, 2); // 缓存是 {1=1, 2=2}
//lRUCache.get(1);    // 返回 1
//lRUCache.put(3, 3); // 该操作会使得关键字 2 作废，缓存是 {1=1, 3=3}
//lRUCache.get(2);    // 返回 -1 (未找到)
//lRUCache.put(4, 4); // 该操作会使得关键字 1 作废，缓存是 {4=4, 3=3}
//lRUCache.get(1);    // 返回 -1 (未找到)
//lRUCache.get(3);    // 返回 3
//lRUCache.get(4);    // 返回 4
// 
//
// 
//
// 提示： 
//
// 
// 1 <= capacity <= 3000 
// 0 <= key <= 10000 
// 0 <= value <= 105 
// 最多调用 2 * 105 次 get 和 put 
// 
// Related Topics 设计 哈希表 链表 双向链表 
// 👍 1631 👎 0


import java.util.HashMap;

//leetcode submit region begin(Prohibit modification and deletion)
public class 最近最少使用缓存实现 {
    HashMap<Integer, Node> map;
    DoubleLinkedList cache;
    //容量
    int cap;

    public 最近最少使用缓存实现(int capacity) {
        map = new HashMap<>();
        //双向列表
        cache = new DoubleLinkedList();
        cap = capacity;
    }

    //添加方法
    public void put(int key, int val) {
        Node newNode = new Node(key, val);

        if (map.containsKey(key)) {
            //从双向列表删除
            cache.delete(map.get(key));
            //然后再加到头部
            cache.addFirst(newNode);
            //添加到hashMap
            map.put(key, newNode);
        } else {
            //如果元素已经满了
            if (map.size() == cap) {
                //删除列表最老的元素
                int k = cache.deleteLast();
                //删除hashMap里面的元素
                map.remove(k);
            }
            //如果没满，则添加到列表和hashMap
            cache.addFirst(newNode);
            map.put(key, newNode);

        }
    }

    public int get(int key) {
        if (!map.containsKey(key)) {
            return -1;
        }

        int val = map.get(key).val;
        //如果存在，使用过之后,则再添加一次
        put(key, val);

        return val;
    }
}

/**
 * head: recently used
 * tail: LRU
 * 双向列表
 */
class DoubleLinkedList {
    Node head;
    Node tail;

    public DoubleLinkedList() {
        head = new Node(0, 0);
        tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    //头插法：双向列表，添加元素：涉及新加入链表节点的前后指向，和 原来头节点和头节点下一个节点的指向
    public void addFirst(Node node) {
        //新节点放在head 之后,正向链接
        node.next = head.next;
        node.prev = head;
        //逆向链接
        head.next.prev = node;
        //指定新的头节点
        head.next = node;
    }

    //双向列表，删除元素
    public int delete(Node n) {
        int key = n.key;
        n.next.prev = n.prev;
        n.prev.next = n.next;

        return key;
    }

    //删除最老元素
    public int deleteLast() {
        if (head.next == tail) {
            return -1;
        }
        //删除尾节点的上一个节点，头节点和尾节点都是虚拟节点
        return delete(tail.prev);
    }
}

class Node {
    public int key;
    public int val;
    public Node prev;
    public Node next;

    public Node(int key, int val) {
        this.key = key;
        this.val = val;
    }
}


/**
 * Your LRUCache object will be instantiated and called as such:
 * LRUCache obj = new LRUCache(capacity);
 * int param_1 = obj.get(key);
 * obj.put(key,value);
 */
//leetcode submit region end(Prohibit modification and deletion)
