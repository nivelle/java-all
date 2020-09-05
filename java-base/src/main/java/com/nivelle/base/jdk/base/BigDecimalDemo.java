package com.nivelle.base.jdk.base;

import java.math.BigDecimal;

/**
 * bigDecimal
 *
 * @author nivelle
 * @date 2019/10/13
 */
public class BigDecimalDemo {

    public static void main(String[] args) {
        BigDecimal bigDecimal = BigDecimal.ZERO;
        System.out.println("初始化0：" + bigDecimal);

        BigDecimal stringDoubleValue = new BigDecimal("1.22");
        System.out.println("stringDoubleValue:" + stringDoubleValue);

        BigDecimal doubleValue = new BigDecimal(1.22);
        System.out.println("doubleValue:" + doubleValue);

        String string = Double.toString(1.22);
        System.out.println("double to String:" + new BigDecimal(string));

        BigDecimal bigDecimal1 = new BigDecimal("2");
        System.out.println("2的3次方：" + bigDecimal1.pow(3));


        BigDecimal doubleValue2 = new BigDecimal(2.22);

        BigDecimal threeValue = doubleValue.add(doubleValue2);
        System.out.println("1.22+2.22=" + threeValue);

        System.out.println(doubleValue.add(doubleValue2));


    }
}
