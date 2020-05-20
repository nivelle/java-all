package com.nivelle.base.jdk.synlock;

/**
 * volatile
 *
 * @author nivell
 * @date 2020/04/14
 */
public class VolidateDemo {

    volatile int params;

    public static void main(String[] args) {
        VolidateDemo volidateDemo = new VolidateDemo();
        System.out.println(volidateDemo.params);
    }
}
