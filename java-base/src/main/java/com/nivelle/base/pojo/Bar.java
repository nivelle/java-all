package com.nivelle.base.pojo;

/**
 * 子类
 */
public class Bar extends Foo {

    int j = 1;

    public Bar() {
        j = 2;
    }

    {
        j = 3;
    }

    @Override
    public int getValue() {
        return j;
    }


}
