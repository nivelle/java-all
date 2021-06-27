//给定一个链表，两两交换其中相邻的节点，并返回交换后的链表。 
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
class Solution {
    public ListNode swapPairs(ListNode head) {

        ListNode dummy = new ListNode(0);
        ListNode l1 = dummy;

        dummy.next = head;
        ListNode l2 = head;

        while (l2 != null && l2.next != null) {
            ListNode start = l2.next;
            ListNode nextStart = l2.next.next;
            //新列表头节点下一个节点 为 老链表头节点下一个节点
            l1.next = start;
            //老节点 头节点后移
            start.next = l2;
            //老节点头节点下一个节点改为 原来 下下个节点
            l2.next = nextStart;
            //新链表虚拟节点指向老链表的新头节点
            l1 = l2;
            //老链表头节点后移¬
            l2 = l2.next;
        }
        return dummy.next;

    }
}
//leetcode submit region end(Prohibit modification and deletion)
