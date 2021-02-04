package com.nivelle.base.jdk.util;

import java.util.Vector;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/03
 */
public class VectorMock {

    public static void main(String[] args) {
        Vector vector = new Vector(3, 1);
        System.out.println(vector.capacity());
        for (int i = 0; i <= 4; i++) {
            vector.add(i);
        }
        System.out.println("容量加1：" + vector.capacity());

        Vector vector1 = new Vector();
        System.out.println(vector1.capacity());
        for (int i = 0; i < 12; i++) {
            vector1.add(i);
        }
        //默认容量是10，不够时扩容为2倍
        System.out.println(vector1.capacity());
    }
}
