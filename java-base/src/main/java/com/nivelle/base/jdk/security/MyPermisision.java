package com.nivelle.base.jdk.security;

import java.security.BasicPermission;

/**
 * 自定义权限
 *
 * @author fuxinzhong
 * @date 2020/11/02
 */
public class MyPermisision extends BasicPermission {

    public MyPermisision(String name) {
        super(name);
    }

    public MyPermisision(String name, String actions) {
        super(name, actions);
    }
}
