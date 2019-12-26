package com.nivelle.base.javacore.loadclass;

/**
 * 类加载顺序:
 * 父类静态属性--->父类静态代码块--->子类静态属性--->子类静态代码块--->父类实例属性--->父类动态代码块--->父类无参构造--->子类实例属性--->子类动态代码块--->子类无参构造
 **/

class Base {

    public static String baseStaticVariable = Method();

    String baseVariable = "父类实例属性";

    static {
        System.out.println("父类静态代码块");
    }

    {
        System.out.println("父类实例代码块");
    }

    public Base() {
        System.out.println("父类构造方法");
    }

    public static void baseStaticMethod() {
        System.out.println("父类静态方法");
    }

    public static String Method() {
        return "父类静态变量";
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("销毁父类");
    }
}

class Sub extends Base {

    /**
     * 静态属性
     */
    public static String subStaticVariable = Method();

    String subVariable = "子类实例属性";

    static {
        System.out.println("子类静态代码块");
    }

    {
        System.out.println("子类实例代码块");
    }

    public Sub() {
        System.out.println("子类构造方法");
    }

    public static void subStaticMethod() {
        System.out.println("子类静态方法");
    }

    public static String Method() {
        System.out.println("子类静态属性");
        return "子类静态属性";
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("销毁子类");
    }
}


public class ClassInitOrderDemo {

    public static void main(String[] args) {
        /**
         * 父类静态代码块
         * 子类静态属性
         * 子类静态代码块
         * 子类静态方法
         */
        Sub.subStaticMethod();

        /**
         * 父类静态代码块
         * 父类静态变量
         */
         System.out.println(Base.baseStaticVariable);


        /**
         * 父类静态代码块
         * 子类静态属性
         * 子类静态代码块
         * 父类实例代码块
         * 父类构造方法
         * 子类实例代码块
         * 子类构造方法
         */
        Sub sub = new Sub();
        Sub.subStaticMethod();


    }
}

