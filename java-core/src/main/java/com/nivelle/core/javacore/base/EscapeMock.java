package com.nivelle.core.javacore.base;

/**
 * 逃逸分析
 *
 * @author fuxinzhong
 * @date 2021/01/29
 */
public class EscapeMock {

    public static final int x = 10;
    EscapeMock escapeMock = null;

    public static void main(String[] args) {
        EscapeMock escapeMock = new EscapeMock();
        escapeMock.finalField();
    }

    public void finalField() {
        int x = 3;
        int y = 4;
        escapeMock = this;
        System.out.println(escapeMock);

    }

}
