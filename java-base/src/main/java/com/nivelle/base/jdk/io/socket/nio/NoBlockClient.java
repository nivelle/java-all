
package com.nivelle.base.jdk.io.socket.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NoBlockClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("用法: java NonBlockingEchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        System.out.println("hostName:" + hostName);
        System.out.println("portNumber:" + portNumber);
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            //绑定ip和端口
            socketChannel.connect(new InetSocketAddress(hostName, portNumber));
        } catch (IOException e) {
            System.err.println("NonBlockingEchoClient异常： " + e.getMessage());
            System.exit(1);
        }
        ByteBuffer writeBuffer = ByteBuffer.allocate(32);
        ByteBuffer readBuffer = ByteBuffer.allocate(32);
        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                writeBuffer.put(userInput.getBytes());
                //设置为可读模式
                writeBuffer.flip();
                // 将position设回0，所以你可以重读Buffer中的所有数据。limit保持不变，仍然表示能从Buffer中读取多少个元素（byte、char等）。
                writeBuffer.rewind();
                 //写消息到管道
                socketChannel.write(writeBuffer);
                //管道读消息
                socketChannel.read(readBuffer);
                 //清理缓冲区
                writeBuffer.clear();
                readBuffer.clear();
                System.out.println("echo: " + userInput);
            }
        } catch (UnknownHostException e) {
            System.err.println("不明主机，主机名为： " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("不能从主机中获取I/O，主机名为：" + hostName);
            System.exit(1);
        }
    }

}
