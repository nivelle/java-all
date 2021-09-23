//给定一个不含重复数字的数组 nums ，返回其 所有可能的全排列 。你可以 按任意顺序 返回答案。 
//
// 
//
// 示例 1： 
//
// 
//输入：nums = [1,2,3]
//输出：[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]
// 
//
// 示例 2： 
//
// 
//输入：nums = [0,1]
//输出：[[0,1],[1,0]]
// 
//
// 示例 3： 
//
// 
//输入：nums = [1]
//输出：[[1]]
// 
//
// 
//
// 提示： 
//
// 
// 1 <= nums.length <= 6 
// -10 <= nums[i] <= 10 
// nums 中的所有整数 互不相同 
// 
// Related Topics 数组 回溯 
// 👍 1425 👎 0


import java.util.ArrayList;
import java.util.List;

//leetcode submit region begin(Prohibit modification and deletion)
class 数组元素全排列 {
    List<List<Integer>> res = new ArrayList<>();

    public List<List<Integer>> permute(int[] nums) {

        if (nums.length == 0 || nums == null) return res;

        boolean[] used = new boolean[nums.length];
        //抽象成树形结构
        dfs(nums, used, new ArrayList<>());

        return res;


    }

    private void dfs(int[] nums, boolean[] used, ArrayList<Integer> path) {
        //递归中止条件
        if (path.size() == nums.length) {
            //java path是引用传递，因此要 new ArrayList() 否则 会是全部为空
            res.add(new ArrayList<>(path));
            //res.add(path);
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            //排除掉上一层选择过的数
            if (used[i]) {
                continue;
            }
            //放入候选集合
            path.add(nums[i]);
            //标记已经使用过了
            used[i] = true;

            //下一层树
            dfs(nums, used, path);
            //回到上一层节点的过程中需要状态重置
            path.remove(path.size() - 1);
            used[i] = false;
        }
    }
}
//leetcode submit region end(Prohibit modification and deletion)
