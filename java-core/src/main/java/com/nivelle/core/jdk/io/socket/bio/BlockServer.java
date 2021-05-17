package com.nivelle.core.jdk.io.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author jiangsuyao
 */
public class BlockServer {

    public static int DEFAULT_PORT = 8080;

    /**
     * 1. 阻塞IO的读、写、连接都会阻塞整个线程
     */
    public static void main(String[] args) {

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (RuntimeException ex) {
            port = DEFAULT_PORT;
        }
        ServerSocket serverSocket = null;
        try {
            // 服务器监听
            serverSocket = new ServerSocket(port);
            System.out.println("BlockServer 已启动,端口:" + port);

        } catch (IOException e) {
            System.out.println("BlockServer 启动异常,端口:" + port);
            System.out.println(e.getMessage());
        }
        try {
            //意味着主线程一直在循环运行
            //某个连接处理导致服务端无法响应. 由于整个接收请求和处理请求都是在同一个线程里（本示例是主线程）当处理客户端请求这一步发生了阻塞，
            // 或者说慢了，后来的所有连接请求都会被阻塞住

            /**
             * ServerSocket上的accept()方法将会一直阻塞到一个连接建立❶，随后返回一个新的Socket用于客户端和服务器之间的通信。
             *
             * 该ServerSocket将继续监听传入的连接 如果accept 在循环外面的话，则会阻塞其他链接，第一个线程处理未结束则其他链接阻塞
             */
            Socket clientSocket = serverSocket.accept();

            while (true) {
                System.out.println("当前连接的地址：" + clientSocket.getInetAddress());
                // 接收客户端的信息
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                //输出流 响应给请求
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                /**
                 * “readLine()方法将会阻塞，直到在❸处一个由换行符或者回车符结尾的字符串被读取”
                 */
                String inputLine = in.readLine();
                System.out.println("收到数据：" + inputLine);
                if (inputLine.equals("quit")) {
                    System.out.println("响应退出");
                    break;
                }
                //使用线程也可以使用线程池
//                new Thread(() -> {
//                    // 发送信息给客户端
//                    out.println(Thread.currentThread().getName()+"处理服务端收到的信息:" + inputLine);
//                    System.out.println("发送信息给客户端 -> " + clientSocket.getRemoteSocketAddress() + ":" + inputLine);
//                }).start();

                out.println(Thread.currentThread().getName() + "处理服务端收到的信息:" + inputLine);
                System.out.println("发送信息给客户端 -> " + clientSocket.getRemoteSocketAddress() + ":" + inputLine);
            }
        } catch (IOException e) {
            System.out.println("发送信息给客户端 异常!" + e);
        }
    }

}
