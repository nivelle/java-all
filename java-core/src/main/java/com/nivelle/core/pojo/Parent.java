package com.nivelle.core.pojo;

/**
 * 父类
 */
public class Parent {

    int i = 1;
    String name;


    public Parent() {
        System.out.println("父类构造函数前 i :" + i);
        /**
         * 子类复写了该方法,这导致会调用子类方法，返回此时子类的i值：0
         */
        int x1 = getValue();
        System.out.println("父类构造函数后 i :" + i);
        System.out.println("子类复写的方法获取值getValue:" + x1);
        System.out.println("此时上下文是子类,父类内部实例：" + this.getClass().getName());
        int x2 = this.getValue();
        System.out.println("子类复写的方法强制使用当前类获取值,getValue:" + x2);

    }

    {
        System.out.println("parent 代码块前i：" + i);
        i = 3;
        System.out.println("parent 代码块后i：" + i);

    }


    public Parent(String name) {
        this.name = name;
    }


    public int getValue() {
        System.out.println("父获取值：" + i);
        return i;
    }

    @Override
    public String toString() {
        return "Parent{" +
                "i=" + i +
                ", name='" + name + '\'' +
                '}';
    }
}


