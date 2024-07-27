package com.wyt;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
    private static ServerSocket serverSocket;
    private static ThreadPool threadPool;

    public static void start() {
        try {
            WebServer.serverSocket = new ServerSocket(Config.port);
            WebServer.threadPool = new ThreadPool(Config.maxConnectionNum, Config.maxCachedConnectionNum);
            System.out.println("[info] 服务器已启动");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new WorkThread(clientSocket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
