package com.nivelle.base.pojo;

/**
 * 子类
 */
public class Son extends Parent {

    int j = 1;

    public Son() {
        j = 2;
    }

    {
        j = 3;
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
