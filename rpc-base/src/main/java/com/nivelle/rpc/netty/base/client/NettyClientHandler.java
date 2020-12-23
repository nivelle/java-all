package com.nivelle.rpc.netty.base.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 客户端入站业务处理器
 *
 * @author nivelle
 * @date 2019/11/12
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<String> {


    /**
     * 客户端请求的心跳命令
     */
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat",
            CharsetUtil.UTF_8));

    /**
     * 空闲次数
     */
    private int idle_count = 1;

    /**
     * 发送次数
     */
    private int count = 1;

    /**
     * 循环次数
     */
    private int synCount = 1;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("客户端接受的消息: " + msg + "服务端地址：" + ctx.channel().remoteAddress());
        System.out.println("第" + count + "次" + ",客户端接受的消息:" + msg);
        count++;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端正在连接... ");
        ctx.writeAndFlush(Unpooled.copiedBuffer("客户端建立链接后默认发送消息", CharsetUtil.UTF_8));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接关闭! ");
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        System.out.println("循环请求的时间：" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()) + "，次数" + synCount);
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.WRITER_IDLE.equals(event.state())) {  //如果写通道处于空闲状态,就发送心跳命令
                if (idle_count <= 3) {   //设置发送次数
                    idle_count++;
                    System.out.println("客户端发送心跳请求，第" + idle_count + "次");
                    ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
                } else {
                    System.out.println("不再发送心跳请求了！");
                }
                synCount++;
            }
        }
    }


}
