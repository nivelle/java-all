package com.nivelle.rpc.netty.codec.serialization;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 说明：处理器
 **/
public class SerializationServerHandler extends SimpleChannelInboundHandler<Object> {
 

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object obj)
			throws Exception {
		if (obj instanceof SerializationBean) {
			SerializationBean user = (SerializationBean)obj;
			ctx.writeAndFlush(user);
			System.out.println("Server get msg form Client - name:"+ user.getName() + ";age:" + user.getAge());
		}
	}

}
