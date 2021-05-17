package com.nivelle.core.utils;

import java.util.HashMap;

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

        new Thread(() -> {
            hashMapTest1();
        }).start();
        hashMapTest1();
    }


    public static void hashMapTest1() {
        HashMap hashMap = new HashMap();
        hashMap.put(1, 1);
        hashMap.put(10, 1);
        System.out.println(hashMap.hashCode() + ":in=> " + Thread.currentThread().getName());
    }
}
