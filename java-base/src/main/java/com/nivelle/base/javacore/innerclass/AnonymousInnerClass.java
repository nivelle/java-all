package com.nivelle.base.javacore.innerclass;

/**
 * 匿名内部类
 *
 * @author fuxinzhong
 * @date 2019/12/15
 */
public class AnonymousInnerClass {

    public int annoymousInber(final int x, final int y) {
        /**
         * new 父类(参数列表)or父接口{
         *     匿名内部类实现
         * }
         */
        System.err.println("outer x+y:" + (x + y));
        InnerClassMethod innerClassMethod = new InnerClassMethod(2, 3) {
            @Override
            public int innerClassMethodValue() {
                System.err.println("内部类测试===>" + "x:" + x + "++" + "y:" + y);
                return x + y;
            }
        };
        System.out.println(innerClassMethod.innerClassMethodValue());
        return innerClassMethod.innerClassMethodValue();
    }


    public static void main(String[] args) {
        AnonymousInnerClass anonymousInnerClass = new AnonymousInnerClass();
        anonymousInnerClass.annoymousInber(1, 2);
    }
}
