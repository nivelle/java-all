package com.nivelle.spring.utils;
import org.springframework.util.StringUtils;

/**
 * spring 框架的工具类
 *
 * @author fuxinzhong
 * @date 2020/07/31
 */
public class SpringUtilsTest {

    public static void main(String[] args) {

        /**
         * 逗号分割的字符串转成字符数组
         */
        String string = "123,456,789,101112";
        String[] stringArray = StringUtils.commaDelimitedListToStringArray(string);
        for (int i = 0; i < stringArray.length; i++) {
            System.out.println(stringArray[i]);
        }

        System.out.println("nivelle");

        /**
         * 删除字符串内的某个字符
         */
        System.out.println(StringUtils.deleteAny(string,"11"));

        int countOccurrencesOf = StringUtils.countOccurrencesOf(string,"1");
        System.out.println(countOccurrencesOf);

    }
}
