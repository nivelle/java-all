package com.nivelle.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 常见面试
 *
 * @author fuxinzhong
 * @date 2021/09/10
 */
public class LeetCodeTest {

    public static void main(String[] args) {

    }

    /**
     * 阿里巴巴笔试 有一个字符串它的构成是词+空格的组合，如“北京 杭州 杭州 北京”， 要求输入一个匹配模式（简单的以字符来写），
     * 比如 aabb,来判断该字符串是否符合该模式， 举个例子：
     *
     * pattern = "abba", str="北京 杭州 杭州 北京" 返回 ture
     * pattern = "aabb", str="北京 杭州 杭州 北京" 返回 false
     * pattern = "baab", str="北京 杭州 杭州 北京" 返回 ture
     *
     */
    public static boolean wordPattern(String pattern, String str) {
        Map<Character, String> map = new HashMap<>();
        //模式字符串
        char[] patternArray = pattern.toCharArray();
        //实际字符串
        String[] strArray = str.split(" ");
        if (patternArray.length != strArray.length) {
            return false;
        }
        for (int i = 0; i < patternArray.length; i++) {
            String val = map.get(patternArray[i]);
            if (null != val) {
                if (!val.equals(strArray[i])) {
                    return false;
                }
            } else {
                if (!map.values().contains(strArray[i])) {
                    map.put(patternArray[i], strArray[i]);
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
