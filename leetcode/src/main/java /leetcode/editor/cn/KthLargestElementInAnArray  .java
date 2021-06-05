//在未排序的数组中找到第 k 个最大的元素。请注意，你需要找的是数组排序后的第 k 个最大的元素，而不是第 k 个不同的元素。 
//
// 示例 1: 
//
// 输入: [3,2,1,5,6,4] 和 k = 2
//输出: 5
// 
//
// 示例 2: 
//
// 输入: [3,2,3,1,2,4,5,5,6] 和 k = 4
//输出: 4 
//
// 说明: 
//
// 你可以假设 k 总是有效的，且 1 ≤ k ≤ 数组的长度。 
// Related Topics 堆 分治算法 
// 👍 1118 👎 0


import java.util.PriorityQueue;

//leetcode submit region begin(Prohibit modification and deletion)
class Solution {
//    public int findKthLargest(int[] nums, int k) {
//        PriorityQueue<Integer> heap = new PriorityQueue<>((n1, n2) -> (n1 - n2));
//        for (int n : nums) {
//            heap.add(n);
//            if (heap.size() > k) {
//                heap.poll();
//            }
//        }
//        return heap.poll();
//    }




    private static void quickSort(int[] arr, int low, int high) {
        if (low > high) return;
        int index = getIndex(arr, low, high);
        quickSort(arr, low, index - 1);
        quickSort(arr, index + 1, high);
    }

    //目标是将比基准元素大的数据放到基准元素的右边，把比基准元素小的数据放到基准元素的左边
    //当两个指针重合时就是基准元素的位置
    private static int getIndex(int[] arr, int low, int high) {
        //临时变量，保存基准数据
        int tmp = arr[low];
        while (low < high) {
            //右指针先往左走，当右指针指向元素比基准数小时，将该元素放到左指针指向的位置（这么做是为了把该数放到基准数的左边）
            while (low < high && arr[high] >= tmp) {
                high--;
            }
            //否则如果尾部元素小于tmp了,需要将其赋值给low
            arr[low] = arr[high];
            //右指针把元素放到左指针位置后，左指针开始向右走。当左指针指向元素比基准数大时，把该元素放到右指针的位置。
            while (low < high && arr[low] <= tmp) {
                low++;
            }
            arr[high] = arr[low];
        }
        //退出循环后，说明此时high==low，让基准数组赋值给当前值
        arr[low] = tmp;
        //返回基准位置，此事比基准数据大的都在基准数据的右边，比它小的都在左边
        return low;
    }
}
//leetcode submit region end(Prohibit modification and deletion)
