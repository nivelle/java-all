package com.nivelle.base.jdk.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockServer {

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
            // 接受客户端建立链接，生成Socket实例
            Socket clientSocket = serverSocket.accept();
            // 接收客户端的信息
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            //输出流 响应给请求
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            while ((inputLine = in.readLine()) != null) {
                // 发送信息给客户端
                out.println(inputLine);
                System.out.println("发送信息给客户端 -> " + clientSocket.getRemoteSocketAddress() + ":" + inputLine);
            }
        } catch (IOException e) {
            System.out.println("发送信息给客户端 异常!" + e.getMessage());
        }
    }

}
