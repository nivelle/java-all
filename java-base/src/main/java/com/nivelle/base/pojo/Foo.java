package com.nivelle.base.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivell
 * @date 2020/04/02
 */
public class Foo {
    public String name;
    public List<Bar> bars = new ArrayList<>();

    public Foo(String name) {
        this.name = name;
    }
}
