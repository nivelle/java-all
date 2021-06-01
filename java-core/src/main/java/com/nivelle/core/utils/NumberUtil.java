package com.nivelle.core.utils;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/06/01
 */
public class NumberUtil {

    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= 3000; i++) {
            stringBuilder.append(i).append(",");
        }
        System.out.println(stringBuilder.toString());
    }
}
