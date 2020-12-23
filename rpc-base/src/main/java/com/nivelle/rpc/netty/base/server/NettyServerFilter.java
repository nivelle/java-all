package com.nivelle.rpc.netty.base.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 服务端过滤器
 *
 * @author nivelle
 * @date 2019/11/12
 */
public class NettyServerFilter extends ChannelInitializer<SocketChannel> {

    /**
     * 读超时
     */
    private static final int READ_IDLE_TIME_OUT = 4;
    /**
     * 写超时
     */
    private static final int WRITE_IDLE_TIME_OUT = 5;
    /**
     * 所有超时
     */
    private static final int ALL_IDLE_TIME_OUT = 7;
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline ph = ch.pipeline();
        // 以("\n")为结尾分割的 解码器
        // 解码和编码，应和客户端一致
        ph.addLast("decoder", new StringDecoder());
        ph.addLast("encoder", new StringEncoder());
        //添加一个ChannelInboundHandlerAdapter 以拦截和处理事件
        ph.addLast(new NettyServerHandler());
        //将自定义的 ChannelHandler 添加到Channel的ChannelPipeline中
        ph.addLast(new DiscardServerHandler());
        ph.addLast(new TimeServerHandler());
        /**
         * 1. DelimiterBasedFrameDecoder 是基于消息边界方式进行粘包拆包处理的。
         * 2. FixedLengthFrameDecoder 是基于固定长度消息进行粘包拆包处理的。
         * 3. LengthFieldBasedFrameDecoder 是基于消息头指定消息长度进行粘包拆包处理的。
         * 4. LineBasedFrameDecoder 是基于行来进行消息粘包拆包处理的。
         *
         */
        //定长数据帧的解码器 ，每帧数据2个字节就切分一次。  用于解决粘包问题
        ph.addLast(new FixedLengthFrameDecoder(2));
        //字节解码器 ,其中2是规定一行数据最大的字节数。  用于解决拆包问题
        ph.addLast(new LineBasedFrameDecoder(3));
        ph.addLast(new IdleStateHandler(READ_IDLE_TIME_OUT, WRITE_IDLE_TIME_OUT, ALL_IDLE_TIME_OUT, TimeUnit.SECONDS));
        ph.addLast(new HeartbeatServerHandler());
    }
}