package com.nivelle.base.algorithms.common;

/**
 * 把一个数组最开始的若干个元素搬到数组的末尾，我们称之为数组的旋转。
 * 输入一个非减排序的数组的一个旋转，输出旋转数组的最小元素。 例如数组{3,4,5,1,2}为{1,2,3,4,5}的一个旋转，该数组的最小值为1。 NOTE：给出的所有元素都大于0，若数组大小为0，请返回0。
 *
 * @author fuxinzhong
 * @date 2019/08/20
 */
public class MinElementArray {

    /**
     * 1.关键思路,通过数组中间下标找到数组左右部分的趋势；
     * 2.注意两个数字的情况
     */

    public static void main(String[] args) {
        int result;
        int[] array = new int[]{3, 4, 5, 6, 2};
        int len = array.length;
        if (len == 0) {
            result = 0;
        } else {
            int low = 0;
            int high = len - 1;
            while (low < high) {
                int mid = low + (high - low) / 2;
                if (array[mid] > array[high]) {//右边递减
                    low = mid + 1;
                } else if (array[mid] == array[high]) {
                    high = high - 1;
                } else {
                    high = mid;
                }
            }
            result = array[low];
        }
        System.out.println(result);
    }


}
