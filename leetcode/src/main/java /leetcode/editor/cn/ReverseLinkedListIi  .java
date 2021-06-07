//给你单链表的头指针 head 和两个整数 left 和 right ，其中 left <= right 。请你反转从位置 left 到位置 right 的链
//表节点，返回 反转后的链表 。
// 
//
// 示例 1： 
//
// 
//输入：head = [1,2,3,4,5], left = 2, right = 4
//输出：[1,4,3,2,5]
// 
//
// 示例 2： 
//
// 
//输入：head = [5], left = 1, right = 1
//输出：[5]
// 
//
// 
//
// 提示： 
//
// 
// 链表中节点数目为 n 
// 1 <= n <= 500 
// -500 <= Node.val <= 500 
// 1 <= left <= right <= n 
// 
//
// 
//
// 进阶： 你可以使用一趟扫描完成反转吗？ 
// Related Topics 链表 
// 👍 916 👎 0


//leetcode submit region begin(Prohibit modification and deletion)

/**
 * /*
 * <p>
 * 反转部分列表
 */

class Solution {
    public ListNode reverseBetween(ListNode head, int m, int n) {
        //构建两个指针
        ListNode dummy = new ListNode(0);
        dummy.next = head;
        ListNode p1 = dummy;
        ListNode p2 = head;

        for (int i = 0; i < m - 1; i++) {
            p1 = p1.next;
            p2 = p2.next;
        }
        //用于扫描的指针
        ListNode cur = null;
        for (int i = 0; i < n - m; i++) {
            //保存后继节点，零时节点
            cur = p2.next;
            //跨过下一个节点，指向下下个节点
            p2.next = p2.next.next;
            //p1节点反转
            cur.next = p1.next;
            p1.next = cur;
        }

        return dummy.next;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
