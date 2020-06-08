package com.nivelle.rpc.netty.base.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

        // 出站操作返回ChannelFuture
        final ChannelFuture f = ctx.writeAndFlush("TimeServerHandler 写出数据:" + time);
        // 增加监听器
        f.addListener(new ChannelFutureListener() {
            // 操作完成，关闭管道
            @Override
            public void operationComplete(ChannelFuture future) {
                ctx.close();
                System.out.println("ChannelHandlerContext 状态：" + ctx.isRemoved());

            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        System.out.println("TimeServerHandler 服务端收到数据消息！" + msg);
        // 返回客户端消息
        ctx.writeAndFlush("TimeServerHandler call back client:" + msg + ",当前的时间是:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
