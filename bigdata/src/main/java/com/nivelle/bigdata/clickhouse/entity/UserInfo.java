package com.nivelle.bigdata.clickhouse.entity;

import lombok.Data;

import java.time.LocalDate;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/02/22
 */
@Data
public class UserInfo {

    private Integer id;
    private String userName;
    private String passWord;
    private String phone;
    private String email;
    private LocalDate createDay;
}
