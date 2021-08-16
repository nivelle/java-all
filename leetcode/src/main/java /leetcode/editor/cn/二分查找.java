package leetcode.editor.cn;

/**
 * 二分法查找
 *
 * @author fuxinzhong
 * @date 2021/05/13
 */
public class 二分查找 {

    public static void main(String[] args) {
        int[] a = {1, 2, 4, 4, 5};
        int res = findIt(5, 4, a);
        System.out.println(res);
    }

    public static int findIt(int length, int targetValue, int[] source) {
        if (length <= 0) {
            return 1;
        }
        int leftIndex = 0;
        int rightIndex = length - 1;
        int index;
        while (leftIndex <= rightIndex) {
            index = (leftIndex + rightIndex) / 2;
            if (source[index] >= targetValue) {
                if (index == 0 || source[index - 1] < targetValue) {
                    return index + 1;
                } else {
                    //右边界 更新 right = index-1
                    rightIndex = index - 1;
                }
            }
        }
        return length + 1;
    }
}
