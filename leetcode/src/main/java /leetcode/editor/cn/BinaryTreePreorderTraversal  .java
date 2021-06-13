//给你二叉树的根节点 root ，返回它节点值的 前序 遍历。 
//
// 
//
// 示例 1： 
//
// 
//输入：root = [1,null,2,3]
//输出：[1,2,3]
// 
//
// 示例 2： 
//
// 
//输入：root = []
//输出：[]
// 
//
// 示例 3： 
//
// 
//输入：root = [1]
//输出：[1]
// 
//
// 示例 4： 
//
// 
//输入：root = [1,2]
//输出：[1,2]
// 
//
// 示例 5： 
//
// 
//输入：root = [1,null,2]
//输出：[1,2]
// 
//
// 
//
// 提示： 
//
// 
// 树中节点数目在范围 [0, 100] 内 
// -100 <= Node.val <= 100 
// 
//
// 
//
// 进阶：递归算法很简单，你可以通过迭代算法完成吗？ 
// Related Topics 栈 树 
// 👍 578 👎 0


//leetcode submit region begin(Prohibit modification and deletion)

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Definition for a binary tree node.
 * public class TreeNode {
 * int val;
 * TreeNode left;
 * TreeNode right;
 * TreeNode() {}
 * TreeNode(int val) { this.val = val; }
 * TreeNode(int val, TreeNode left, TreeNode right) {
 * this.val = val;
 * this.left = left;
 * this.right = right;
 * }
 * }
 */
class Solution {
    //二叉树遍历
    List<Integer> res = new ArrayList<>();

    //前序遍历：首先访问根结点，然后遍历左子树，最后遍历右子树（根->左->右）
    //中序遍历：首先遍历左子树，然后访问根节点，最后遍历右子树（左->根->右）
    //后序遍历：首先遍历左子树，然后遍历右子树，最后访问根节点（左->右->根）
    public List<Integer> preorderTraversal(TreeNode root) {
        dfs2(root);
        return res;
    }

    //递归实现
    private void dfs(TreeNode root) {
        if (root == null) {
            return;
        }
        res.add(root.val);
        dfs(root.left);
        dfs(root.right);
    }

    private List<Integer> dfs2(TreeNode root) {
        if (root == null) {
            return res;
        }
        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            //相对跟节点
            TreeNode node = stack.pop();
            if (node != null) {
                if (node.right != null) {//先放右节点，后弹出
                    stack.push(node.right);
                }
                if (node.left != null) {//再放左节点，先弹出
                    stack.push(node.left);
                }
                stack.push(node);//把相对跟节点放回去
                stack.push(null);//放一个空节点，第二次过来的时候跳到else路径，再就是left,最后right
            } else {
                res.add(stack.pop().val);
            }
        }
        return res;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
