package com.nivelle.base.patterns.guardedsuspension;

/**
 * 请求对象
 *
 * @author fuxinzhong
 * @date 2021/02/01
 */
public class MyRequest {
    private final String name;

    public MyRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return "[ Request " + name + " ]";
    }
}
