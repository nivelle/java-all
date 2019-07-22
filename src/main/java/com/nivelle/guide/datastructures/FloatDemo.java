package com.nivelle.guide.datastructures;

/**
 * Float
 *
 * @author fuxinzhong
 * @date 2019/06/16
 */
public class FloatDemo {

    public static void main(String[] args) {
        Float myFloat = new Float(0.6F);
        Float myFloatDouble = new Float(0.5);
        Float myFloatString = new Float("0.655");

        System.out.println("myFloat = " + myFloat);
        System.out.println("myFloatDouble = " + myFloatDouble);
        System.out.println("myFloatString = " + myFloatString);

        System.out.println("int value = " + myFloat.intValue());
        System.out.println("double value = " + myFloat.doubleValue());
        System.out.println("float value = " + myFloat.floatValue());
        System.out.println("myFloatDouble short value = " + myFloatDouble.shortValue());
        System.out.println("myFloat short value = " + myFloat.shortValue());
        System.out.println("myFloatString short value = " + myFloatString.shortValue());


        System.out.println("max value:" + Float.MAX_VALUE);
        System.out.println("max value normal:" + Float.MAX_VALUE);


        System.out.println("min value:" + Float.MIN_NORMAL);
        /**
         * floatToIntBits
         */
        System.out.println("float to int bits:" + Float.floatToIntBits(myFloat));

        System.out.println("是否是有限:" + Float.isFinite(myFloat));
        System.out.println("是否是有限:" + Float.isFinite(Float.NEGATIVE_INFINITY));

        System.out.println(Float.toHexString(myFloat));


    }
}
