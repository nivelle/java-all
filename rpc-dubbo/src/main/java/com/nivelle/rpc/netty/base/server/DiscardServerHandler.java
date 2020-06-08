package com.nivelle.rpc.netty.base.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DiscardServerHandler extends SimpleChannelInboundHandler<String> { // (1)

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) { // (2)
        System.out.println("DiscardServerHandler handler 收到消息:" + msg);
        if (msg.equals("discard")) {
            ctx.writeAndFlush("DiscardServerHandler call back client:" + "DiscardServerHandler 收到数据"+msg + ",当前的时间是:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ctx.close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.err.println("服务端监听器,写出到客户端成功");
                    } else {
                        System.err.println("执行失败：" + future.cause().getStackTrace());
                    }
                }
            });
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
        ctx.close().addListener(ChannelFutureListener.CLOSE);
    }
}