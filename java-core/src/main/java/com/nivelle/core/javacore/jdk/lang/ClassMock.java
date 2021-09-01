package com.nivelle.core.javacore.jdk.lang;

import com.nivelle.core.pojo.Father;
import com.nivelle.core.pojo.Parent;
import com.nivelle.core.pojo.Son;

/**
 * Class
 *
 * @author fuxinzhong
 * @date 2020/08/02
 */
public class ClassMock {

    public static void main(String[] args) {
        System.err.println("son 是 father的子类或者子接口：" + Father.class.isAssignableFrom(Son.class));
        System.err.println("son 是 Parent的子类或者子接口：" + Parent.class.isAssignableFrom(Son.class));
        System.err.println("son 是 Object的子类或者子接口：" + Object.class.isAssignableFrom(Son.class));
        System.err.println("Object 是所有类的父类");

        Son son = new Son();
        System.err.println("实例的类型是否是某个class类型：" + son.getClass().isInstance(Son.class));


    }
}
