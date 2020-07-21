package com.nivelle.rpc.netty.base.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 客户端过滤器
 *
 * @author nivelle
 * @date 2019/11/12
 */
public class NettyClientFilter extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline ph = ch.pipeline();
        /**
         * 解码和编码，应和服务端一致
         *
         * */
        ph.addLast("decoder", new StringDecoder());
        ph.addLast("encoder", new StringEncoder());
        //在 ChannelPipeline 中安装 ChannelHandler
        ph.addLast("handler", new NettyClientHandler());

    }
}
