package com.nivelle.core.jdk.lang;

import org.apache.commons.lang3.StringUtils;

/**
 * Character
 *
 * @author nivelle
 * @date 2019/12/16
 */
public class CharacterMock {

    public static void main(String[] args) {
        System.out.println("系统默认的编码格式:" + System.getProperty("sun.jnu.encoding"));


        Character character = new Character('a');
        System.out.println(character);
        System.out.println("a 字符类型：" + Character.getType('a'));
        System.out.println("A 字符类型：" + Character.getType('A'));
        System.out.println("==================================== ");
        try {
            /**
             * Unicode给世界上每个字符分配了一个编号,编号范围0x000000~0x10FFFF
             *
             * 1. 编号范围在 0x0000～0xFFFF的字符为常用字符集，称为BMP(Basic Multilingual Plane)字符
             *
             * 2. 编号范围在0x10000~0x10FFFF的字符叫做增补字符(supplementary character)
             */
            byte[] byteStr = character.toString().getBytes("utf-8");
            System.out.println("字符转字符数组:" + byteStr);
            System.out.println("字符数组转字符 utf-8:" + new String(byteStr, "utf-8"));
            System.out.println("字符数组转字符 utf-16:" + new String(byteStr, "utf-16"));
            System.out.println("字符数组转字符 gbk:" + new String(byteStr, "gbk"));


            byte[] byteStr2 = character.toString().getBytes("gbk");
            System.out.println("字符数组转字符 gkb:" + new String(byteStr2, "gbk"));
            System.out.println("字符数组转字符 gkb:" + new String(byteStr2, "utf-16"));
            System.out.println("字符数组转字符 utf-8:" + new String(byteStr2, "utf-8"));

            String str = "今天是个好日子";
            System.out.println(str.charAt(1));
            /**
             * 使用int可以表示任意一个Unicode字符,低21位表示Unicode编号,高11位设为0。
             * 整数编号在Unicode中称为代码点（code point）,表示一个Unicode字符,与之相对,还有一个词,代码单元,表示一个char
             */
            System.out.println("天字对应的编码:" + str.codePointAt(1));
            System.out.println("字符对应的编码:" + "a".codePointAt(0));
            char[] codeToChar = Character.toChars(22826);
            System.out.println("编码转字符:" + new String(codeToChar));

            System.out.println("ASCII 码:");
            System.out.println("a字符对应的code码:" + 'a' + 0);
            System.out.println((char) 97);

            String text = "hello你好";
            System.out.println(text + "字符串长度:" + text.length());
            System.out.println(text + "字节个数:" + text.getBytes().length);
            System.out.println("一个汉字默认用字节数：" + "你".getBytes().length);
            System.out.println();
            System.out.println("'你'对应的转二进制:" + Integer.toBinaryString(((int) "你".toCharArray()[0])));
            System.out.println("'你'对应的转二进制:" + Integer.toBinaryString(new Character('你')));
            System.out.println("你字的二进制转十进制:" + Integer.parseInt("100111101100000", 2));
            System.out.println("你字对应的编码:" + "你".codePointAt(0));


            System.out.println();
            System.out.println("0xff对应的十进制：" + 0xff);
            System.out.println("十进制转二进制:" + Integer.toBinaryString(255));


            String text1 = "你";
            byte[] bytes = text1.getBytes();
            StringBuilder stringBuilder2 = new StringBuilder("低位字节码:");
            for (int i = 0; i < bytes.length; i++) {
                String binaryString = Integer.toBinaryString(bytes[i] & 0xff);
                stringBuilder2.append(binaryString);
                System.out.println("低位字节码 i is:" + i + "binary is:" + binaryString);
            }

            StringBuilder stringBuilder3 = new StringBuilder("低位字节码:");
            for (int i = 0; i < bytes.length; i++) {
                String binaryString = Integer.toBinaryString(bytes[i] & 0xff);
                stringBuilder3.append(binaryString);
                System.out.println("低位字节码 i is:" + i + "binary is:" + binaryString);
                stringBuilder3.append(StringUtils.leftPad(binaryString, 8, "0"));
                System.out.println("低位字节码 leftPad i is:" + i + "binary is:" + binaryString);

            }
            StringBuilder stringBuilder1 = new StringBuilder("全部字节码:");
            for (int i = 0; i < bytes.length; i++) {
                String binaryString = Integer.toBinaryString(bytes[i]);
                stringBuilder1.append(binaryString);
                System.out.println("全部字节码 i is:" + i + "binary is:" + binaryString);
            }
            System.out.println("text1 的低八位:" + stringBuilder2);
            System.out.println("text1 的二进制:" + stringBuilder1);
        } catch (Exception e) {
            System.out.println(e);
        }

        String ipv6 = "240e:82:f0ab:4b57:ec2f:6c42:77a4:78";
        System.out.println(ipv6.length());
    }

}
