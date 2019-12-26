package com.nivelle.base.javacore.innerclass;

/**
 * 静态内部类
 *
 * @author fuxinzhong
 * @date 2019/12/14
 */
public class StaticInnerClass {

    private static int shared = 100;

    /**
     * 静态内部类:与外部类依赖密切，切不依赖外部类实列
     */
    public static class StaticInner {
        public void innerMethod() {
            /**
             * 内部类可以访问外部类的静态变量和静态方法
             */
            System.out.println("静态内部类直接访问外部类的成员变量:" + shared);
            outStaticMethod();
        }

        public static void innerStaticMethod() {
            System.out.println("innerStaticMethod....");
        }
    }

    /**
     * 外部类类可以直接使用内部静态类
     */
    public void test() {
        StaticInner staticInner = new StaticInner();
        staticInner.innerMethod();
        StaticInner.innerStaticMethod();
    }

    public static void outStaticMethod() {
        System.out.println("内部类可以直接调用外部类方法");
    }

    public static void main(String[] args) {
        StaticInner staticInner = new StaticInner();
        staticInner.innerMethod();
        StaticInner.innerStaticMethod();

        StaticInnerClass staticInnerClass = new StaticInnerClass();
        staticInnerClass.test();
    }
}
