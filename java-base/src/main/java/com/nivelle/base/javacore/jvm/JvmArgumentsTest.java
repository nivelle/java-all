package com.nivelle.base.javacore.jvm;

/**
 * jvm虚拟机参数配置学习
 *
 *
 * @author nivell
 * @date 2020/03/21
 */
public class JvmArgumentsTest {

    public static void main(String[] args) {

        /**
         * - byte、short、int、long、float 以及 double 的值域依次扩大，而且前面的值域被后面的值域所包含。
         * 因此，从前面的基本类型转换至后面的基本类型，无需强制转换。另外一点值得注意的是，尽管他们的默认值看起来不一样，但在内存中都是 0。
         */
        int a= 2;
        long b = a;
        System.out.println(a);
        System.out.println(b);

        Boolean b1 = true;

    }
}
