package com.nivelle.rpc.netty.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ProtocolServerHandler extends SimpleChannelInboundHandler<Object> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object obj)
            throws Exception {
        Channel incoming = ctx.channel();

        if (obj instanceof MsgObject) {
            MsgObject msg = (MsgObject) obj;
            System.out.println("服务器收到消息:" + incoming.remoteAddress() + msg.getBody());
            incoming.write(obj);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
