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

        System.out.println("string to float:" + Float.parseFloat("1.005F"));
        //识别首位的负号
        System.out.println("识别首位的负号 string to float:" + Float.parseFloat("-1.005F"));
        //十六进制转十进制
        System.out.println("十六进制转十进制 string to float:" + Float.parseFloat("0x12.3512p+11f"));
        //科学计数法
        System.out.println("科学计数法 string to float:" + Float.parseFloat("10001.222E+2"));


        System.out.println("int value = " + myFloat.intValue());
        System.out.println("double value = " + myFloat.doubleValue());
        System.out.println("float value = " + myFloat.floatValue());
        System.out.println("myFloatDouble short value = " + myFloatDouble.shortValue());
        System.out.println("myFloat short value = " + myFloat.shortValue());
        System.out.println("myFloatString short value = " + myFloatString.shortValue());

        //最大最小值，以16进制形式+科学计数法表示，后面有10进制+科学计数法的注释
        System.out.println("最大浮点数值:" + Float.MAX_VALUE);
        System.out.println("最小浮点数值:" + Float.MIN_VALUE);

        System.out.println("最大指数:" + Float.MAX_EXPONENT);
        System.out.println("最小指数:" + Float.MIN_EXPONENT);

        System.out.println("最小标准值:" + Float.MIN_NORMAL);


        System.out.println("用来表示二进制float值的比特数，值为32，静态变量且不可变:" + Float.SIZE);
        System.out.println("用来表示二进制float值的字节数，值为SIZE除于Byte.SIZE，结果为4:" + Float.BYTES);

        /**
         * Return the Virtual Machine's Class object for the named primitive type.
         *
         * 通过调用Class的Native方法返回虚拟机中的基础数据类型
         */
        System.out.println("float type:" + Float.TYPE);

        /**
         * 先将浮点数转成IEEE-754标准的二进制形式，并且还要判断是否是正负无穷大，是否是NaN。
         * 然后再按照IEEE-754标准从二进制转换成十进制，此过程十分复杂，需要考虑的点相当多。最后生成浮点数对应的字符串
         */
        System.out.println("float toString is:" + myFloat.toString());
        System.out.println("float toString is:" + new Float("0"));
        System.out.println("float toString is:" + new Float(0.1 / 0));
        System.out.println("float toString is:" + new Float(-0.1 / 0));
        System.out.println("float NaN toString is:" + new Float(0.0f / 0.0f));


        System.out.println("是否是有限:" + Float.isFinite(myFloat));
        System.out.println("是否是有限:" + Float.isFinite(Float.NEGATIVE_INFINITY));


        /**
         * floatToRawIntBits是一个本地方法，该方法主要是将一个浮点数转成IEEE 754标准的二进制形式对应的整型数。
         * 对应的本地方法的处理逻辑简单而且有效，就是通过一个union实现了int和float的转换，最后再转成java的整型jint。
         */
        System.out.println("floatToRawIntBits:" + Float.floatToRawIntBits(myFloat));


        /**
         * floatToIntBits同样是一个本地方法，该方法主要是将一个IEEE 754标准的二进制形式对应的整型数转成一个浮点数。
         * 可以看到其本地实现也是通过union来实现的，完成int转成float，最后再转成java的浮点型jfloat。
         */
        System.out.println("intBitsToFloat:" + Float.intBitsToFloat(01));

        /**
         * 主要看第二个hashCode方法即可，它是通过调用floatToIntBits来实现的
         * ，所以它返回的哈希码其实就是某个浮点数的IEEE 754标准对应的整型数。
         */
        System.out.println("float hashCode:" + myFloat.hashCode());

        /**
         * toHexString(float f):转成16进制的字符串（用科学计数法表示）
         */
        System.out.println(Float.toHexString(new Float("0.1")));



    }
}
