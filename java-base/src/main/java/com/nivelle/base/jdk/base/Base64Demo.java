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
        System.out.println("压缩前字符长度:" + text.length());
        System.out.println("压缩前字节长度:" + text.getBytes().length);

        String encode = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        System.out.println("压缩后字符串:" + encode);

        System.out.println("压缩后字符串长度:" + encode.length());
        System.out.println("压缩后字符串字节长度:" + encode.getBytes().length);

        String decode = new String(Base64.getDecoder().decode(encode), StandardCharsets.UTF_8);
        System.out.println("解压后字符串：" + decode);
        System.out.println("原字符串与解压后的字符串比较:" + text.equals(decode));

        System.out.println("base64加密后占字节数:" + encode.length());
        System.out.println("原字符串字节数:" + text.getBytes().length + ";位数:" + text.getBytes().length * 8);


    }
}
