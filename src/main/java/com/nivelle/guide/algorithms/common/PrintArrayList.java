package com.nivelle.guide.algorithms.common;

import java.util.ArrayList;
import java.util.Stack;

/**
 * 从尾到头打印列表
 */
public class PrintArrayList {

    public static class ListNode {
        int val; // 结点的值
        ListNode next; // 下一个结点
    }

    /**
     * 输入一个链表，按链表值从尾到头的顺序返回一个ArrayList。
     */
    public static ArrayList<Integer> printListFromTailToHead(ListNode root) {
        if (root == null) {
            return new ArrayList();
        }
        ListNode head = root;
        ListNode cur = root.next;
        while (cur != null) {
            ListNode temp = cur.next;//记录下来防止丢失

            cur.next = head;//当前结点下一个结点改为目前头结点

            head = cur;//当前结点变成现在头结点，实现交换

            cur = temp;
        }
        //此时listNode的next还指向第二个node，所以要让listNode.next=null,防止循环
        root.next = null;
        ArrayList<Integer> res = new ArrayList();
        while (head != null) {
            res.add(head.val);
            head = head.next;
        }
        System.out.println(res);
        return res;
    }

    public static void printListInverselyUsingIteration(ListNode root) {
        Stack<ListNode> stack = new Stack();
        while (root != null) {
            stack.push(root);
            root = root.next;
        }
        ListNode tmp;
        while (!stack.isEmpty()) {
            tmp = stack.pop();
            System.out.print(tmp.val + " ");
        }
    }

    public static void main(String[] args) {
        ListNode root = new ListNode();
        root.val = 1;
        root.next = new ListNode();
        root.next.val = 2;
        root.next.next = new ListNode();
        root.next.next.val = 3;
        root.next.next.next = new ListNode();
        root.next.next.next.val = 4;
        root.next.next.next.next = new ListNode();
        root.next.next.next.next.val = 5;
        printListInverselyUsingIteration(root);
        System.out.println();
        printListFromTailToHead(root);
    }
}
