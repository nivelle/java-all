//给定一个非负整数数组 A，返回一个数组，在该数组中， A 的所有偶数元素之后跟着所有奇数元素。 
//
// 你可以返回满足此条件的任何数组作为答案。 
//
// 
//
// 示例： 
//
// 输入：[3,1,2,4]
//输出：[2,4,3,1]
//输出 [4,2,3,1]，[2,4,1,3] 和 [4,2,1,3] 也会被接受。
// 
//
// 
//
// 提示： 
//
// 
// 1 <= A.length <= 5000 
// 0 <= A[i] <= 5000 
// 
// Related Topics 数组 双指针 排序 
// 👍 219 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class 数组按奇偶排序 {
    public int[] sortArrayByParity(int[] nums) {
        if (nums == null || nums.length == 0) {
            return nums;
        }
        int left = 0;
        int right = nums.length - 1;
        //双指针，从前往后 ，从后往前
        while (left < right) {
            while (left < right && (nums[left] % 2 == 0)) {
                //如果是偶数，不动
                left++;
            }
            while (left < right && nums[right] % 2 == 1) {
                //如果是偶数不动
                right--;
            }
            //如果有不满足的left 和 right,则交换位置
            int tmp = nums[left];
            nums[left] = nums[right];
            nums[right] = tmp;
        }
        return nums;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
