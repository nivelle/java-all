package com.nivelle.base.javacore;

import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

import java.util.Stack;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/01/15
 */
public class JavaLangAccessDemo {

    public static void main(String[] args) {
        Stack e = new Stack();
        e.push(1);
        e.push(2);
        e.push(3);
        e.push(4);

        JavaLangAccess access = SharedSecrets.getJavaLangAccess();
        Throwable throwable = new Throwable();
        int depth = access.getStackTraceDepth(throwable);
        //输出JVM栈帧中的所有类实例
        for (int i = 0; i < depth; i++) {
            StackTraceElement frame = access.getStackTraceElement(throwable, i);
            System.err.println(frame);
        }
    }
}
