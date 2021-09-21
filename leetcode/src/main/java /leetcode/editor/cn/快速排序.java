package leetcode.editor.cn;

/**
 * 快速排序
 *
 * @author fuxinzhong
 * @date 2021/06/05
 */
public class 快速排序 {
    //递归排序
    public static void main(String[] args) {
        int[] arr = new int[]{2, 1, 0, 4, 98, 5, 7};
        quickSort(arr, 0, arr.length - 1);
        for (int i = 0; i < arr.length; i++) {
            System.out.println(arr[i]);
        }
    }

    private static void quickSort(int[] arr, int low, int high) {
        if (low > high) return;
        //基准
        int index = getIndex(arr, low, high);
        //基准左侧排序
        quickSort(arr, low, index - 1);
        quickSort(arr, index + 1, high);
    }


    //目标是将比基准元素大的数据放到基准元素的右边,把比基准元素小的数据放到基准元素的左边
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
        //退出循环后，说明此时high==low，让基准数组值赋值给当前值
        arr[low] = tmp;
        //返回基准位置，此事比基准数据大的都在基准数据的右边，比它小的都在左边
        return low;
    }
}
