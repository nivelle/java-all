package com.nivelle.base.javacore.loadclass;


import java.io.*;
import java.lang.reflect.Constructor;

/**
 * 构造对象的方法
 */
public class ConstructorDemo implements Cloneable, Serializable {

    private Integer id;

    public ConstructorDemo(Integer id) {
        this.id = id;
    }

    public ConstructorDemo() {

    }

    @Override
    public String toString() {
        return "ConstructorDemo [id=" + id + "]";
    }

    public static void main(String args[]) throws Exception {
        System.out.println("使用new关键字创建对象：");
        ConstructorDemo constructor1 = new ConstructorDemo(123);
        System.out.println(constructor1);
        System.out.println("\n---------------------------\n");

        System.out.println("使用Class类的newInstance方法创建对象：");
        //对应类必须具有无参构造方法，且只有这一种创建方式
        ConstructorDemo constructor2 = ConstructorDemo.class.newInstance();
        System.out.println(constructor2);
        System.out.println("\n---------------------------\n");

        System.out.println("使用Constructor类的newInstance方法创建对象：");
        // 调用有参构造方法
        Constructor<ConstructorDemo> constructor = ConstructorDemo.class
                .getConstructor(Integer.class);
        ConstructorDemo constructor3 = constructor.newInstance(123);
        System.out.println(constructor3);
        System.out.println("\n---------------------------\n");

        System.out.println("使用Clone方法创建对象：");
        ConstructorDemo constructor4 = (ConstructorDemo) constructor3.clone();
        System.out.println(constructor4);
        System.out.println("\n---------------------------\n");

        System.out.println("使用(反)序列化机制创建对象：");
        // 写对象
        ObjectOutputStream output = new ObjectOutputStream(
                new FileOutputStream("constructordent.bin"));
        output.writeObject(constructor4);
        output.close();

        // 读取对象
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(
                "constructordent.bin"));
        ConstructorDemo constructor5 = (ConstructorDemo) input.readObject();
        System.out.println(constructor5);

    }


}
