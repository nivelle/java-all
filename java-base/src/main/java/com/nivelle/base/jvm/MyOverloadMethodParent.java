package com.nivelle.base.jvm;

/**
 * 方法重载继承
 *
 * @author fuxinzhong
 * @date 2021/02/21
 */
public class MyOverloadMethodParent {

    public void myMethod3(long i, int j) {
        System.out.println("MyOverloadMethodParent==>i am myMethod3");
    }

    public int myMethod3(long i, int j, int k) {
        System.out.println("MyOverloadMethodParent==> Overload myMethod3");
        return 0;
    }

    public int myMethod4(long i, int j) {
        System.out.println("MyOverloadMethodParent==>i am myMethod4");
        return 1;
    }


}
