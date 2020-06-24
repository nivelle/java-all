package com.nivelle.base.jdk.base;

/**
 * Character
 *
 * @author nivell
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

            System.out.println("Character 字符对应的ascii:" + (int) character);

            String str = "今天是个好日子";
            System.out.println("字符串数组:" + str.charAt(1));
            /**
             * 使用int可以表示任意一个Unicode字符，低21位表示Unicode编号,高11位设为0。
             * 整数编号在Unicode中称为代码点（code point）,表示一个Unicode字符，与之相对，还有一个词，代码单元，表示一个char
             */
            System.out.println("今字对应的unicode编码:" + str.codePointAt(1));
            char[] codeToChar = Character.toChars(22826);
            System.out.println("22826 unicode 编码对应的字符:" + new String(codeToChar));

            System.out.println("ASCII 码 与 字符转换:");
            System.out.println('a' + 0);
            System.out.println((char) 97);

            String text = "hello你好";
            System.out.println("字符串长度：" + text.length());
            System.out.println("字节个数：" + text.getBytes().length);
            System.out.println("系统默认的编码格式：" + System.getProperty("sun.jnu.encoding"));
            System.out.println("一个汉字默认用字节数：" + "你".getBytes().length);
            System.out.println();
            System.out.println("字符转二进制:" + Integer.toBinaryString(((int) "你".toCharArray()[0])));

            byte[] bytes = text.getBytes();

            StringBuilder stringBuilder = new StringBuilder("低位字节码：");
            for (int i = 0; i < bytes.length; i++) {
                String binaryString = Integer.toBinaryString(bytes[i] & 0xff);
                stringBuilder.append(binaryString);
            }
            System.out.println();
            StringBuilder stringBuilder1 = new StringBuilder("全部字节码");
            for (int i = 0; i < bytes.length; i++) {
                String binaryString = Integer.toBinaryString(bytes[i]);
                stringBuilder1.append(binaryString);
            }

            System.out.println(stringBuilder);
            System.out.println(stringBuilder1);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
