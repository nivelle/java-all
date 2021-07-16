package com.nivelle.core.utils;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/07/13
 */
public class TestArray {

    public static void main(String[] args) {
        String s = "abaccaa";
        int[] count = new int[128];
        int length = s.length();
        for (int i = 0; i < length; ++i) {
            char c = s.charAt(i);
            System.out.println("index-" + i + "字符-" + c + ":" + "-对应值：" + count[c] + "-char:" + Integer.valueOf(c));
            count[c]++;
            //System.out.println("字符count:" + count[c]);
        }
        System.out.println();
        for (int c : count) {
            System.out.println(c);
        }
    }
}
