//给定一个大小为 n 的数组，找到其中的多数元素。
//多数元素是指在数组中出现次数 大于 ⌊ n/2 ⌋ 的元素。
//
// 你可以假设数组是非空的，并且给定的数组总是存在多数元素。 
//
// 
//
// 示例 1： 
//
// 
//输入：[3,2,3]
//输出：3 
//
// 示例 2： 
//
// 
//输入：[2,2,1,1,1,2,2]
//输出：2
// 
//
// 
//
// 进阶： 
//
// 
// 尝试设计时间复杂度为 O(n)、空间复杂度为 O(1) 的算法解决此问题。 
// 
// Related Topics 位运算 数组 分治算法 
// 👍 1026 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class 数组多数元素 {
    public int majorityElement(int[] nums) {
        if (nums == null || nums.length == 0) {return 0;}
        //出现次数最多的元素
        int mainNum = 0;
        //出现次数最多的次数
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            //如果次数为0,则默认第i个元素为主要元素
            if (count == 0) {
                mainNum = nums[i];
            }
            //如果新元素与当前主要元素相同，则计数+1；否则-1
            if (mainNum == nums[i]) {
                count++;
            } else {
                count--;
            }
        }
        return mainNum;

    }
}
//leetcode submit region end(Prohibit modification and deletion)
