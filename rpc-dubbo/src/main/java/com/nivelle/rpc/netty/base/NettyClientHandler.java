package com.nivelle.rpc.netty.base;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * 客户端入站业务处理器
 *
 * @author nivell
 * @date 2019/11/12
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("客户端接受的消息: " + msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端正在连接... ");
        ctx.writeAndFlush(Unpooled.copiedBuffer("netty rocks", CharsetUtil.UTF_8));

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接关闭! ");
        super.channelInactive(ctx);
    }
}
