package com.nivelle.guide.netty;

import java.net.ServerSocket;
import java.net.Socket;

public class TimeServer {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("use port 8080");
            }
        }
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("连接进来的端口号：" + port);
            Socket socket = null;
            while (true) {
                socket = serverSocket.accept();
                new Thread(new TimeServerHandler(socket)).start();
            }
        } catch (Exception e) {
            System.out.println("server socket error!!");
        } finally {
            if (serverSocket != null) {
                System.out.println("server is closed");
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    System.out.println("server close fail");
                }
                serverSocket = null;
            }
        }

    }
}
