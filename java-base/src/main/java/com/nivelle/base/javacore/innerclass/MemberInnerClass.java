package com.nivelle.base.javacore.innerclass;

/**
 * 成员内部类
 *
 * @author fuxinzhong
 * @date 2019/12/14
 */
public class MemberInnerClass {

    private int shared = 100;

    /**
     * 成员内部类：内部类与外部类关系密切，而且内部类需要访问外部类的实例变量和方法则可以使用成员内部类
     */
    public class Inner {
        public void innerMethod() {
            System.out.println("==================================");
            System.out.println("内部类直接访问外部类的成员变量:" + shared);
            System.out.print("内部类直接访问外部类方法:");
            MemberInnerClass.this.action();
        }
    }

    private void action() {
        System.out.println("action");
    }

    /**
     * 外部类方法直接访问内部类
     */
    public void outMethod() {
        Inner inner = new Inner();
        inner.innerMethod();
    }

    public static void main(String[] args) {
        MemberInnerClass memberInnerClass = new MemberInnerClass();
        memberInnerClass.action();

        MemberInnerClass.Inner  inner = memberInnerClass.new Inner();
        inner.innerMethod();

        memberInnerClass.outMethod();

    }
}
