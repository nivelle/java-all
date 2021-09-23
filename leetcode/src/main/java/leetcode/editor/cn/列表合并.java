package leetcode.editor.cn;//输入两个递增排序的链表，合并这两个链表并使新链表中的节点仍然是递增排序的。
//
// 示例1： 
//
// 输入：1->2->4, 1->3->4
//输出：1->1->2->3->4->4 
//
// 限制： 
//
// 0 <= 链表长度 <= 1000 
//
// 注意：本题与主站 21 题相同：https://leetcode-cn.com/problems/merge-two-sorted-lists/ 
// Related Topics 分治算法 
// 👍 122 👎 0


//leetcode submit region begin(Prohibit modification and deletion)

/**
 * Definition for singly-linked list.
 * public class ListNode {
 * int val;
 * ListNode next;
 * ListNode(int x) { val = x; }
 * }
 */

import leetcode.editor.cn.base.ListNode;

/**
 *  合并两个有序链表
 */
class 列表合并 {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        //当前指针
        ListNode cur = new ListNode(0);
        //列表长度一样的情况
        while (l1 != null && l2 != null) {
            if (l1.val < l2.val) {
                //满足条件的节点 加入到当前节点
                cur.next = l1;
                //满足条件的节点后移
                l1 = l1.next;
            } else {
                cur.next = l2;
                l2 = l2.next;
            }
            //cur 后移
            cur = cur.next;
        }
        //链表长度不一样单表连接
        if (l1 != null) {
            cur.next = l1;
        } else {
            cur.next = l2;
        }
        return cur.next;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
