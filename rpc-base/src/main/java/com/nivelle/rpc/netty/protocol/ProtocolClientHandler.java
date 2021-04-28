package com.nivelle.rpc.netty.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 说明：处理器
 **/
public class ProtocolClientHandler extends SimpleChannelInboundHandler<Object> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object obj)
            throws Exception {
        Channel incoming = ctx.channel();
        if (obj instanceof MsgObject) {
            MsgObject msg = (MsgObject) obj;
            System.out.println("客户端收到服务端消息:" + incoming.remoteAddress() + msg.getBody());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //ctx.flush();
    }

}
