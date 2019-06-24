package com.nivelle.guide.algorithms.common;


import java.util.Stack;

/**
 * 栈实现链表
 */
public class StackForQueue {

    /**
     * 用两个栈来实现一个队列，完成队列的Push和Pop操作。 队列中的元素为int类型。
     */

    /**
     * push 动作都在 stack1 中进行，
     * pop 动作在 stack2 中进行。当 stack2 不为空时，直接 pop，当 stack2 为空时，
     * 先把 stack1 中的元素 pop 出来，push 到 stack2 中，再从 stack2 中 pop 元素。
     */


    Stack<Integer> stack1 = new Stack();

    Stack<Integer> stack2 = new Stack();


    public int add(int a) {

        return stack1.push(a);

    }

    public int delete() {

        if (stack2.isEmpty()) {
            while (!stack1.isEmpty()) {
                stack2.push(stack1.pop());
            }
        }

        if (stack2.isEmpty()) {
            throw new RuntimeException("No more element.");
        }

        return stack2.pop();
    }




}
