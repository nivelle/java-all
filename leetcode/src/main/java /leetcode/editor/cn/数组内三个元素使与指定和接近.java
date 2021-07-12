//给定一个包括 n 个整数的数组 nums 和 一个目标值 target。找出 nums 中的三个整数，使得它们的和与 target 最接近。返回这三个数的和
//。假定每组输入只存在唯一答案。 
//
// 
//
// 示例： 
//
// 输入：nums = [-1,2,1,-4], target = 1
//输出：2
//解释：与 target 最接近的和是 2 (-1 + 2 + 1 = 2) 。
// 
//
// 
//
// 提示： 
//
// 
// 3 <= nums.length <= 10^3 
// -10^3 <= nums[i] <= 10^3 
// -10^4 <= target <= 10^4 
// 
// Related Topics 数组 双指针 
// 👍 795 👎 0


import java.util.Arrays;

//leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    public int threeSumClosest(int[] nums, int target) {
        Arrays.sort(nums);
        //初始值为前三个数字之和初始值
        int res = nums[0] + nums[1] + nums[2];
        for (int i = 0; i < nums.length; i++) {
            // 三个值 i , left ,right 在排好顺序的数组中开始遍历
            int left = i + 1;
            int right = nums.length - 1;
            //退出条件：数据没有交汇
            while (left < right) {
                //三个位移的值，保存下来与
                int sum = nums[left] + nums[right] + nums[i];
                //绝对值比较，找出这轮最接近target的值
                if (Math.abs(sum - target) < Math.abs(res - target)) {
                    res = sum;
                }
                //再迭代尝试
                if (sum > target) {
                    right--;
                } else {
                    left++;
                }

            }

        }
        return res;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
