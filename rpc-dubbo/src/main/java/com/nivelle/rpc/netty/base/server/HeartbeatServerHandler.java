package com.nivelle.rpc.netty.base.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

/**
 * 说明：心跳服务器处理器
 */
public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {


	private static final String HEART_FLAG = "Heartbeat";


	// Return a unreleasable view on the given ByteBuf
	// which will just ignore release and retain calls.
	private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled
			.unreleasableBuffer(Unpooled.copiedBuffer(HEART_FLAG,
					CharsetUtil.UTF_8));


	/** 空闲次数 */
	private int idle_count =1;
	/** 发送次数 */
	private int count = 1;

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {

		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			String type = "";
			if (event.state() == IdleState.READER_IDLE) {
				type = "read idle";
				if (idle_count > 2) {
					System.out.println("关闭这个不活跃的channel");
					ctx.channel().close();
				}
				idle_count++;
			} else if (event.state() == IdleState.WRITER_IDLE) {
				type = "write idle";
			} else if (event.state() == IdleState.ALL_IDLE) {
				type = "all idle";
			}
			ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			System.out.println( "心跳服务器处理器向客户端发送消息:"+ctx.channel().remoteAddress()+"超时类型：" + type);
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("第"+count+"次"+",服务端接受的消息:"+msg);
		String message = (String) msg;
		if (HEART_FLAG.equals(message)) {  //如果是心跳命令，则发送给客户端;否则什么都不做
			ctx.write("服务端成功收到心跳信息");
			ctx.flush();
		}
		count++;
	}
}
