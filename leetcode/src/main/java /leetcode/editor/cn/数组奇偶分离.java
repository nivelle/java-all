//输入一个整数数组，实现一个函数来调整该数组中数字的顺序，使得所有奇数位于数组的前半部分，所有偶数位于数组的后半部分。 
//
// 
//
// 示例： 
//
// 
//输入：nums = [1,2,3,4]
//输出：[1,3,2,4] 
//注：[3,1,2,4] 也是正确的答案之一。 
//
// 
//
// 提示： 
//
// 
// 0 <= nums.length <= 50000 
// 1 <= nums[i] <= 10000 
// 
// 👍 137 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class 数组奇偶分离 {

    public int[] exchange(int[] nums){
        return insertOrderArray(nums);
    };

    public int[] twoPtrExchange(int[] nums) {

        if (nums == null || nums.length == 0) {
            return nums;
        }
        int left = 0;
        int right = nums.length - 1;
        //双指针，从前往后 ，从后往前
        while (left < right) {
            while (left < right && (nums[left] % 2 == 1)) {
                //如果是奇数，不动
                left++;
            }
            while (left < right && nums[right] % 2 == 0) {
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

    // 保证稳定性的插入排序算法
    private int[] insertOrderArray(int[] array) {
        if (array == null || array.length == 0) {
            return array;
        }
        for (int i = 1; i < array.length; i++) {
            int insertValue = array[i];
            //当前值如果是奇数
            if (array[i] % 2 == 1) {
                //当前index
                int insertIndex = i;
                //当前值的前一个值如果是偶数。
                while (insertIndex >= 1 && array[insertIndex - 1] % 2 == 0) {
                    //则和insetIndex交换值
                    array[insertIndex] = array[insertIndex - 1];
                    //当前位移前移一位
                    insertIndex--;
                }
                //当前值设置到前一个位移
                array[insertIndex] = insertValue;
            }
        }
        return array;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
