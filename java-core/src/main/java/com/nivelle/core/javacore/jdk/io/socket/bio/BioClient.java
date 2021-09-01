package com.nivelle.core.javacore.jdk.io.socket.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class BioClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("用法: java BlockingEchoClient <host name> <port number>");
            System.exit(1);
        }
        System.out.println("hostname is:" + args[0]);
        System.out.println("port is:" + args[1]);

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try {
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                System.out.println("发送数据开始: " + userInput);
                out.println(userInput);
                System.out.println("发送数据结束: " + userInput);
                System.out.println("客户端发送给远程服务地址：" + echoSocket.getRemoteSocketAddress());
            }
        } catch (UnknownHostException e) {
            System.err.println("不明主机，主机名为： " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("不能从主机中获取I/O，主机名为：" +
                    hostName + e);
            System.exit(1);
        }
    }

}
