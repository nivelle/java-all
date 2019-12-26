package com.nivelle.base.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.util.Scanner;

/**
 * netty客户端
 *
 * @author fuxinzhong
 * @date 2019/11/12
 */
public class NettyClient {
    public static String host = "127.0.0.1";
    public static int port = 6789;
    /**
     * 反应器类型：通过nio方式来接收连接和处理连接
     */
    private static EventLoopGroup groupBoss = new NioEventLoopGroup();

    /**
     * 启动类:组装类和集成器
     */
    private static Bootstrap b = new Bootstrap();
    private static Channel ch;

    /**
     * Netty创建全部都是实现自AbstractBootstrap。
     * 客户端的是Bootstrap，服务端的则是	ServerBootstrap。
     **/
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("客户端成功启动...");
        b.group(groupBoss);
        b.channel(NioSocketChannel.class);
        b.handler(new NettyClientFilter());
        // 连接服务端
        ch = b.connect(host, port).sync().channel();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            star(line);
        }
    }

    public static void star(String line) {
        for (int i = 0; i <= 100; i++) {
            String str = line;
            ch.writeAndFlush(str);
            System.out.println("客户端发送数据:" + str);
        }
    }
}
