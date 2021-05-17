package com.nivelle.core.innerclass;

/**
 * 静态内部类
 *
 * @author nivelle
 * @date 2019/12/14
 */
public class StaticInnerClass {

    private static int shared = 100;
    private Integer innerInstanceShared = 52;

    {
        System.out.println("外部类实例代码块");
    }

    public StaticInnerClass() {
        System.out.println("----------外部类构造函数----------");
    }

    /**
     * 静态内部类:与外部类依赖密切,且不依赖外部类实列
     */
    public static class StaticInner {
        private static long innerStaticShared = 50;
        private Integer innerInstanceShared = 51;

        {
            System.out.println("内部类代码块！");
        }

        public StaticInner() {
            System.out.println("--------内部类构造函数----------");
        }

        public void innerMethod() {
            /**
             * 内部类可以访问外部类的静态变量和静态方法
             */
            System.out.println("静态内部类直接访问外部类的成员变量:" + shared);
            outStaticMethod();
            System.out.println("内部类成员变量-》innerInstanceShared：" + this.innerInstanceShared);
            System.out.println("内部类静态成员变量-》innerStaticShared:" + this.innerStaticShared);
        }

        public static void innerStaticMethod() {
            System.out.println("外部类通过类直接调用内部类的静态方法-》innerStaticMethod....");
        }
    }

    /**
     * 外部类静态方法
     */
    public static void outStaticMethod() {
        System.out.println("我是外部类的静态方法-》内部类可以直接调用外部类方法");
    }

    /**
     * 外部类实例方法
     */
    public void test() {
        System.out.println("外部类直接创建内部类实例。。。");
        StaticInner staticInner = new StaticInner();
        staticInner.innerMethod();
        StaticInner.innerStaticMethod();
        System.out.println("外部类的成员变量:" + this.innerInstanceShared);
    }


    public static void main(String[] args) {
//        System.out.println("内部类实例方法调用。。。。");
//        StaticInner staticInner = new StaticInner();
//        staticInner.innerMethod();
//        System.out.println("内部类静态方法调用。。。。");
//        StaticInner.innerStaticMethod();
//        System.out.println("外部类实例方法调用。。。。");
//        StaticInnerClass staticInnerClass = new StaticInnerClass();
//        staticInnerClass.test();
//        System.out.println("外部类静态方法调用。。。。");
//        StaticInnerClass.outStaticMethod();
//
//        System.out.println("外部类通过内部类调用静态方法。。。。");
//        StaticInnerClass.StaticInner.innerStaticMethod();
        StaticInnerClass staticInnerClass = new StaticInnerClass();
        StaticInner.innerStaticMethod();


    }
}
