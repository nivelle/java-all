//给定一个无重复元素的数组 candidates 和一个目标数 target ，找出 candidates 中所有可以使数字和为 target 的组合。 
//
// candidates 中的数字可以无限制重复被选取。 
//
// 说明： 
//
// 
// 所有数字（包括 target）都是正整数。 
// 解集不能包含重复的组合。 
// 
//
// 示例 1： 
//
// 输入：candidates = [2,3,6,7], target = 7,
//所求解集为：
//[
//  [7],
//  [2,2,3]
//]
// 
//
// 示例 2： 
//
// 输入：candidates = [2,3,5], target = 8,
//所求解集为：
//[
//  [2,2,2,2],
//  [2,3,3],
//  [3,5]
//] 
//
// 
//
// 提示： 
//
// 
// 1 <= candidates.length <= 30 
// 1 <= candidates[i] <= 200 
// candidate 中的每个元素都是独一无二的。 
// 1 <= target <= 500 
// 
// Related Topics 数组 回溯 
// 👍 1413 👎 0


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//leetcode submit region begin(Prohibit modification and deletion)
class 求和组合问题 {
    List<List<Integer>> res = new ArrayList<>();

    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        if (candidates == null || candidates.length == 0) return res;
        Arrays.sort(candidates);
        return res;
    }

    /**
     * @param candidates
     * @param target
     * @param path       用来保存满足条件的数值
     * @param start
     */
    public void dfs(int[] candidates, int target, ArrayList<Integer> path, int start) {
        if (target == 0) {
            res.add(new ArrayList<>(path));
        }
        for (int i = start; i < candidates.length; i++) {
            //对于已经排好序的数组，如果candidates[i] 已经大于了目标值，则没有必要继续往后找了
            if (target < candidates[i]) continue;
            //剪枝 相同数字作用是一样的
            if (i > 0 && candidates[i] == candidates[i - 1]) continue;
            path.add(candidates[i]);
            //新的目标值
            target = target - candidates[i];
            //回溯
            dfs(candidates, target, path, i);

            path.remove(path.size() - 1);
            target = target + candidates[i];
        }
    }

}
//leetcode submit region end(Prohibit modification and deletion)
