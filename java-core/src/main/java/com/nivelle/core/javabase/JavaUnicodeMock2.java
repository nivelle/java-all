package com.nivelle.core.javabase;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.SortedMap;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/08/03
 */
public class JavaUnicodeMock2 {

    public static void main(String[] args) throws UnsupportedEncodingException {
        String a = "我是猪";

        // 先按UTF-8获取，转化成GBK编码
        String b = new String(a.getBytes("UTF-8"), "GBK");
        System.out.println(b);

        // 按GBK获取，又转换成UTF-8的编码
        String c = new String(b.getBytes("GBK"), "UTF-8");
        System.out.println(c);

        ys();
    }

    public static void ys() {
        // 获取当前计算机编码
        Charset aa = Charset.defaultCharset();
        System.out.println(aa.name());

        // 输出所有 支持的字符集
        SortedMap<String, Charset> sm = Charset.availableCharsets();
        Set<String> keyset = sm.keySet();
        System.out.println("支持的所有字符集");
        for(String s: keyset) {
            System.out.println(s);
        }
    }
}
