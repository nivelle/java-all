//给定一棵二叉树，想象自己站在它的右侧，按照从顶部到底部的顺序，返回从右侧所能看到的节点值。 
//
// 示例: 
//
// 输入: [1,2,3,null,5,null,4]
//输出: [1, 3, 4]
//解释:
//
//   1            <---
// /   \
//2     3         <---
// \     \
//  5     4       <---
// 
// Related Topics 树 深度优先搜索 广度优先搜索 递归 队列 
// 👍 480 👎 0


//leetcode submit region begin(Prohibit modification and deletion)

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
    public List<Integer> rightSideView(TreeNode root) {
        // DFS（Deep First Search）深度优先搜索。

        //BFS（Breath First Search）广度优先搜索。
        // return bfsSearch(root);
        return dfsSearch(root);
    }

    //广度优先遍历
    private List<Integer> bfsSearch(TreeNode root) {
        List<Integer> res = new ArrayList<Integer>();
        if (root == null) {
            return res;
        }
        LinkedList<TreeNode> queue = new LinkedList<TreeNode>();
        queue.add(root);
        while (!queue.isEmpty()) {
            int len = queue.size();
            for (int i = 0; i < len; i++) {
                TreeNode treeNode = queue.poll();
                if (treeNode.left != null) {
                    queue.add(treeNode.left);
                }
                if (treeNode.right != null) {
                    queue.add(treeNode.right);
                }
                //最后一个节点正好是右节点
                if (i == len - 1) {
                    //将这一层节点从左到右加入队列，将这一层的最后一个节点放入res
                    res.add(treeNode.val);
                }
            }
        }
        return res;

    }

    List<Integer> res2 = new ArrayList<Integer>();

    //深度优先遍历
    private List<Integer> dfsSearch(TreeNode root) {
        if (root == null) {
            return res2;
        }
        int level = 0;
        dfs(root, level);
        return res2;
    }

    private void dfs(TreeNode root, int level) {
        if (root == null) return;

        if (level == res2.size()) {
            res2.add(root.val);
        }
        level++;
        //先遍历右子树
        dfs(root.right, level);
        dfs(root.left, level);
    }
}
//leetcode submit region end(Prohibit modification and deletion)
