package com.nivelle.guide.java2e.core;

/**
 * Integer
 *
 * @author fuxinzhong
 * @date 2019/06/05
 */
public class BaseDataTypes {

    public static void main(String[] args) {

        Integer integer = new Integer("128");
        //向下转型,超出范围会导致精度丢失。byte 范围 -128～127,超出范围的值会因为高位丢失，导致首位变成1,成为负数。
        System.out.println(integer.byteValue());
        //向上转型不会丢失精度
        Byte myByte = new Byte("127");
        System.out.println(myByte.intValue());
        System.out.println(myByte.doubleValue());

        //精度超过36会默认为10
        String integer2 = Integer.toUnsignedString(-300,80);

        System.out.println(integer2);
    }
}
