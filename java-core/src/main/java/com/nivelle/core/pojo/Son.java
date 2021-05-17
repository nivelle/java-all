package com.nivelle.core.pojo;

/**
 * 子类
 */
public class Son extends Parent {

    int j = 1;

    public Son() {
        System.out.println("子类构造函数前，j=" + j);
        j = 2;
        System.out.println("子类构造函数后，j=" + j);

    }

    {
        System.out.println("子类代码块前，j=" + j);
        j = 3;
        System.out.println("子类代码块后，j=" + j);

    }

    @Override
    public int getValue() {
        return j;
    }

    private int age;
    private String name;
    private int idNum;

    public Son(int age, String name, int idNum) {
        this.age = age;
        this.name = name;
        this.idNum = idNum;
    }

    @Override
    public String toString() {
        return "Son{" +
                "j=" + j +
                ", age=" + age +
                ", name='" + name + '\'' +
                ", idNum=" + idNum +
                '}';
    }
}
