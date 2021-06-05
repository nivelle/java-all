//给定一个非空整数数组，除了某个元素只出现一次以外，其余每个元素均出现两次。找出那个只出现了一次的元素。 
//
// 说明： 
//
// 你的算法应该具有线性时间复杂度。 你可以不使用额外空间来实现吗？ 
//
// 示例 1: 
//
// 输入: [2,2,1]
//输出: 1
// 
//
// 示例 2: 
//
// 输入: [4,1,2,1,2]
//输出: 4 
// Related Topics 位运算 哈希表 
// 👍 1881 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class Solution {
    //给定一个非空整数数组，除了某个元素只出现一次以外，其余每个元素均出现两次。找出那个只出现了一次的元素。
    public int singleNumber(int[] nums) {
        int res = 0;
         //数组中的全部元素的异或运算结果即为数组中只出现一次的数字
        // 0与原值进行异或,保留原值
        // 0^a = a; a^a = 0;
        for (int i = 0; i < nums.length; i++) {
            res = res ^ nums[i];
        }
        return res;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
