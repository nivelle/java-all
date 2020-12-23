package com.nivelle.rpc.dubbo.model;

/**
 * TODO:DOCUMENT ME!
 *
 * @author nivelle
 * @date 2019/08/19
 */
public class UserInfo {

    private String userName;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    @Override
    public String toString() {
        return "UserInfo{" +
                "userName='" + userName + '\'' +
                '}';
    }
}
