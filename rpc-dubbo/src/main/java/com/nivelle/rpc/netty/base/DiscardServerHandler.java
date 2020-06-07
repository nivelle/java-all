package com.nivelle.rpc.netty.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;

public class DiscardServerHandler extends SimpleChannelInboundHandler<String> { // (1)

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) { // (2)
        System.out.println("DiscardServerHandler handler 收到消息:" + msg);
        if (msg.equals("discard")) {
            ctx.writeAndFlush("DiscardServerHandler 服务端返回消息给客户端:" + msg + ",当前的时间是:" + new Date());
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }
}