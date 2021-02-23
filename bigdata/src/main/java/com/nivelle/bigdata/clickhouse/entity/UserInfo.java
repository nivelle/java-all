package com.nivelle.bigdata.clickhouse.entity;

import lombok.Data;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/22
 */
@Data
public class UserInfo {

    private int id;
    private String userName;
    private String passWord;
    private String phone;
    private String email;
    private String createDay;
}
