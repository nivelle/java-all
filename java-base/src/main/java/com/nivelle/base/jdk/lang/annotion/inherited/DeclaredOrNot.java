package com.nivelle.base.jdk.lang.annotion.inherited;

import java.util.Arrays;

public class DeclaredOrNot {

    public static void main(String[] args) {
        Class<Sub> clazz = Sub.class;
        System.out.println("============================Field===========================");
        System.out.println("包括继承的public属性:" + Arrays.toString(clazz.getFields()));
        System.out.println("自身属性,包括私有的属性:" + Arrays.toString(clazz.getDeclaredFields()));

        System.out.println("============================Method===========================");
        System.out.println("包括继承来自Object的方法:" + Arrays.toString(clazz.getMethods()));
        System.out.println("自身的public,protected,private方法:" + Arrays.toString(clazz.getDeclaredMethods()));

        System.out.println("============================Constructor===========================");
        System.out.println("仅仅自身public的构造函数：" + Arrays.toString(clazz.getConstructors()));
        System.out.println("自身public和private的构造函数:" + Arrays.toString(clazz.getDeclaredConstructors()));


        System.out.println("============================AnnotatedElement===========================");
        //注解 DBTable2 是否存在于元素上
        System.out.println("注解是否在 DBTable2 上:" + clazz.isAnnotationPresent(DBTable2.class));
        //如果存在该元素的指定类型的注释DBTable2，则返回这些注释，否则返回 null。
        System.out.println("Sub获取的注解:" + clazz.getAnnotation(DBTable2.class));
        //继承
        System.out.println("PUBLIC的注解：" + Arrays.toString(clazz.getAnnotations()));
        //自身
        System.out.println("DECLARED的注解：" + Arrays.toString(clazz.getDeclaredAnnotations()));

        /**
         *
         *          * Identifies the set of all public members of a class or interface,
         *          * including inherited members.
         *
         *  public static final int PUBLIC = 0;
         *
         *
         *          * Identifies the set of declared members of a class or interface.
         *          * Inherited members are not included.
         *
         *   public static final int DECLARED = 1;
         */

    }

}
