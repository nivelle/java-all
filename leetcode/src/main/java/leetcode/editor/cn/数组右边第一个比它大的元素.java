package leetcode.editor.cn;

import java.util.Stack;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/11/02
 */
public class 数组右边第一个比它大的元素 {

    public int[] findMaxRightWithStack(int[] array) {
        if(array == null) {
            return array;
        }
        int size = array.length;
        int[] result = new int[size];
        //我们用栈来保存未找到右边第一个比它大的元素的索引（保存索引是因为后面需要靠索引来给新数组赋值），
        //初始时，栈里放的是第一个元素的索引0值。
        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        int index = 1;
        while(index < size) {
            //找到第一个大于它的元素
            if(!stack.isEmpty() && array[index] > array[stack.peek()]) {
                //该元素下标对应的值 是 array[index]
                result[stack.pop()] = array[index];
            } else {
                stack.push(index);
                index++;
            }
        }
        if(!stack.isEmpty()) {
            //pop 移除
            result[stack.pop()] = -1;
        }
        return result;
    }
}
