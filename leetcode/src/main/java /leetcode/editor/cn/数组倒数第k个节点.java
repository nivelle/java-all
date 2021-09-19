//输入一个链表，输出该链表中倒数第k个节点。为了符合大多数人的习惯，
//本题从1开始计数，即链表的尾节点是倒数第1个节点。
//
// 例如，一个链表有 6 个节点，从头节点开始，它们的值依次是 1、2、3、4、5、6。这个链表的倒数第 3 个节点是值为 4 的节点。 
//
// 
//
// 示例： 
//
// 
//给定一个链表: 1->2->3->4->5, 和 k = 2.
//
//返回链表 4->5. 
// Related Topics 链表 双指针 
// 👍 203 👎 0


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
class 数组倒数第k个节点 {
    public ListNode getKthFromEnd(ListNode head, int k) {
        if (head == null) {
            return null;
        }

        ListNode fast = head;
        ListNode slow = head;

        for (int i = 0; i < k; i++) {
            //fast先走到 正数第k个位置
            fast = fast.next;
        }
        //fast和slow一起再往前走，fast走到尾的时候，slow正好到达倒数k,比 slow快k个位移
        while (fast != null) {
            fast = fast.next;
            slow = slow.next;
        }
        return slow;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
