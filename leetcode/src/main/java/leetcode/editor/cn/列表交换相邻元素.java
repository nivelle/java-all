package leetcode.editor.cn;//给定一个链表，两两交换其中相邻的节点，并返回交换后的链表。
//
// 你不能只是单纯的改变节点内部的值，而是需要实际的进行节点交换。 
//
// 
//
// 示例 1： 
//
// 
//输入：head = [1,2,3,4]
//输出：[2,1,4,3]
// 
//
// 示例 2： 
//
// 
//输入：head = []
//输出：[]
// 
//
// 示例 3： 
//
// 
//输入：head = [1]
//输出：[1]
// 
//
// 
//
// 提示： 
//
// 
// 链表中节点的数目在范围 [0, 100] 内 
// 0 <= Node.val <= 100 
// 
//
// 
//
// 进阶：你能在不修改链表节点值的情况下解决这个问题吗?（也就是说，仅修改节点本身。） 
// Related Topics 递归 链表 
// 👍 952 👎 0


//leetcode submit region begin(Prohibit modification and deletion)

import leetcode.editor.cn.base.ListNode;

/**
 * Definition for singly-linked list.
 * public class ListNode {
 * int val;
 * ListNode next;
 * ListNode() {}
 * ListNode(int val) { this.val = val; }
 * ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class 列表交换相邻元素 {
    //迭代解法
    public ListNode swapPairs1(ListNode head) {
        // 已有的链表加一个头部 head node
        ListNode tmp = new ListNode();
        tmp.next = head;

        // curNode 遍历链表时用
        ListNode cur = tmp;

        // 开始遍历链表
        while (cur != null && cur.next != null && cur.next.next != null) {
            ListNode f = cur;
            ListNode s = cur.next;
            ListNode t = s.next;
            // 两两交换链表结点
            f.next = t;
            s.next = t.next;
            t.next = s;
            // 标杆位后移2位
            cur = cur.next.next;
        }
        return tmp.next;
    }

    //递归解法
    public ListNode swapPairs2(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode newHead = head.next;
        head.next = swapPairs2(newHead.next);
        newHead.next = head;
        return newHead;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
