package com.nivelle.base.datastructures;

/**
 * Character
 *
 * @author fuxinzhong
 * @date 2019/12/16
 */
public class CharacterDemo {

    public static void main(String[] args) {
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

            byte[] byteStr2 = character.toString().getBytes("gbk");
            System.out.println("字符数组转字符 gkb:" + new String(byteStr2, "gbk"));
            System.out.println("字符数组转字符 gkb:" + new String(byteStr2, "utf-16"));

            String str = "今天是个好日子";
            System.out.println(str.charAt(1));
            /**
             * 使用int可以表示任意一个Unicode字符，低21位表示Unicode编号，高11位设为0。
             * 整数编号在Unicode中称为代码点（code point）,表示一个Unicode字符，与之相对，还有一个词，代码单元，表示一个char
             */
            System.out.println("char codePoint is:"+str.codePointAt(1));
            char[] codeToChar = Character.toChars(22826);
            System.out.println("code to char is:"+new String(codeToChar));

            System.out.println("ASCII 码:");
            System.out.println('a' + 0);
            System.out.println((char) 97);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
