package com.nivelle.core.utils;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/06/06
 */
public class Test {

    public static void main(String[] args) {
        String s = "  hello world  ";
        System.out.println(s.trim());
        //String[] str = s.trim().split("//s+");

        String st = "ada_%s";

        System.out.println(String.format(st,"hh","dd"));

        SnowFlake snowFlake = new SnowFlake(10,10);
        System.out.println(snowFlake.nextId());

        Long id = snowFlake.nextId();
        System.out.println(Long.toBinaryString(id));

        System.out.println("110001001110010110111010111111001010100101001010000000000000".length());
    }
}
