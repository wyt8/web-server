package com.wyt;

public class Config {
    public static String rootPath = "./webroot";
    public static int port = 8888; // 服务器监听端口
    public static int maxConnectionNum = 5; // 允许最大同时连接数
    public static int maxCachedConnectionNum = 1; // 最大缓存连接数
    public static String defaultPage = "index.html";
    public static String notFoundPage = "404.html";
    public static String badRequestPage = "400.html";
    public static String forbiddenPage = "403.html";
    public static String cgiPath = "cgi-bin"; // CGI目录
    public static String logPath = "log"; // 日志文件目录
}
