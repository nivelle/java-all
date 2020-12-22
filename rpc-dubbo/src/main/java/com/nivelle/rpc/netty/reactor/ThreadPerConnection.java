package com.nivelle.rpc.netty.reactor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * reactor thread-Per-Connection模式
 *
 * @author fuxinzhong
 * @date 2020/12/22
 */
public class ThreadPerConnection implements Runnable {

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    static class Handler implements Runnable {
        final Socket socket;

        Handler(Socket s) {
            socket = s;
        }

        @Override
        public void run() {
            try {
                byte[] input = new byte[1024];
                 //阻塞
                socket.getInputStream().read(input);
                byte[] output = process(input);
                 //阻塞
                socket.getOutputStream().write(output);
            } catch (Exception e) {
            }
        }

        private byte[] process(byte[] input) {
            return null;
        }
    }
}

