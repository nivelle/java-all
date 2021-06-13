package leetcode.editor.cn;

/**
 * 二分法查找
 *
 * @author fuxinzhong
 * @date 2021/05/13
 */
public class BinaryArraySearch {

    public static void main(String[] args) {
        int[] a = {1, 2, 4, 4, 5};
        int res = findIt(5, 3, a);
        System.out.println(res);
    }

    public static int findIt(int length, int value, int[] target) {
        if (length <= 0) {
            return 1;
        }
        int leftIndex = 0;
        int rightIndex = length - 1;
        int index;
        while (leftIndex <= rightIndex) {
            index = (leftIndex + rightIndex) / 2;
            if (target[index] >= value) {
                if (index == 0 || target[index - 1] < value) {
                    return index + 1;
                } else {
                    rightIndex = index - 1;
                }
            }
        }
        return length + 1;
    }
}
