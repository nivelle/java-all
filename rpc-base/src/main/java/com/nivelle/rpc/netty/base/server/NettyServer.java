package com.nivelle.rpc.netty.base.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * netty服务端
 *
 * @author nivelle
 * @date 2019/11/12
 */
public class NettyServer {
    private static final int port = 6789;


    /**
     * Netty创建全部都是实现自AbstractBootstrap。
     * 客户端的是Bootstrap，服务端的则是	ServerBootstrap。
     **/
    public static void main(String[] args) throws InterruptedException {
        /**
         * 主从多线程模式
         * <p>
         * boos:仅处理acceptor, 不设置线程的话 线程数会通过CPU来计算
         */
        EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1);
        //private static EventLoopGroup boss = new OioEventLoopGroup(1);

        /**
         * worker
         */
        EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();
        /**
         * 引导器
         */
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup);
            /**
             *
             * 设置nio类型的通道 ServerChannel: “ServerChannel 的实现负责创建子Channel ，这些子Channel 代表了已被接受的连接”
             *
             * */
            serverBootstrap.channel(NioServerSocketChannel.class);
            /**
             * “设置被添加到 ServerChannel 的 ChannelPipeline 中的 ChannelHandler 。”
             *
             * b.handler(null);
             *
             *
             * “设置将被添加到已被接受的子Channel 的ChannelPipeline 中的Channel- Handler 。
             *
             * handler() 方法和childHandler() 方法之间的区别是：
             *
             * 1. handler()是发生在初始化的时候;
             *
             * 2. childHandler() 方法所添加的ChannelHandler将由已被接受的子Channel 处理，其代表一个绑定到远程节点的套接字”
             *
             * 如果需要在客户端连接前的请求进行handler处理，则需要配置handler(),如果是处理客户端连接之后的handler,则需要配置在childHandler();
             *
             */
            serverBootstrap.handler(new NettyServerFilter());
            /**
             * 开启TCP心跳检测机制
             *
             * “指定要应用到新创建的ServerChannel 的ChannelConfig 的Channel- Option 。这些选项将会通过bind() 方法设置到Channel 。在bind() 方法被调用之后，设置或者改变ChannelOption 都不会有任何的效果。所支持的ChannelOption 取决于所使用的Channel 类型”
             *
             * */
            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            /**
             * ChannelOption.SO_BACKLOG对应的是tcp/ip协议listen函数中的backlog参数，函数listen(int socketfd,int backlog)用来初始化服务端可连接队列,
             *
             * 服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接，多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理,
             *
             * backlog参数指定了队列的大小
             */
            /**
             * “指定当子Channel 被接受时，应用到子Channel 的ChannelConfig 的ChannelOption 。所支持的ChannelOption 取决于所使用的Channel 的类型。”
             *
             */
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO));
            // 异步地绑定服务器,调用 sync()方法阻塞等待直到绑定完成;连接到远程节点,并返回 ChannelFuture
            // bind():“绑定Channel 并返回一个ChannelFuture,其将会在绑定操作完成后接收到通知，在那之后必须调用Channel.connect() 方法来建立连接”
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            //b.bind(new InetSocketAddress(8080));
            System.out.println("服务端启动成功,端口是:" + port);
            // 获取Channel的 closeFuture,并且阻塞当前线程直到它完成
            channelFuture.channel().closeFuture().sync();
            /**
             * “指定ServerChannel 应该绑定到的本地地址。如果没有指定，则将由操作系统使用一个随机地址。或者，可以通过bind() 方法来指定该localAddress
             **/
        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup.shutdownGracefully();
        }
    }
}
