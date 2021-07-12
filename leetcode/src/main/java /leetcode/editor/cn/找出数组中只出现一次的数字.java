//给定一个只包含整数的有序数组，每个元素都会出现两次，唯有一个数只会出现一次，找出这个数。 
//
// 示例 1: 
//
// 
//输入: [1,1,2,3,3,4,4,8,8]
//输出: 2
// 
//
// 示例 2: 
//
// 
//输入: [3,3,7,7,10,11,11]
//输出: 10
// 
//
// 注意: 您的方案应该在 O(log n)时间复杂度和 O(1)空间复杂度中运行。 
// Related Topics 二分查找 
// 👍 246 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    public int singleNonDuplicate(int[] nums) {
        int res = 0;
        //a^a =0; a^0=a;0^a = a;
        for (int i = 0; i < nums.length; i++) {
            res = res ^ nums[i];
        }
        return res;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
