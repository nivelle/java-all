package com.nivelle.core.javacore.guava;

import com.google.common.base.Joiner;

/**
 * join
 *
 * @author fuxinzhong
 * @date 2022/01/08
 */
public class GuavaTest {


    public static void main(String[] args) {
        String[] strArray = new String[]{"1","2","3"};
        String joinStr = Joiner.on(",").join(strArray);
        System.out.println(joinStr);
    }
}
