package com.nivelle.base.jdk.socket.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NoBlockServer {
    public static int DEFAULT_PORT = 8080;

    /**
     * @param args
     */
    public static void main(String[] args) {
        int port;

        try {
            port = Integer.parseInt(args[0]);
        } catch (RuntimeException ex) {
            port = DEFAULT_PORT;
        }

        ServerSocketChannel serverChannel;
        Selector selector;
        try {
            serverChannel = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress(port);
            serverChannel.bind(address);
            //设置非阻塞模式
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("NonBlockingEchoServer,监听连接请求端口:" + port);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        while (true) {
            try {
                //阻塞直到有事件触发
                selector.select();
            } catch (IOException e) {
                System.out.println("NonBlockingEchoServer异常!" + e.getMessage());
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    // 可连接
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = server.accept();
                        System.out.println("可连接事件发生:" + socketChannel);
                        // 设置为非阻塞
                        socketChannel.configureBlocking(false);
                        // 客户端注册到Selector
                        SelectionKey clientKey = socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                        // 分配缓存区
                        ByteBuffer buffer = ByteBuffer.allocate(100);
                        clientKey.attach(buffer);
                    }
                    // 可读
                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        client.read(output);
                        System.out.println("远程ip地址:" + client.getRemoteAddress() + " -> 可读事件发生:" + output.toString());
                        key.interestOps(SelectionKey.OP_WRITE);
                        String string = new String(output.array());
                        System.out.println("可读事件发生时的消息内容:" + string.intern());
                    }

                    // 可写
                    if (key.isWritable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        output.flip();
                        client.write(output);
                        System.out.println("远程ip地址:" + "可写事件发生:" + client.getRemoteAddress() + "写的内容是：" + output.toString());
                        output.compact();
                        key.interestOps(SelectionKey.OP_READ);
                    }
                } catch (IOException ex) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                        System.out.println("NonBlockingEchoServer 异常!" + cex.getMessage());
                    }
                }
            }
        }
    }

}
