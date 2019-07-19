package com.nivelle.guide.datastructures;

import com.nivelle.guide.springboot.enums.MyEnum;

/**
 * Enum
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class EnumDemo {

    public static void main(String[] args) {

        /**
         * 所有Enum继承自抽象Enum
         */

        MyEnum oneEnum = MyEnum.ONE;

        MyEnum oneEnum2 = MyEnum.ONE;


        MyEnum twoEnum = MyEnum.TWO;


        System.out.println("枚举类名称:" + oneEnum.getClass());

        System.out.println("枚举类名称:" + oneEnum.getDeclaringClass());

        System.out.println(oneEnum.getType());

        /**
         * 通过类型获得指定的枚举
         */
        System.out.println("获取指定枚举:" + MyEnum.valueOf(MyEnum.class, "TWO").getDesc());

        System.out.println("获取指定枚举:" + MyEnum.valueOf("ONE").getDesc());


        /**
         * 获取指定枚举的名称
         */
        System.out.println("指定枚举的名称:" + oneEnum.name());
        /**
         * 使用 == 是否是同一个对象
         */
        System.out.println("枚举比较:" + oneEnum.equals(twoEnum));

        /**
         * 遍历枚举集合,获得枚举数组
         */
        System.out.print("遍历枚举集合:" + MyEnum.values()[0].getType());
        System.out.println();
        System.out.print("遍历枚举集合:" + MyEnum.values()[1].getType());


    }
}
