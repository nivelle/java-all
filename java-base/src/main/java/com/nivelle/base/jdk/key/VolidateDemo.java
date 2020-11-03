package com.nivelle.base.jdk.key;

/**
 * volatile
 *
 * @author nivelle
 * @date 2020/04/14
 */
public class VolidateDemo {

    volatile int params;

    public static void main(String[] args) {
        VolidateDemo volidateDemo = new VolidateDemo();
        System.out.println(volidateDemo.params);
    }
}
