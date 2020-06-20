
package com.nivelle.rpc.netty.codec.jackcon;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class JacksonClientHandler extends
		SimpleChannelInboundHandler<Object> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object obj)
			throws Exception {
		String jsonString = "";
		if (obj instanceof JacksonBean) {
			JacksonBean user = (JacksonBean) obj;
			
			jsonString = JacksonMapper.getInstance().writeValueAsString(user); // 对象转为json字符串
		} else {
			jsonString = JacksonMapper.getInstance().writeValueAsString(obj); // 对象转为json字符串
		}
		System.out.println("Client get msg form Server -" + jsonString);
	}

}
