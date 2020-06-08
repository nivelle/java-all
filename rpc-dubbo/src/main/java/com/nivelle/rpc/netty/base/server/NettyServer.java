package com.nivelle.rpc.netty.base.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * netty服务端
 *
 * @author nivell
 * @date 2019/11/12
 */
public class NettyServer {
    private static final int port = 6789;
    /**
     * 主从多线程模式
     */
    //仅处理acceptor, 不设置线程的话 线程数会通过CPU来计算
    private static EventLoopGroup boss = new NioEventLoopGroup(1);
    //worker
    private static EventLoopGroup worker = new NioEventLoopGroup();
    //引导器
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
            /**
             * 指定ChannelInitializer 每个连接都调用它
             */
            b.childHandler(new NettyServerFilter());
            //b.childHandler(new DiscardServerHandler());
            /**
             * 开启TCP心跳检测机制
             */
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO));
            // 异步地绑定服务器,调用 sync()方法阻塞  等待直到绑定完成
            ChannelFuture f = b.bind(port).sync();
            System.out.println("服务端启动成功,端口是:" + port);

            // 获取Channel的 closeFuture,并且阻塞当前线程直到它完成
            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
