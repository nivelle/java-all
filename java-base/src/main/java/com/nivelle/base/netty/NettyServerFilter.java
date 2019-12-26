package com.nivelle.base.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 服务端过滤器
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class NettyServerFilter extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline ph = ch.pipeline();
        // 以("\n")为结尾分割的 解码器
        // 解码和编码，应和客户端一致
        ph.addLast("decoder", new StringDecoder());
        ph.addLast("encoder", new StringEncoder());
        ph.addLast("handler", new NettyServerHandler());
        /**
         * 1. DelimiterBasedFrameDecoder是基于消息边界方式进行粘包拆包处理的。
         * 2. FixedLengthFrameDecoder是基于固定长度消息进行粘包拆包处理的。
         * 3. LengthFieldBasedFrameDecoder是基于消息头指定消息长度进行粘包拆包处理的。
         * 4. LineBasedFrameDecoder是基于行来进行消息粘包拆包处理的。
         * 5. ph.addLast(new FixedLengthFrameDecoder(11));   //
         * 6. ph.addLast(new LineBasedFrameDecoder(2048));     //
         */
         //定长数据帧的解码器 ，每帧数据100个字节就切分一次。  用于解决粘包问题
         ph.addLast(new FixedLengthFrameDecoder(11));
         //字节解码器 ,其中2048是规定一行数据最大的字节数。  用于解决拆包问题
         ph.addLast(new LineBasedFrameDecoder(2048));
    }
}