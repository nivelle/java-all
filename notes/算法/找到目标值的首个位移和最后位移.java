//给定一个按照升序排列的整数数组 nums，和一个目标值 target。找出给定目标值在数组中的开始位置和结束位置。 
//
// 如果数组中不存在目标值 target，返回 [-1, -1]。 
//
// 进阶： 
//
// 
// 你可以设计并实现时间复杂度为 O(log n) 的算法解决此问题吗？ 
// 
//
// 
//
// 示例 1： 
//
// 
//输入：nums = [5,7,7,8,8,10], target = 8
//输出：[3,4] 
//
// 示例 2： 
//
// 
//输入：nums = [5,7,7,8,8,10], target = 6
//输出：[-1,-1] 
//
// 示例 3： 
//
// 
//输入：nums = [], target = 0
//输出：[-1,-1] 
//
// 
//
// 提示： 
//
// 
// 0 <= nums.length <= 105 
// -109 <= nums[i] <= 109 
// nums 是一个非递减数组 
// -109 <= target <= 109 
// 
// Related Topics 数组 二分查找 
// 👍 1048 👎 0


//leetcode submit region begin(Prohibit modification and deletion)
class 找到目标值的首个位移和最后位移 {
    public int[] searchRange(int[] nums, int target) {
        //二分法查找左边边界值
        int leftIdx = binarySearch(nums, target, true);
        //二分法查找右边边界值
        int rightIdx = binarySearch(nums, target, false) - 1;
        if (leftIdx <= rightIdx && rightIdx < nums.length && nums[leftIdx] == target && nums[rightIdx] == target) {
            return new int[]{leftIdx, rightIdx};
        }
        return new int[]{-1, -1};
    }

    /**
     * 二分用这个模板就不会出错了。满足条件的都写l = mid或者r = mid，mid首先写成l + r >> 1，如果满足条件选择的是l = mid，那么mid那里就加个1，
     * 写成l + r + 1 >> 1。然后就是else对应的写法l = mid对应r = mid - 1，r = mid对应l = mid + 1
     */
    public int binarySearch(int[] nums, int target, boolean lower) {
        int left = 0;
        int right = nums.length - 1;
        //元素个数
        int ans = nums.length;
        //退出条件: 指针交汇
        while (left <= right) {
            //中间位移
            int mid = (left + right) >> 1;
            // 左边第一个： 第一个等于target的位置
            // 右边第一个： 第一个大于target减一的位置
            // 充分利用数组的有序性
            //
            if (nums[mid] > target || (lower && nums[mid] >= target)) {
                //右下标左移
                right = mid - 1;
                //子集元素个数
                ans = mid;
            } else {
                //左下标右移
                left = mid + 1;
            }
        }
        return ans;
    }
}

//leetcode submit region end(Prohibit modification and deletion)
