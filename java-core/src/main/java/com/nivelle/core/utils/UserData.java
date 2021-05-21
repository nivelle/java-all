package com.nivelle.core.utils;

import lombok.Data;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/05/21
 */
@Data
public class UserData {

    private String userName;
    private String bookId;
    private String readTimes;

    public UserData(String userName, String bookId, String readTimes) {
        this.userName = userName;
        this.bookId = bookId;
        this.readTimes = readTimes;
    }
}
