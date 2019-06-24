package com.nivelle.guide.datastructure.linkedList.oneWayLinked;

/**
 * 单向链表数据节点
 */
public class Node<T> {
    public T data;
    public Node<T> next;

    public Node() {

    }

    public Node(T data) {
        this.data = data;
    }

    public Node(T data, Node<T> next) {
        this.data = data;
        this.next = next;
    }

    public Node nextNode() {
        return this.next;
    }

}
