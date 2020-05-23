package com.nivelle.rpc.socket.nio;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NonBlockingEchoServer {
	public static int DEFAULT_PORT = 7;

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
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("NonBlockingEchoServer，端口：" + port);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}

		while (true) {
			try {
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
						System.out.println("NonBlockingEchoServer 接受客户端的连接：" + socketChannel);
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
						System.out.println(client.getRemoteAddress() + " -> NonBlockingEchoServer is readable" + output.toString());
						key.interestOps(SelectionKey.OP_WRITE);
					}

					// 可写
					if (key.isWritable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						output.flip();
						client.write(output);
						System.out.println("NonBlockingEchoServer is writable  -> " + client.getRemoteAddress() + "：" + output.toString());
						output.compact();
						key.interestOps(SelectionKey.OP_READ);
					}
				} catch (IOException ex) {
					key.cancel();
					try {
						key.channel().close();
					} catch (IOException cex) {
						System.out.println(
								"NonBlockingEchoServer 异常!" + cex.getMessage());
					}
				}
			}
		}
	}

}
