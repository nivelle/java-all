//请判断一个链表是否为回文链表。 
//
// 示例 1: 
//
// 输入: 1->2
//输出: false 
//
// 示例 2: 
//  
// 输入: 1->2->2->1
//输出: true
// 
//
// 进阶： 
//你能否用 O(n) 时间复杂度和 O(1) 空间复杂度解决此题？ 
// Related Topics 链表 双指针 
// 👍 914 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
import leetcode.editor.cn.base.ListNode;

import java.util.ArrayList;
import java.util.List;

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
        public boolean isPalindrome(ListNode head) {
        List<Integer> vals = new ArrayList<Integer>();
        ListNode currentNode = head;
        //依次将元素放入列表
        while (currentNode != null) {
            vals.add(currentNode.val);
            currentNode = currentNode.next;
        }
        //头节点
        int front = 0;
        //尾节点
        int back = vals.size() - 1;
        while (front < back) {
            //如果对层节点不相等，则
            if (!vals.get(front).equals(vals.get(back))) {
                return false;
            }
            front++;
            back--;
        }
        return true;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
