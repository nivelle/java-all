//给定单向链表的头指针和一个要删除的节点的值，定义一个函数删除该节点。 
//
// 返回删除后的链表的头节点。 
//
// 注意：此题对比原题有改动 
//
// 示例 1: 
//
// 输入: head = [4,5,1,9], val = 5
//输出: [4,1,9]
//解释: 给定你链表中值为 5 的第二个节点，那么在调用了你的函数之后，该链表应变为 4 -> 1 -> 9.
// 
//
// 示例 2: 
//
// 输入: head = [4,5,1,9], val = 1
//输出: [4,5,9]
//解释: 给定你链表中值为 1 的第三个节点，那么在调用了你的函数之后，该链表应变为 4 -> 5 -> 9.
// 
//
// 
//
// 说明： 
//
// 
// 题目保证链表中节点的值互不相同 
// 若使用 C 或 C++ 语言，你不需要 free 或 delete 被删除的节点 
// 
// Related Topics 链表 
// 👍 141 👎 0


//leetcode submit region begin(Prohibit modification and deletion)

import leetcode.editor.cn.base.ListNode;

/**
 * Definition for singly-linked list.
 * public class ListNode {
 * int val;
 * ListNode next;
 * ListNode(int x) { val = x; }
 * }
 */
class 删除链表指定节点值 {
    public ListNode deleteNode(ListNode head, int val) {
        if (head == null) {
            return null;
        }
        //构建一个临时指针,指向的是头节点,暂存起来，删除目标值后可以通过dummy 返回指定的头
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode cur = dummy;
        //节点值不相等，则后移
        while (cur.next.val != val) {
            cur = cur.next;
        }
        //若相等，则指向下下个节点
        cur.next = cur.next.next;
        return dummy.next;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
