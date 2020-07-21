package com.nivelle.rpc.netty.base.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 服务端出站处理器
 *
 * @author nivelle
 * @date 2019/11/12
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {


    private int count = 0;

    /**
     * 收到消息时，返回信息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        // 收到消息直接打印输出
        System.out.println("NettyServerHandler可读时收到消息:" + msg + "当前条数目:" + (++count));
        //服务端断开的条件
        if ("quit".equals(msg)) {
            ctx.close();
        }
        // 返回客户端消息
        ctx.writeAndFlush("NettyServerHandler call back client:" + "DiscardServerHandler 收到数据" + msg + ",当前的时间是:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ctx.fireChannelRead(msg);
    }

    /**
     * 建立连接时，返回消息
     **/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        ctx.writeAndFlush("NettyServerHandler 建立连接时:" + ",当前的时间是:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

}
