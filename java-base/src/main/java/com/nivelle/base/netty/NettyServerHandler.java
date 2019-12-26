package com.nivelle.base.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;
import java.util.Date;

/**
 * 服务端出站处理器
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {


    private int count = 0;

    /**
     * 收到消息时，返回信息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg)
            throws Exception {
        // 收到消息直接打印输出
        System.out.println("收到的消息:" + msg + "当前条数目:" + (++count));
        //服务端断开的条件
        if ("quit".equals(msg)) {
            ctx.close();
        }
        // 返回客户端消息
        ctx.writeAndFlush("收到消息:" + msg + ",当前的时间是:" + new Date());
    }

    /**
     * 建立连接时，返回消息
     **/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        ctx.writeAndFlush("客户端" + InetAddress.getLocalHost().getHostName() + "成功与服务端建立连接！ ");
        super.channelActive(ctx);
    }
}
