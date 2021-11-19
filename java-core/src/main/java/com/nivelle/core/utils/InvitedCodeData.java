package com.nivelle.core.utils;

import lombok.Data;
import lombok.ToString;

/**
 * TODO:DOCUMENT ME!
 *
 * @author fuxinzhong
 * @date 2021/05/21
 */
@Data
@ToString
public class InvitedCodeData {

    private String  invitedUserName;
    private String bookNewsInviteCode;
    private String version;
    private String channel;

    public String getInvitedUserName() {
        return invitedUserName;
    }

    public void setInvitedUserName(String invitedUserName) {
        this.invitedUserName = invitedUserName;
    }

    public String getBookNewsInviteCode() {
        return bookNewsInviteCode;
    }

    public void setBookNewsInviteCode(String bookNewsInviteCode) {
        this.bookNewsInviteCode = bookNewsInviteCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    private String createTime;


    @Override
    public String toString() {
        return "InvitedCodeData{" +
                "invitedUserName='" + invitedUserName + '\'' +
                ", bookNewsInviteCode='" + bookNewsInviteCode + '\'' +
                ", version='" + version + '\'' +
                ", channel='" + channel + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
