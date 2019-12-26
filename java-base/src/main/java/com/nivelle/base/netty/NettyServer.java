package com.nivelle.base.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * netty服务端
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class NettyServer {
    private static final int port = 6789;
    private static EventLoopGroup boss = new NioEventLoopGroup(1);
    private static EventLoopGroup worker = new NioEventLoopGroup();

    private static ServerBootstrap b = new ServerBootstrap();

    /**
     * Netty创建全部都是实现自AbstractBootstrap。
     * 客户端的是Bootstrap，服务端的则是	ServerBootstrap。
     **/
    public static void main(String[] args) throws InterruptedException {
        try {
            b.group(boss, worker);
            /**
             * 设置nio类型的通道
             */
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new NettyServerFilter());
            /**
             * 开启TCP心跳检测机制
             */
            b.option(ChannelOption.SO_KEEPALIVE,true);
            // 异步地绑定服务器；调用 sync()方法阻塞  等待直到绑定完成
            ChannelFuture f = b.bind(port).sync();
            System.out.println("服务端启动成功,端口是:" + port);
            // 获取 Channel 的  CloseFuture，并且阻塞当前线程直到它完成
            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
