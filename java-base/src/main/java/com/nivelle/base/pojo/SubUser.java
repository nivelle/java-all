package com.nivelle.base.pojo;

import lombok.Data;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2020/10/22
 */

@Data
public class SubUser {

    private String userName;

    public SubUser(String userName) {
        this.userName = userName;
    }
}
