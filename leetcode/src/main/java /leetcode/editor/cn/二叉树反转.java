//定义一个函数，输入一个链表的头节点，反转该链表并输出反转后链表的头节点。 
//
// 
//
// 示例: 
//
// 输入: 1->2->3->4->5->NULL
//输出: 5->4->3->2->1->NULL 
//
// 
//
// 限制： 
//
// 0 <= 节点个数 <= 5000 
//
// 
//
// 注意：本题与主站 206 题相同：https://leetcode-cn.com/problems/reverse-linked-list/ 
// Related Topics 链表 
// 👍 243 👎 0


//leetcode submit region begin(Prohibit modification and deletion)

/**
 * Definition for singly-linked list.
 * public class ListNode {
 * int val;
 * ListNode next;
 * ListNode(int x) { val = x; }
 * }
 */

/**
 * 反转链表
 */
class Solution {
    public ListNode reverseList(ListNode head) {

        if (head == null) {
            return null;
        }
        ListNode cur = head;
        //新列表 的表头
        ListNode pre = null;
        while (cur != null) {
            //临时指针，暂存下一个节点
            ListNode tmp = cur.next;
            //交换指针
            cur.next = pre;
            //交换位置
            pre = cur;
            //后移
            cur = tmp;
        }
        return pre;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
