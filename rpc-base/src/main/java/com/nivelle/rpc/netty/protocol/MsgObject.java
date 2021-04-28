package com.nivelle.rpc.netty.protocol;

/**
 * 说明：消息对象
 **/
public class MsgObject {

    private MsgHeader msgHeader = new MsgHeader();
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    /**
     *
     */
    public MsgObject() {
        // TODO Auto-generated constructor stub
    }

    public MsgHeader getProtocolHeader() {
        return msgHeader;
    }

    public void setProtocolHeader(MsgHeader msgHeader) {
        this.msgHeader = msgHeader;
    }


}
