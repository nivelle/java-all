//存在一个按升序排列的链表，给你这个链表的头节点 head ，请你删除所有重复的元素，使每个元素 只出现一次 。 
//
// 返回同样按升序排列的结果链表。 
//
// 
//
// 示例 1： 
//
// 
//输入：head = [1,1,2]
//输出：[1,2]
// 
//
// 示例 2： 
//
// 
//输入：head = [1,1,2,3,3]
//输出：[1,2,3]
// 
//
// 
//
// 提示： 
//
// 
// 链表中节点数目在范围 [0, 300] 内 
// -100 <= Node.val <= 100 
// 题目数据保证链表已经按升序排列 
// 
// Related Topics 链表 
// 👍 597 👎 0


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
class Solution {
    public ListNode deleteDuplicates(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        //cur 首先指向头节点
        ListNode cur = head;
        while (cur.next != null) {
            //如果当前节点的值等于下个节点的值，则当前节点的下一个节点指向下下个节点
            if (cur.val == cur.next.val) {
                cur.next = cur.next.next;
            } else {
                //否则当前节点后移
                cur = cur.next;
            }
        }
        //返回原头节点
        return head;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
