package com.nivelle.rpc.netty.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;


public class DatagramChannelEchoClientHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		
		// 从管道读消息
		DatagramPacket packet = (DatagramPacket) msg; // 转为DatagramPacket类型
		String m = packet.content().toString(CharsetUtil.UTF_8);  // 转为字符串
		System.out.println( "echo :" + m);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

		// 当出现异常就关闭连接
		cause.printStackTrace();
		ctx.close();
	}
}