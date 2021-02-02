package com.nivelle.base.javacore;

/**
 * finall不会被执行的特例
 *
 * @author fuxinzhong
 * @date 2021/02/02
 */
public class FinallyMock {

    public static void main(String[] args) {

        try {
            // do something
            System.out.println("finally 执行之前");
            System.exit(1);
        } finally {
            System.out.println("finally 不会被执行");
        }
    }
}
