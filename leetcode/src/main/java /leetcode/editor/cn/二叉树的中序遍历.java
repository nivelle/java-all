//给定一个二叉树的根节点 root ，返回它的 中序 遍历。 
//
// 
//
// 示例 1： 
//
// 
//输入：root = [1,null,2,3]
//输出：[1,3,2]
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
//输出：[2,1]
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
// 进阶: 递归算法很简单，你可以通过迭代算法完成吗？ 
// Related Topics 栈 树 哈希表 
// 👍 996 👎 0


//leetcode submit region begin(Prohibit modification and deletion)

import leetcode.editor.cn.base.TreeNode;

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
class 二叉树的中序遍历 {
    public List<Integer> inorderTraversal(TreeNode root) {
        inorderTraversal1(root);

        return inorderTraversal2(root);
    }
    List<Integer> res = new ArrayList<Integer>();
    //递归方式实现
    private List<Integer> inorderTraversal1(TreeNode root) {
        if (root == null) {
            return res;
        }
        dfs(root);
        return res;
    }

    //递归遍历二叉树
    private void dfs(TreeNode root) {
        if (root == null) {
            return;
        }
        dfs(root.left);
        res.add(root.val);
        dfs(root.right);
    }

    //迭代实现
    private List<Integer> inorderTraversal2(TreeNode root) {
        if (root == null) {
            return res;
        }
        Stack<TreeNode> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            //根节点弹出
            TreeNode node = stack.pop();
            if (node != null) {
                if (node.right != null) {
                    //右节点压入
                    stack.push(node.right);
                }
                //当前根节点压入
                stack.push(node);
                //标记 null的下一个为left
                stack.push(null);
                //最后放left左节点，则先弹出左节点,实现中序遍历
                if (node.left != null) {
                    stack.push(node.left);
                }
            } else {
                //这里是在: stack.pop()判断为空后 又取的一个值
                TreeNode saveNode = stack.pop();
                res.add(saveNode.val);
            }
        }
        return res;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
