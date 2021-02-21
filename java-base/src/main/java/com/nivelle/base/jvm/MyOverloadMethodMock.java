package com.nivelle.base.jvm;

/**
 * 方法重载
 *
 * @author fuxinzhong
 * @date 2021/02/20
 */
public class MyOverloadMethodMock {

    public static void main(String[] args) {
        /**
         * 方法重载支持 包装类：先不考虑包装类进行精确匹配，如果没有完全精确匹配则考虑装箱类
         */
        MyOverloadMethod myOverloadMethod = new MyOverloadMethod();
        long result1 = myOverloadMethod.myMethod("2", new Integer(1));
        System.out.println(result1);
        long result2 = myOverloadMethod.myMethod("2", 1);
        System.out.println(result2);

        myOverloadMethod.myMethod1(null, 1);
        myOverloadMethod.myMethod1(null, new Object[]{1});
        //myOverloadMethod.myMethod1(null,1,2);

        myOverloadMethod.myMethod3(1L, 2);
        myOverloadMethod.myMethod3(1L, 2, 1);


    }
}
