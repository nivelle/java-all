package com.nivelle.base.utils;

import org.springframework.util.StringUtils;

/**
 * spring工具类
 *
 * @author fuxinzhong
 * @date 2020/09/04
 */
public class SpringUtils {
    public static void main(String[] args) {

        /**
         * 字符串指定字符的个数
         */
        int strCount = StringUtils.countOccurrencesOf("aaaBBcc", "a");
        System.out.println("a的个数：" + strCount);

        String deleteStr = StringUtils.deleteAny("aaaBBcc", "a");
        System.out.println("字符串删除指定字符:" + deleteStr);

        Object[] array = new Object[]{"a", "b", "d"};
        String 逗号风的字符 = StringUtils.arrayToCommaDelimitedString(array);
        System.out.println(逗号风的字符);

        String[] arrayObject = new String[]{};
        String[] appendArray = StringUtils.addStringToArray(arrayObject, 逗号风的字符);

        for (int i = 0; i < appendArray.length; i++) {
            System.out.println("字符串转数组:" + appendArray[i]);
        }
        String whitSpace = "ad, b,cdd dds, ";
        System.out.println("包含字符串：" + StringUtils.containsWhitespace(whitSpace));
        String noWhitespace = StringUtils.trimAllWhitespace(whitSpace);
        System.out.println("去除空白字符：" + noWhitespace);
        System.out.println("包含字符串：" + StringUtils.containsWhitespace(noWhitespace));
        System.out.println();
        String testStr = "abcdefg";
        System.out.println("是否以某个字符结尾:" + StringUtils.endsWithIgnoreCase(testStr, "g"));
        String testStr1 = "a.b.c.d.e.f.g";
        System.out.println("取文件名，最后点号后面的字符:" + StringUtils.unqualify(testStr1));
        System.out.println("parseLocale:" + StringUtils.parseLocale("2020-09-11"));
        System.out.println("字符判空：" + StringUtils.hasLength(testStr1));

    }
}
