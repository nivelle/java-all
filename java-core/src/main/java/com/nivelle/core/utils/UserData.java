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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getReadTimes() {
        return readTimes;
    }

    public void setReadTimes(String readTimes) {
        this.readTimes = readTimes;
    }
}
