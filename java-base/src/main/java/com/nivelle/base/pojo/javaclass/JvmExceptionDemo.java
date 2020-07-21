package com.nivelle.base.pojo.javaclass;

/**
 * java编译反编译demo
 *
 * @author nivelle
 * @date 2020/03/23
 */
public class JvmExceptionDemo {

    private int tryBlock;
    private int catchBlock;
    private int finallyBlock;
    private int methodExit;

    public void test() {
        try {
            tryBlock = 0;
        } catch (Exception e) {
            catchBlock = 1;
        } finally {
            finallyBlock = 2;
        }
        methodExit = 3;
    }

    public static void main(String[] args) {
        System.out.println("javac is right");
    }
}
