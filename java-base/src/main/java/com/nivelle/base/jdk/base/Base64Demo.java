package com.nivelle.base.jdk.base;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64
 *
 * @author fuxinzhong
 * @date 2020/06/22
 */
public class Base64Demo {

    public static void main(String[] args) {

        String text = "hello你好";
        System.out.println("字符串长度：" + text.length());
        System.out.println("字节个数：" + text.getBytes().length);
        System.out.println("系统默认的编码格式：" + System.getProperty("sun.jnu.encoding"));
        System.out.println("一个汉字默认用字节数：" + "你".getBytes().length);
        System.out.println();

        System.out.println("字符转二进制:" + Integer.toBinaryString(((int) "你".toCharArray()[0])));

        String encode = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        System.out.println("压缩后字符串：" + encode);
        String decode = new String(Base64.getDecoder().decode(encode), StandardCharsets.UTF_8);
        System.out.println("解压后字符串：" + decode);
        System.out.println("原字符串与解压后的字符串比较:" + text.equals(decode));

        System.out.println("base64加密后占字节数:" + encode.getBytes().length);
        System.out.println("原字符串字节数:" + text.getBytes().length + ";位数:" + text.getBytes().length * 8);

        byte[] bytes = text.getBytes();

        System.out.println("0xff的值：" + Integer.toBinaryString(0xff));

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

    }
}
