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
        System.out.println(wordPattern("abba", "北京 杭州 杭州 北京"));
    }

    /**
     *  有一个字符串它的构成是词+空格的组合，如“北京 杭州 杭州 北京”， 要求输入一个匹配模式（简单的以字符来写），
     * 比如 aabb,来判断该字符串是否符合该模式， 举个例子：
     * <p>
     * pattern = "abba", str="北京 杭州 杭州 北京" 返回 ture
     * pattern = "aabb", str="北京 杭州 杭州 北京" 返回 false
     * pattern = "baab", str="北京 杭州 杭州 北京" 返回 ture
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
            //对模式和字符串建立匹配 key= parttern value = city
            String val = map.get(patternArray[i]);
            // 按位匹配
            if (null != val) {
                if (!val.equals(strArray[i])) {
                    return false;
                }
            } else {
                //如果该模式对对应的值为空，也未保存过该值,则保存;如果对应的值，说明当前city值模式与当前pattern不匹配
                if (!map.values().contains(strArray[i])) {
                    map.put(patternArray[i], strArray[i]);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param array
     * @return
     */
    public static int maxLongSubArray(int[] array){
        return 0;
    }


}
