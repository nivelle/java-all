package com.nivelle.core.javacore.io.socket.aio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class AsyncEchoClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("用法: java AsyncEchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        AsynchronousSocketChannel socketChannel = null;
        try {
            socketChannel = AsynchronousSocketChannel.open();
            socketChannel.connect(new InetSocketAddress(hostName, portNumber));
        } catch (IOException e) {
            System.err.println("AsyncEchoClient异常： " + e.getMessage());
            System.exit(1);
        }

        ByteBuffer writeBuffer = ByteBuffer.allocate(32);
        ByteBuffer readBuffer = ByteBuffer.allocate(32);

        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                writeBuffer.put(userInput.getBytes());
                writeBuffer.flip();
                writeBuffer.rewind();
                socketChannel.write(writeBuffer);
                socketChannel.read(readBuffer);
                writeBuffer.clear();
                readBuffer.clear();
                System.out.println("发送消息: " + userInput);
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
