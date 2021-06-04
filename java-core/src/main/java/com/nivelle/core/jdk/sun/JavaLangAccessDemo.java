package com.nivelle.core.jdk.sun;

import com.nivelle.core.pojo.User;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

/**
 * SharedSecrets 和  JavaLangAccess的作用
 *
 * @author nivelle
 * @date 2020/01/15
 */
public class JavaLangAccessDemo {

    /**
     * 当我们需要对使用类名进行的推断的时候,我们就需要知道JVM里面的实例对象了，这时候我们就需要使用到SharedSecrets和JavaLangAccess，
     * 通过这两个类来获取Java栈帧中存储的类信息，然后进行挑选，从而找出调用的类。
     */
    public static void main(String[] args) {

        JavaLangAccess access = SharedSecrets.getJavaLangAccess();
        Throwable throwable = new Throwable("fuck");

        int depth = access.getStackTraceDepth(throwable);
        System.out.println(depth);
        //输出JVM栈帧中的所有类实例
        for (int i = 0; i < depth; i++) {
            System.out.println(i);
            StackTraceElement frame = access.getStackTraceElement(throwable, 0);
            System.out.println("StackTraceElement is"+frame);
        }
    }
}
