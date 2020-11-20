package com.nivelle.base.utils;

/**
 * 测试用
 *
 * @author fuxinzhong
 * @date 2020/11/20
 */
public class BaseTest {

    public static void main(String[] args) {

        int e = 0;
        int p = 2;
        if ((e = p) == 3) {
            System.out.println("in if  e:" + e + ";p:" + p);
        }
        System.out.println("out if  e:" + e + ";p:" + p);


    }
}
