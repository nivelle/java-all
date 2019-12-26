package com.nivelle.base.javacore.innerclass;

/**
 * 方法内部类
 *
 * @author fuxinzhong
 * @date 2019/12/15
 */
public class MethodInnerClass {

    private int shared = 1;

    public void outerMethod(final int param, int param2) {
        final String str = "hello";
        String str2 = "nivelle";
        /**
         * 方法内部类只能在方法内使用
         */
        class InnerClass {
            public void innerMethod() {
                /**
                 * 内部类直接访问外部实例
                 */
                System.out.println("outer shared is:" + shared);
                /**
                 * 内部类直接访问方法的参数和方法中的成员变量,不过这些变量必须是final的
                 *
                 * 方法内操作的并不是外部的变量，而是他们自己的实例变量，只是这些变量的值和外部的一样，对于这些变量的赋值并不会改变外部的值，所以干脆声明为final
                 */
                System.out.println("outer method param is:" + param);
                System.out.println("local var str is:" + str);
                System.out.println("local var str2 is:" + str2);
                System.out.println("outer method param is:" + param2);
                /**
                 * 内部类直接访问外部方法
                 */
                outMethod1();
            }
        }
        InnerClass inner = new InnerClass();
        inner.innerMethod();
    }

    public void outMethod1() {
        System.out.println("out method 2");
    }

    public static void main(String[] args) {
        MethodInnerClass methodInnerClass = new MethodInnerClass();
        methodInnerClass.outerMethod(100, 200);
    }

}
