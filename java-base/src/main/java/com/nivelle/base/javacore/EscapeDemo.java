package com.nivelle.base.javacore;

/**
 * 逃逸分析
 *
 * @author fuxinzhong
 * @date 2021/01/29
 */
public class EscapeDemo {

    public static final int x = 10;

    public static void main(String[] args) {
        EscapeDemo escapeDemo = new EscapeDemo();
        escapeDemo.finalField();
    }

    public void finalField() {
        int x = 3;
        int y = 4;
        EscapeDemo escapeDemo = this;
        System.out.println(escapeDemo);

    }

}
