//输入一个整型数组，数组中的一个或连续多个整数组成一个子数组。求所有子数组的和的最大值。 
//
// 要求时间复杂度为O(n)。 
//
// 
//
// 示例1: 
//
// 输入: nums = [-2,1,-3,4,-1,2,1,-5,4]
//输出: 6
//解释: 连续子数组 [4,-1,2,1] 的和最大，为 6。 
//
// 
//
// 提示： 
//
// 
// 1 <= arr.length <= 10^5 
// -100 <= arr[i] <= 100 
// 
//
// 注意：本题与主站 53 题相同：https://leetcode-cn.com/problems/maximum-subarray/ 
//
// 
// Related Topics 分治算法 动态规划 
// 👍 285 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class 数组子数组的和最大值 {
    public int maxSubArray(int[] nums) {
        if (nums.length == 0 || nums == null) return 0;
        int n = nums.length;
        int[] dp = new int[n];
        //dp 代表子数组，用来存放最大值的元素集合
        dp[0] = nums[0];
        //res 用来记录之前和现在加上num[i]后最大值
        int res = dp[0];
        for (int i = 1; i < n; i++) {
            //目标结果数组第i个位置的值
            dp[i] = Math.max(dp[i-1]+nums[i],nums[i]);
            //需要使用 res 记录之前和现在加上num[i]后哪个更大： -1 1 -3 4 -1 2 1 -5 4
            res = Math.max(res,dp[i]);
        }
        return res;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
