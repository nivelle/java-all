//数字 n 代表生成括号的对数，请你设计一个函数，用于能够生成所有可能的并且 有效的 括号组合。 
//
// 
//
// 示例 1： 
//
// 
//输入：n = 3
//输出：["((()))","(()())","(())()","()(())","()()()"]
// 
//
// 示例 2： 
//
// 
//输入：n = 1
//输出：["()"]
// 
//
// 
//
// 提示： 
//
// 
// 1 <= n <= 8 
// 
// Related Topics 字符串 动态规划 回溯 
// 👍 1852 👎 0


import java.util.ArrayList;
import java.util.List;

//leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    List<String> res = new ArrayList<>();

    public List<String> generateParenthesis(int n) {
        if (n == 0) {
            return res;
        }
        String s = "";
        dfs(s, n, n);
        return res;

    }

    /**
     * @param s              路径
     * @param remainingLeft  剩下的左括号个数
     * @param remainingRight 剩下的右括号个数
     */
    private void dfs(String s, int remainingLeft, int remainingRight) {
        if (remainingLeft == 0 && remainingRight == 0) {
            res.add(s);
            return;
        }
        //排除不符合条件的
        if (remainingLeft > remainingRight) {
            return;
        }
        //先生成左括号
        if (remainingLeft > 0) {
            dfs(s + "(", remainingLeft - 1, remainingRight);
        }
        if (remainingRight > 0) {
            dfs(s + ")", remainingLeft, remainingRight - 1);
        }
    }
}
//leetcode submit region end(Prohibit modification and deletion)
